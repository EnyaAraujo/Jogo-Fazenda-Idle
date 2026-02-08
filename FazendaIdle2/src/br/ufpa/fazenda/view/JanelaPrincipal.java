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

public class JanelaPrincipal extends JFrame implements GerenciadorEventos, ActionListener {

    private PainelFazenda painelFazenda;
    private PainelLateral painelLateral;
    private GameLoop gameLoop;
    private Timer timerUI; 
    private ExecutorTarefas executor; 

    public JanelaPrincipal() {
        setTitle("Fazenda Idle 2.0");
        setMinimumSize(new Dimension(Constantes.TAMANHO_MINIMO_JANELA_X, Constantes.TAMANHO_MINIMO_JANELA_Y));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
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
    
    // --- MÉTODOS DE NAVEGAÇÃO E SELEÇÃO VISUAL ---
    
    public void selecionarSolo(int id) { 
        // Atualiza a lógica do menu lateral
        painelLateral.mostrarMenuSolo(FazendaEstado.getInstance().getSolos().get(id));
        // Atualiza o visual da caixa branca no painel da fazenda
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

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        executor.executar(() -> processarAcao(cmd));
    }
    
    private void processarAcao(String cmd) {
        FazendaEstado fazenda = FazendaEstado.getInstance();

        if (cmd.equals("CMD_TOGGLE_IA")) {
            if (fazenda.isModoIAAtivado()) {
                gameLoop.desativarIA();
                fazenda.setModoIAAtivado(false);
            } else {
                gameLoop.ativarIA();
                fazenda.setModoIAAtivado(true);
            }
        }
        else if (cmd.startsWith("CMD_LOJA_COMPRAR_ANIMAL_")) {
            String especieStr = cmd.substring("CMD_LOJA_COMPRAR_ANIMAL_".length());
            Animal animal = Animal.valueOf(especieStr);
            
            int resultado = fazenda.comprarAnimalComVerificacao(animal);
            
            SwingUtilities.invokeLater(() -> {
                if (resultado == 0) mostrarAviso("Sucesso! " + animal.getNome() + " enviado para um cercado novo/vazio.");
                else if (resultado == 1) mostrarErro("Dinheiro insuficiente para comprar " + animal.getNome() + "!");
                else if (resultado == 2) mostrarErro("Todos os cercados para " + animal.getNome() + " estão CHEIOS (Máx 3)!");
                else if (resultado == 3) mostrarAviso("Mais uma " + animal.getNome() + " adicionada ao cercado existente!");
            });
        }
        else if (cmd.startsWith("CMD_LOJA_COMPRAR_MAQUINA_")) {
            String maqStr = cmd.substring("CMD_LOJA_COMPRAR_MAQUINA_".length());
            Maquina m = Maquina.valueOf(maqStr);
            
            int res = fazenda.comprarMaquinaComVerificacao(m);
            SwingUtilities.invokeLater(() -> {
                if (res == 0) mostrarAviso(m.getNome() + " comprada! Equipe-a em um solo.");
                else if (res == 1) mostrarErro("Dinheiro insuficiente!");
                else if (res == 2) mostrarErro("Limite Atingido!\nVocê já tem máquinas suficientes para todos os solos.");
            });
        }
        else if (cmd.startsWith("CMD_EVOLUIR_SOLO_")) {
            int id = Integer.parseInt(cmd.split("_")[3]);
            if (fazenda.evoluirSolo(id)) {
                SwingUtilities.invokeLater(() -> {
                    selecionarSolo(id); 
                    mostrarAviso("Solo evoluído com sucesso!");
                });
            } else {
                SwingUtilities.invokeLater(() -> mostrarErro("Dinheiro insuficiente para evoluir!"));
            }
        }
        else if (cmd.startsWith("CMD_DESBLOQUEAR_SOLO_")) {
            int id = Integer.parseInt(cmd.split("_")[3]);
            if (fazenda.desbloquearSolo(id)) {
                SwingUtilities.invokeLater(() -> {
                    selecionarLoja(); 
                    mostrarAviso("Solo desbloqueado!");
                });
            } else {
                SwingUtilities.invokeLater(() -> mostrarErro("Dinheiro insuficiente!"));
            }
        }
        else if (cmd.startsWith("CMD_PLANTAR_")) {
            String[] parts = cmd.split("_");
            int id = Integer.parseInt(parts[2]);
            Vegetal v = Vegetal.valueOf(parts[3]);
            
            if(fazenda.plantarManual(id, v)) {
                SwingUtilities.invokeLater(() -> selecionarSolo(id));
            }
        }
        else if (cmd.startsWith("CMD_COLHER_")) {
            int id = Integer.parseInt(cmd.split("_")[2]);
            Solo s = fazenda.getSolos().get(id);
            double val = s.colher();
            fazenda.ganharDinheiro(val);
            SwingUtilities.invokeLater(() -> selecionarSolo(id));
        }
        else if (cmd.startsWith("CMD_ARRANCAR_")) {
            int id = Integer.parseInt(cmd.split("_")[2]);
            fazenda.getSolos().get(id).arrancar();
            SwingUtilities.invokeLater(() -> selecionarSolo(id));
        }
        else if (cmd.startsWith("CMD_COLETAR_ANIMAL_")) {
            int idCercado = Integer.parseInt(cmd.split("_")[3]);
            Cercado c = fazenda.getCercados().get(idCercado);
            double valor = c.coletarProdutos();
            fazenda.ganharDinheiro(valor);
            SwingUtilities.invokeLater(() -> selecionarCercado(idCercado));
        }
        else if (cmd.equals("CMD_PAUSE")) {
            gameLoop.alternarPause();
        }
        else if (cmd.equals("CMD_COMPRAR_FERTILIZANTE")) {
             if(!fazenda.comprarFertilizante()) {
                 SwingUtilities.invokeLater(() -> mostrarErro("Dinheiro insuficiente!"));
             } else {
                 SwingUtilities.invokeLater(() -> painelLateral.repaint());
             }
        }
        else if (cmd.equals("CMD_SAIR")) {
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
    }

    private void mostrarErro(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atenção", JOptionPane.ERROR_MESSAGE);
    }
    private void mostrarAviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Informação", JOptionPane.INFORMATION_MESSAGE);
    }

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
            // Para o jogo momentaneamente para o usuário ler
            gameLoop.alternarPause(); 
            
            Object[] options = {"Continuar Jogando", "Sair do Jogo"};
            
            int n = JOptionPane.showOptionDialog(this,
                "PARABÉNS!\n\n" +
                "Você atingiu a meta de R$ " + Constantes.DINHEIRO_VITORIA + "!\n" +
                "Sua fazenda é um sucesso absoluto.",
                "Vitória!",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,     // ícone padrão
                options,  // botões
                options[0]); // botão padrão

            if (n == 1) { // Se escolheu "Sair do Jogo" (índice 1)
                System.exit(0);
            } else {
                // Se escolheu "Continuar", apenas despausa
                gameLoop.alternarPause();
            }
        });
    }
}