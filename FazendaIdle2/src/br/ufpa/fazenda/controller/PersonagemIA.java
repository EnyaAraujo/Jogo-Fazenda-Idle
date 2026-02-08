package br.ufpa.fazenda.controller;

import br.ufpa.fazenda.model.*;
import br.ufpa.fazenda.util.Constantes;
import java.util.*;

/**
 * Sistema de IA que simula um personagem autônomo realizando ações na fazenda.
 * Trabalha em paralelo com as máquinas (trator, arador) e pode ser ativado/desativado independentemente.
 * 
 * Funcionamento:
 * 1. Mantém uma fila de tarefas prioritárias baseadas em lucro
 * 2. A cada ciclo, processa a tarefa mais lucrativa disponível
 * 3. Pode trabalhar simultaneamente com máquinas autônomas
 * 
 * REGRA: A IA NÃO COMPRA NADA (animais, máquinas, fertilizante, etc). Apenas usa o que já está disponível.
 */
public class PersonagemIA {
    
    // ----- ENUMS INTERNOS -----
    
    public enum Tarefa {
        COLHER_SOLO,
        PLANTAR_SOLO,
        COLETAR_ANIMAIS,
        APLICAR_FERTILIZANTE,
        NADA                   // Estado de espera
    }
    
    public static class Decisao {
        public final Tarefa tipo;
        public final int alvoId;           // ID do solo ou cercado
        public final Vegetal vegetal;      // Para plantio
        public final double valorEsperado; // Lucro/benefício estimado
        public final String descricao;     // Para debug/log
        
        public Decisao(Tarefa tipo, int alvoId, double valorEsperado, String descricao) {
            this(tipo, alvoId, null, valorEsperado, descricao);
        }
        
        public Decisao(Tarefa tipo, int alvoId, Vegetal vegetal, double valorEsperado, String descricao) {
            this.tipo = tipo;
            this.alvoId = alvoId;
            this.vegetal = vegetal;
            this.valorEsperado = valorEsperado;
            this.descricao = descricao;
        }
    }
    
    // ----- ATRIBUTOS -----
    
    private boolean ativo = false;
    private FazendaEstado fazenda;
    private PriorityQueue<Decisao> filaTarefas;
    private double tempoUltimaAcao = 0;
    private static final double INTERVALO_ENTRE_ACOES = 0.5; // 0.5 segundos entre ações
    
    // Configurações da IA
    private boolean priorizarLucroImediato = true;
    
    // ----- CONSTRUTOR -----
    
    public PersonagemIA(FazendaEstado fazenda) {
        this.fazenda = fazenda;
        // Comparador baseado no valor esperado (maior primeiro)
        this.filaTarefas = new PriorityQueue<>(
            (d1, d2) -> Double.compare(d2.valorEsperado, d1.valorEsperado)
        );
    }
    
    // ----- MÉTODOS PÚBLICOS -----
    
    public void ativar() {
        this.ativo = true;
        System.out.println("IA do Personagem ATIVADA - Trabalhando em paralelo com máquinas");
    }
    
    public void desativar() {
        this.ativo = false;
        this.filaTarefas.clear();
        System.out.println("IA do Personagem DESATIVADA");
    }
    
    public boolean isAtivo() {
        return ativo;
    }
    
    /**
     * Método chamado pelo GameLoop a cada ciclo
     * @param deltaTempo Tempo passado desde a última chamada
     * @return true se uma ação foi executada
     */
    public boolean atualizar(double deltaTempo) {
        if (!ativo) return false;
        
        tempoUltimaAcao += deltaTempo;
        
        // Só age após o intervalo mínimo (simula tempo de movimento/execução)
        if (tempoUltimaAcao < INTERVALO_ENTRE_ACOES) {
            return false;
        }
        
        // 1. Recalcular tarefas disponíveis
        recalcularFilaTarefas();
        
        // 2. Executar próxima tarefa (se houver)
        if (!filaTarefas.isEmpty()) {
            Decisao decisao = filaTarefas.poll();
            boolean sucesso = executarDecisao(decisao);
            
            if (sucesso) {
                tempoUltimaAcao = 0;
                System.out.println("IA executou: " + decisao.descricao + 
                                 " (Lucro estimado: R$" + String.format("%.2f", decisao.valorEsperado) + ")");
                return true;
            }
        }
        
        return false;
    }
    
    // ----- LÓGICA DE DECISÃO -----
    
    private void recalcularFilaTarefas() {
        filaTarefas.clear();
        
        // Apenas processa tarefas que não estão sendo feitas por máquinas
        // OU que são complementares às máquinas
        
        // 1. COLHEITA MANUAL (em solos desbloqueados, sem trator)
        avaliarColheitas();
        
        // 2. PLANTIO MANUAL (em solos desbloqueados, sem arador)
        avaliarPlantios();
        
        // 3. COLETA DE ANIMAIS (sempre manual)
        avaliarColetaAnimais();
        
        // 4. APLICAÇÃO DE FERTILIZANTE (se configurado, com estoque e solo desbloqueado)
        avaliarAplicacaoFertilizante();
    }
    
