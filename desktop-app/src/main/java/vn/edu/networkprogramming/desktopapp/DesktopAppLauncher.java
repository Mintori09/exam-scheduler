package vn.edu.networkprogramming.desktopapp;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.UIManager;

public final class DesktopAppLauncher {

    private DesktopAppLauncher() {
    }

    public static void main(String[] args) {
        configureLookAndFeel();
        String serverBaseUrl = resolveServerBaseUrl();
        EventQueue.invokeLater(() -> {
            DesktopAppFrame frame = new DesktopAppFrame(new DesktopApiClient(serverBaseUrl));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }

    private static void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    private static String resolveServerBaseUrl() {
        String fromSystemProperty = System.getProperty("serverBaseUrl");
        if (fromSystemProperty != null && !fromSystemProperty.isBlank()) {
            return fromSystemProperty;
        }
        String fromEnvironment = System.getenv("SERVER_BASE_URL");
        if (fromEnvironment != null && !fromEnvironment.isBlank()) {
            return fromEnvironment;
        }
        return "http://localhost:8081/assign-server";
    }
}
