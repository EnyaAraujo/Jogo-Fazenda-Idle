package br.ufpa.fazenda.main;

import br.ufpa.fazenda.view.JanelaPrincipal;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class App {
    public static void main(String[] args) {
        // Tenta usar o visual nativo do sistema operacional (Windows/Mac/Linux)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Se falhar, usa o padrão do Java, sem problemas
            System.out.println("Estilo nativo não disponível.");
        }

        SwingUtilities.invokeLater(() -> {
            new JanelaPrincipal();
        });
    }
}