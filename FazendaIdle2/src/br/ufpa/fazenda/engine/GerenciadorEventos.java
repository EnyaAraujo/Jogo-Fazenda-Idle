package br.ufpa.fazenda.engine;

import br.ufpa.fazenda.model.Solo;

/**
 * Interface que a Interface Gráfica (GUI) deve implementar.
 * Serve para o motor do jogo avisar a tela que algo mudou.
 */
public interface GerenciadorEventos {
    
    // Avisa que o dinheiro ou dia mudou (para atualizar o HUD)
    void aoAtualizarStatusFazenda(double dinheiro, int dia, int estoqueFertilizante);
    
    // Avisa que um solo específico mudou (cresceu, foi plantado, colhido)
    // A Enya usará isso para trocar o sprite daquele quadrado específico
    void aoAtualizarSolo(Solo solo);
    
    // (Opcional) Para tocar sons ou mostrar mensagens
    void aoNotificarEvento(String mensagem);

    // --- NOVO EVENTO ---
    void aoVencerJogo(); 
}