package br.ufpa.fazenda.model;

import br.ufpa.fazenda.util.Constantes;
import java.util.HashSet;
import java.util.Set;

public class Solo {
    
    private int id;
    private int nivel;
    private boolean desbloqueado;
    private Vegetal vegetalPlantado;
    private double progressoCrescimento; 
    
    private Set<Maquina> maquinasInstaladas;
    private boolean fertilizanteAtivado;
    private boolean estaComFertilizanteAplicado;
    
    private boolean bloqueadoReplantioAutomatico = false;
    
    private final Object lock = new Object();
    
    public Solo(int id, boolean desbloqueadoInicialmente) {
        this.id = id;
        this.nivel = 1;
        this.desbloqueado = desbloqueadoInicialmente;
        this.maquinasInstaladas = new HashSet<>();
        this.fertilizanteAtivado = false;
        limparSolo();
    }
    
    public boolean plantar(Vegetal vegetal, boolean automatico) {
        synchronized (lock) {
            if (automatico && bloqueadoReplantioAutomatico) return false;
            
            if (!automatico) this.bloqueadoReplantioAutomatico = false;
            
            if (vegetalPlantado != null || !desbloqueado) return false;
            
            this.vegetalPlantado = vegetal;
            this.progressoCrescimento = 0.0;
            
            if (fertilizanteAtivado) {
                 aplicarFertilizante();
            } else {
                this.estaComFertilizanteAplicado = false;
            }
            
            return true;
        }
    }
    
    // Método original mantido para compatibilidade
    public boolean plantar(Vegetal vegetal) {
        return plantar(vegetal, false);
    }
    
    public boolean aplicarFertilizante() {
        synchronized (lock) {
            if (estaComFertilizanteAplicado) return false;
            
            if (FazendaEstado.getInstance().consumirFertilizanteDoEstoque()) {
                this.estaComFertilizanteAplicado = true;
                return true;
            }
            return false;
        }
    }
    
    public void atualizarTempo(double deltaTempoSegundos) {
        synchronized (lock) {
            if (vegetalPlantado == null) return;
            if (progressoCrescimento >= 1.0) return;
            
            double tempoTotalNecessario = vegetalPlantado.getTempoEmSegundos();
            double fatorReducao = 0.0;
            
            for (Maquina m : maquinasInstaladas) {
                fatorReducao += m.aplicarReducaoTempo(tempoTotalNecessario);
            }
            if (estaComFertilizanteAplicado) {
                fatorReducao += Constantes.BONUS_FERTILIZANTE_TEMPO;
            }
            fatorReducao += (nivel - 1) * Constantes.BONUS_SOLO_NV_CRESCIMENTO;
            
            if (fatorReducao > 0.9) fatorReducao = 0.9;
            
            double tempoFinal = tempoTotalNecessario * (1.0 - fatorReducao);
            
            this.progressoCrescimento += (deltaTempoSegundos / tempoFinal);
            
            if (this.progressoCrescimento > 1.0) this.progressoCrescimento = 1.0;
        }
    }
    
    public double calcularValorVenda() {
        synchronized (lock) {
            if (vegetalPlantado == null) return 0.0;
            
            double valorBase = vegetalPlantado.getValorVenda();
            double valorFinal = valorBase;
            
            valorFinal += valorBase * ((nivel - 1) * Constantes.BONUS_SOLO_NV_VALOR);
            
            for (Maquina m : maquinasInstaladas) {
                valorFinal += m.aplicarBonusValor(valorBase);
            }
            
            if (estaComFertilizanteAplicado) {
                valorFinal += valorBase * Constantes.BONUS_FERTILIZANTE_VALOR;
            }
            
            return valorFinal;
        }
    }
    
