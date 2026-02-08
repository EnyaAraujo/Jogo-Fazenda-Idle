package br.ufpa.fazenda.model;

import br.ufpa.fazenda.util.Constantes;

/**
 * Define os tipos de máquinas e encapsula a lógica de seus bônus.
 */
public enum Maquina {
    
    TRATOR("Trator Automático", 300.0, "Colhe e vende automaticamente"),
    ARADOR("Arador Automático", 250.0, "Planta automaticamente a última semente"),
    IRRIGADOR("Sistema de Irrigação", 400.0, "Aumenta valor e reduz tempo") {
        @Override
        public double aplicarBonusValor(double valorBase) {
            return valorBase * Constantes.BONUS_IRRIGADOR_VALOR;
        }

        @Override
        public double aplicarReducaoTempo(double tempoBase) {
            return Constantes.BONUS_IRRIGADOR_TEMPO; // Retorna % de redução
        }
    };

    private final String nome;
    private final double custo;
    private final String descricao;

    Maquina(String nome, double custo, String descricao) {
        this.nome = nome;
        this.custo = custo;
        this.descricao = descricao;
    }

    // --- MÉTODOS DE LÓGICA DE BUFFS (Strategy Pattern Simplificado) ---
    
    /**
     * Calcula o valor EXTRA adicionado por esta máquina.
     * Por padrão é 0, mas Irrigador sobrescreve.
     */
    public double aplicarBonusValor(double valorBase) {
        return 0.0;
    }

    /**
     * Retorna a porcentagem de redução de tempo (0.0 a 1.0).
     * Por padrão é 0.
     */
    public double aplicarReducaoTempo(double tempoTotal) {
        return 0.0;
    }

    // Getters
    public String getNome() { return nome; }
    public double getCusto() { return custo; }
    public String getDescricao() { return descricao; }
    
    public String getInfoCompleta() {
        return String.format("%s (R$ %.2f) - %s", nome, custo, descricao);
    }
}