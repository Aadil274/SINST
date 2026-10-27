package com.sinst.util;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Manages dynamic application-wide font scaling and accessibility zoom for SINST.
 * Allows users to scale UI typography smoothly without altering layout geometry
 * or component styling.
 */
public final class FontManager {

    public static final float SCALE_MIN = 0.85f;
    public static final float SCALE_DEFAULT = 1.00f;
    public static final float SCALE_MEDIUM = 1.15f;
    public static final float SCALE_LARGE = 1.30f;
    public static final float SCALE_XLARGE = 1.50f;
    public static final float SCALE_MAX = 1.75f;
    public static final float SCALE_STEP = 0.15f;

    private static final String PROP_BASE_FONT = "sinst.baseFont";
    private static final String PROP_BASE_ROW_HEIGHT = "sinst.baseRowHeight";
    private static final String PROP_BASE_BORDER_FONT = "sinst.baseBorderFont";

    private static float currentScale = SCALE_DEFAULT;
    private static final List<Consumer<Float>> scaleListeners = new ArrayList<>();

    private FontManager() {}

    public static synchronized float getScale() {
        return currentScale;
    }

    public static synchronized void setScale(float newScale) {
        float clamped = Math.max(SCALE_MIN, Math.min(SCALE_MAX, Math.round(newScale * 100f) / 100f));
        currentScale = clamped;
        applyScaleToAllWindows(currentScale);
        notifyListeners(currentScale);
    }

    public static void zoomIn() {
        setScale(currentScale + SCALE_STEP);
    }

    public static void zoomOut() {
        setScale(currentScale - SCALE_STEP);
    }

    public static void resetScale() {
        setScale(SCALE_DEFAULT);
    }

    public static synchronized void addScaleChangeListener(Consumer<Float> listener) {
        if (listener != null && !scaleListeners.contains(listener)) {
            scaleListeners.add(listener);
        }
    }

    private static synchronized void notifyListeners(float scale) {
        for (Consumer<Float> listener : scaleListeners) {
            try {
                listener.accept(scale);
            } catch (Exception ignored) {}
        }
    }

    public static void applyScaleToAllWindows(float scale) {
        for (Window window : Window.getWindows()) {
            if (window != null && window.isDisplayable()) {
                applyScale(window, scale);
            }
        }
    }

    public static void applyScale(Window window) {
        applyScale(window, currentScale);
    }

    public static void applyScale(Window window, float scale) {
        if (window == null) return;
        applyScaleToComponent(window, scale);
        window.revalidate();
        window.repaint();
    }

    public static void applyScaleToComponent(Component c, float scale) {
        if (c == null) return;

        if (c instanceof JComponent jc) {
            Font baseFont = (Font) jc.getClientProperty(PROP_BASE_FONT);
            if (baseFont == null) {
                baseFont = jc.getFont();
                if (baseFont != null) {
                    jc.putClientProperty(PROP_BASE_FONT, baseFont);
                }
            }
            if (baseFont != null) {
                float newSize = (float) Math.round(baseFont.getSize2D() * scale);
                jc.setFont(baseFont.deriveFont(newSize));
            }

            // Adjust JTable row height and header font
            if (jc instanceof JTable table) {
                Integer baseRowHeight = (Integer) table.getClientProperty(PROP_BASE_ROW_HEIGHT);
                if (baseRowHeight == null) {
                    baseRowHeight = table.getRowHeight();
                    table.putClientProperty(PROP_BASE_ROW_HEIGHT, baseRowHeight);
                }
                if (Math.abs(scale - 1.0f) < 0.001f) {
                    table.setRowHeight(baseRowHeight);
                } else {
                    table.setRowHeight(Math.max(12, Math.round(baseRowHeight * scale)));
                }
                if (table.getTableHeader() != null) {
                    applyScaleToComponent(table.getTableHeader(), scale);
                }
            }

            // Adjust TitledBorder title font if present
            if (jc.getBorder() instanceof TitledBorder tb) {
                Font baseTbFont = (Font) jc.getClientProperty(PROP_BASE_BORDER_FONT);
                if (baseTbFont == null) {
                    baseTbFont = tb.getTitleFont();
                    if (baseTbFont == null) baseTbFont = jc.getFont();
                    if (baseTbFont != null) {
                        jc.putClientProperty(PROP_BASE_BORDER_FONT, baseTbFont);
                    }
                }
                if (baseTbFont != null) {
                    float newSize = (float) Math.round(baseTbFont.getSize2D() * scale);
                    tb.setTitleFont(baseTbFont.deriveFont(newSize));
                }
            }
        } else {
            Font f = c.getFont();
            if (f != null) {
                float newSize = (float) Math.round(f.getSize2D() * scale);
                c.setFont(f.deriveFont(newSize));
            }
        }

        if (c instanceof Container container) {
            for (Component child : container.getComponents()) {
                applyScaleToComponent(child, scale);
            }
        }
    }

