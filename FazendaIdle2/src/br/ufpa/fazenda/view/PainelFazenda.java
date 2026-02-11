package br.ufpa.fazenda.view;

import br.ufpa.fazenda.model.*;
import br.ufpa.fazenda.util.Constantes;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JPanel;

public class PainelFazenda extends JPanel {

    private JanelaPrincipal janelaMae;
    private Image imgGrama, imgLoja, imgCerca, imgSoloArado, imgSoloBloqueado, imgSoloPlantado, imgCeleiro;

    //ESTADOS DE SELEÇÃO VISUAL
    // -1 significa "nenhum"
    private int idSoloHover = -1;
    private int idSoloSelecionado = -1;
    
    private int idCercadoHover = -1;
    private int idCercadoSelecionado = -1;
    
    private boolean lojaHover = false;
    private boolean lojaSelecionada = false;

    // debugar
    private static final boolean DESENHAR_HITBOXES = false; // Mudei para false para ficar mais bonito agora

    // configuracao do layout
    private double LOJA_X = 0.62;     
    private double LOJA_Y = 0.08;     
    private double LOJA_W = 0.25;     
    private double LOJA_H = 0.18;     

    private double SOLO_START_X = 0.06; 
    private double SOLO_START_Y = 0.32; 
    private double SOLO_SIZE_W = 0.13;  
    private double SOLO_SIZE_H = 0.13;  
    private double SOLO_GAP_X = 0.02;   
    private double SOLO_GAP_Y = 0.02;   

    private double CERCA_Y = 0.70;      
    private double CERCA_W = 0.20;      
    private double CERCA_H = 0.20;      
    private double CERCA_ESPACO_TOTAL = 0.10; 


    private double CELEIRO_X = 0.05;
    private double CELEIRO_Y = 0.05;
    private double CELEIRO_H = 0.25;
    private double CELEIRO_W = 0.20;

    // Áreas lógicas (Hitboxes)
    private Rectangle areaLoja = new Rectangle();
    private Rectangle[] areaSolos = new Rectangle[Constantes.QTD_SOLOS];
    private Rectangle[] areaCercados = new Rectangle[Constantes.QTD_CERCADOS];
    private Rectangle areaCeleiro = new Rectangle();
    