    private void avaliarColheitas() {
        List<Solo> solos = fazenda.getSolos();
        
        for (int i = 0; i < solos.size(); i++) {
            Solo solo = solos.get(i);
            
            // Só colhe se: solo desbloqueado, estiver pronto E NÃO tiver trator
            if (solo.isDesbloqueado() && solo.isPronto() && !solo.temMaquina(Maquina.TRATOR)) {
                double valorEsperado = solo.calcularValorVenda();
                filaTarefas.add(new Decisao(
                    Tarefa.COLHER_SOLO,
                    i,
                    valorEsperado,
                    "Colher " + solo.getVegetal().getNome() + " no Solo " + (i+1)
                ));
            }
        }
    }
    
    private void avaliarPlantios() {
        List<Solo> solos = fazenda.getSolos();
        Vegetal ultimoVegetal = fazenda.getUltimoVegetalPlantado();
        
        for (int i = 0; i < solos.size(); i++) {
            Solo solo = solos.get(i);
            
            // Só planta se: solo desbloqueado, estiver vazio E NÃO tiver arador
            if (solo.isDesbloqueado() && !solo.isOcupado() && !solo.temMaquina(Maquina.ARADOR)) {
                // Escolhe o vegetal mais lucrativo que o solo suporta
                Vegetal melhorVegetal = escolherMelhorVegetalPara(solo);
                
                if (melhorVegetal != null) {
                    // Estima lucro baseado no valor de venda e tempo
                    double valorEsperado = calcularLucroEsperado(melhorVegetal, solo);
                    
                    filaTarefas.add(new Decisao(
                        Tarefa.PLANTAR_SOLO,
                        i,
                        melhorVegetal,
                        valorEsperado,
                        "Plantar " + melhorVegetal.getNome() + " no Solo " + (i+1)
                    ));
                }
            }
        }
    }
    
    private void avaliarColetaAnimais() {
        List<Cercado> cercados = fazenda.getCercados();
        
        for (int i = 0; i < cercados.size(); i++) {
            Cercado cercado = cercados.get(i);
            
            if (cercado.isProdutoPronto()) {
                double valorEsperado = cercado.getEspecie().getProdutoValor() * cercado.getQuantidade();
                
                filaTarefas.add(new Decisao(
                    Tarefa.COLETAR_ANIMAIS,
                    i,
                    valorEsperado,
                    "Coletar " + cercado.getEspecie().getProduto() + 
                    " do Cercado " + (i+1) + " (" + cercado.getQuantidade() + " animais)"
                ));
            }
        }
    }
    
    private void avaliarAplicacaoFertilizante() {
        // Só aplica se tiver estoque
        if (fazenda.getEstoqueFertilizante() <= 0) {
            return;
        }
        
        List<Solo> solos = fazenda.getSolos();
        
        for (int i = 0; i < solos.size(); i++) {
            Solo solo = solos.get(i);
            
            // Só em solos desbloqueados
            if (!solo.isDesbloqueado()) continue;
            
            // Condições para aplicar fertilizante:
            // 1. Solo ocupado
            // 2. Fertilizante ativado para este solo
            // 3. Ainda não tem fertilizante aplicado
            // 4. Planta vale a pena (ex: Abóbora ou Cenoura)
            if (solo.isOcupado() && 
                solo.isFertilizanteAtivado() && 
                !solo.isEstaComFertilizanteAplicado()) {
                
                Vegetal vegetal = solo.getVegetal();
                double custoFertilizante = Constantes.CUSTO_FERTILIZANTE_LOTE / Constantes.QTD_FERTILIZANTE_LOTE; // R$15
                double beneficio = vegetal.getValorVenda() * Constantes.BONUS_FERTILIZANTE_VALOR; // +50%
                
                // Só aplica se o benefício for maior que o custo
                if (beneficio > custoFertilizante) {
                    double valorEsperado = beneficio - custoFertilizante;
                    
                    filaTarefas.add(new Decisao(
                        Tarefa.APLICAR_FERTILIZANTE,
                        i,
                        valorEsperado,
                        "Aplicar fertilizante em " + vegetal.getNome() + 
                        " no Solo " + (i+1) + " (+" + (int)(Constantes.BONUS_FERTILIZANTE_VALOR*100) + "%)"
                    ));
                }
            }
        }
    }
    
    // ----- CÁLCULOS AUXILIARES -----
    
    private Vegetal escolherMelhorVegetalPara(Solo solo) {
        Vegetal melhor = null;
        double melhorLucro = 0;
        
        for (Vegetal vegetal : Vegetal.values()) {
            // Verifica se o solo tem nível suficiente
            if (vegetal.getNivelMinimo() <= solo.getNivel()) {
                double lucro = calcularLucroEsperado(vegetal, solo);
                
                if (lucro > melhorLucro) {
                    melhorLucro = lucro;
                    melhor = vegetal;
                }
            }
        }
        
        return melhor;
    }
    
