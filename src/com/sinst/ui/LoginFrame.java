package com.sinst.ui;

import com.sinst.db.AppDAO;
import com.sinst.db.DBConnection;
import com.sinst.model.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class LoginFrame extends JFrame {

    private JTextField loginIdentifierField;
    private JPasswordField loginPasswordField;

    private JTextField regRollField;
    private JTextField regNameField;
    private JTextField regEmailField;
    private JPasswordField regPasswordField;
    private JComboBox<String> regDeptCombo;
    private JComboBox<Integer> regYearCombo;
    private JTextField regPhoneField;

    public LoginFrame() {
        setTitle("Smart Internship & Skill Tracker (SINST) - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(560, 640);
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        mainPanel.setBackground(new Color(245, 247, 250));

        // Header Banner
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(20, 15, 20, 15));

        JLabel titleLabel = new JLabel("SMART INTERNSHIP & SKILL TRACKER", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Centralized Career Preparation & Skill-Gap Analysis System", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(200, 220, 245));

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Tabbed Pane for Login / Register
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("Student Login", createLoginPanel());
        tabbedPane.addTab("New Registration", createRegisterPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Footer with DB Settings Button
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(245, 247, 250));
        footerPanel.setBorder(new EmptyBorder(8, 5, 5, 5));

        JButton dbConfigBtn = new JButton("Database Settings");
        dbConfigBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dbConfigBtn.setFocusPainted(false);
        dbConfigBtn.addActionListener(e -> showDBSettingsDialog());

        boolean isConnected = DBConnection.testConnection();
        JLabel statusLabel = new JLabel(isConnected ? "Database: Connected" : "Database: Disconnected");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(isConnected ? new Color(39, 174, 96) : new Color(192, 57, 43));

        footerPanel.add(statusLabel, BorderLayout.WEST);
        footerPanel.add(dbConfigBtn, BorderLayout.EAST);

        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Roll Number or Email
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel userLabel = new JLabel("Roll No / Email:");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(userLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        loginIdentifierField = new JTextField("25R11A0501", 15);
        loginIdentifierField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(loginIdentifierField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(passLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.7;
        loginPasswordField = new JPasswordField("password123", 15);
        loginPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(loginPasswordField, gbc);

        // Login Button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JButton loginBtn = new JButton("Secure Login");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginBtn.setBackground(new Color(24, 100, 180));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setPreferredSize(new Dimension(150, 36));
        loginBtn.addActionListener(e -> performLogin());
        panel.add(loginBtn, gbc);

        // Demo Accounts hint
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JLabel hintLabel = new JLabel("<html><center><font color='#555555'>Demo Credentials:<br/>Roll No: <b>25R11A0501</b> &nbsp;|&nbsp; Password: <b>password123</b></font></center></html>", JLabel.CENTER);
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        panel.add(hintLabel, gbc);

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 25, 15, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Roll Number
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        panel.add(new JLabel("Roll Number:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        regRollField = new JTextField(15);
        panel.add(regRollField, gbc);

        // Full Name
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        regNameField = new JTextField(15);
        panel.add(regNameField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Email Address:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        regEmailField = new JTextField(15);
        panel.add(regEmailField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        regPasswordField = new JPasswordField(15);
        panel.add(regPasswordField, gbc);

        // Department
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        regDeptCombo = new JComboBox<>(new String[]{"CSE", "CSE (AI & ML)", "CSE (Data Science)", "IT", "ECE"});
        panel.add(regDeptCombo, gbc);

        // Year
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Year of Study:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5;
        regYearCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        regYearCombo.setSelectedItem(2);
        panel.add(regYearCombo, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("Contact Phone:"), gbc);
        gbc.gridx = 1; gbc.gridy = 6;
        regPhoneField = new JTextField(15);
        panel.add(regPhoneField, gbc);

        // Register Button
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        JButton regBtn = new JButton("Register Profile");
        regBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        regBtn.setBackground(new Color(39, 174, 96));
        regBtn.setForeground(Color.WHITE);
        regBtn.setFocusPainted(false);
        regBtn.setPreferredSize(new Dimension(150, 34));
        regBtn.addActionListener(e -> performRegistration());
        panel.add(regBtn, gbc);

        return panel;
    }

    private void performLogin() {
        String identifier = loginIdentifierField.getText().trim();
        String password = new String(loginPasswordField.getPassword()).trim();

        if (identifier.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Roll Number/Email and Password.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Student student = AppDAO.login(identifier, password);
            if (student != null) {
                new MainFrame(student).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials. Please check your roll number/password.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Connection Error: " + e.getMessage() + "\n\nPlease check Database Settings (button at the bottom).", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performRegistration() {
        String roll = regRollField.getText().trim();
        String name = regNameField.getText().trim();
        String email = regEmailField.getText().trim();
        String password = new String(regPasswordField.getPassword()).trim();
        String dept = (String) regDeptCombo.getSelectedItem();
        int year = (Integer) regYearCombo.getSelectedItem();
        String phone = regPhoneField.getText().trim();

        if (roll.isEmpty() || name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all mandatory fields (Roll No, Name, Email, Password).", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Student newStudent = new Student(0, roll, name, email, password, dept, year, phone);
        try {
            boolean success = AppDAO.registerStudent(newStudent);
            if (success) {
                JOptionPane.showMessageDialog(this, "Registration Successful! You may now login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loginIdentifierField.setText(roll);
                loginPasswordField.setText(password);
            } else {
                JOptionPane.showMessageDialog(this, "Registration could not be completed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Registration Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDBSettingsDialog() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));
        JTextField hostField = new JTextField(DBConnection.getHost());
        JTextField portField = new JTextField(String.valueOf(DBConnection.getPort()));
        JTextField dbField = new JTextField(DBConnection.getDatabase());
        JTextField userField = new JTextField(DBConnection.getUser());
        JPasswordField passField = new JPasswordField(DBConnection.getPassword());

        panel.add(new JLabel("MySQL Host:"));
        panel.add(hostField);
        panel.add(new JLabel("Port:"));
        panel.add(portField);
        panel.add(new JLabel("Database Name:"));
        panel.add(dbField);
        panel.add(new JLabel("Username:"));
        panel.add(userField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);

        int result = JOptionPane.showConfirmDialog(this, panel, "MySQL Database Settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int port = Integer.parseInt(portField.getText().trim());
                DBConnection.updateCredentials(
                    hostField.getText().trim(),
                    port,
                    dbField.getText().trim(),
                    userField.getText().trim(),
                    new String(passField.getPassword())
                );
                boolean connected = DBConnection.testConnection();
                if (connected) {
                    JOptionPane.showMessageDialog(this, "Database connection successful!", "Connected", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Could not connect with provided settings. Please verify MySQL service is running.", "Connection Failed", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid settings: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
