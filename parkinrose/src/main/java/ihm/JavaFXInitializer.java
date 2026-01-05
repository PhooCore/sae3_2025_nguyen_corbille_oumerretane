package ihm;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

public class JavaFXInitializer {
    private static boolean javaFXInitialized = false;
    private static boolean javaFXRunning = false;
    
    public static synchronized void initializeJavaFX() {
        if (!javaFXInitialized) {
            try {
                // Initialiser JavaFX une seule fois
                Platform.setImplicitExit(false); // IMPORTANT: Empêche JavaFX de s'arrêter
                new JFXPanel(); // Ceci force l'initialisation de JavaFX
                javaFXInitialized = true;
                javaFXRunning = true;
                
                // Garder JavaFX en vie
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    shutdownJavaFX();
                }));
                
            } catch (Exception e) {
                System.err.println("Erreur lors de l'initialisation de JavaFX: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public static synchronized void shutdownJavaFX() {
        if (javaFXRunning) {
            try {
                Platform.exit();
                javaFXRunning = false;
            } catch (Exception e) {
                System.err.println("Erreur lors de l'arrêt de JavaFX: " + e.getMessage());
            }
        }
    }
    
    public static synchronized void restartJavaFX() {
        if (javaFXRunning) {
            shutdownJavaFX();
        }
        javaFXInitialized = false;
        initializeJavaFX();
    }
    
    public static boolean isJavaFXInitialized() {
        return javaFXInitialized;
    }
    
    public static boolean isJavaFXRunning() {
        return javaFXRunning;
    }
}