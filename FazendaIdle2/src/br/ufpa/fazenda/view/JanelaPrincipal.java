package br.ufpa.fazenda.view;

import br.ufpa.fazenda.engine.GameLoop;
import br.ufpa.fazenda.engine.GerenciadorEventos;
import br.ufpa.fazenda.engine.ExecutorTarefas;
import br.ufpa.fazenda.model.*;
import br.ufpa.fazenda.util.Constantes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class JanelaPrincipal extends JFrame implements GerenciadorEventos, ActionListener {

    private PainelFazenda painelFazenda;
    private PainelLateral painelLateral;
    private GameLoop gameLoop;
    private Timer timerUI; 
    private ExecutorTarefas executor; 
    private Map<String, Consumer<String>> mapaComandos;

    public JanelaPrincipal() {
        setTitle("Fazenda Idle 2.0");
        setMinimumSize(new Dimension(Constantes.TAMANHO_MINIMO_JANELA_X, Constantes.TAMANHO_MINIMO_JANELA_Y));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();


        Image icone = RenderizadorAssets.get().getImagem("celeiro.png", true);
        
        if (icone != null) {
            setIconImage(icone); // <--- Essa é a linha que faz a mágica
        }
        
        painelLateral = new PainelLateral(this); 
        painelFazenda = new PainelFazenda(this);
        
        // Layout Responsivo
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1.0 - Constantes.PROPORCAO_PAINEL_LATERAL;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(painelFazenda, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = Constantes.PROPORCAO_PAINEL_LATERAL;
        add(painelLateral, gbc);

        executor = ExecutorTarefas.getInstance();
        
        // Inicializa o mapa de comandos ANTES de começar o jogo
        inicializarComandos();

        gameLoop = new GameLoop(this);
        gameLoop.start();
        
        timerUI = new Timer(100, e -> {
            if (painelLateral != null) {
                painelLateral.atualizarDadosDinamicos();
                painelLateral.repaint();
            }
            if (painelFazenda != null) {
                painelFazenda.repaint();
            }
        });
        timerUI.start();
        
        setVisible(true);
    }
    
    // --- 1. CONFIGURAÇÃO DOS COMANDOS (SETUP) ---
    
    private void inicializarComandos() {
        mapaComandos = new HashMap<>();

        // Comandos Simples (Exatos)
        mapaComandos.put("CMD_TOGGLE_IA", this::executarToggleIA);
        mapaComandos.put("CMD_PAUSE", cmd -> gameLoop.alternarPause());
        mapaComandos.put("CMD_SAIR", this::executarSair);
        mapaComandos.put("CMD_COMPRAR_FERTILIZANTE", this::executarComprarFertilizante);

        // Comandos com Argumentos (Prefixos)
        mapaComandos.put("CMD_LOJA_COMPRAR_ANIMAL", this::executarComprarAnimal);
        mapaComandos.put("CMD_LOJA_COMPRAR_MAQUINA", this::executarComprarMaquina);
        mapaComandos.put("CMD_EVOLUIR_SOLO", this::executarEvoluirSolo);
        mapaComandos.put("CMD_DESBLOQUEAR_SOLO", this::executarDesbloquearSolo);
        mapaComandos.put("CMD_PLANTAR", this::executarPlantar);
        mapaComandos.put("CMD_COLHER", this::executarColher);
        mapaComandos.put("CMD_ARRANCAR", this::executarArrancar);
        mapaComandos.put("CMD_COLETAR_ANIMAL", this::executarColetarAnimal);
    }

    // --- 2. O DESPACHANTE DE AÇÕES (PROCESSADOR) ---

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        executor.executar(() -> processarAcao(cmd));
    }
    
    private void processarAcao(String cmd) {
        // 1. Tenta achar comando exato
        if (mapaComandos.containsKey(cmd)) {
            mapaComandos.get(cmd).accept(cmd);
            return;
        }

        // 2. Se não achou, procura por prefixo
        for (String chave : mapaComandos.keySet()) {
            if (cmd.startsWith(chave)) {
                mapaComandos.get(chave).accept(cmd);
                return;
            }
        }

        System.err.println("Comando não reconhecido: " + cmd);
    }

    // --- 3. MÉTODOS DE AÇÃO (LÓGICA ESPECÍFICA) ---

    private void executarToggleIA(String cmd) {
        FazendaEstado fazenda = FazendaEstado.getInstance();
        if (fazenda.isModoIAAtivado()) {
            gameLoop.desativarIA();
            fazenda.setModoIAAtivado(false);
        } else {
            gameLoop.ativarIA();
            fazenda.setModoIAAtivado(true);
        }
    }

    private void executarArrancar(String cmd) {
        // cmd ex: CMD_ARRANCAR_2
        int id = Integer.parseInt(cmd.split("_")[2]);
        FazendaEstado.getInstance().getSolos().get(id).arrancar();
        SwingUtilities.invokeLater(() -> selecionarSolo(id));
    }

    private void executarPlantar(String cmd) {
        // cmd ex: CMD_PLANTAR_2_MILHO
        String[] parts = cmd.split("_");
        int id = Integer.parseInt(parts[2]);
        Vegetal v = Vegetal.valueOf(parts[3]);
        
        if(FazendaEstado.getInstance().plantarManual(id, v)) {
            SwingUtilities.invokeLater(() -> selecionarSolo(id));
        }
    }
    
    private void executarColher(String cmd) {
        // cmd ex: CMD_COLHER_2
        int id = Integer.parseInt(cmd.split("_")[2]);
        FazendaEstado fazenda = FazendaEstado.getInstance();
        Solo s = fazenda.getSolos().get(id);
        double val = s.colher();
        fazenda.ganharDinheiro(val);
        SwingUtilities.invokeLater(() -> selecionarSolo(id));
    }

    private void executarComprarAnimal(String cmd) {
        String especieStr = cmd.substring("CMD_LOJA_COMPRAR_ANIMAL_".length());
        Animal animal = Animal.valueOf(especieStr);
        
        int resultado = FazendaEstado.getInstance().comprarAnimalComVerificacao(animal);
        
        SwingUtilities.invokeLater(() -> {
            if (resultado == 0) mostrarAviso("Sucesso! " + animal.getNome() + " enviado para um cercado novo/vazio.");
            else if (resultado == 1) mostrarErro("Dinheiro insuficiente para comprar " + animal.getNome() + "!");
            else if (resultado == 2) mostrarErro("Todos os cercados para " + animal.getNome() + " estão CHEIOS (Máx 3)!");
            else if (resultado == 3) mostrarAviso("Mais uma " + animal.getNome() + " adicionada ao cercado existente!");
        });
    }

    private void executarComprarMaquina(String cmd) {
        String maqStr = cmd.substring("CMD_LOJA_COMPRAR_MAQUINA_".length());
        Maquina m = Maquina.valueOf(maqStr);
        
        int res = FazendaEstado.getInstance().comprarMaquinaComVerificacao(m);
        
        SwingUtilities.invokeLater(() -> {
            if (res == 0) mostrarAviso(m.getNome() + " comprada! Equipe-a em um solo.");
            else if (res == 1) mostrarErro("Dinheiro insuficiente!");
            else if (res == 2) mostrarErro("Limite Atingido!\nVocê já tem máquinas suficientes para todos os solos.");
        });
    }
    
    private void executarComprarFertilizante(String cmd) {
        if(!FazendaEstado.getInstance().comprarFertilizante()) {
             SwingUtilities.invokeLater(() -> mostrarErro("Dinheiro insuficiente!"));
        } else {
             SwingUtilities.invokeLater(() -> painelLateral.repaint());
        }
    }

    private void executarEvoluirSolo(String cmd) {
        int id = Integer.parseInt(cmd.split("_")[3]);
        if (FazendaEstado.getInstance().evoluirSolo(id)) {
            SwingUtilities.invokeLater(() -> {
                selecionarSolo(id); 
                mostrarAviso("Solo evoluído com sucesso!");
            });
        } else {
            SwingUtilities.invokeLater(() -> mostrarErro("Dinheiro insuficiente para evoluir!"));
        }
    }

    private void executarDesbloquearSolo(String cmd) {
        int id = Integer.parseInt(cmd.split("_")[3]);
        if (FazendaEstado.getInstance().desbloquearSolo(id)) {
            SwingUtilities.invokeLater(() -> {
                selecionarLoja(); 
                mostrarAviso("Solo desbloqueado!");
            });
        } else {
            SwingUtilities.invokeLater(() -> mostrarErro("Dinheiro insuficiente!"));
        }
    }

    private void executarColetarAnimal(String cmd) {
        int idCercado = Integer.parseInt(cmd.split("_")[3]);
        FazendaEstado fazenda = FazendaEstado.getInstance();
        Cercado c = fazenda.getCercados().get(idCercado);
        double valor = c.coletarProdutos();
        fazenda.ganharDinheiro(valor);
        SwingUtilities.invokeLater(() -> selecionarCercado(idCercado));
    }

    private void executarSair(String cmd) {
        SwingUtilities.invokeLater(() -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Deseja realmente sair do jogo?", "Confirmar Saída", 
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                gameLoop.parar();
                timerUI.stop();
                executor.parar();
                this.dispose();
                System.exit(0);
            }
        });
    }

    // --- 4. MÉTODOS AUXILIARES E DE NAVEGAÇÃO ---

    public void selecionarSolo(int id) { 
        painelLateral.mostrarMenuSolo(FazendaEstado.getInstance().getSolos().get(id));
        painelFazenda.setSoloSelecionado(id);
    }
    
    public void selecionarLoja() { 
        painelLateral.mostrarLoja();
        painelFazenda.setLojaSelecionada();
    }
    
    public void selecionarCercado(int id) { 
        painelLateral.mostrarCercado(id); 
        painelFazenda.setCercadoSelecionado(id);
    }
    
    public void selecionarGeral() { 
        painelLateral.mostrarGeral(); 
        painelFazenda.limparSelecao();
    }

    private void mostrarErro(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atenção", JOptionPane.ERROR_MESSAGE);
    }
    
    private void mostrarAviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Informação", JOptionPane.INFORMATION_MESSAGE);
    }

    // --- 5. IMPLEMENTAÇÃO DA INTERFACE GERENCIADOR EVENTOS ---

    @Override
    public void aoAtualizarStatusFazenda(double dinheiro, int dia, int estoqueFert) {
        SwingUtilities.invokeLater(() -> painelLateral.atualizarHUD(dinheiro, dia, estoqueFert));
    }

    @Override
    public void aoAtualizarSolo(Solo solo) {
        // Tratado pelo TimerUI
    }

    @Override
    public void aoNotificarEvento(String mensagem) { 
        System.out.println("Evento: " + mensagem);
    }

    @Override
    public void aoVencerJogo() {
        SwingUtilities.invokeLater(() -> {
            gameLoop.alternarPause(); 
            Object[] options = {"Continuar Jogando", "Sair do Jogo"};
            int n = JOptionPane.showOptionDialog(this,
                "PARABÉNS!\n\nVocê atingiu a meta de R$ " + Constantes.DINHEIRO_VITORIA + "!\nSua fazenda é um sucesso absoluto.",
                "Vitória!", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);

            if (n == 1) System.exit(0);
            else gameLoop.alternarPause();
        });
    }
}