    private double calcularLucroEsperado(Vegetal vegetal, Solo solo) {
        double valorBase = vegetal.getValorVenda();
        
        // Aplica bônus do solo
        double bonusSolo = 1.0 + (solo.getNivel() - 1) * Constantes.BONUS_SOLO_NV_VALOR;
        
        // Considera bônus de irrigador (se instalado)
        if (solo.temMaquina(Maquina.IRRIGADOR)) {
            bonusSolo += Constantes.BONUS_IRRIGADOR_VALOR;
        }
        
        // Considera fertilizante (se ativado e com estoque)
        if (solo.isFertilizanteAtivado() && fazenda.getEstoqueFertilizante() > 0) {
            bonusSolo += Constantes.BONUS_FERTILIZANTE_VALOR;
        }
        
        double valorFinal = valorBase * bonusSolo;
        
        // Penaliza por tempo (lucro por segundo)
        double tempoCrescimento = vegetal.getTempoEmSegundos();
        
        // Aplica reduções de tempo
        double reducaoTempo = 0;
        if (solo.temMaquina(Maquina.IRRIGADOR)) reducaoTempo += Constantes.BONUS_IRRIGADOR_TEMPO;
        if (solo.isFertilizanteAtivado() && fazenda.getEstoqueFertilizante() > 0) {
            reducaoTempo += Constantes.BONUS_FERTILIZANTE_TEMPO;
        }
        reducaoTempo += (solo.getNivel() - 1) * Constantes.BONUS_SOLO_NV_CRESCIMENTO;
        
        tempoCrescimento *= (1.0 - Math.min(reducaoTempo, 0.9));
        
        // Lucro por segundo (quanto maior, melhor)
        return valorFinal / tempoCrescimento;
    }
    
    // ----- EXECUÇÃO DE DECISÕES -----
    
    private boolean executarDecisao(Decisao decisao) {
        try {
            switch (decisao.tipo) {
                case COLHER_SOLO:
                    return executarColheita(decisao.alvoId);
                    
                case PLANTAR_SOLO:
                    return executarPlantio(decisao.alvoId, decisao.vegetal);
                    
                case COLETAR_ANIMAIS:
                    return executarColetaAnimais(decisao.alvoId);
                    
                case APLICAR_FERTILIZANTE:
                    return executarAplicacaoFertilizante(decisao.alvoId);
                    
                case NADA:
                default:
                    return false;
            }
        } catch (Exception e) {
            System.err.println("Erro na IA ao executar decisão: " + e.getMessage());
            return false;
        }
    }
    
    private boolean executarColheita(int soloId) {
        if (soloId < 0 || soloId >= fazenda.getSolos().size()) return false;
        
        Solo solo = fazenda.getSolos().get(soloId);
        if (!solo.isDesbloqueado() || !solo.isPronto()) return false;
        
        double valor = solo.colher();
        fazenda.ganharDinheiro(valor);
        
        // Atualiza último vegetal plantado (para arador)
        if (solo.getVegetal() != null) {
            fazenda.setUltimoVegetalPlantado(solo.getVegetal());
        }
        
        return true;
    }
    
    private boolean executarPlantio(int soloId, Vegetal vegetal) {
        if (soloId < 0 || soloId >= fazenda.getSolos().size()) return false;
        if (vegetal == null) return false;
        
        Solo solo = fazenda.getSolos().get(soloId);
        
        // Verifica se o solo está bloqueado para replantio automático
        if (solo.isBloqueadoReplantioAutomatico()) {
            return false; // Não planta automaticamente em solo bloqueado
        }
        
        if (!solo.isDesbloqueado() || solo.isOcupado()) return false;
        
        // Chama o método com flag de automático
        boolean plantou = solo.plantar(vegetal, true);
        if (plantou) {
            fazenda.setUltimoVegetalPlantado(vegetal);
            
            if (solo.isFertilizanteAtivado()) {
                solo.aplicarFertilizante();
            }
        }
        
        return plantou;
    }
    
    private boolean executarColetaAnimais(int cercadoId) {
        if (cercadoId < 0 || cercadoId >= fazenda.getCercados().size()) return false;
        
        Cercado cercado = fazenda.getCercados().get(cercadoId);
        if (!cercado.isProdutoPronto()) return false;
        
        double valor = cercado.coletarProdutos();
        fazenda.ganharDinheiro(valor);
        return valor > 0;
    }
    
    private boolean executarAplicacaoFertilizante(int soloId) {
        if (soloId < 0 || soloId >= fazenda.getSolos().size()) return false;
        
        Solo solo = fazenda.getSolos().get(soloId);
        if (!solo.isDesbloqueado()) return false;
        
        return solo.aplicarFertilizante();
    }
    
    // ----- GETTERS E SETTERS PARA CONFIGURAÇÃO -----
    
    public void setPriorizarLucroImediato(boolean priorizar) {
        this.priorizarLucroImediato = priorizar;
    }
    
    public int getTarefasPendentes() {
        return filaTarefas.size();
    }
    
    public String getStatus() {
        if (!ativo) return "INATIVO";
        
        if (filaTarefas.isEmpty()) {
            return "ATIVO - Aguardando tarefas...";
        } else {
            Decisao proxima = filaTarefas.peek();
            return String.format("ATIVO - Próxima: %s (Lucro: R$%.2f)", 
                proxima.descricao, proxima.valorEsperado);
        }
    }
}