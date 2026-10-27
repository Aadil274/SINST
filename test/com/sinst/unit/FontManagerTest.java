package com.sinst.unit;

import com.sinst.util.FontManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests: Dynamic UI Font Scaling & Accessibility Zoom")
public class FontManagerTest {

    @BeforeEach
    @AfterEach
    void resetScale() {
        FontManager.resetScale();
    }

    @Test
    @DisplayName("Default font scale must be 1.00 (100%)")
    void testDefaultScale() {
        assertEquals(FontManager.SCALE_DEFAULT, FontManager.getScale(), 0.001f);
    }

    @Test
    @DisplayName("Valid scale updates must update current scale")
    void testSetScale() {
        FontManager.setScale(FontManager.SCALE_LARGE);
        assertEquals(FontManager.SCALE_LARGE, FontManager.getScale(), 0.001f);

        FontManager.setScale(FontManager.SCALE_XLARGE);
        assertEquals(FontManager.SCALE_XLARGE, FontManager.getScale(), 0.001f);
    }

    @Test
    @DisplayName("Font scale must be clamped to safe minimum and maximum bounds")
    void testScaleBounds() {
        FontManager.setScale(0.10f); // too small
        assertEquals(FontManager.SCALE_MIN, FontManager.getScale(), 0.001f);

        FontManager.setScale(3.50f); // too large
        assertEquals(FontManager.SCALE_MAX, FontManager.getScale(), 0.001f);
    }

    @Test
    @DisplayName("Zoom in and zoom out must increment and decrement scale by step")
    void testZoomInAndOut() {
        FontManager.resetScale();
        float initial = FontManager.getScale();

        FontManager.zoomIn();
        assertEquals(initial + FontManager.SCALE_STEP, FontManager.getScale(), 0.01f);

        FontManager.zoomOut();
        assertEquals(initial, FontManager.getScale(), 0.01f);

        FontManager.zoomIn();
        FontManager.zoomIn();
        FontManager.resetScale();
        assertEquals(FontManager.SCALE_DEFAULT, FontManager.getScale(), 0.001f);
    }

    @Test
    @DisplayName("Component tree font scaling must be proportional and fully reversible")
    void testComponentTreeFontScaling() {
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Test Label");
        Font originalFont = new Font("Segoe UI", Font.PLAIN, 14);
        label.setFont(originalFont);
        panel.add(label);

        // Apply 130% scale
        FontManager.applyScaleToComponent(panel, 1.30f);
        float scaledSize = label.getFont().getSize2D();
        assertTrue(scaledSize > 14.0f, "Scaled font must be strictly larger than original");
        assertEquals(18.0f, scaledSize, 0.5f);

        // Revert to 100% scale
        FontManager.applyScaleToComponent(panel, 1.00f);
        assertEquals(14.0f, label.getFont().getSize2D(), 0.01f, "Reverting to 100% must restore exact original size");
    }

    @Test
    @DisplayName("JTable row height and header must scale proportionally with font size")
    void testTableScaling() {
        String[] cols = {"Col1", "Col2"};
        Object[][] data = {{"A", "B"}};
        JTable table = new JTable(data, cols);
        int originalRowHeight = table.getRowHeight();

        FontManager.applyScaleToComponent(table, 1.50f);
        assertTrue(table.getRowHeight() > originalRowHeight, "Scaled row height must be larger");

        FontManager.applyScaleToComponent(table, 1.00f);
        assertEquals(originalRowHeight, table.getRowHeight(), "Reverted row height must match original");
    }

    @Test
    @DisplayName("TitledBorder font must scale proportionally")
    void testTitledBorderScaling() {
        JPanel box = new JPanel();
        TitledBorder tb = BorderFactory.createTitledBorder("Title");
        tb.setTitleFont(new Font("Segoe UI", Font.BOLD, 12));
        box.setBorder(tb);

        FontManager.applyScaleToComponent(box, 1.30f);
        assertTrue(tb.getTitleFont().getSize2D() > 12.0f);

        FontManager.applyScaleToComponent(box, 1.00f);
        assertEquals(12.0f, tb.getTitleFont().getSize2D(), 0.01f);
    }

    @Test
    @DisplayName("Scale change listener must receive updated scale factor")
    void testScaleListener() {
        AtomicReference<Float> observed = new AtomicReference<>(0f);
        FontManager.addScaleChangeListener(observed::set);

        FontManager.setScale(FontManager.SCALE_MEDIUM);
        assertEquals(FontManager.SCALE_MEDIUM, observed.get(), 0.001f);
    }
}