    /**
     * Gera uma string HTML com o detalhamento dos lucros para a interface gráfica.
     */
    public String getEstimativaLucroHTML() {
        synchronized (lock) {
            if (vegetalPlantado == null) return "<html><center>Sem Plantação</center></html>";

            double base = vegetalPlantado.getValorVenda();
            double bonusNivel = base * ((nivel - 1) * Constantes.BONUS_SOLO_NV_VALOR);
            
            double bonusMaquinas = 0;
            for (Maquina m : maquinasInstaladas) {
                bonusMaquinas += m.aplicarBonusValor(base);
            }
            
            double bonusFert = 0;
            // Calculamos o bônus potencial se o fertilizante estiver ATIVADO (mesmo se não aplicado ainda na visualização)
            if (fertilizanteAtivado || estaComFertilizanteAplicado) {
                bonusFert = base * Constantes.BONUS_FERTILIZANTE_VALOR;
            }

            double total = base + bonusNivel + bonusMaquinas + bonusFert;

            // Formatação HTML para o JLabel
            StringBuilder sb = new StringBuilder("<html><body style='text-align: center; font-family: Arial; font-size: 10px;'>");
            sb.append(String.format("Base: R$ %.2f<br>", base));
            
            if (bonusNivel > 0) sb.append(String.format("<font color='blue'>+ Nível %d: R$ %.2f</font><br>", nivel, bonusNivel));
            if (bonusMaquinas > 0) sb.append(String.format("<font color='green'>+ Máquinas: R$ %.2f</font><br>", bonusMaquinas));
            if (bonusFert > 0) sb.append(String.format("<font color='purple'>+ Fertilizante: R$ %.2f</font><br>", bonusFert));
            
            sb.append("<hr>");
            sb.append(String.format("<b>Total: R$ %.2f</b>", total));
            sb.append("</body></html>");
            
            return sb.toString();
        }
    }
    
    public double colher() {
        synchronized (lock) {
            if (progressoCrescimento < 1.0 || vegetalPlantado == null) return 0.0;
            
            double valorVenda = calcularValorVenda();
            limparSolo();
            return valorVenda;
        }
    }
    
    public void arrancar() {
        synchronized (lock) {
            limparSolo();
            this.bloqueadoReplantioAutomatico = true; 
        }
    }
    
    private void limparSolo() {
        this.vegetalPlantado = null;
        this.progressoCrescimento = 0.0;
        this.estaComFertilizanteAplicado = false;
    }

    public void instalarMaquina(Maquina maquina) {
        synchronized (lock) {
            if (!desbloqueado) return;
            maquinasInstaladas.add(maquina);
        }
    }
    
    public void removerMaquina(Maquina maquina) {
        synchronized (lock) {
            maquinasInstaladas.remove(maquina);
        }
    }
    
    public boolean temMaquina(Maquina maquina) {
        synchronized (lock) {
            return maquinasInstaladas.contains(maquina);
        }
    }
    
    public boolean upgrade() {
        synchronized (lock) {
            if (nivel >= 10 || !desbloqueado) return false;
            nivel++;
            return true;
        }
    }
    
    public boolean desbloquear() {
        synchronized (lock) {
            if (desbloqueado) return true;
            this.desbloqueado = true;
            return true;
        }
    }

    public int getId() { synchronized (lock) { return id; } }
    public int getNivel() { synchronized (lock) { return nivel; } }
    public boolean isDesbloqueado() { synchronized (lock) { return desbloqueado; } }
    public boolean isOcupado() { synchronized (lock) { return vegetalPlantado != null; } }
    public boolean isPronto() { synchronized (lock) { return progressoCrescimento >= 1.0; } }
    public double getProgresso() { synchronized (lock) { return progressoCrescimento; } }
    public Vegetal getVegetal() { synchronized (lock) { return vegetalPlantado; } }
    public Set<Maquina> getMaquinasInstaladas() { 
        synchronized (lock) { return new HashSet<>(maquinasInstaladas); } 
    }
    public void setFertilizanteAtivado(boolean ativo) { synchronized (lock) { this.fertilizanteAtivado = ativo; } }
    public boolean isFertilizanteAtivado() { synchronized (lock) { return fertilizanteAtivado; } }
    public boolean isEstaComFertilizanteAplicado() { synchronized (lock) { return estaComFertilizanteAplicado; } }
    public boolean isBloqueadoReplantioAutomatico() { synchronized (lock) { return bloqueadoReplantioAutomatico; } }
    public void desbloquearReplantioAutomatico() { synchronized (lock) { this.bloqueadoReplantioAutomatico = false; } }
}