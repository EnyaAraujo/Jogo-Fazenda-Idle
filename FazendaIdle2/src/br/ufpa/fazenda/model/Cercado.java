package br.ufpa.fazenda.model;

import br.ufpa.fazenda.util.Constantes;

public class Cercado {
    
    private int id;
    private Animal especie;
    private int quantidadeAnimais;
    
    // Controle de Produção
    private double progressoProducao; 
    private boolean produtoPronto;
    
    public Cercado(int id) {
        this.id = id;
        this.quantidadeAnimais = 0;
        this.progressoProducao = 0.0;
        this.produtoPronto = false;
    }
    
    public boolean adicionarAnimal(Animal animal) {
        if (quantidadeAnimais == 0) {
            this.especie = animal;
        }
        
        if (this.especie != animal) return false; 
        if (quantidadeAnimais >= Constantes.CAPACIDADE_POR_CERCADO) return false; 
        
        this.quantidadeAnimais++;
        return true;
    }
    
    public void atualizarTempo(double deltaTempoSegundos) {
        if (quantidadeAnimais == 0 || produtoPronto) return;
        
        double tempoNecessario = especie.getTempoProducaoSegundos();
        this.progressoProducao += (deltaTempoSegundos / tempoNecessario);
        
        if (this.progressoProducao >= 1.0) {
            this.progressoProducao = 1.0;
            this.produtoPronto = true;
        }
    }
    
    public double coletarProdutos() {
        if (!produtoPronto || quantidadeAnimais == 0) return 0.0;
        
        double valorTotal = especie.getProdutoValor() * quantidadeAnimais;
        
        this.produtoPronto = false;
        this.progressoProducao = 0.0;
        
        return valorTotal;
    }
    
    public double calcularCustoManutencao() {
        if (quantidadeAnimais == 0) return 0.0;
        return especie.getCustoManutencaoDiaria() * quantidadeAnimais;
    }
    
    /**
     * Gera string HTML com receita e custo para a GUI.
     */
    public String getDetalhesLucroHTML() {
        if (quantidadeAnimais == 0) return "<html><center>Vazio</center></html>";
        
        double receita = especie.getProdutoValor() * quantidadeAnimais;
        double custoDia = especie.getCustoManutencaoDiaria() * quantidadeAnimais;
        
        StringBuilder sb = new StringBuilder("<html><body style='text-align: center; font-family: Arial; font-size: 10px;'>");
        sb.append(String.format("<font color='green'>Receita: R$ %.2f / ciclo</font><br>", receita));
        sb.append(String.format("<font color='red'>Custo: -R$ %.2f / dia</font>", custoDia));
        sb.append("</body></html>");
        
        return sb.toString();
    }

    public int getId() { return id; }
    public int getQuantidade() { return quantidadeAnimais; }
    public Animal getEspecie() { return especie; }
    public boolean isProdutoPronto() { return produtoPronto; }
    public double getProgresso() { return progressoProducao; }
    public boolean isVazio() { return quantidadeAnimais == 0; }
    public int getCapacidadeMaxima() { return Constantes.CAPACIDADE_POR_CERCADO; }
}