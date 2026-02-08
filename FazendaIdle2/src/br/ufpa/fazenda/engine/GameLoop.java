package br.ufpa.fazenda.engine;

import br.ufpa.fazenda.controller.PersonagemIA;
import br.ufpa.fazenda.model.*;
import br.ufpa.fazenda.util.Constantes;

public class GameLoop extends Thread {
    
    private boolean rodando = true;
    private boolean pausado = false;
    
    private final FazendaEstado fazenda;
    private GerenciadorEventos ouvinte;
    
    // Controle de tempo para animais (em segundos)
    private double acumuladorTempoAnimal = 0.0;
    private static final double INTERVALO_ANIMAL = 0.5;

    // IA
    private PersonagemIA personagemIA;
    private boolean modoIAActivo = false;
    
    // Controle de tempo para dia (em segundos)
    private double acumuladorTempoDia = 0.0;
    
    public GameLoop(GerenciadorEventos ouvinte) {
        this.fazenda = FazendaEstado.getInstance();
        this.ouvinte = ouvinte;
        this.personagemIA = new PersonagemIA(fazenda);
    }
    
    // --- CONTROLE DO LOOP ---
    
    public void alternarPause() {
        this.pausado = !this.pausado;
        if (ouvinte != null) {
            ouvinte.aoNotificarEvento(pausado ? "Jogo PAUSADO" : "Jogo RESUMIDO");
        }
    }
    
    public boolean isPausado() { return pausado; }
    
    public void parar() {
        this.rodando = false;
        this.pausado = true;
        if (ouvinte != null) {
            ouvinte.aoNotificarEvento("Jogo finalizado");
        }
    }

    @Override
    public void run() {
        long ultimoTempo = System.currentTimeMillis();
        long tempoAcumulado = 0;
        
        while (rodando) {
            long tempoAtual = System.currentTimeMillis();
            long tempoDecorrido = tempoAtual - ultimoTempo;
            ultimoTempo = tempoAtual;
            
            // Acumula o tempo decorrido
            tempoAcumulado += tempoDecorrido;
            
            // Processa frames enquanto houver tempo acumulado
            while (tempoAcumulado >= Constantes.OPTIMAL_TIME_MS) {
                if (!pausado) {
                    // Converte milissegundos para segundos
                    atualizarJogo(Constantes.OPTIMAL_TIME_MS / 1000.0);
                }
                tempoAcumulado -= Constantes.OPTIMAL_TIME_MS;
            }
            
            // Dorme um pouco para liberar CPU
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void atualizarJogo(double deltaSegundos) {
        
        // --- NOVO: CHECAGEM DE VITÓRIA ---
        // Se ainda não venceu E atingiu a meta
        if (!fazenda.isVitoriaAlcancada() && fazenda.getDinheiro() >= Constantes.DINHEIRO_VITORIA) {
            fazenda.setVitoriaAlcancada(true); // Marca que venceu para não repetir
            if (ouvinte != null) {
                ouvinte.aoVencerJogo(); // Avisa a tela
            }
        }

        // 1. Atualizar Ciclo do Dia
        acumuladorTempoDia += deltaSegundos;
        if (acumuladorTempoDia >= Constantes.SEGUNDOS_POR_DIA) {
            acumuladorTempoDia = 0;
            fazenda.avancarDia();
            
            if (ouvinte != null) {
                // Notifica virada do dia
                ouvinte.aoNotificarEvento("Dia " + fazenda.getDiaAtual() + " começou!");
                ouvinte.aoAtualizarStatusFazenda(fazenda.getDinheiro(), fazenda.getDiaAtual(), fazenda.getEstoqueFertilizante());
            }
        }
        
        // 2. Atualizar Solos (Crescimento e Automação)
        for (Solo solo : fazenda.getSolos()) {
            if (!solo.isDesbloqueado()) continue;
            
            boolean mudou = false;
            
            // A. Crescimento
            if (solo.isOcupado() && !solo.isPronto()) {
                solo.atualizarTempo(deltaSegundos);
                mudou = true;
            }
            
            // B. Automação (Robôs)
            if (processarAutomacao(solo)) {
                mudou = true;
            }
            
            // Notificar tela se houve mudança visual (cresceu ou máquina agiu)
            if (mudou && ouvinte != null) {
                ouvinte.aoAtualizarSolo(solo);
            }
        }
        
        // 3. Animais
        acumuladorTempoAnimal += deltaSegundos;
        if (acumuladorTempoAnimal >= INTERVALO_ANIMAL) {
            acumuladorTempoAnimal = 0;
            for (Cercado cercado : fazenda.getCercados()) {
                cercado.atualizarTempo(INTERVALO_ANIMAL);
                
                // Notifica produção se estiver pronta (Opcional, pode gerar spam de eventos)
                // if (cercado.isProdutoPronto() && ouvinte != null) { ... }
            }
        }
        
        // 4. IA Automática (Botão "Jogar Sozinho")
        if (modoIAActivo) {
            personagemIA.atualizar(deltaSegundos);
        }
        
        // 5. Atualizar HUD Geral (garantia)
        if (ouvinte != null) {
            ouvinte.aoAtualizarStatusFazenda(
                fazenda.getDinheiro(), 
                fazenda.getDiaAtual(), 
                fazenda.getEstoqueFertilizante()
            );
        }
    }
    
    /**
     * Lógica dos Tratores e Aradores.
     */
    private boolean processarAutomacao(Solo solo) {
        boolean agiu = false;
        
        // TRATOR: Colhe se estiver pronto (não precisa alterar)
        if (solo.temMaquina(Maquina.TRATOR) && solo.isPronto()) {
            // CORREÇÃO: Pegar o vegetal ANTES de colher para poder mostrar o nome
            Vegetal v = solo.getVegetal(); 
            double valorColheita = solo.colher(); 
            
            fazenda.ganharDinheiro(valorColheita);
            agiu = true;
            
            if (ouvinte != null && v != null) {
                ouvinte.aoNotificarEvento("Trator vendeu " + v.getNome() + 
                                        " por R$" + String.format("%.2f", valorColheita));
            }
        }
        
        // ARADOR: Planta se estiver vazio E NÃO estiver bloqueado
        if (solo.temMaquina(Maquina.ARADOR) && !solo.isOcupado()) {
            // Verifica se o solo está bloqueado para replantio automático
            if (solo.isBloqueadoReplantioAutomatico()) {
                return agiu; // Não planta automaticamente em solo bloqueado
            }
            
            Vegetal paraPlantar = fazenda.getUltimoVegetalPlantado();
            
            if (paraPlantar != null) {
                // Usa o método com flag de automático
                if (solo.plantar(paraPlantar, true)) {
                    agiu = true;
                    if (ouvinte != null) {
                        ouvinte.aoNotificarEvento("Arador plantou " + paraPlantar.getNome());
                    }
                }
            }
        }
        
        return agiu;
    }
    
    // --- MÉTODOS DE CONTROLE DA IA ---
    
    public void ativarIA() {
        modoIAActivo = true;
        personagemIA.ativar();
        if (ouvinte != null) ouvinte.aoNotificarEvento("Modo IA ATIVADO");
    }
    
    public void desativarIA() {
        modoIAActivo = false;
        personagemIA.desativar();
        if (ouvinte != null) ouvinte.aoNotificarEvento("Modo IA DESATIVADO");
    }
}