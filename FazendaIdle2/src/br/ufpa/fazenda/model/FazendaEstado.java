package br.ufpa.fazenda.model;

import br.ufpa.fazenda.util.Constantes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class FazendaEstado {
    
    private static FazendaEstado instance;
    
    // Dados Globais
    private double dinheiro;
    private int diaAtual;
    private int estoqueFertilizante;
    private boolean autoCompraFertilizante = false;
    private boolean modoIAAtivado = false;
    private boolean vitoriaAlcancada = false; // <--- NOVO
    
    private List<Solo> solos;
    private List<Cercado> cercados;
    private Map<Maquina, Integer> inventarioMaquinas;
    private Vegetal ultimoVegetalPlantado;
    
    private final ReentrantLock lock = new ReentrantLock();
    
    private FazendaEstado() {
        // USO DA CONSTANTE DE DINHEIRO INICIAL
        this.dinheiro = Constantes.DINHEIRO_INICIAL;
        this.diaAtual = 1;
        this.estoqueFertilizante = 5;
        this.solos = new ArrayList<>();
        this.cercados = new ArrayList<>();
        this.inventarioMaquinas = new HashMap<>();
        this.ultimoVegetalPlantado = Vegetal.ALFACE; 
        
        for (int i = 0; i < Constantes.QTD_SOLOS; i++) {
            solos.add(new Solo(i, i < 3));
        }
        
        for (int i = 0; i < Constantes.QTD_CERCADOS; i++) {
            cercados.add(new Cercado(i));
        }
        
        for (Maquina maquina : Maquina.values()) {
            inventarioMaquinas.put(maquina, 0);
        }
    }
    
    public static FazendaEstado getInstance() {
        if (instance == null) {
            synchronized (FazendaEstado.class) {
                if (instance == null) {
                    instance = new FazendaEstado();
                }
            }
        }
        return instance;
    }

    // --- Lógica de Fertilizante Segura ---
    public boolean consumirFertilizanteDoEstoque() {
        lock.lock();
        try {
            if (estoqueFertilizante <= 0 && autoCompraFertilizante) {
                comprarFertilizante();
            }
            if (estoqueFertilizante > 0) {
                estoqueFertilizante--;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
    
    public boolean comprarFertilizante() {
        lock.lock();
        try {
            if (gastarDinheiro(Constantes.CUSTO_FERTILIZANTE_LOTE)) {
                estoqueFertilizante += Constantes.QTD_FERTILIZANTE_LOTE;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public int getTotalMaquinas(Maquina m) {
        lock.lock();
        try {
            int noInventario = inventarioMaquinas.getOrDefault(m, 0);
            int instaladas = 0;
            for (Solo s : solos) {
                if (s.temMaquina(m)) instaladas++;
            }
            return noInventario + instaladas;
        } finally {
            lock.unlock();
        }
    }

    public int comprarMaquinaComVerificacao(Maquina maquina) {
        lock.lock();
        try {
            if (getTotalMaquinas(maquina) >= Constantes.QTD_SOLOS) {
                return 2; 
            }
            if (gastarDinheiro(maquina.getCusto())) {
                inventarioMaquinas.put(maquina, inventarioMaquinas.get(maquina) + 1);
                return 0;
            }
            return 1;
        } finally {
            lock.unlock();
        }
    }

    public int comprarAnimalComVerificacao(Animal especie) {
        lock.lock();
        try {
            for (Cercado c : cercados) {
                boolean mesmaEspecie = !c.isVazio() && c.getEspecie() == especie;
                boolean cercadoVazio = c.isVazio();
                boolean temEspaco = c.getQuantidade() < Constantes.CAPACIDADE_POR_CERCADO;
                
                if ((mesmaEspecie || cercadoVazio)) {
                    if (!temEspaco) continue; 
                    
                    if (gastarDinheiro(especie.getPrecoCompra())) {
                        boolean jaTinha = !c.isVazio(); 
                        c.adicionarAnimal(especie);
                        return jaTinha ? 3 : 0; 
                    } else {
                        return 1; 
                    }
                }
            }
            return 2; 
        } finally {
            lock.unlock();
        }
    }

    public boolean evoluirSolo(int soloId) {
        lock.lock();
        try {
            if (soloId < 0 || soloId >= solos.size()) return false;
            
            Solo s = solos.get(soloId);
            // USO DA CONSTANTE DE CUSTO EVOLUÇÃO
            double custo = Constantes.CUSTO_EVOLUCAO_BASE * s.getNivel();
            
            if (gastarDinheiro(custo)) {
                boolean sucesso = s.upgrade();
                return sucesso;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public boolean gastarDinheiro(double valor) {
        lock.lock();
        try {
            if (dinheiro >= valor) {
                dinheiro -= valor;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
    
    public void ganharDinheiro(double valor) { 
        lock.lock();
        try {
            this.dinheiro += valor;
        } finally {
            lock.unlock();
        }
    }
    
    public void avancarDia() { 
        lock.lock();
        try {
            this.diaAtual++; 
        } finally {
            lock.unlock();
        }
    }
    
    public boolean desbloquearSolo(int id) { 
        lock.lock();
        try {
            // Verifica dinheiro aqui antes de chamar o solo (Segurança extra)
            if (dinheiro >= Constantes.CUSTO_DESBLOQUEIO_SOLO) {
                if (solos.get(id).desbloquear()) {
                    gastarDinheiro(Constantes.CUSTO_DESBLOQUEIO_SOLO);
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
    
    public void setAutoCompraFertilizante(boolean v) { 
        lock.lock();
        try {
            this.autoCompraFertilizante = v; 
        } finally {
            lock.unlock();
        }
    }
    
    public boolean isAutoCompraFertilizante() { 
        lock.lock();
        try {
            return autoCompraFertilizante; 
        } finally {
            lock.unlock();
        }
    }
    
    public boolean plantarManual(int soloId, Vegetal vegetal) {
        lock.lock();
        try {
            Solo s = solos.get(soloId);
            if(s.plantar(vegetal, false)) {  
                setUltimoVegetalPlantado(vegetal);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
    
    public boolean isModoIAAtivado() { 
        lock.lock();
        try {
            return modoIAAtivado; 
        } finally {
            lock.unlock();
        }
    }
    
    public void setModoIAAtivado(boolean modoIAAtivado) { 
        lock.lock();
        try {
            this.modoIAAtivado = modoIAAtivado; 
        } finally {
            lock.unlock();
        }
    }

    public boolean instalarMaquina(int soloId, Maquina maquina) {
        lock.lock();
        try {
            int qtd = inventarioMaquinas.get(maquina);
            if (qtd > 0) {
                Solo solo = solos.get(soloId);
                if (!solo.temMaquina(maquina)) {
                    solo.instalarMaquina(maquina);
                    inventarioMaquinas.put(maquina, qtd - 1);
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
    
    public void desinstalarMaquina(int soloId, Maquina maquina) {
        lock.lock();
        try {
            Solo solo = solos.get(soloId);
            if (solo.temMaquina(maquina)) {
                solo.removerMaquina(maquina);
                inventarioMaquinas.put(maquina, inventarioMaquinas.get(maquina) + 1);
            }
        } finally {
            lock.unlock();
        }
    }

    // --- Getter e Setter para vitória ---
    public boolean isVitoriaAlcancada() {
        lock.lock();
        try {
            return vitoriaAlcancada;
        } finally {
            lock.unlock();
        }
    }

    public void setVitoriaAlcancada(boolean v) {
        lock.lock();
        try {
            this.vitoriaAlcancada = v;
        } finally {
            lock.unlock();
        }
    }
    // --- Fim dos métodos de vitória ---

    public double getDinheiro() { 
        lock.lock();
        try {
            return dinheiro; 
        } finally {
            lock.unlock();
        }
    }
    
    public int getDiaAtual() { 
        lock.lock();
        try {
            return diaAtual; 
        } finally {
            lock.unlock();
        }
    }
    
    public int getEstoqueFertilizante() { 
        lock.lock();
        try {
            return estoqueFertilizante; 
        } finally {
            lock.unlock();
        }
    }
    
    public List<Solo> getSolos() { 
        lock.lock();
        try {
            return new ArrayList<>(solos);
        } finally {
            lock.unlock();
        }
    }
    
    public Solo getSolo(int id) {
        lock.lock();
        try {
            if (id >= 0 && id < solos.size()) {
                return solos.get(id);
            }
            return null;
        } finally {
            lock.unlock();
        }
    }
    
    public List<Cercado> getCercados() { 
        lock.lock();
        try {
            return new ArrayList<>(cercados); 
        } finally {
            lock.unlock();
        }
    }
    
    public Map<Maquina, Integer> getInventarioMaquinas() { 
        lock.lock();
        try {
            return new HashMap<>(inventarioMaquinas); 
        } finally {
            lock.unlock();
        }
    }
    
    public Vegetal getUltimoVegetalPlantado() { 
        lock.lock();
        try {
            return ultimoVegetalPlantado; 
        } finally {
            lock.unlock();
        }
    }
    
    public void setUltimoVegetalPlantado(Vegetal v) { 
        lock.lock();
        try {
            this.ultimoVegetalPlantado = v; 
        } finally {
            lock.unlock();
        }
    }
}