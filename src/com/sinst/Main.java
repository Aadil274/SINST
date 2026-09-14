package com.sinst;

import com.formdev.flatlaf.FlatLightLaf;
import com.sinst.db.DBConnection;
import com.sinst.ui.LoginFrame;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // Setup modern FlatLaf Look and Feel for crisp rendering and proper button styling
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 8);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("TabbedPane.showTabSeparators", true);
            UIManager.put("TabbedPane.tabHeight", 34);
            UIManager.put("Table.rowHeight", 28);
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Quiet connection check
        boolean dbOk = DBConnection.testConnection();
        if (!dbOk) {
            System.out.println("Note: MySQL database not currently connected. Check db.properties or settings dialog.");
        }

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