    /**
     * Attaches global Ctrl++, Ctrl+-, Ctrl+0 keyboard zoom accelerators to a RootPane.
     */
    public static void attachZoomKeyBindings(JRootPane rootPane) {
        if (rootPane == null) return;
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.CTRL_DOWN_MASK), "fontZoomIn");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, KeyEvent.CTRL_DOWN_MASK), "fontZoomIn");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK), "fontZoomIn");
        actionMap.put("fontZoomIn", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { zoomIn(); }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK), "fontZoomOut");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, KeyEvent.CTRL_DOWN_MASK), "fontZoomOut");
        actionMap.put("fontZoomOut", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { zoomOut(); }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_DOWN_MASK), "fontZoomReset");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, KeyEvent.CTRL_DOWN_MASK), "fontZoomReset");
        actionMap.put("fontZoomReset", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { resetScale(); }
        });
    }

    /**
     * Creates an interactive, styled Font Size dropdown button with a preset popup menu.
     */
    public static JButton createFontSizeButton(JFrame parentFrame, Color bgColor, Color fgColor) {
        int pct = Math.round(currentScale * 100);
        JButton btn = new JButton("Font: " + pct + "% \u25be");
        if (bgColor != null) {
            btn.setBackground(bgColor);
            btn.setOpaque(true);
        }
        if (fgColor != null) {
            btn.setForeground(fgColor);
        }
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText("Adjust application font size / zoom (Ctrl + / Ctrl -)");

        addScaleChangeListener(scale -> {
            int p = Math.round(scale * 100);
            btn.setText("Font: " + p + "% \u25be");
        });

        btn.addActionListener(e -> {
            JPopupMenu menu = new JPopupMenu();

            JMenuItem item100 = new JMenuItem("Standard (100%)");
            item100.addActionListener(ev -> setScale(SCALE_DEFAULT));
            menu.add(item100);

            JMenuItem item115 = new JMenuItem("Medium (115%)");
            item115.addActionListener(ev -> setScale(SCALE_MEDIUM));
            menu.add(item115);

            JMenuItem item130 = new JMenuItem("Large (130%)");
            item130.addActionListener(ev -> setScale(SCALE_LARGE));
            menu.add(item130);

            JMenuItem item150 = new JMenuItem("Extra Large (150%)");
            item150.addActionListener(ev -> setScale(SCALE_XLARGE));
            menu.add(item150);

            menu.addSeparator();

            JMenuItem zoomInItem = new JMenuItem("Zoom In (Ctrl +)");
            zoomInItem.addActionListener(ev -> zoomIn());
            menu.add(zoomInItem);

            JMenuItem zoomOutItem = new JMenuItem("Zoom Out (Ctrl -)");
            zoomOutItem.addActionListener(ev -> zoomOut());
            menu.add(zoomOutItem);

            JMenuItem resetItem = new JMenuItem("Reset Zoom (Ctrl 0)");
            resetItem.addActionListener(ev -> resetScale());
            menu.add(resetItem);

            menu.show(btn, 0, btn.getHeight());
        });

        if (parentFrame != null) {
            attachZoomKeyBindings(parentFrame.getRootPane());
        }

        return btn;
    }
}
