package br.ufpa.fazenda.view;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Singleton responsável por carregar e armazenar as imagens do jogo.
 * Evita ler do disco a cada frame (Performance).
 */
public class RenderizadorAssets {
    
    private static RenderizadorAssets instance;
    private Map<String, Image> cacheImagens;
    private Set<String> imagensAusentes; // Para evitar tentar carregar repetidamente imagens que não existem
    
    // Caminho base (ajuste conforme sua pasta de projeto)
    private static final String PATH = ""; 

    private RenderizadorAssets() {
        cacheImagens = new HashMap<>();
        imagensAusentes = new HashSet<>();
    }
    
    public static synchronized RenderizadorAssets get() {
        if (instance == null) {
            instance = new RenderizadorAssets();
        }
        return instance;
    }
    
    /**
     * Carrega uma imagem do disco ou retorna do cache.
     * @param nomeArquivo Nome do arquivo de imagem
     * @param silencioso Se true, não imprime erro no console para imagens não encontradas
     */
    public Image getImagem(String nomeArquivo) {
        return getImagem(nomeArquivo, false);
    }
    
    /**
     * Carrega uma imagem do disco ou retorna do cache.
     * @param nomeArquivo Nome do arquivo de imagem
     * @param silencioso Se true, não imprime erro no console para imagens não encontradas
     */
    public Image getImagem(String nomeArquivo, boolean silencioso) {
        // Se já sabemos que esta imagem não existe, retorna null imediatamente
        if (imagensAusentes.contains(nomeArquivo)) {
            return null;
        }
        
        if (!cacheImagens.containsKey(nomeArquivo)) {
            try {
                File f = new File(PATH + nomeArquivo);
                if (f.exists()) {
                    Image img = ImageIO.read(f);
                    cacheImagens.put(nomeArquivo, img);
                } else {
                    // Marca como ausente para não tentar carregar novamente
                    imagensAusentes.add(nomeArquivo);
                    if (!silencioso) {
                        System.err.println("ERRO: Imagem não encontrada: " + nomeArquivo);
                    }
                    return null;
                }
            } catch (IOException e) {
                // Marca como ausente para não tentar carregar novamente
                imagensAusentes.add(nomeArquivo);
                if (!silencioso) {
                    e.printStackTrace();
                }
                return null;
            }
        }
        return cacheImagens.get(nomeArquivo);
    }
    
    /**
     * Retorna uma versão redimensionada da imagem (para ícones do menu lateral).
     */
    public Image getIcone(String nomeArquivo, int w, int h) {
        return getIcone(nomeArquivo, w, h, false);
    }
    
    /**
     * Retorna uma versão redimensionada da imagem (para ícones do menu lateral).
     */
    public Image getIcone(String nomeArquivo, int w, int h, boolean silencioso) {
        String key = nomeArquivo + "_" + w + "x" + h;
        if (!cacheImagens.containsKey(key)) {
            Image original = getImagem(nomeArquivo, silencioso);
            if (original != null) {
                Image icon = original.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                cacheImagens.put(key, icon);
            }
        }
        return cacheImagens.get(key);
    }
    
    /**
     * Limpa o cache de imagens ausentes, permitindo tentar carregar novamente.
     * Útil se você adicionar novas imagens durante a execução.
     */
    public void limparCacheAusentes() {
        imagensAusentes.clear();
    }
    
    /**
     * Remove uma imagem específica do cache de ausentes, para tentar carregar novamente.
     */
    public void redefinirImagem(String nomeArquivo) {
        imagensAusentes.remove(nomeArquivo);
        cacheImagens.remove(nomeArquivo);
    }
}