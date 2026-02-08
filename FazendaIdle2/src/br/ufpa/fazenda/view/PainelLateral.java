package br.ufpa.fazenda.view;

import br.ufpa.fazenda.model.*;
import br.ufpa.fazenda.util.Constantes;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Map;

public class PainelLateral extends JPanel {

    private CardLayout layoutCartas;
    private JPanel painelConteudo;
    
    // HUD Global
    private JLabel lblDinheiro, lblDia;
    private JButton btnPause;
    
    // Referências para atualização em tempo real
    private JProgressBar barraProgressoAtiva;
    private JLabel lblStatusAtivo;
    private Solo soloAtualVisualizado; 
    private Cercado cercadoAtualVisualizado;
    
    private JButton btnToggleIA;
    private JButton btnSair;

    private ActionListener acaoBotaoListener;

    public PainelLateral(ActionListener listener) {
        this.acaoBotaoListener = listener;
        this.setLayout(new BorderLayout());
        this.setBackground(new Color(245, 245, 220));
        this.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, new Color(139, 69, 19)));

        // 1. Cabeçalho (HUD)
        JPanel hud = new JPanel(new GridLayout(3, 1, 5, 5));
        hud.setOpaque(false);
        hud.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        lblDinheiro = new JLabel("R$ 0.00");
        lblDinheiro.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblDinheiro.setForeground(new Color(34, 139, 34));
        lblDinheiro.setHorizontalAlignment(SwingConstants.CENTER);
        
        lblDia = new JLabel("Dia 1");
        lblDia.setHorizontalAlignment(SwingConstants.CENTER);
        
        btnPause = new JButton("Pausar / Resumir");
        btnPause.setActionCommand("CMD_PAUSE");
        btnPause.addActionListener(listener);
        
        hud.add(lblDinheiro);
        hud.add(lblDia);
        hud.add(btnPause);
        
        this.add(hud, BorderLayout.NORTH);

        // 2. Corpo (Cards)
        layoutCartas = new CardLayout();
        painelConteudo = new JPanel(layoutCartas);
        painelConteudo.setOpaque(false);
        
        painelConteudo.add(criarMenuGeral(), "GERAL");
        painelConteudo.add(new JPanel(), "SOLO");
        painelConteudo.add(new JPanel(), "LOJA");
        painelConteudo.add(new JPanel(), "CERCADO");
        
        this.add(painelConteudo, BorderLayout.CENTER);
        
        layoutCartas.show(painelConteudo, "GERAL");
    }

    public void atualizarDadosDinamicos() {
        if (soloAtualVisualizado != null && barraProgressoAtiva != null && painelConteudo.isVisible()) {
            if (soloAtualVisualizado.isOcupado()) {
                barraProgressoAtiva.setValue((int)(soloAtualVisualizado.getProgresso() * 100));
                if (soloAtualVisualizado.isPronto() && barraProgressoAtiva.getForeground() != Color.ORANGE) {
                     Solo soloAtualizado = FazendaEstado.getInstance().getSolos().get(soloAtualVisualizado.getId());
                     mostrarMenuSolo(soloAtualizado);
                }
            } else {
                barraProgressoAtiva.setValue(0);
            }
        }
        
        if (cercadoAtualVisualizado != null && barraProgressoAtiva != null) {
            barraProgressoAtiva.setValue((int)(cercadoAtualVisualizado.getProgresso() * 100));
            if (cercadoAtualVisualizado.isProdutoPronto()) {
                barraProgressoAtiva.setString("PRONTO!");
            } else {
                barraProgressoAtiva.setString((int)(cercadoAtualVisualizado.getProgresso() * 100) + "%");
            }
        }
        
        if (btnToggleIA != null) {
            boolean ativo = FazendaEstado.getInstance().isModoIAAtivado();
            btnToggleIA.setText(ativo ? "[IA LIGADA]" : "[IA DESLIGADA]");
            btnToggleIA.setBackground(ativo ? new Color(150, 255, 150) : new Color(220, 220, 220));
        }
    }

    public void atualizarHUD(double dinheiro, int dia, int fert) {
        lblDinheiro.setText(String.format("R$ %.2f", dinheiro));
        lblDia.setText("Dia " + dia);
        atualizarDadosDinamicos();
    }

    // =================================================================================
    // 🏠 MENU GERAL
    // =================================================================================
    private JPanel criarMenuGeral() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        
        JLabel lblTitulo = new JLabel("Fazenda Idle");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblTitulo);
        p.add(Box.createVerticalStrut(10));
        
        JLabel lblInstrucao = new JLabel("Selecione um local para interagir.");
        lblInstrucao.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblInstrucao);
        p.add(Box.createVerticalStrut(30));
        
        btnToggleIA = new JButton("[IA DESLIGADA]");
        btnToggleIA.setFont(new Font("Arial", Font.BOLD, 14));
        btnToggleIA.setActionCommand("CMD_TOGGLE_IA");
        btnToggleIA.addActionListener(acaoBotaoListener);
        btnToggleIA.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(btnToggleIA);
        p.add(Box.createVerticalStrut(20));
        
        btnSair = new JButton("Sair do Jogo");
        btnSair.setFont(new Font("Arial", Font.BOLD, 14));
        btnSair.setActionCommand("CMD_SAIR");
        btnSair.addActionListener(acaoBotaoListener);
        btnSair.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(btnSair);
        
        return p;
    }
    
    public void mostrarGeral() {
        limparReferenciasDinamicas();
        layoutCartas.show(painelConteudo, "GERAL");
    }

    // =================================================================================
    // 🌱 MENU DO SOLO
    // =================================================================================
    public void mostrarMenuSolo(Solo solo) {
        Solo soloAtualizado = FazendaEstado.getInstance().getSolos().get(solo.getId());
        this.soloAtualVisualizado = soloAtualizado;
        this.cercadoAtualVisualizado = null;
        
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (!soloAtualizado.isDesbloqueado()) {
            JLabel lblTitulo = new JLabel("Solo " + (soloAtualizado.getId() + 1) + " - BLOQUEADO");
            lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(lblTitulo);
            p.add(Box.createVerticalStrut(10));
            
            JLabel lblDesc = new JLabel("Este solo está bloqueado.");
            lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(lblDesc);
            p.add(Box.createVerticalStrut(20));
            
            JButton btnDesbloquear = new JButton("Desbloquear por R$" + (int)Constantes.CUSTO_DESBLOQUEIO_SOLO);
            btnDesbloquear.setFont(new Font("Arial", Font.BOLD, 14));
            btnDesbloquear.setActionCommand("CMD_DESBLOQUEAR_SOLO_" + soloAtualizado.getId());
            btnDesbloquear.addActionListener(acaoBotaoListener);
            btnDesbloquear.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(btnDesbloquear);
            
            painelConteudo.add(p, "SOLO_BLOQUEADO");
            layoutCartas.show(painelConteudo, "SOLO_BLOQUEADO");
            return;
        }

        // Título e Nível
        JLabel lblTitulo = new JLabel("Solo " + (soloAtualizado.getId() + 1));
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblTitulo);
        p.add(Box.createVerticalStrut(5));
        
        JLabel lblNivel = new JLabel("Nível " + soloAtualizado.getNivel());
        lblNivel.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblNivel);
        
        // Botão Evoluir
        if (soloAtualizado.getNivel() < 10) {
            double custo = Constantes.CUSTO_EVOLUCAO_BASE * soloAtualizado.getNivel();
            JButton btnEvoluir = new JButton("Evoluir (R$" + (int)custo + ")");
            btnEvoluir.setFont(new Font("Arial", Font.PLAIN, 11));
            btnEvoluir.setActionCommand("CMD_EVOLUIR_SOLO_" + soloAtualizado.getId());
            btnEvoluir.addActionListener(acaoBotaoListener);
            btnEvoluir.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(btnEvoluir);
        } else {
            JLabel lblMax = new JLabel("(Nível Máximo)");
            lblMax.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(lblMax);
        }
        
        p.add(Box.createVerticalStrut(10));

        // Status e Barra
        String texto = soloAtualizado.isOcupado() ? soloAtualizado.getVegetal().getNome() : "Disponível para plantio";
        lblStatusAtivo = new JLabel(texto);
        lblStatusAtivo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblStatusAtivo.setFont(new Font("Arial", Font.BOLD, 14));
        p.add(lblStatusAtivo);

        barraProgressoAtiva = new JProgressBar(0, 100);
        barraProgressoAtiva.setStringPainted(true);
        barraProgressoAtiva.setVisible(soloAtualizado.isOcupado());
        if (soloAtualizado.isOcupado()) barraProgressoAtiva.setValue((int)(soloAtualizado.getProgresso() * 100));
        p.add(barraProgressoAtiva);
        
        p.add(Box.createVerticalStrut(15));
        
        // Ações de Plantio/Colheita
        if (soloAtualizado.isOcupado()) {
            if (soloAtualizado.isPronto()) {
                JButton btn = new JButton("Colher");
                btn.setBackground(Color.ORANGE);
                btn.setActionCommand("CMD_COLHER_" + soloAtualizado.getId());
                btn.addActionListener(acaoBotaoListener);
                btn.setAlignmentX(Component.CENTER_ALIGNMENT);
                p.add(btn);
            } else {
                JButton btn = new JButton("Arrancar");
                btn.setBackground(new Color(200, 100, 100));
                btn.setActionCommand("CMD_ARRANCAR_" + soloAtualizado.getId());
                btn.addActionListener(acaoBotaoListener);
                btn.setAlignmentX(Component.CENTER_ALIGNMENT);
                p.add(btn);
            }
        } else {
            JPanel pPlantas = new JPanel();
            pPlantas.setLayout(new BoxLayout(pPlantas, BoxLayout.Y_AXIS));
            pPlantas.setOpaque(false);
            
            for (Vegetal v : Vegetal.values()) {
                boolean desbloqueado = v.getNivelMinimo() <= soloAtualizado.getNivel();
                String txtBotao = v.getNome();
                if (!desbloqueado) txtBotao = "[BLOQ] " + v.getNome() + " (Nv " + v.getNivelMinimo() + ")";
                
                JButton btn = new JButton(txtBotao);
                btn.setAlignmentX(Component.CENTER_ALIGNMENT);
                if (desbloqueado) {
                    btn.setActionCommand("CMD_PLANTAR_" + soloAtualizado.getId() + "_" + v.name());
                    btn.addActionListener(acaoBotaoListener);
                } else {
                    btn.setEnabled(false);
                    btn.setToolTipText("Evolua o solo para plantar isso.");
                }
                pPlantas.add(btn);
                pPlantas.add(Box.createVerticalStrut(5));
            }
            p.add(pPlantas);
        }

        // ===================================================================
        // 💰 NOVO PAINEL DE LUCRO DETALHADO (Inserido aqui)
        // ===================================================================
        p.add(Box.createVerticalStrut(10));
        if (soloAtualizado.isOcupado()) {
            JLabel lblLucro = new JLabel(soloAtualizado.getEstimativaLucroHTML());
            lblLucro.setAlignmentX(Component.CENTER_ALIGNMENT);
            // Borda leve para destacar
            lblLucro.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            p.add(lblLucro);
        }
        // ===================================================================

        p.add(Box.createVerticalStrut(10));
        p.add(new JSeparator());
        
        // EQUIPAMENTOS E AUTOMAÇÃO
        JLabel lblEquipamentos = new JLabel("Equipamentos");
        lblEquipamentos.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEquipamentos.setForeground(new Color(100, 80, 50));
        lblEquipamentos.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblEquipamentos);
        
        Map<Maquina, Integer> inventario = FazendaEstado.getInstance().getInventarioMaquinas();
        
        for (Maquina m : Maquina.values()) {
            JPanel pMaq = new JPanel(new FlowLayout(FlowLayout.CENTER));
            pMaq.setOpaque(false);
            
            boolean instalado = soloAtualizado.temMaquina(m);
            JCheckBox chk = new JCheckBox(m.getNome());
            chk.setSelected(instalado);
            chk.setOpaque(false);
            
            int qtd = inventario.getOrDefault(m, 0);
            JLabel lblQtd = new JLabel(instalado ? "(On)" : "(" + qtd + ")");
            lblQtd.setFont(new Font("Arial", Font.PLAIN, 10));
            
            chk.addActionListener(e -> {
                if (chk.isSelected()) {
                    boolean ok = FazendaEstado.getInstance().instalarMaquina(soloAtualizado.getId(), m);
                    if (!ok) {
                        chk.setSelected(false);
                        JOptionPane.showMessageDialog(this, "Sem estoque de " + m.getNome());
                    }
                } else {
                    FazendaEstado.getInstance().desinstalarMaquina(soloAtualizado.getId(), m);
                }
                mostrarMenuSolo(soloAtualizado); 
            });
            
            pMaq.add(chk);
            pMaq.add(lblQtd);
            p.add(pMaq);
        }
        
        p.add(Box.createVerticalStrut(10));
        
        JLabel lblFert = new JLabel("Fertilizante");
        lblFert.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblFert.setForeground(new Color(100, 80, 50));
        lblFert.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblFert);
        
        JCheckBox chkFert = new JCheckBox("Usar Fertilizante");
        chkFert.setSelected(soloAtualizado.isFertilizanteAtivado());
        chkFert.setOpaque(false);
        chkFert.setAlignmentX(Component.CENTER_ALIGNMENT);
        chkFert.addActionListener(e -> {
            soloAtualizado.setFertilizanteAtivado(chkFert.isSelected());
            mostrarMenuSolo(soloAtualizado); // Atualiza para recalcular lucro
        });
        p.add(chkFert);
        
        JLabel lblEstoque = new JLabel("Estoque: " + FazendaEstado.getInstance().getEstoqueFertilizante());
        lblEstoque.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblEstoque.setFont(new Font("Arial", Font.ITALIC, 11));
        p.add(lblEstoque);
        
        JCheckBox chkAutoBuy = new JCheckBox("Auto-Comprar");
        chkAutoBuy.setSelected(FazendaEstado.getInstance().isAutoCompraFertilizante());
        chkAutoBuy.setOpaque(false);
        chkAutoBuy.setFont(new Font("Arial", Font.ITALIC, 11));
        chkAutoBuy.setAlignmentX(Component.CENTER_ALIGNMENT);
        chkAutoBuy.addActionListener(e -> {
            FazendaEstado.getInstance().setAutoCompraFertilizante(chkAutoBuy.isSelected());
            mostrarMenuSolo(soloAtualizado); 
        });
        p.add(chkAutoBuy);
        
        JButton btnBuyFert = new JButton("Comprar +10 (R$" + (int)Constantes.CUSTO_FERTILIZANTE_LOTE + ")");
        btnBuyFert.setFont(new Font("Arial", Font.PLAIN, 10));
        btnBuyFert.setActionCommand("CMD_COMPRAR_FERTILIZANTE");
        btnBuyFert.addActionListener(acaoBotaoListener);
        btnBuyFert.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(btnBuyFert);

        painelConteudo.add(p, "SOLO_ATUAL");
        layoutCartas.show(painelConteudo, "SOLO_ATUAL");
    }

    // =================================================================================
    // 🐔 MENU CERCADO
    // =================================================================================
    public void mostrarCercado(int idCercado) {
        this.soloAtualVisualizado = null;
        this.cercadoAtualVisualizado = FazendaEstado.getInstance().getCercados().get(idCercado);
        
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitulo = new JLabel("Cercado " + (idCercado + 1));
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblTitulo);

        if (cercadoAtualVisualizado.isVazio()) {
            p.add(Box.createVerticalStrut(10));
            JLabel lblVazio = new JLabel("Status: VAZIO");
            lblVazio.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(lblVazio);
            p.add(Box.createVerticalStrut(10));
            JLabel lblInstrucao = new JLabel("Compre animais na Loja.");
            lblInstrucao.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(lblInstrucao);
        } else {
            JLabel lblTipo = new JLabel("Animal: " + cercadoAtualVisualizado.getEspecie().getNome());
            lblTipo.setFont(new Font("Arial", Font.BOLD, 16));
            lblTipo.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(lblTipo);
            
            JLabel lblQtd = new JLabel("Qtd: " + cercadoAtualVisualizado.getQuantidade() + "/3");
            lblQtd.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(lblQtd);
            
            p.add(Box.createVerticalStrut(10));
            
            barraProgressoAtiva = new JProgressBar(0, 100);
            barraProgressoAtiva.setStringPainted(true);
            barraProgressoAtiva.setValue((int)(cercadoAtualVisualizado.getProgresso() * 100));
            if (cercadoAtualVisualizado.isProdutoPronto()) {
                barraProgressoAtiva.setString("PRONTO!");
                barraProgressoAtiva.setForeground(Color.ORANGE);
            } else {
                barraProgressoAtiva.setString((int)(cercadoAtualVisualizado.getProgresso() * 100) + "%");
            }
            p.add(barraProgressoAtiva);
            
            // ===================================================================
            // 💰 NOVO PAINEL DE LUCRO ANIMAL (Inserido aqui)
            // ===================================================================
            p.add(Box.createVerticalStrut(10));
            JLabel lblLucro = new JLabel(cercadoAtualVisualizado.getDetalhesLucroHTML());
            lblLucro.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblLucro.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            p.add(lblLucro);
            // ===================================================================
            
            p.add(Box.createVerticalStrut(10));
            
            if (cercadoAtualVisualizado.isProdutoPronto()) {
                JButton btn = new JButton("Coletar");
                btn.setBackground(Color.ORANGE);
                btn.setActionCommand("CMD_COLETAR_ANIMAL_" + idCercado);
                btn.addActionListener(acaoBotaoListener);
                btn.setAlignmentX(Component.CENTER_ALIGNMENT);
                p.add(btn);
            } else {
                JLabel lblProduzindo = new JLabel("Produzindo...");
                lblProduzindo.setAlignmentX(Component.CENTER_ALIGNMENT);
                p.add(lblProduzindo);
            }
        }
        
        painelConteudo.add(p, "CERCADO_ATUAL");
        layoutCartas.show(painelConteudo, "CERCADO_ATUAL");
    }

    // =================================================================================
    // 🏪 MENU DA LOJA
    // =================================================================================
    public void mostrarLoja() {
        limparReferenciasDinamicas();
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblTitulo = new JLabel("LOJA DA FAZENDA");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblTitulo);
        p.add(Box.createVerticalStrut(15));

        JLabel lblAnimais = new JLabel("Animais (Max 3/cercado)");
        lblAnimais.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblAnimais.setForeground(new Color(100, 80, 50));
        lblAnimais.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblAnimais);
        p.add(Box.createVerticalStrut(5));
        
        for (Animal animal : Animal.values()) {
            JButton btn = new JButton(String.format("Comprar %s (R$%.0f)", animal.getNome(), animal.getPrecoCompra()));
            btn.setActionCommand("CMD_LOJA_COMPRAR_ANIMAL_" + animal.name());
            btn.addActionListener(acaoBotaoListener);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(btn);
            p.add(Box.createVerticalStrut(5));
        }
        
        p.add(Box.createVerticalStrut(15));

        JLabel lblMaquinas = new JLabel("Máquinas (Inventário)");
        lblMaquinas.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblMaquinas.setForeground(new Color(100, 80, 50));
        lblMaquinas.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblMaquinas);
        p.add(Box.createVerticalStrut(5));
        
        for (Maquina maq : Maquina.values()) {
            JButton btn = new JButton(String.format("%s (R$%.0f)", maq.getNome(), maq.getCusto()));
            btn.setToolTipText(maq.getDescricao());
            btn.setActionCommand("CMD_LOJA_COMPRAR_MAQUINA_" + maq.name());
            btn.addActionListener(acaoBotaoListener);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(btn);
            p.add(Box.createVerticalStrut(5));
        }

        p.add(Box.createVerticalStrut(15));
        JLabel lblExpansao = new JLabel("Expansão");
        lblExpansao.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblExpansao.setForeground(new Color(100, 80, 50));
        lblExpansao.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lblExpansao);
        p.add(Box.createVerticalStrut(5));
        
        int proximoBloqueado = -1;
        for(Solo s : FazendaEstado.getInstance().getSolos()) {
            if(!s.isDesbloqueado()) {
                proximoBloqueado = s.getId();
                break;
            }
        }
        
        if (proximoBloqueado != -1) {
            JButton btnUnlock = new JButton("Desbloquear Solo " + (proximoBloqueado+1) + " (R$" + (int)Constantes.CUSTO_DESBLOQUEIO_SOLO + ")");
            btnUnlock.setActionCommand("CMD_DESBLOQUEAR_SOLO_" + proximoBloqueado);
            btnUnlock.addActionListener(acaoBotaoListener);
            btnUnlock.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(btnUnlock);
        } else {
            JLabel lblTodos = new JLabel("Todos os solos desbloqueados!");
            lblTodos.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(lblTodos);
        }

        JScrollPane scroll = new JScrollPane(p);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        
        painelConteudo.add(scroll, "LOJA_ATUAL");
        layoutCartas.show(painelConteudo, "LOJA_ATUAL");
    }

    private void limparReferenciasDinamicas() {
        this.soloAtualVisualizado = null;
        this.cercadoAtualVisualizado = null;
        this.barraProgressoAtiva = null;
    }
}