    public PainelFazenda(JanelaPrincipal janela) {
        this.janelaMae = janela;
        this.setBackground(new Color(34, 139, 34)); 

        carregarImagens();

        // vai receber os cliques do mouse
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                processarClique(e.getX(), e.getY());
            }
        });

        // vai captar os movimentos do mouse
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                processarHover(e.getX(), e.getY());
            }
        });
    }

    private void carregarImagens() {
        RenderizadorAssets assets = RenderizadorAssets.get();
        imgGrama = assets.getImagem("grama_bg.png");
        imgLoja = assets.getImagem("lojinha.png");
        imgCerca = assets.getImagem("cercado.png");
        imgSoloArado = assets.getImagem("solo_arado.png", true);
        imgSoloBloqueado = assets.getImagem("solo_bloqueado.png", true);
        imgSoloPlantado = assets.getImagem("solo_plantado.png", true);
        imgCeleiro = assets.getImagem("celeiro.png", true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        // Melhora a qualidade do desenho (anti-aliasing)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. Desenha o Fundo
        if (imgGrama != null) {
            g2d.drawImage(imgGrama, 0, 0, w, h, null);
        } else {
            g2d.setColor(new Color(34, 139, 34));
            g2d.fillRect(0, 0, w, h);
        }

        // --- ATUALIZA AS HITBOXES ---
        atualizarHitboxes(w, h);

        // --- DESENHA ELEMENTOS ---
        desenharLoja(g2d, areaLoja);

        desenharCeleiro(g2d, areaCeleiro);

        for (int i = 0; i < Constantes.QTD_SOLOS; i++) {
            desenharSolo(g2d, areaSolos[i], i);
        }

        for (int i = 0; i < Constantes.QTD_CERCADOS; i++) {
            desenharCercado(g2d, areaCercados[i], i);
        }
        
        // Desenha caixa de selecao pra indicar q foi selecionado
        desenharEfeitosVisuais(g2d);
        }

     private void desenharCeleiro(Graphics2D g, Rectangle r) {
        if (DESENHAR_HITBOXES) {
            g.setColor(Color.RED); g.drawRect(r.x, r.y, r.width, r.height);
        }
        // Desenha a imagem se ela foi carregada
        if (imgCeleiro != null) {
            g.drawImage(imgCeleiro, r.x, r.y, r.width, r.height, null);
        }
    }
}
    
    private void atualizarHitboxes(int w, int h) {
        // Loja
        int lX = (int)(w * LOJA_X); int lY = (int)(h * LOJA_Y);
        int lW = (int)(w * LOJA_W); int lH = (int)(h * LOJA_H);
        areaLoja.setBounds(lX, lY, lW, lH);

        // Solos
        int sW = (int)(w * SOLO_SIZE_W); int sH = (int)(h * SOLO_SIZE_H);
        int gapX = (int)(w * SOLO_GAP_X); int gapY = (int)(w * SOLO_GAP_Y);
        int startX = (int)(w * SOLO_START_X); int startY = (int)(h * SOLO_START_Y);

        for (int i = 0; i < Constantes.QTD_SOLOS; i++) {
            int row = i / 6; int col = i % 6;
            int x = startX + (col * (sW + gapX));
            int y = startY + (row * (sH + gapY));
            if (areaSolos[i] == null) areaSolos[i] = new Rectangle();
            areaSolos[i].setBounds(x, y, sW, sH);
        }

        // Cercados
        int cW = (int)(w * CERCA_W); int cH = (int)(h * CERCA_H);
        int cY = (int)(h * CERCA_Y);
        int espacoEntreCercas = (int)(w * CERCA_ESPACO_TOTAL);
        int larguraTotalOcupada = (Constantes.QTD_CERCADOS * cW) + ((Constantes.QTD_CERCADOS - 1) * espacoEntreCercas);
        int cXInicial = (w - larguraTotalOcupada) / 2;

        for (int i = 0; i < Constantes.QTD_CERCADOS; i++) {
            int x = cXInicial + (i * (cW + espacoEntreCercas));
            if (areaCercados[i] == null) areaCercados[i] = new Rectangle();
            areaCercados[i].setBounds(x, cY, cW, cH);
        }
    
        int celX = (int)(w * CELEIRO_X); 
        int celY = (int)(h * CELEIRO_Y);
        int celW = (int)(w * CELEIRO_W); 
        int celH = (int)(h * CELEIRO_H);
        areaCeleiro.setBounds(celX, celY, celW, celH);
    
    }

    // --- LÓGICA DE VISUALIZAÇÃO DE HOVER E SELECT ---
    private void desenharEfeitosVisuais(Graphics2D g2d) {
        // Configurações de estilo
        Stroke linhaFina = new BasicStroke(2.0f); // Hover
        Stroke linhaGrossa = new BasicStroke(4.0f); // Selecionado
        Color corBranca = Color.WHITE;
        Color corHover = new Color(255, 255, 255, 180); // Branco transparente

        // 1. LOJA
        if (lojaSelecionada) {
            g2d.setColor(corBranca);
            g2d.setStroke(linhaGrossa);
            g2d.drawRect(areaLoja.x, areaLoja.y, areaLoja.width, areaLoja.height);
        } else if (lojaHover) {
            g2d.setColor(corHover);
            g2d.setStroke(linhaFina);
            g2d.drawRect(areaLoja.x, areaLoja.y, areaLoja.width, areaLoja.height);
        }

        // 2. SOLOS
        for (int i = 0; i < Constantes.QTD_SOLOS; i++) {
            Rectangle r = areaSolos[i];
            if (i == idSoloSelecionado) {
                g2d.setColor(corBranca);
                g2d.setStroke(linhaGrossa);
                g2d.drawRect(r.x, r.y, r.width, r.height);
            } else if (i == idSoloHover) {
                g2d.setColor(corHover);
                g2d.setStroke(linhaFina);
                g2d.drawRect(r.x, r.y, r.width, r.height);
            }
        }

        // 3. CERCADOS
        for (int i = 0; i < Constantes.QTD_CERCADOS; i++) {
            Rectangle r = areaCercados[i];
            if (i == idCercadoSelecionado) {
                g2d.setColor(corBranca);
                g2d.setStroke(linhaGrossa);
                g2d.drawRect(r.x, r.y, r.width, r.height);
            } else if (i == idCercadoHover) {
                g2d.setColor(corHover);
                g2d.setStroke(linhaFina);
                g2d.drawRect(r.x, r.y, r.width, r.height);
            }
        }
        
        // Reseta o stroke para o padrão
        g2d.setStroke(new BasicStroke(1.0f));
    }

    // --- MÉTODOS DE DESENHO (Conteúdo) ---

    private void desenharLoja(Graphics2D g, Rectangle r) {
        if (DESENHAR_HITBOXES) {
            g.setColor(Color.RED); g.drawRect(r.x, r.y, r.width, r.height);
        }
        if (imgLoja != null) g.drawImage(imgLoja, r.x, r.y, r.width, r.height, null);
    }

    private void desenharSolo(Graphics2D g, Rectangle r, int id) {
        Solo solo = FazendaEstado.getInstance().getSolos().get(id);
        RenderizadorAssets assets = RenderizadorAssets.get();

        if (!solo.isDesbloqueado()) {
            if (imgSoloBloqueado != null) {
                g.drawImage(imgSoloBloqueado, r.x, r.y, r.width, r.height, null);
            } else {
                g.setColor(new Color(0, 0, 0, 100));
                g.fillRect(r.x, r.y, r.width, r.height);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 14));
                g.drawString("BLOQ.", r.x + r.width/2 - 20, r.y + r.height/2);
            }
            return;
        }

        Image baseSolo = imgSoloArado;
        if (solo.isOcupado()) {
            if (solo.isPronto()) {
                Vegetal vegetal = solo.getVegetal();
                String nomeImgPlanta = "solo_" + vegetal.name().toLowerCase() + ".png";
                Image imgPlanta = assets.getImagem(nomeImgPlanta, true);
                if (imgPlanta != null) baseSolo = imgPlanta;
            } else {
                baseSolo = imgSoloPlantado;
            }
        }

        if (baseSolo != null) g.drawImage(baseSolo, r.x, r.y, r.width, r.height, null);

        // Fallbacks e Barras
        if (solo.isOcupado()) {
            if (!solo.isPronto() && imgSoloPlantado == null) {
                g.setColor(new Color(34, 139, 34));
                int p = (int)(r.width * 0.25);
                g.fillOval(r.x + p, r.y + p, r.width - 2*p, r.height - 2*p);
            }
            
            if (solo.isPronto()) {
                Vegetal vegetal = solo.getVegetal();
                String nomeImgPlanta = "solo_" + vegetal.name().toLowerCase() + ".png";
                if (assets.getImagem(nomeImgPlanta, true) == null) {
                    g.setColor(new Color(255, 215, 0));
                    int p = (int)(r.width * 0.25);
                    g.fillOval(r.x + p, r.y + p, r.width - 2*p, r.height - 2*p);
                    g.setColor(Color.RED);
                    g.setFont(new Font("SansSerif", Font.BOLD, 24)); 
                    g.drawString("!", r.x + r.width - 20, r.y + 25);
                }
            }
            
            if (!solo.isPronto()) {
                int barW = (int)(r.width * 0.8); int barH = 5;
                int barX = r.x + (r.width - barW) / 2; int barY = r.y + 5;
                g.setColor(Color.BLACK); g.fillRect(barX, barY, barW, barH);
                g.setColor(Color.GREEN); g.fillRect(barX, barY, (int)(barW * solo.getProgresso()), barH);
            }
        }
        
        desenharIconesSolo(g, r, solo);
    }

    private void desenharIconesSolo(Graphics2D g, Rectangle r, Solo solo) {
        RenderizadorAssets assets = RenderizadorAssets.get();
        int iconSize = 16; int iconGap = 2;
        int startX = r.x + r.width - iconSize - 5; int startY = r.y + r.height - iconSize - 5;
        int count = 0;
        
        if (solo.isFertilizanteAtivado()) {
            Image fertIcon = assets.getImagem("fertilizante_icon.png", true);
            if (fertIcon != null) g.drawImage(fertIcon, startX, startY - (count * (iconSize + iconGap)), iconSize, iconSize, null);
            else { g.setColor(Color.CYAN); g.fillRect(startX, startY - (count * (iconSize + iconGap)), iconSize, iconSize); }
            count++;
        }
        if (solo.temMaquina(Maquina.ARADOR)) {
            Image icon = assets.getImagem("arador_icon.png", true);
            if (icon != null) g.drawImage(icon, startX, startY - (count * (iconSize + iconGap)), iconSize, iconSize, null);
            else { g.setColor(new Color(139, 69, 19)); g.fillRect(startX, startY - (count * (iconSize + iconGap)), iconSize, iconSize); }
            count++;
        }
        if (solo.temMaquina(Maquina.TRATOR)) {
            Image icon = assets.getImagem("trator_icon.png", true);
            if (icon != null) g.drawImage(icon, startX, startY - (count * (iconSize + iconGap)), iconSize, iconSize, null);
            else { g.setColor(Color.BLUE); g.fillRect(startX, startY - (count * (iconSize + iconGap)), iconSize, iconSize); }
            count++;
        }
        if (solo.temMaquina(Maquina.IRRIGADOR)) {
            Image icon = assets.getImagem("irrigador_icon.png", true);
            if (icon != null) g.drawImage(icon, startX, startY - (count * (iconSize + iconGap)), iconSize, iconSize, null);
            else { g.setColor(new Color(100, 100, 255)); g.fillRect(startX, startY - (count * (iconSize + iconGap)), iconSize, iconSize); }
            count++;
        }
    }

    private void desenharCercado(Graphics2D g, Rectangle r, int id) {
        if (DESENHAR_HITBOXES) { g.setColor(Color.RED); g.drawRect(r.x, r.y, r.width, r.height); }

        if (imgCerca != null) g.drawImage(imgCerca, r.x, r.y, r.width, r.height, null);

        Cercado cercado = FazendaEstado.getInstance().getCercados().get(id);
        if (!cercado.isVazio()) {
            RenderizadorAssets assets = RenderizadorAssets.get();
            String nomeImg = cercado.getEspecie().name().toLowerCase() + ".png"; 
            Image imgAnimal = assets.getImagem(nomeImg, true);

            if (imgAnimal != null) {
                int aW = (int)(r.width * 0.7); int aH = (int)(r.height * 0.7);
                int aX = r.x + (r.width - aW) / 2; int aY = r.y + (r.height - aH) / 2;
                g.drawImage(imgAnimal, aX, aY, aW, aH, null);
            } else {
                g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.BOLD, 12));
                String nome = cercado.getEspecie().getNome();
                FontMetrics fm = g.getFontMetrics();
                g.drawString(nome, r.x + (r.width - fm.stringWidth(nome)) / 2, r.y + r.height/2);
            }
            
            if (!cercado.isProdutoPronto()) {
                int barW = (int)(r.width * 0.8); int barH = 5;
                int barX = r.x + (r.width - barW) / 2; int barY = r.y + 5;
                g.setColor(Color.BLACK); g.fillRect(barX, barY, barW, barH);
                g.setColor(new Color(255, 165, 0)); 
                g.fillRect(barX, barY, (int)(barW * cercado.getProgresso()), barH);
            } else {
                g.setColor(Color.YELLOW); g.setFont(new Font("SansSerif", Font.BOLD, 24));
                g.drawString("!", r.x + r.width - 20, r.y + 25);
            }
        }
    }

    // --- PROCESSAMENTO DE ENTRADA ---

    private void processarHover(int x, int y) {
        int oldSolo = idSoloHover;
        int oldCercado = idCercadoHover;
        boolean oldLoja = lojaHover;

        idSoloHover = -1;
        idCercadoHover = -1;
        lojaHover = false;

        if (areaLoja.contains(x, y)) {
            lojaHover = true;
        } else {
            for (int i = 0; i < Constantes.QTD_SOLOS; i++) {
                if (areaSolos[i] != null && areaSolos[i].contains(x, y)) {
                    idSoloHover = i;
                    break;
                }
            }
            if (idSoloHover == -1) {
                for (int i = 0; i < Constantes.QTD_CERCADOS; i++) {
                    if (areaCercados[i] != null && areaCercados[i].contains(x, y)) {
                        idCercadoHover = i;
                        break;
                    }
                }
            }
        }

        // Redesenha apenas se mudou alguma coisa (otimização)
        if (oldSolo != idSoloHover || oldCercado != idCercadoHover || oldLoja != lojaHover) {
            repaint();
        }
    }

    private void processarClique(int x, int y) {
        if (areaLoja.contains(x, y)) {
            janelaMae.selecionarLoja(); 
            return;
        }

        for (int i = 0; i < Constantes.QTD_SOLOS; i++) {
            if (areaSolos[i] != null && areaSolos[i].contains(x, y)) {
                janelaMae.selecionarSolo(i);
                return;
            }
        }

        for (int i = 0; i < Constantes.QTD_CERCADOS; i++) {
            if (areaCercados[i] != null && areaCercados[i].contains(x, y)) {
                janelaMae.selecionarCercado(i); 
                return;
            }
        }
        
        janelaMae.selecionarGeral();
    }
    
    // --- SETTERS DE ESTADO (Chamados pela JanelaPrincipal) ---
    
    public void setSoloSelecionado(int id) {
        this.idSoloSelecionado = id;
        this.idCercadoSelecionado = -1;
        this.lojaSelecionada = false;
        repaint();
    }
    
    public void setCercadoSelecionado(int id) {
        this.idCercadoSelecionado = id;
        this.idSoloSelecionado = -1;
        this.lojaSelecionada = false;
        repaint();
    }
    
    public void setLojaSelecionada() {
        this.lojaSelecionada = true;
        this.idSoloSelecionado = -1;
        this.idCercadoSelecionado = -1;
        repaint();
    }
    
    public void limparSelecao() {
        this.idSoloSelecionado = -1;
        this.idCercadoSelecionado = -1;
        this.lojaSelecionada = false;
        repaint();
    }
}
