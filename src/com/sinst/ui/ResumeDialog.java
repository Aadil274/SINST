package com.sinst.ui;

import com.sinst.model.ResumeData;
import com.sinst.service.ResumeService;
import com.sinst.service.ResumeServiceImpl;
import com.sinst.util.FontManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Interactive Swing dialog providing ATS-friendly resume generation,
 * formatted HTML preview, markdown export, clipboard copy, and file saving.
 */
public class ResumeDialog extends JDialog {

    private final ResumeData resumeData;
    private final ResumeService resumeService;
    private final String plainTextContent;
    private final String markdownContent;
    private final String htmlContent;

    public ResumeDialog(Frame owner, ResumeData data) {
        this(owner, data, new ResumeServiceImpl());
    }

    public ResumeDialog(Frame owner, ResumeData data, ResumeService service) {
        super(owner, "Resume Builder - " + (data != null && data.getStudent() != null ? data.getStudent().getFullName() : "Student"), true);
        this.resumeData = data;
        this.resumeService = service != null ? service : new ResumeServiceImpl();

        this.plainTextContent = resumeService.generatePlainText(resumeData);
        this.markdownContent = resumeService.generateMarkdown(resumeData);
        this.htmlContent = resumeService.generateHtml(resumeData);

        setSize(880, 720);
        setMinimumSize(new Dimension(720, 560));
        setLocationRelativeTo(owner);

        initUI();
        FontManager.applyScale(this);
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(new Color(245, 247, 250));

        // 1. Top Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel titleLbl = new JLabel("ATS-Friendly Student Resume Builder");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLbl.setForeground(Color.WHITE);

        String subtitleText = (resumeData != null && resumeData.getStudent() != null)
            ? "Compiled from profile, " + resumeData.getVerifiedSkillCount() + " verified skill(s), "
              + resumeData.getProjectCount() + " project(s), and " + resumeData.getCertificationCount() + " certification(s)."
            : "Aggregated student competencies and credentials.";

        JLabel subLbl = new JLabel(subtitleText);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(new Color(200, 220, 245));

        headerPanel.add(titleLbl, BorderLayout.NORTH);
        headerPanel.add(subLbl, BorderLayout.SOUTH);
        root.add(headerPanel, BorderLayout.NORTH);

        // 2. Center Tabs: Formatted HTML Preview vs Markdown / Plain Text
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Formatted Preview
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setText(htmlContent);
        editorPane.setCaretPosition(0);
        JScrollPane htmlScroll = new JScrollPane(editorPane);
        htmlScroll.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 238), 1));
        tabs.addTab("Formatted Preview", htmlScroll);

        // Markdown / Plain Text Tab
        JTextArea textArea = new JTextArea(markdownContent);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setCaretPosition(0);
        JScrollPane textScroll = new JScrollPane(textArea);
        textScroll.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 238), 1));
        tabs.addTab("Markdown / Text Code", textScroll);

        root.add(tabs, BorderLayout.CENTER);

        // 3. Bottom Action Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(new Color(245, 247, 250));
        bottomBar.setBorder(new EmptyBorder(8, 16, 12, 16));

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftActions.setOpaque(false);

        JButton copyBtn = new JButton("Copy to Clipboard");
        copyBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        copyBtn.addActionListener(e -> copyToClipboard());
        leftActions.add(copyBtn);

        JButton openBrowserBtn = new JButton("Open in Browser");
        openBrowserBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        openBrowserBtn.addActionListener(e -> openInBrowser());
        leftActions.add(openBrowserBtn);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton saveBtn = new JButton("Export / Save to File...");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveBtn.setBackground(new Color(24, 100, 180));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> saveResumeToFile());
        rightActions.add(saveBtn);

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeBtn.addActionListener(e -> dispose());
        rightActions.add(closeBtn);

        bottomBar.add(leftActions, BorderLayout.WEST);
        bottomBar.add(rightActions, BorderLayout.EAST);
        root.add(bottomBar, BorderLayout.SOUTH);

        add(root);
    }

    private void copyToClipboard() {
        try {
            StringSelection sel = new StringSelection(markdownContent);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
            JOptionPane.showMessageDialog(this, "Resume markdown successfully copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not copy to clipboard: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openInBrowser() {
        try {
            String prefix = "sinst_resume_";
            if (resumeData != null && resumeData.getStudent() != null) {
                prefix += resumeData.getStudent().getRollNumber() + "_";
            }
            File tempFile = File.createTempFile(prefix, ".html");
            tempFile.deleteOnExit();
            try (FileWriter fw = new FileWriter(tempFile)) {
                fw.write(htmlContent);
            }
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(tempFile.toURI());
            } else {
                JOptionPane.showMessageDialog(this, "Temp resume saved to: " + tempFile.getAbsolutePath(), "File Saved", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error previewing in browser: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveResumeToFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Resume File");

        String defaultFileName = "Resume";
        if (resumeData != null && resumeData.getStudent() != null) {
            defaultFileName = "Resume_" + resumeData.getStudent().getFullName().replaceAll("[^a-zA-Z0-9_-]", "_");
        }

        chooser.setSelectedFile(new File(defaultFileName + ".html"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("HTML Document (*.html)", "html"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Markdown Document (*.md)", "md"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Plain Text Document (*.txt)", "txt"));

        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            String path = selected.getAbsolutePath().toLowerCase();
            String contentToWrite;

            if (path.endsWith(".md")) {
                contentToWrite = markdownContent;
            } else if (path.endsWith(".txt")) {
                contentToWrite = plainTextContent;
            } else {
                if (!path.endsWith(".html") && !path.endsWith(".htm")) {
                    selected = new File(selected.getAbsolutePath() + ".html");
                }
                contentToWrite = htmlContent;
            }

            try (FileWriter fw = new FileWriter(selected)) {
                fw.write(contentToWrite);
                JOptionPane.showMessageDialog(this, "Resume exported successfully to:\n" + selected.getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error exporting resume file: " + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
