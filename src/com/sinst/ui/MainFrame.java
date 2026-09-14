package com.sinst.ui;

import com.sinst.db.AppDAO;
import com.sinst.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {

    private final Student currentStudent;
    private JTabbedPane mainTabbedPane;

    // Dashboard widgets
    private JLabel totalAppsVal;
    private JLabel underReviewVal;
    private JLabel interviewsVal;
    private JLabel selectedVal;
    private DefaultTableModel statusModel;
    private DefaultTableModel upcomingInterviewsModel;

    // Applications widgets
    private JTable appsTable;
    private DefaultTableModel appsModel;
    private JComboBox<String> appFilterCombo;
    private List<Application> loadedApplications;

    // Skills widgets
    private JTable studentSkillsTable;
    private DefaultTableModel studentSkillsModel;
    private JComboBox<Skill> masterSkillsCombo;
    private JComboBox<String> proficiencyCombo;

    // Certifications & Projects widgets
    private JTable certsTable;
    private DefaultTableModel certsModel;
    private JTable projTable;
    private DefaultTableModel projectsModel;

    // Skill Gap widgets
    private JComboBox<Role> gapRoleCombo;
    private JProgressBar gapProgressBar;
    private JLabel gapSummaryLabel;
    private DefaultTableModel gapMatchedModel;
    private DefaultTableModel gapMissingModel;
    private JTable gapMissingTable;
    private List<SkillGapResult> currentGapResults;

    public MainFrame(Student student) {
        this.currentStudent = student;
        setTitle("Smart Internship & Skill Tracker (SINST) - " + student.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 720);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);

        initUI();
        refreshAllData();

        // Auto-run initial skill gap analysis for default role
        SwingUtilities.invokeLater(this::performSkillGapAnalysis);
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());

        // 1. Top Student Header Banner
        JPanel topBanner = new JPanel(new BorderLayout());
        topBanner.setBackground(new Color(24, 43, 73));
        topBanner.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel welcomeLabel = new JLabel("STUDENT: " + currentStudent.getFullName().toUpperCase() + 
                                         "  |  Roll No: " + currentStudent.getRollNumber() + 
                                         "  |  Dept: " + currentStudent.getDepartment() + 
                                         " (Year " + currentStudent.getYearOfStudy() + ")");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        welcomeLabel.setForeground(Color.WHITE);

        JButton logoutBtn = createStyledButton("Sign Out", new Color(217, 83, 79), Color.WHITE);
        logoutBtn.setPreferredSize(new Dimension(100, 32));
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        topBanner.add(welcomeLabel, BorderLayout.WEST);
        topBanner.add(logoutBtn, BorderLayout.EAST);
        root.add(topBanner, BorderLayout.NORTH);

        // 2. Main Tabbed Interface (No broken emoji glyphs)
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        mainTabbedPane.addTab("Dashboard", createDashboardTab());
        mainTabbedPane.addTab("Applications & Interviews", createApplicationsTab());
        mainTabbedPane.addTab("Skills & Profile", createSkillsProfileTab());
        mainTabbedPane.addTab("Skill-Gap Analyzer", createSkillGapTab());

        root.add(mainTabbedPane, BorderLayout.CENTER);
        add(root);
    }

    public static JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // =========================================================================
    // TAB 1: DASHBOARD
    // =========================================================================

    private JPanel createDashboardTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(new Color(245, 247, 250));

        // Top Section: KPI Cards + Quick Actions Bar
        JPanel topSection = new JPanel(new BorderLayout(8, 8));
        topSection.setOpaque(false);

        // KPI Cards
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        kpiPanel.setOpaque(false);
        kpiPanel.add(createKpiCard("Total Applications", totalAppsVal = new JLabel("0", JLabel.CENTER), new Color(41, 128, 185)));
        kpiPanel.add(createKpiCard("In Review", underReviewVal = new JLabel("0", JLabel.CENTER), new Color(243, 156, 18)));
        kpiPanel.add(createKpiCard("Interviews Scheduled", interviewsVal = new JLabel("0", JLabel.CENTER), new Color(142, 68, 173)));
        kpiPanel.add(createKpiCard("Offers (Selected)", selectedVal = new JLabel("0", JLabel.CENTER), new Color(39, 174, 96)));
        topSection.add(kpiPanel, BorderLayout.NORTH);

        // Quick Actions Bar
        JPanel quickBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        quickBar.setBackground(Color.WHITE);
        quickBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 230, 238), 1),
            new EmptyBorder(4, 10, 4, 10)
        ));

        JLabel quickLabel = new JLabel("Quick Actions:");
        quickLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        quickLabel.setForeground(new Color(70, 80, 95));
        quickBar.add(quickLabel);

        JButton newAppBtn = createStyledButton("+ New Application", new Color(24, 100, 180), Color.WHITE);
        newAppBtn.addActionListener(e -> showAddApplicationDialog());
        quickBar.add(newAppBtn);

        JButton addSkillBtn = createStyledButton("+ Add Skill", new Color(39, 174, 96), Color.WHITE);
        addSkillBtn.addActionListener(e -> mainTabbedPane.setSelectedIndex(2));
        quickBar.add(addSkillBtn);

        JButton runGapBtn = createStyledButton("Analyze Skill Gap", new Color(142, 68, 173), Color.WHITE);
        runGapBtn.addActionListener(e -> {
            mainTabbedPane.setSelectedIndex(3);
            performSkillGapAnalysis();
        });
        quickBar.add(runGapBtn);

        JButton refreshBtn = new JButton("Refresh Dashboard");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.addActionListener(e -> refreshDashboard());
        quickBar.add(refreshBtn);

        topSection.add(quickBar, BorderLayout.SOUTH);
        panel.add(topSection, BorderLayout.NORTH);

        // Center Content: Status Breakdown & Upcoming Interviews
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 15, 0));
        centerGrid.setOpaque(false);

        // Status Breakdown Panel
        JPanel statusPanel = new JPanel(new BorderLayout(5, 5));
        statusPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Application Status Breakdown", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13)));
        statusPanel.setBackground(Color.WHITE);

        statusModel = new DefaultTableModel(new String[]{"Application Status", "Count"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable statusTable = new JTable(statusModel);
        statusTable.setRowHeight(28);
        statusTable.getColumnModel().getColumn(0).setCellRenderer(new StatusBadgeRenderer());
        statusPanel.add(new JScrollPane(statusTable), BorderLayout.CENTER);
        centerGrid.add(statusPanel);

        // Upcoming Interviews Panel
        JPanel interviewPanel = new JPanel(new BorderLayout(5, 5));
        interviewPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Upcoming Interviews & Deadlines", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13)));
        interviewPanel.setBackground(Color.WHITE);

        upcomingInterviewsModel = new DefaultTableModel(new String[]{"Date & Time", "Company", "Role", "Round", "Mode"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable upcomingTable = new JTable(upcomingInterviewsModel);
        upcomingTable.setRowHeight(28);
        interviewPanel.add(new JScrollPane(upcomingTable), BorderLayout.CENTER);
        centerGrid.add(interviewPanel);

        panel.add(centerGrid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 230, 238), 1),
            new EmptyBorder(12, 15, 12, 15)
        ));

        JLabel titleLbl = new JLabel(title, JLabel.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLbl.setForeground(new Color(100, 110, 120));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(accentColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // TAB 2: APPLICATIONS & INTERVIEWS
    // =========================================================================

    private JPanel createApplicationsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(Color.WHITE);

        // Top Controls: Filter & Actions
        JPanel topControls = new JPanel(new BorderLayout());
        topControls.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filterPanel.setOpaque(false);
        filterPanel.add(new JLabel("Filter by Status:"));
        appFilterCombo = new JComboBox<>(new String[]{"All", "Applied", "Shortlisted", "Interview", "Selected", "Rejected"});
        appFilterCombo.addActionListener(e -> filterApplicationsTable());
        filterPanel.add(appFilterCombo);
        topControls.add(filterPanel, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionBtns.setOpaque(false);

        JButton addAppBtn = createStyledButton("+ New Application", new Color(24, 100, 180), Color.WHITE);
        addAppBtn.addActionListener(e -> showAddApplicationDialog());

        JButton updateStatusBtn = new JButton("Update Status");
        updateStatusBtn.addActionListener(e -> showUpdateStatusDialog());

        JButton scheduleIntBtn = new JButton("Schedule Interview");
        scheduleIntBtn.addActionListener(e -> showScheduleInterviewDialog());

        JButton viewIntsBtn = new JButton("View Interviews");
        viewIntsBtn.addActionListener(e -> showViewInterviewsDialog());

        JButton deleteAppBtn = createStyledButton("Delete", new Color(217, 83, 79), Color.WHITE);
        deleteAppBtn.addActionListener(e -> deleteSelectedApplication());

        actionBtns.add(addAppBtn);
        actionBtns.add(updateStatusBtn);
        actionBtns.add(scheduleIntBtn);
        actionBtns.add(viewIntsBtn);
        actionBtns.add(deleteAppBtn);
        topControls.add(actionBtns, BorderLayout.EAST);

        panel.add(topControls, BorderLayout.NORTH);

        // Table
        appsModel = new DefaultTableModel(new String[]{"ID", "Company", "Target Role", "Date Applied", "Deadline", "Status", "Stipend", "Notes"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        appsTable = new JTable(appsModel);
        appsTable.setRowHeight(28);
        appsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appsTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        appsTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        appsTable.getColumnModel().getColumn(2).setPreferredWidth(170);
        appsTable.getColumnModel().getColumn(5).setPreferredWidth(95);
        appsTable.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        // Double click to update status
        appsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showUpdateStatusDialog();
                }
            }
        });

        panel.add(new JScrollPane(appsTable), BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // TAB 3: SKILLS, PROJECTS & CERTIFICATIONS
    // =========================================================================

    private JPanel createSkillsProfileTab() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(Color.WHITE);

        // Left Panel: Student Skills Management
        JPanel skillsBox = new JPanel(new BorderLayout(8, 8));
        skillsBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "My Skills & Competencies", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13)));
        skillsBox.setBackground(Color.WHITE);

        // Add Skill Bar
        JPanel addSkillBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        addSkillBar.setOpaque(false);
        masterSkillsCombo = new JComboBox<>();
        proficiencyCombo = new JComboBox<>(new String[]{"Beginner", "Intermediate", "Advanced", "Expert"});
        proficiencyCombo.setSelectedItem("Intermediate");

        JButton addSkillBtn = createStyledButton("+ Add / Update Skill", new Color(39, 174, 96), Color.WHITE);
        addSkillBtn.addActionListener(e -> addStudentSkill());

        JButton deleteSkillBtn = new JButton("Remove");
        deleteSkillBtn.addActionListener(e -> removeStudentSkill());

        addSkillBar.add(new JLabel("Skill:"));
        addSkillBar.add(masterSkillsCombo);
        addSkillBar.add(new JLabel("Level:"));
        addSkillBar.add(proficiencyCombo);
        addSkillBar.add(addSkillBtn);
        addSkillBar.add(deleteSkillBtn);
        skillsBox.add(addSkillBar, BorderLayout.NORTH);

        studentSkillsModel = new DefaultTableModel(new String[]{"ID", "Skill Name", "Category", "Proficiency Level"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        studentSkillsTable = new JTable(studentSkillsModel);
        studentSkillsTable.setRowHeight(28);
        studentSkillsTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        skillsBox.add(new JScrollPane(studentSkillsTable), BorderLayout.CENTER);

        panel.add(skillsBox);

        // Right Panel: Certifications & Projects Tabbed
        JTabbedPane bioTabs = new JTabbedPane();

        // Sub-Tab: Certifications
        JPanel certsPanel = new JPanel(new BorderLayout(5, 5));
        certsPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        certsModel = new DefaultTableModel(new String[]{"ID", "Certification", "Issuing Org", "Issue Date"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        certsTable = new JTable(certsModel);
        certsTable.setRowHeight(26);
        certsPanel.add(new JScrollPane(certsTable), BorderLayout.CENTER);

        JPanel certBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addCertBtn = createStyledButton("+ Add Certification", new Color(24, 100, 180), Color.WHITE);
        addCertBtn.addActionListener(e -> showAddCertDialog());
        JButton delCertBtn = new JButton("Remove");
        delCertBtn.addActionListener(e -> deleteSelectedCert());
        certBtns.add(addCertBtn);
        certBtns.add(delCertBtn);
        certsPanel.add(certBtns, BorderLayout.SOUTH);
        bioTabs.addTab("Certifications", certsPanel);

        // Sub-Tab: Projects
        JPanel projPanel = new JPanel(new BorderLayout(5, 5));
        projPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        projectsModel = new DefaultTableModel(new String[]{"ID", "Project Title", "Tech Stack"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        projTable = new JTable(projectsModel);
        projTable.setRowHeight(26);
        projPanel.add(new JScrollPane(projTable), BorderLayout.CENTER);

        JPanel projBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addProjBtn = createStyledButton("+ Add Project", new Color(24, 100, 180), Color.WHITE);
        addProjBtn.addActionListener(e -> showAddProjectDialog());
        JButton delProjBtn = new JButton("Remove");
        delProjBtn.addActionListener(e -> deleteSelectedProject());
        projBtns.add(addProjBtn);
        projBtns.add(delProjBtn);
        projPanel.add(projBtns, BorderLayout.SOUTH);
        bioTabs.addTab("Academic Projects", projPanel);

        panel.add(bioTabs);
        return panel;
    }

    // =========================================================================
    // TAB 4: SKILL-GAP ANALYZER (Direct Relational Database Comparison)
    // =========================================================================

    private JPanel createSkillGapTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        // Top Selection Header
        JPanel topHeader = new JPanel(new BorderLayout(10, 10));
        topHeader.setOpaque(false);

        JPanel selectBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        selectBox.setOpaque(false);
        selectBox.add(new JLabel("Select Target Internship Role:"));
        gapRoleCombo = new JComboBox<>();
        gapRoleCombo.setPreferredSize(new Dimension(320, 32));
        gapRoleCombo.addActionListener(e -> performSkillGapAnalysis());
        selectBox.add(gapRoleCombo);

        JButton analyzeBtn = createStyledButton("Run Skill-Gap Analysis", new Color(24, 100, 180), Color.WHITE);
        analyzeBtn.addActionListener(e -> performSkillGapAnalysis());
        selectBox.add(analyzeBtn);

        topHeader.add(selectBox, BorderLayout.NORTH);

        // Progress Bar & Readiness Summary
        JPanel progressCard = new JPanel(new BorderLayout(8, 8));
        progressCard.setBackground(new Color(245, 248, 253));
        progressCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 225, 245), 1),
            new EmptyBorder(12, 16, 12, 16)
        ));

        gapSummaryLabel = new JLabel("Evaluating profile competencies against target role...", JLabel.LEFT);
        gapSummaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        gapProgressBar = new JProgressBar(0, 100);
        gapProgressBar.setStringPainted(true);
        gapProgressBar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        gapProgressBar.setPreferredSize(new Dimension(300, 26));
        gapProgressBar.setValue(0);
        gapProgressBar.setString("Calculating...");

        progressCard.add(gapSummaryLabel, BorderLayout.NORTH);
        progressCard.add(gapProgressBar, BorderLayout.CENTER);
        topHeader.add(progressCard, BorderLayout.SOUTH);

        panel.add(topHeader, BorderLayout.NORTH);

        // Tables Split: Matched Competencies vs Missing Competencies
        JPanel tablesGrid = new JPanel(new GridLayout(1, 2, 15, 0));
        tablesGrid.setOpaque(false);

        // Matched Skills Table
        JPanel matchedBox = new JPanel(new BorderLayout(5, 5));
        matchedBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(39, 174, 96), 1), "Matched Competencies (Possessed)", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), new Color(39, 174, 96)));
        matchedBox.setBackground(Color.WHITE);

        gapMatchedModel = new DefaultTableModel(new String[]{"Required Skill", "Category", "Benchmark", "Your Level"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable matchedTable = new JTable(gapMatchedModel);
        matchedTable.setRowHeight(28);
        matchedBox.add(new JScrollPane(matchedTable), BorderLayout.CENTER);
        tablesGrid.add(matchedBox);

        // Missing Skills Table
        JPanel missingBox = new JPanel(new BorderLayout(5, 5));
        missingBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(217, 83, 79), 1), "Missing Competencies (Skills to Improve)", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), new Color(217, 83, 79)));
        missingBox.setBackground(Color.WHITE);

        gapMissingModel = new DefaultTableModel(new String[]{"Required Skill", "Category", "Required Benchmark", "Current Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        gapMissingTable = new JTable(gapMissingModel);
        gapMissingTable.setRowHeight(28);
        missingBox.add(new JScrollPane(gapMissingTable), BorderLayout.CENTER);

        // Quick button to add missing skill
        JButton acquireBtn = createStyledButton("+ Add Selected Missing Skill To Profile", new Color(39, 174, 96), Color.WHITE);
        acquireBtn.setPreferredSize(new Dimension(300, 32));
        acquireBtn.addActionListener(e -> addSelectedMissingSkill());
        missingBox.add(acquireBtn, BorderLayout.SOUTH);

        tablesGrid.add(missingBox);

        panel.add(tablesGrid, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // DATA REFRESH LOGIC
    // =========================================================================

    public void refreshAllData() {
        refreshDashboard();
        refreshApplications();
        refreshSkills();
        refreshCertsAndProjects();
        refreshRolesAndCompanies();
    }

    private void refreshDashboard() {
        try {
            Map<String, Integer> stats = AppDAO.getDashboardCounts(currentStudent.getStudentId());
            totalAppsVal.setText(String.valueOf(stats.getOrDefault("Total", 0)));
            int underReview = stats.getOrDefault("Applied", 0) + stats.getOrDefault("Shortlisted", 0);
            underReviewVal.setText(String.valueOf(underReview));
            interviewsVal.setText(String.valueOf(stats.getOrDefault("UpcomingInterviews", 0)));
            selectedVal.setText(String.valueOf(stats.getOrDefault("Selected", 0)));

            // Status Breakdown
            statusModel.setRowCount(0);
            for (String key : new String[]{"Applied", "Shortlisted", "Interview", "Selected", "Rejected"}) {
                statusModel.addRow(new Object[]{key, stats.getOrDefault(key, 0)});
            }

            // Upcoming Interviews
            upcomingInterviewsModel.setRowCount(0);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
            List<Interview> upcoming = AppDAO.getUpcomingInterviews(currentStudent.getStudentId());
            for (Interview inv : upcoming) {
                upcomingInterviewsModel.addRow(new Object[]{
                    sdf.format(inv.getInterviewDate()),
                    inv.getCompanyName(),
                    inv.getRoleTitle(),
                    inv.getRoundType(),
                    inv.getMode()
                });
            }
        } catch (SQLException e) {
            System.err.println("Dashboard refresh error: " + e.getMessage());
        }
    }

    private void refreshApplications() {
        try {
            loadedApplications = AppDAO.getApplications(currentStudent.getStudentId());
            filterApplicationsTable();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Could not load applications: " + e.getMessage());
        }
    }

    private void filterApplicationsTable() {
        if (loadedApplications == null) return;
        String filter = (String) appFilterCombo.getSelectedItem();
        appsModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");

        for (Application a : loadedApplications) {
            if ("All".equalsIgnoreCase(filter) || a.getStatus().equalsIgnoreCase(filter)) {
                appsModel.addRow(new Object[]{
                    a.getApplicationId(),
                    a.getCompanyName(),
                    a.getRoleTitle(),
                    a.getApplicationDate() != null ? sdf.format(a.getApplicationDate()) : "",
                    a.getDeadline() != null ? sdf.format(a.getDeadline()) : "N/A",
                    a.getStatus(),
                    a.getStipend() != null ? a.getStipend() : "-",
                    a.getNotes() != null ? a.getNotes() : ""
                });
            }
        }
    }

    private void refreshSkills() {
        try {
            List<Skill> master = AppDAO.getAllSkills();
            masterSkillsCombo.removeAllItems();
            for (Skill s : master) {
                masterSkillsCombo.addItem(s);
            }

            List<Skill> studentSkills = AppDAO.getStudentSkills(currentStudent.getStudentId());
            studentSkillsModel.setRowCount(0);
            for (Skill s : studentSkills) {
                studentSkillsModel.addRow(new Object[]{
                    s.getSkillId(),
                    s.getSkillName(),
                    s.getCategory(),
                    s.getProficiencyLevel()
                });
            }
        } catch (SQLException e) {
            System.err.println("Skills refresh error: " + e.getMessage());
        }
    }

    private void refreshCertsAndProjects() {
        try {
            List<Certification> certs = AppDAO.getCertifications(currentStudent.getStudentId());
            certsModel.setRowCount(0);
            for (Certification c : certs) {
                certsModel.addRow(new Object[]{c.getCertificationId(), c.getCertificateName(), c.getIssuingOrganization(), c.getIssueDate()});
            }

            List<Project> projs = AppDAO.getProjects(currentStudent.getStudentId());
            projectsModel.setRowCount(0);
            for (Project p : projs) {
                projectsModel.addRow(new Object[]{p.getProjectId(), p.getTitle(), p.getTechnologiesUsed()});
            }
        } catch (SQLException e) {
            System.err.println("Cert/Project refresh error: " + e.getMessage());
        }
    }

    private void refreshRolesAndCompanies() {
        try {
            List<Role> roles = AppDAO.getAllRoles();
            gapRoleCombo.removeAllItems();
            for (Role r : roles) {
                gapRoleCombo.addItem(r);
            }
        } catch (SQLException e) {
            System.err.println("Roles refresh error: " + e.getMessage());
        }
    }

    // =========================================================================
    // SKILL GAP EXECUTION
    // =========================================================================

    private void performSkillGapAnalysis() {
        Role selectedRole = (Role) gapRoleCombo.getSelectedItem();
        if (selectedRole == null) return;

        try {
            currentGapResults = AppDAO.getSkillGapAnalysis(currentStudent.getStudentId(), selectedRole.getRoleId());
            gapMatchedModel.setRowCount(0);
            gapMissingModel.setRowCount(0);

            int matchedCount = 0;
            int totalRequired = currentGapResults.size();

            for (SkillGapResult r : currentGapResults) {
                if (r.isMatched()) {
                    matchedCount++;
                    gapMatchedModel.addRow(new Object[]{r.getSkillName(), r.getCategory(), r.getRequiredLevel(), r.getStudentLevel()});
                } else {
                    gapMissingModel.addRow(new Object[]{r.getSkillName(), r.getCategory(), r.getRequiredLevel(), "Not Possessed"});
                }
            }

            int pct = totalRequired > 0 ? (int) Math.round(((double) matchedCount / totalRequired) * 100) : 0;
            gapProgressBar.setValue(pct);
            gapProgressBar.setString(pct + "% Match (" + matchedCount + " of " + totalRequired + " required skills met)");

            if (pct >= 80) {
                gapProgressBar.setForeground(new Color(39, 174, 96));
                gapSummaryLabel.setText("High Readiness: You meet " + matchedCount + " of " + totalRequired + " competencies for " + selectedRole.getRoleTitle() + ".");
            } else if (pct >= 50) {
                gapProgressBar.setForeground(new Color(243, 156, 18));
                gapSummaryLabel.setText("Moderate Match: You possess " + matchedCount + " of " + totalRequired + " skills. Review the missing skills below to prepare.");
            } else {
                gapProgressBar.setForeground(new Color(217, 83, 79));
                gapSummaryLabel.setText("Skill Gap Detected: You possess " + matchedCount + " of " + totalRequired + " required skills. Target the missing skills below.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error analyzing skill gap: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addSelectedMissingSkill() {
        int row = gapMissingTable.getSelectedRow();
        if (row == -1) {
            if (gapMissingTable.getRowCount() > 0) {
                gapMissingTable.setRowSelectionInterval(0, 0);
                row = 0;
            } else {
                JOptionPane.showMessageDialog(this, "No missing skills remaining for this role!", "All Skills Matched", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        String skillName = (String) gapMissingModel.getValueAt(row, 0);
        addSkillByName(skillName);
    }

    private void addSkillByName(String skillName) {
        try {
            List<Skill> all = AppDAO.getAllSkills();
            for (Skill s : all) {
                if (s.getSkillName().equalsIgnoreCase(skillName)) {
                    AppDAO.addOrUpdateStudentSkill(currentStudent.getStudentId(), s.getSkillId(), "Intermediate");
                    JOptionPane.showMessageDialog(this, "Successfully added '" + skillName + "' (Intermediate) to your profile!", "Skill Acquired", JOptionPane.INFORMATION_MESSAGE);
                    refreshSkills();
                    performSkillGapAnalysis();
                    return;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding skill: " + e.getMessage());
        }
    }

    private void addStudentSkill() {
        Skill selected = (Skill) masterSkillsCombo.getSelectedItem();
        String prof = (String) proficiencyCombo.getSelectedItem();
        if (selected == null || prof == null) return;

        try {
            AppDAO.addOrUpdateStudentSkill(currentStudent.getStudentId(), selected.getSkillId(), prof);
            refreshSkills();
            refreshDashboard();
            JOptionPane.showMessageDialog(this, "Skill '" + selected.getSkillName() + "' (" + prof + ") saved to your profile!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error saving skill: " + e.getMessage());
        }
    }

    private void removeStudentSkill() {
        int row = studentSkillsTable.getSelectedRow();
        if (row == -1) {
            if (studentSkillsTable.getRowCount() > 0) {
                studentSkillsTable.setRowSelectionInterval(0, 0);
                row = 0;
            } else {
                JOptionPane.showMessageDialog(this, "No skills in profile to remove.");
                return;
            }
        }
        int skillId = (int) studentSkillsModel.getValueAt(row, 0);
        String skillName = (String) studentSkillsModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this, "Remove skill '" + skillName + "' from your profile?", "Confirm Removal", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                AppDAO.deleteStudentSkill(currentStudent.getStudentId(), skillId);
                refreshSkills();
                refreshDashboard();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    // =========================================================================
    // DIALOGS & APPLICATION ACTIONS
    // =========================================================================

    private int getSelectedApplicationId() {
        int row = appsTable.getSelectedRow();
        if (row == -1) {
            if (appsTable.getRowCount() > 0) {
                appsTable.setRowSelectionInterval(0, 0);
                row = 0;
            } else {
                JOptionPane.showMessageDialog(this, "No applications found. Please click '+ New Application' first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
                return -1;
            }
        }
        return (int) appsModel.getValueAt(row, 0);
    }

    private void showAddApplicationDialog() {
        try {
            List<Company> companies = AppDAO.getAllCompanies();
            List<Role> roles = AppDAO.getAllRoles();

            if (companies.isEmpty() || roles.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please ensure companies and roles are seeded in the database.", "Data Notice", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JPanel panel = new JPanel(new GridLayout(6, 2, 8, 8));
            JComboBox<Company> compCombo = new JComboBox<>(companies.toArray(new Company[0]));
            JComboBox<Role> roleCombo = new JComboBox<>(roles.toArray(new Role[0]));
            JTextField stipendField = new JTextField("₹ 35,000 / mo");
            JTextField deadlineField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(System.currentTimeMillis() + 86400000L * 30)));
            JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Applied", "Shortlisted", "Interview", "Selected"});
            JTextField notesField = new JTextField("Submitted via campus recruitment drive.");

            panel.add(new JLabel("Target Company:"));
            panel.add(compCombo);
            panel.add(new JLabel("Target Role:"));
            panel.add(roleCombo);
            panel.add(new JLabel("Application Deadline (YYYY-MM-DD):"));
            panel.add(deadlineField);
            panel.add(new JLabel("Initial Status:"));
            panel.add(statusCombo);
            panel.add(new JLabel("Expected Stipend:"));
            panel.add(stipendField);
            panel.add(new JLabel("Notes:"));
            panel.add(notesField);

            int result = JOptionPane.showConfirmDialog(this, panel, "Record New Internship Application", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                Company c = (Company) compCombo.getSelectedItem();
                Role r = (Role) roleCombo.getSelectedItem();
                Date appDate = new Date(System.currentTimeMillis());

                Date deadline;
                try {
                    deadline = Date.valueOf(deadlineField.getText().trim());
                } catch (Exception ex) {
                    deadline = new Date(System.currentTimeMillis() + 86400000L * 30);
                }

                Application app = new Application(0, currentStudent.getStudentId(), c.getCompanyId(), r.getRoleId(), appDate, deadline, (String) statusCombo.getSelectedItem(), stipendField.getText().trim(), notesField.getText().trim());
                AppDAO.addApplication(app);
                refreshApplications();
                refreshDashboard();
                JOptionPane.showMessageDialog(this, "Application recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding application: " + ex.getMessage());
        }
    }

    private void showUpdateStatusDialog() {
        int appId = getSelectedApplicationId();
        if (appId == -1) return;

        int row = appsTable.getSelectedRow();
        String currentStatus = (String) appsModel.getValueAt(row, 5);
        String company = (String) appsModel.getValueAt(row, 1);
        String role = (String) appsModel.getValueAt(row, 2);

        JComboBox<String> combo = new JComboBox<>(new String[]{"Applied", "Shortlisted", "Interview", "Selected", "Rejected"});
        combo.setSelectedItem(currentStatus);

        JPanel p = new JPanel(new GridLayout(2, 1, 6, 6));
        p.add(new JLabel("Update status for " + company + " (" + role + "):"));
        p.add(combo);

        int result = JOptionPane.showConfirmDialog(this, p, "Update Application Status", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String newStatus = (String) combo.getSelectedItem();
                AppDAO.updateApplicationStatus(appId, newStatus);
                refreshApplications();
                refreshDashboard();
                JOptionPane.showMessageDialog(this, "Status updated to '" + newStatus + "'!", "Updated", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error updating status: " + e.getMessage());
            }
        }
    }

    private void showScheduleInterviewDialog() {
        int appId = getSelectedApplicationId();
        if (appId == -1) return;

        int row = appsTable.getSelectedRow();
        String company = (String) appsModel.getValueAt(row, 1);
        String role = (String) appsModel.getValueAt(row, 2);

        JPanel panel = new JPanel(new GridLayout(6, 2, 8, 8));
        JTextField dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(System.currentTimeMillis() + 86400000L * 3)));
        JComboBox<String> roundCombo = new JComboBox<>(new String[]{"Technical Round 1", "Technical Round 2", "Online Assessment", "Managerial Round", "HR Round"});
        JComboBox<String> modeCombo = new JComboBox<>(new String[]{"Online", "In-Person"});
        JTextField feedbackField = new JTextField("Prepare system design & DSA questions.");

        panel.add(new JLabel("Application:"));
        panel.add(new JLabel(company + " - " + role));
        panel.add(new JLabel("Interview Date & Time:"));
        panel.add(dateField);
        panel.add(new JLabel("Round Type:"));
        panel.add(roundCombo);
        panel.add(new JLabel("Mode:"));
        panel.add(modeCombo);
        panel.add(new JLabel("Preparation Notes:"));
        panel.add(feedbackField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Schedule Interview", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String rawDate = dateField.getText().trim();
                if (!rawDate.contains(":")) rawDate += " 10:00:00";
                Timestamp ts = Timestamp.valueOf(rawDate);

                Interview inv = new Interview(0, appId, ts, (String) roundCombo.getSelectedItem(), (String) modeCombo.getSelectedItem(), "Scheduled", "Pending", feedbackField.getText());
                AppDAO.addInterview(inv);
                AppDAO.updateApplicationStatus(appId, "Interview");
                refreshApplications();
                refreshDashboard();
                JOptionPane.showMessageDialog(this, "Interview scheduled for " + company + "!", "Scheduled", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Date format error. Please use: YYYY-MM-DD HH:MM:SS\n" + e.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showViewInterviewsDialog() {
        try {
            List<Interview> interviews = AppDAO.getInterviews(currentStudent.getStudentId());
            String[] cols = {"ID", "Company", "Role", "Date & Time", "Round", "Mode", "Status", "Outcome"};
            DefaultTableModel m = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
            for (Interview i : interviews) {
                m.addRow(new Object[]{
                    i.getInterviewId(),
                    i.getCompanyName(),
                    i.getRoleTitle(),
                    sdf.format(i.getInterviewDate()),
                    i.getRoundType(),
                    i.getMode(),
                    i.getStatus(),
                    i.getOutcome()
                });
            }
            JTable t = new JTable(m);
            t.setRowHeight(26);
            JScrollPane sp = new JScrollPane(t);
            sp.setPreferredSize(new Dimension(750, 320));
            JOptionPane.showMessageDialog(this, sp, "All Scheduled & Logged Interviews", JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void deleteSelectedApplication() {
        int appId = getSelectedApplicationId();
        if (appId == -1) return;

        int row = appsTable.getSelectedRow();
        String company = (String) appsModel.getValueAt(row, 1);
        String role = (String) appsModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete application for " + company + " (" + role + ")?", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                AppDAO.deleteApplication(appId);
                refreshApplications();
                refreshDashboard();
                JOptionPane.showMessageDialog(this, "Application deleted successfully.");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting application: " + e.getMessage());
            }
        }
    }

    private void showAddCertDialog() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        JTextField nameField = new JTextField();
        JTextField orgField = new JTextField("Oracle / AWS / HackerRank");
        JTextField dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
        JTextField credField = new JTextField();

        panel.add(new JLabel("Certification Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Issuing Organization:"));
        panel.add(orgField);
        panel.add(new JLabel("Issue Date (YYYY-MM-DD):"));
        panel.add(dateField);
        panel.add(new JLabel("Credential ID:"));
        panel.add(credField);

        int res = JOptionPane.showConfirmDialog(this, panel, "Add Certification", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION && !nameField.getText().trim().isEmpty()) {
            try {
                Date d = Date.valueOf(dateField.getText().trim());
                Certification c = new Certification(0, currentStudent.getStudentId(), nameField.getText().trim(), orgField.getText().trim(), d, credField.getText().trim());
                AppDAO.addCertification(c);
                refreshCertsAndProjects();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding certification: " + ex.getMessage());
            }
        }
    }

    private void deleteSelectedCert() {
        int row = certsTable.getSelectedRow();
        if (row == -1) {
            if (certsTable.getRowCount() > 0) {
                certsTable.setRowSelectionInterval(0, 0);
                row = 0;
            } else {
                JOptionPane.showMessageDialog(this, "No certifications to remove.");
                return;
            }
        }
        int certId = (int) certsModel.getValueAt(row, 0);
        String name = (String) certsModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this, "Remove certification '" + name + "'?", "Confirm Removal", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                AppDAO.deleteCertification(certId);
                refreshCertsAndProjects();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void showAddProjectDialog() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        JTextField titleField = new JTextField();
        JTextField descField = new JTextField();
        JTextField techField = new JTextField("Java, Swing, MySQL");
        JTextField urlField = new JTextField("https://github.com/...");

        panel.add(new JLabel("Project Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        panel.add(new JLabel("Technologies Used:"));
        panel.add(techField);
        panel.add(new JLabel("Project Link / Repo URL:"));
        panel.add(urlField);

        int res = JOptionPane.showConfirmDialog(this, panel, "Add Academic Project", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION && !titleField.getText().trim().isEmpty()) {
            try {
                Project p = new Project(0, currentStudent.getStudentId(), titleField.getText().trim(), descField.getText().trim(), techField.getText().trim(), urlField.getText().trim());
                AppDAO.addProject(p);
                refreshCertsAndProjects();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding project: " + ex.getMessage());
            }
        }
    }

    private void deleteSelectedProject() {
        int row = projTable.getSelectedRow();
        if (row == -1) {
            if (projTable.getRowCount() > 0) {
                projTable.setRowSelectionInterval(0, 0);
                row = 0;
            } else {
                JOptionPane.showMessageDialog(this, "No academic projects to remove.");
                return;
            }
        }
        int pid = (int) projectsModel.getValueAt(row, 0);
        String title = (String) projectsModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this, "Remove project '" + title + "'?", "Confirm Removal", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                AppDAO.deleteProject(pid);
                refreshCertsAndProjects();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    // =========================================================================
    // CUSTOM CELL RENDERERS (Clean Status Badges & Colors)
    // =========================================================================

    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setFont(new Font("Segoe UI", Font.BOLD, 12));
            if (!isSelected && value != null) {
                String val = value.toString().toLowerCase();
                if (val.contains("selected")) {
                    c.setForeground(new Color(39, 174, 96));
                } else if (val.contains("interview")) {
                    c.setForeground(new Color(142, 68, 173));
                } else if (val.contains("shortlisted")) {
                    c.setForeground(new Color(243, 156, 18));
                } else if (val.contains("applied")) {
                    c.setForeground(new Color(41, 128, 185));
                } else if (val.contains("rejected")) {
                    c.setForeground(new Color(217, 83, 79));
                } else {
                    c.setForeground(Color.DARK_GRAY);
                }
            }
            return c;
        }
    }
}
