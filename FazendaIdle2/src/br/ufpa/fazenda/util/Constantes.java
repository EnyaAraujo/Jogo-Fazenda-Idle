package br.ufpa.fazenda.util;

/**
 * Centraliza todas as configurações de balanceamento e constantes do sistema.
 */
public class Constantes {
    
    // --- ECONOMIA: GERAL ---
    public static final double DINHEIRO_INICIAL = 2000.0;
    public static final double DINHEIRO_VITORIA = 100000.0; // Exemplo para vitória
    
    // --- ECONOMIA: CUSTOS ---
    public static final double CUSTO_DESBLOQUEIO_SOLO = 300.0;
    public static final double CUSTO_EVOLUCAO_BASE = 100.0; // Multiplicado pelo nível
    
    // --- ECONOMIA: FERTILIZANTE ---
    public static final double CUSTO_FERTILIZANTE_LOTE = 150.0;
    public static final int QTD_FERTILIZANTE_LOTE = 10;
    
    // --- QUANTIDADES E LIMITES ---
    public static final int QTD_SOLOS = 12;
    public static final int QTD_CERCADOS = 3;
    public static final int CAPACIDADE_POR_CERCADO = 3;
    
    // --- TEMPO E GAME LOOP ---
    public static final int SEGUNDOS_POR_DIA = 15;
    public static final int TARGET_FPS = 60;
    public static final long OPTIMAL_TIME_MS = 1000 / TARGET_FPS; 
    
    // --- BUFFS (Percentuais em decimal: 0.15 = 15%) ---
    // Irrigador
    public static final double BONUS_IRRIGADOR_TEMPO = 0.15; 
    public static final double BONUS_IRRIGADOR_VALOR = 0.25; 
    
    // Fertilizante
    public static final double BONUS_FERTILIZANTE_TEMPO = 0.40;
    public static final double BONUS_FERTILIZANTE_VALOR = 0.50;
    
    // Solo (por nível)
    public static final double BONUS_SOLO_NV_VALOR = 0.20;
    public static final double BONUS_SOLO_NV_CRESCIMENTO = 0.10;
    
    // --- GUI / VISUAL ---
    public static final double PROPORCAO_PAINEL_LATERAL = 0.30; 
    public static final int TAMANHO_MINIMO_JANELA_X = 800;
    public static final int TAMANHO_MINIMO_JANELA_Y = 600;
}