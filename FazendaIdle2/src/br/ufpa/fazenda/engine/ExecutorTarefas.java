package br.ufpa.fazenda.engine;

import java.util.concurrent.*;
import javax.swing.SwingUtilities;

/**
 * Executor de tarefas síncrono para evitar condições de corrida.
 * Processa todas as ações do usuário em uma única thread.
 */
public class ExecutorTarefas {
    
    private static ExecutorTarefas instance;
    private final ExecutorService executor;
    
    private ExecutorTarefas() {
        // Executor de thread única para processamento sequencial
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    public static synchronized ExecutorTarefas getInstance() {
        if (instance == null) {
            instance = new ExecutorTarefas();
        }
        return instance;
    }
    
    /**
     * Executa uma tarefa de forma síncrona, garantindo que não há concorrência.
     * @param tarefa A tarefa a ser executada
     */
    public void executar(Runnable tarefa) {
        executor.submit(() -> {
            try {
                // Executa a tarefa na thread do executor
                tarefa.run();
            } catch (Exception e) {
                System.err.println("Erro ao executar tarefa: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Executa uma tarefa de forma síncrona e espera pelo resultado.
     * @param tarefa A tarefa a ser executada
     * @return O resultado da tarefa
     */
    public <T> Future<T> executarComResultado(Callable<T> tarefa) {
        return executor.submit(tarefa);
    }
    
    /**
     * Para o executor de tarefas.
     */
    public void parar() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}