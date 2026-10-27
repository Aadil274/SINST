package com.sinst.ui;

import com.sinst.exception.*;
import com.sinst.model.*;
import com.sinst.service.*;
import com.sinst.util.AppExecutor;
import com.sinst.util.FontManager;
import com.sinst.util.UserSession;
import com.sinst.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.net.URI;

public class MainFrame extends JFrame {

    private final Student currentStudent;
    private final StudentService studentService;
    private final ApplicationService applicationService;
    private final InterviewService interviewService;
    private final SkillService skillService;
    private final CertificationService certificationService;
    private final ProjectService projectService;
    private final SkillGapService skillGapService;
    private final CompanyService companyService;
    private final RoleService roleService;
    private final DashboardService dashboardService;
    private final RecommendationService recommendationService;
    private final ResumeService resumeService;

    private JTabbedPane mainTabbedPane;
    private JLabel welcomeLabel;

    // Status bar widgets
    private JLabel statusLabel;
    private JProgressBar statusBarProgress;

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

    // Company and role catalog management
    private JTable companiesTable;
    private DefaultTableModel companiesModel;
    private JTable rolesTable;
    private DefaultTableModel rolesModel;

    // Skill Gap widgets
    private JComboBox<Role> gapRoleCombo;
    private JProgressBar gapProgressBar;
    private JLabel gapSummaryLabel;
    private DefaultTableModel gapMatchedModel;
    private DefaultTableModel gapMissingModel;
    private JTable gapMissingTable;
    private List<SkillGapResult> currentGapResults;

    // Recommendations widgets
    private JTable recommendationsTable;
    private DefaultTableModel recommendationsModel;
    private JComboBox<String> recFilterCombo;
    private JLabel recDetailHeader;
    private JTextArea recDetailReason;
    private JLabel recDetailVacancy;
    private JLabel recDetailMatching;
    private JLabel recDetailUpgrades;
    private JLabel recDetailMissingMandatory;
    private JLabel recDetailMissingOptional;
    private JButton recApplyBtn;
    private JButton recOpenCareersBtn;
    private List<RoleRecommendation> cachedRecommendations = new ArrayList<>();

    public MainFrame(Student student) {
        this(student,
             new StudentServiceImpl(),
             new ApplicationServiceImpl(),
             new InterviewServiceImpl(),
             new SkillServiceImpl(),
             new CertificationServiceImpl(),
             new ProjectServiceImpl(),
             new SkillGapServiceImpl(),
             new CompanyServiceImpl(),
             new RoleServiceImpl(),
             new DashboardServiceImpl(),
             new RecommendationServiceImpl());
    }

    public MainFrame(Student student,
                     StudentService studentService,
                     ApplicationService applicationService,
                     InterviewService interviewService,
                     SkillService skillService,
                     CertificationService certificationService,
                     ProjectService projectService,
                     SkillGapService skillGapService,
                     CompanyService companyService,
                     RoleService roleService,
                     DashboardService dashboardService) {
        this(student,
             studentService,
             applicationService,
             interviewService,
             skillService,
             certificationService,
             projectService,
             skillGapService,
             companyService,
             roleService,
             dashboardService,
             new RecommendationServiceImpl());
    }

    public MainFrame(Student student,
                     StudentService studentService,
                     ApplicationService applicationService,
                     InterviewService interviewService,
                     SkillService skillService,
                     CertificationService certificationService,
                     ProjectService projectService,
                     SkillGapService skillGapService,
                     CompanyService companyService,
                     RoleService roleService,
                     DashboardService dashboardService,
                     RecommendationService recommendationService) {
        this.currentStudent = student;
        UserSession.setCurrentStudent(student);
        this.studentService = studentService;
        this.applicationService = applicationService;
        this.interviewService = interviewService;
        this.skillService = skillService;
        this.certificationService = certificationService;
        this.projectService = projectService;
        this.skillGapService = skillGapService;
        this.companyService = companyService;
        this.roleService = roleService;
        this.dashboardService = dashboardService;
        this.recommendationService = recommendationService != null ? recommendationService : new RecommendationServiceImpl();
        this.resumeService = new ResumeServiceImpl();

        setTitle("Smart Internship & Skill Tracker (SINST) - " + student.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 720);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);

        initUI();
        FontManager.applyScale(this);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                AppExecutor.cancelAllTasks();
            }
        });
        refreshAllDataAsync();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());

        // 1. Top Student Header Banner
        JPanel topBanner = new JPanel(new BorderLayout());
        topBanner.setBackground(new Color(24, 43, 73));
        topBanner.setBorder(new EmptyBorder(12, 20, 12, 20));

        welcomeLabel = new JLabel();
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        welcomeLabel.setForeground(Color.WHITE);
        updateWelcomeLabelText();

        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topActions.setOpaque(false);

        JButton fontSizeBtn = FontManager.createFontSizeButton(this, new Color(52, 73, 94), Color.WHITE);
        fontSizeBtn.setPreferredSize(new Dimension(115, 32));
        topActions.add(fontSizeBtn);

        JButton makeResumeBtn = createStyledButton("Make Resume", new Color(13, 148, 136), Color.WHITE);
        makeResumeBtn.setPreferredSize(new Dimension(115, 32));
        makeResumeBtn.setToolTipText("Generate an ATS-friendly, professional resume from your verified profile");
        makeResumeBtn.addActionListener(e -> showResumeDialog());
        topActions.add(makeResumeBtn);

        JButton editProfileBtn = createStyledButton("Edit Profile", new Color(41, 128, 185), Color.WHITE);
        editProfileBtn.setPreferredSize(new Dimension(105, 32));
        editProfileBtn.addActionListener(e -> showEditProfileDialog());
        topActions.add(editProfileBtn);

        JButton logoutBtn = createStyledButton("Sign Out", new Color(217, 83, 79), Color.WHITE);
        logoutBtn.setPreferredSize(new Dimension(100, 32));
        logoutBtn.addActionListener(e -> {
            AppExecutor.cancelAllTasks();
            UserSession.clear();
            new LoginFrame().setVisible(true);
            dispose();
        });
        topActions.add(logoutBtn);

        topBanner.add(welcomeLabel, BorderLayout.CENTER);
        topBanner.add(topActions, BorderLayout.EAST);
        root.add(topBanner, BorderLayout.NORTH);

        // 2. Main Tabbed Interface (No broken emoji glyphs)
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        mainTabbedPane.addTab("Dashboard", createDashboardTab());
        mainTabbedPane.addTab("Applications & Interviews", createApplicationsTab());
        mainTabbedPane.addTab("Skills & Profile", createSkillsProfileTab());
        mainTabbedPane.addTab("Skill-Gap Analyzer", createSkillGapTab());
        mainTabbedPane.addTab("Role Recommendations", createRecommendationsTab());
        mainTabbedPane.addTab("Companies & Roles", createCatalogsTab());

        // Immediately refresh views on tab change asynchronously to prevent UI freeze and stale data
        mainTabbedPane.addChangeListener(e -> {
            int selected = mainTabbedPane.getSelectedIndex();
            switch (selected) {
                case 0:
                    refreshDashboardAsync();
                    break;
                case 1:
                    refreshApplicationsAsync();
                    break;
                case 2:
                    refreshSkillsAsync();
                    refreshCertsAndProjectsAsync();
                    break;
                case 3:
                    performSkillGapAnalysisAsync();
                    break;
                case 4:
                    refreshRecommendationsAsync();
                    break;
                case 5:
                    refreshCatalogsAsync();
                    break;
            }
        });

        root.add(mainTabbedPane, BorderLayout.CENTER);

        // 3. Subtle Status Bar at South
        JPanel statusBar = new JPanel(new BorderLayout(8, 0));
        statusBar.setBackground(new Color(240, 243, 246));
        statusBar.setBorder(new EmptyBorder(4, 14, 4, 14));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 110, 120));

        statusBarProgress = new JProgressBar();
        statusBarProgress.setIndeterminate(true);
        statusBarProgress.setPreferredSize(new Dimension(100, 14));
        statusBarProgress.setVisible(false);

        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(statusBarProgress, BorderLayout.EAST);
        root.add(statusBar, BorderLayout.SOUTH);

        add(root);
    }

    private void setBusy(boolean busy, String message) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> setBusy(busy, message));
            return;
        }
        if (statusLabel != null) {
            statusLabel.setText(message != null ? message : (busy ? "Working..." : "Ready"));
        }
        if (statusBarProgress != null) {
            statusBarProgress.setVisible(busy);
        }
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    private void updateWelcomeLabelText() {
        if (welcomeLabel != null) {
            String dept = currentStudent.getDepartment() != null ? currentStudent.getDepartment() : "CSE";
            String email = currentStudent.getEmail() != null ? currentStudent.getEmail() : "";
            welcomeLabel.setText("<html><b style='color: #ffffff; font-size: 13px;'>" + escapeHtml(currentStudent.getFullName()) + "</b>" +
                                 " &nbsp;|&nbsp; <span style='color: #93c5fd;'>" + escapeHtml(currentStudent.getRollNumber()) + "</span>" +
                                 "<br/><span style='font-size: 11px; color: #cbd5e1; font-weight: normal;'>" +
                                 escapeHtml(dept) + " &bull; Year " + currentStudent.getYearOfStudy() +
                                 (!email.isEmpty() ? " &bull; " + escapeHtml(email) : "") +
                                 "</span></html>");
        }
        setTitle("Smart Internship & Skill Tracker (SINST) - " + currentStudent.getFullName());
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
            performSkillGapAnalysisAsync();
        });
        quickBar.add(runGapBtn);

        JButton recBtn = createStyledButton("Role Recommendations", new Color(13, 148, 136), Color.WHITE);
        recBtn.addActionListener(e -> {
            mainTabbedPane.setSelectedIndex(4);
            refreshRecommendationsAsync();
        });
        quickBar.add(recBtn);

        JButton resumeQuickBtn = createStyledButton("Make Resume", new Color(30, 64, 175), Color.WHITE);
        resumeQuickBtn.addActionListener(e -> showResumeDialog());
        quickBar.add(resumeQuickBtn);

        JButton refreshBtn = new JButton("Refresh Dashboard");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.addActionListener(e -> refreshDashboardAsync());
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

        // Top Controls: Filter & Actions (2-tier layout prevents button clipping / horizontal overlap)
        JPanel topControls = new JPanel(new BorderLayout(0, 6));
        topControls.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        filterPanel.setOpaque(false);
        filterPanel.add(new JLabel("Filter by Status:"));
        appFilterCombo = new JComboBox<>(new String[]{"All", "Applied", "Shortlisted", "Interview", "Selected", "Rejected"});
        appFilterCombo.addActionListener(e -> filterApplicationsTable());
        filterPanel.add(appFilterCombo);
        topControls.add(filterPanel, BorderLayout.NORTH);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        actionBtns.setOpaque(false);

        JButton addAppBtn = createStyledButton("+ New Application", new Color(24, 100, 180), Color.WHITE);
        addAppBtn.addActionListener(e -> showAddApplicationDialog());

        JButton updateStatusBtn = new JButton("Update Status");
        updateStatusBtn.addActionListener(e -> showUpdateStatusDialog());

        JButton editAppBtn = new JButton("Edit Application");
        editAppBtn.addActionListener(e -> showEditApplicationDialog());

        JButton scheduleIntBtn = new JButton("Schedule Interview");
        scheduleIntBtn.addActionListener(e -> showScheduleInterviewDialog());

        JButton viewIntsBtn = new JButton("View Interviews");
        viewIntsBtn.addActionListener(e -> showViewInterviewsDialog());

        JButton deleteAppBtn = createStyledButton("Delete", new Color(217, 83, 79), Color.WHITE);
        deleteAppBtn.addActionListener(e -> deleteSelectedApplication());

        actionBtns.add(addAppBtn);
        actionBtns.add(updateStatusBtn);
        actionBtns.add(editAppBtn);
        actionBtns.add(scheduleIntBtn);
        actionBtns.add(viewIntsBtn);
        actionBtns.add(deleteAppBtn);
        topControls.add(actionBtns, BorderLayout.CENTER);

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

        // Double click to edit application details
        appsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showEditApplicationDialog();
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

        // Add Skill Bar - 2 structured rows so selectors and buttons never overlap or clip
        JPanel addSkillBar = new JPanel(new GridLayout(2, 1, 4, 4));
        addSkillBar.setOpaque(false);
        addSkillBar.setBorder(new EmptyBorder(4, 4, 4, 4));

        masterSkillsCombo = new JComboBox<>();
        proficiencyCombo = new JComboBox<>(new String[]{"Beginner", "Intermediate", "Advanced", "Expert"});
        proficiencyCombo.setSelectedItem("Intermediate");

        JButton addSkillBtn = createStyledButton("+ Add / Update Skill", new Color(39, 174, 96), Color.WHITE);
        addSkillBtn.addActionListener(e -> addStudentSkill());

        JButton deleteSkillBtn = new JButton("Remove");
        deleteSkillBtn.addActionListener(e -> removeStudentSkill());

        JPanel skillRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        skillRow1.setOpaque(false);
        skillRow1.add(new JLabel("Skill:"));
        skillRow1.add(masterSkillsCombo);
        skillRow1.add(new JLabel("Level:"));
        skillRow1.add(proficiencyCombo);

        JPanel skillRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        skillRow2.setOpaque(false);
        skillRow2.add(addSkillBtn);
        skillRow2.add(deleteSkillBtn);

        addSkillBar.add(skillRow1);
        addSkillBar.add(skillRow2);
        skillsBox.add(addSkillBar, BorderLayout.NORTH);

        studentSkillsModel = new DefaultTableModel(new String[]{"ID", "Skill Name", "Category", "Proficiency Level"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        studentSkillsTable = new JTable(studentSkillsModel);
        studentSkillsTable.setRowHeight(28);
        studentSkillsTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        skillsBox.add(new JScrollPane(studentSkillsTable), BorderLayout.CENTER);

        JPanel skillBottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        skillBottomBar.setOpaque(false);

        JButton makeResumeFromSkillsBtn = createStyledButton("Make Resume", new Color(13, 148, 136), Color.WHITE);
        makeResumeFromSkillsBtn.addActionListener(e -> showResumeDialog());
        skillBottomBar.add(makeResumeFromSkillsBtn);

        JButton viewSkillEvidenceBtn = new JButton("View Skill Evidence");
        viewSkillEvidenceBtn.addActionListener(e -> showSkillEvidenceDialog());
        skillBottomBar.add(viewSkillEvidenceBtn);
        skillsBox.add(skillBottomBar, BorderLayout.SOUTH);

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

        JPanel certBtns = new JPanel(new GridLayout(2, 1, 4, 4));
        certBtns.setOpaque(false);
        certBtns.setBorder(new EmptyBorder(4, 4, 4, 4));

        JButton addCertBtn = createStyledButton("+ Add Certification", new Color(24, 100, 180), Color.WHITE);
        addCertBtn.addActionListener(e -> showAddCertDialog());
        JButton viewSkillsBtn = new JButton("View Skills & Evidence");
        viewSkillsBtn.addActionListener(e -> showViewCertSkillsDialog());
        JButton editCertBtn = new JButton("Edit Selected");
        editCertBtn.addActionListener(e -> showEditCertDialog());
        JButton delCertBtn = new JButton("Remove");
        delCertBtn.addActionListener(e -> deleteSelectedCert());

        JPanel certRow1 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        certRow1.setOpaque(false);
        certRow1.add(addCertBtn);
        certRow1.add(viewSkillsBtn);

        JPanel certRow2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        certRow2.setOpaque(false);
        certRow2.add(editCertBtn);
        certRow2.add(delCertBtn);

        certBtns.add(certRow1);
        certBtns.add(certRow2);
        certsPanel.add(certBtns, BorderLayout.SOUTH);
        bioTabs.addTab("Certifications", certsPanel);

        // Sub-Tab: Projects
        JPanel projPanel = new JPanel(new BorderLayout(5, 5));
        projPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        projectsModel = new DefaultTableModel(new String[]{"ID", "Project Title", "Tech Stack", "Project Link"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        projTable = new JTable(projectsModel);
        projTable.setRowHeight(26);
        projTable.getColumnModel().getColumn(3).setCellRenderer(new HyperlinkRenderer());
        projTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = projTable.rowAtPoint(e.getPoint());
                int col = projTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 3) {
                    Object val = projectsModel.getValueAt(row, 3);
                    if (val != null) {
                        openWebUrl(MainFrame.this, val.toString());
                    }
                }
            }
        });
        projTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = projTable.columnAtPoint(e.getPoint());
                projTable.setCursor(col == 3 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }
        });
        projPanel.add(new JScrollPane(projTable), BorderLayout.CENTER);

        JPanel projBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        JButton addProjBtn = createStyledButton("+ Add Project", new Color(24, 100, 180), Color.WHITE);
        addProjBtn.addActionListener(e -> showAddProjectDialog());
        JButton editProjBtn = new JButton("Edit Selected");
        editProjBtn.addActionListener(e -> showEditProjectDialog());
        JButton openProjLinkBtn = new JButton("Open Link");
        openProjLinkBtn.addActionListener(e -> {
            int row = projTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(MainFrame.this, "Please select an academic project first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Object val = projectsModel.getValueAt(row, 3);
            openWebUrl(MainFrame.this, val != null ? val.toString() : "");
        });
        JButton delProjBtn = new JButton("Remove");
        delProjBtn.addActionListener(e -> deleteSelectedProject());
        projBtns.add(addProjBtn);
        projBtns.add(editProjBtn);
        projBtns.add(openProjLinkBtn);
        projBtns.add(delProjBtn);
        projPanel.add(projBtns, BorderLayout.SOUTH);
        bioTabs.addTab("Academic Projects", projPanel);
        projBtns.add(addProjBtn);
        projBtns.add(editProjBtn);
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
        gapRoleCombo.addActionListener(e -> performSkillGapAnalysisAsync());
        selectBox.add(gapRoleCombo);

        JButton analyzeBtn = createStyledButton("Run Skill-Gap Analysis", new Color(24, 100, 180), Color.WHITE);
        analyzeBtn.addActionListener(e -> performSkillGapAnalysisAsync());
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
    // TAB 5: ROLE RECOMMENDATIONS
    // =========================================================================

    private JPanel createRecommendationsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        panel.setBackground(new Color(245, 247, 250));

        // 1. Top Section: Banner + Notice + Filter Bar
        JPanel topContainer = new JPanel(new BorderLayout(6, 6));
        topContainer.setOpaque(false);

        // Header Title
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 2, 2));
        titlePanel.setOpaque(false);
        JLabel titleLbl = new JLabel("Internship Role & Company Recommendations");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(new Color(30, 41, 59));

        JLabel subLbl = new JLabel("Personalized role recommendations evaluated from your demonstrated skills, certifications, and required competencies.");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(new Color(100, 116, 139));
        titlePanel.add(titleLbl);
        titlePanel.add(subLbl);
        topContainer.add(titlePanel, BorderLayout.NORTH);

        // Critical Distinction Notice Box
        JPanel noticePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        noticePanel.setBackground(new Color(254, 243, 199));
        noticePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(245, 158, 11), 1),
            new EmptyBorder(4, 8, 4, 8)
        ));
        JLabel noticeLbl = new JLabel("<html><b>Potential Match Notice:</b> Recommendations reflect curricular skill alignment against company hiring profiles. They represent potential role matches and do not guarantee active vacancies or employment. Check official company careers portals for active job listings.</html>");
        noticeLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        noticeLbl.setForeground(new Color(146, 64, 14));
        noticePanel.add(noticeLbl);
        topContainer.add(noticePanel, BorderLayout.CENTER);

        // Control / Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        filterBar.setBackground(Color.WHITE);
        filterBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 230, 238), 1),
            new EmptyBorder(4, 8, 4, 8)
        ));

        filterBar.add(new JLabel("Filter by Alignment:"));
        recFilterCombo = new JComboBox<>(new String[]{
            "All Recommendations",
            "High Readiness (>=80%)",
            "Moderate Readiness (>=50%)",
            "Not Yet Applied"
        });
        recFilterCombo.addActionListener(e -> filterAndDisplayRecommendations());
        filterBar.add(recFilterCombo);

        JButton refreshRecBtn = createStyledButton("Refresh Recommendations", new Color(13, 148, 136), Color.WHITE);
        refreshRecBtn.addActionListener(e -> refreshRecommendations());
        filterBar.add(refreshRecBtn);

        JButton weightsBtn = new JButton("View Match Formula");
        weightsBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        weightsBtn.addActionListener(e -> showMatchFormulaDialog());
        filterBar.add(weightsBtn);

        topContainer.add(filterBar, BorderLayout.SOUTH);
        panel.add(topContainer, BorderLayout.NORTH);

        // 2. Center Table Section
        recommendationsModel = new DefaultTableModel(new String[]{
            "Rank", "Match Score", "Company", "Target Role", "Vacancy Status", "Application Status", "Missing Mandatory Skills", "Official Careers Portal"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        recommendationsTable = new JTable(recommendationsModel);
        recommendationsTable.setRowHeight(28);
        recommendationsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        recommendationsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        recommendationsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        recommendationsTable.getColumnModel().getColumn(1).setPreferredWidth(95);
        recommendationsTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        recommendationsTable.getColumnModel().getColumn(3).setPreferredWidth(190);
        recommendationsTable.getColumnModel().getColumn(4).setPreferredWidth(140);
        recommendationsTable.getColumnModel().getColumn(5).setPreferredWidth(130);
        recommendationsTable.getColumnModel().getColumn(6).setPreferredWidth(180);
        recommendationsTable.getColumnModel().getColumn(7).setPreferredWidth(160);

        // Custom Renderer for Match Score
        recommendationsTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                if (!isSelected) {
                    String str = String.valueOf(value).replace("%", "").trim();
                    try {
                        int score = Integer.parseInt(str);
                        if (score >= 80) {
                            lbl.setForeground(new Color(39, 174, 96));
                        } else if (score >= 50) {
                            lbl.setForeground(new Color(230, 126, 34));
                        } else {
                            lbl.setForeground(new Color(127, 140, 141));
                        }
                    } catch (Exception ignored) {
                        lbl.setForeground(Color.DARK_GRAY);
                    }
                }
                return lbl;
            }
        });

        // Custom Renderer for Application Status
        recommendationsTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                if (!isSelected) {
                    String str = String.valueOf(value);
                    if (str.startsWith("Applied")) {
                        lbl.setForeground(new Color(24, 100, 180));
                    } else {
                        lbl.setForeground(new Color(100, 110, 120));
                    }
                }
                return lbl;
            }
        });

        // Clickable Official Careers Portal Link Renderer & Hand Cursor Hover
        recommendationsTable.getColumnModel().getColumn(7).setCellRenderer(new HyperlinkRenderer());
        recommendationsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = recommendationsTable.rowAtPoint(e.getPoint());
                int col = recommendationsTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 7) {
                    Object val = recommendationsModel.getValueAt(row, 7);
                    if (val != null) {
                        openWebUrl(MainFrame.this, val.toString());
                    }
                }
            }
        });
        recommendationsTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = recommendationsTable.columnAtPoint(e.getPoint());
                recommendationsTable.setCursor(col == 7 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }
        });

        recommendationsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = recommendationsTable.getSelectedRow();
                if (row >= 0) {
                    updateRecommendationDetail(row);
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(recommendationsTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 238), 1));

        // 3. Bottom Detail Panel
        JPanel detailContainer = new JPanel(new BorderLayout(8, 8));
        detailContainer.setBackground(Color.WHITE);
        detailContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Recommendation Analysis & Competency Breakdown", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13)),
            new EmptyBorder(8, 10, 8, 10)
        ));

        // Top info inside detail panel
        JPanel detailTop = new JPanel(new BorderLayout(6, 4));
        detailTop.setOpaque(false);
        recDetailHeader = new JLabel("Select a recommended role above to inspect competency alignment.");
        recDetailHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        recDetailHeader.setForeground(new Color(30, 41, 59));
        detailTop.add(recDetailHeader, BorderLayout.NORTH);

        recDetailReason = new JTextArea(2, 60);
        recDetailReason.setEditable(false);
        recDetailReason.setLineWrap(true);
        recDetailReason.setWrapStyleWord(true);
        recDetailReason.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        recDetailReason.setBackground(new Color(248, 250, 252));
        recDetailReason.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            new EmptyBorder(6, 8, 6, 8)
        ));
        detailTop.add(recDetailReason, BorderLayout.CENTER);

        recDetailVacancy = new JLabel("");
        recDetailVacancy.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        recDetailVacancy.setForeground(new Color(100, 116, 139));
        detailTop.add(recDetailVacancy, BorderLayout.SOUTH);
        detailContainer.add(detailTop, BorderLayout.NORTH);

        // Center Grid inside detail panel: Competencies breakdown
        JPanel compGrid = new JPanel(new GridLayout(2, 2, 8, 6));
        compGrid.setOpaque(false);
        compGrid.setBorder(new EmptyBorder(6, 0, 6, 0));

        compGrid.add(recDetailMatching = new JLabel("Satisfied Skills: -"));
        recDetailMatching.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        recDetailMatching.setForeground(new Color(22, 101, 52));

        compGrid.add(recDetailUpgrades = new JLabel("Upgrades Needed: -"));
        recDetailUpgrades.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        recDetailUpgrades.setForeground(new Color(194, 65, 12));

        compGrid.add(recDetailMissingMandatory = new JLabel("Missing Mandatory: -"));
        recDetailMissingMandatory.setFont(new Font("Segoe UI", Font.BOLD, 12));
        recDetailMissingMandatory.setForeground(new Color(185, 28, 28));

        compGrid.add(recDetailMissingOptional = new JLabel("Missing Optional: -"));
        recDetailMissingOptional.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        recDetailMissingOptional.setForeground(new Color(100, 116, 139));

        detailContainer.add(compGrid, BorderLayout.CENTER);

        // Bottom Actions inside detail panel
        JPanel detailActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        detailActions.setOpaque(false);

        recApplyBtn = createStyledButton("+ Record Application for this Role", new Color(24, 100, 180), Color.WHITE);
        recApplyBtn.addActionListener(e -> applyToSelectedRecommendation());

        recOpenCareersBtn = new JButton("Open Official Careers Portal");
        recOpenCareersBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        recOpenCareersBtn.addActionListener(e -> openCareersPortalForSelected());

        JButton viewGapBtn = new JButton("Detailed Skill-Gap Analysis");
        viewGapBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewGapBtn.addActionListener(e -> jumpToSkillGapForSelected());

        detailActions.add(viewGapBtn);
        detailActions.add(recOpenCareersBtn);
        detailActions.add(recApplyBtn);
        detailContainer.add(detailActions, BorderLayout.SOUTH);

        // Split pane holding table (top) and detail (bottom)
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, detailContainer);
        splitPane.setResizeWeight(0.55);
        splitPane.setDividerSize(6);
        splitPane.setBorder(null);

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    public void refreshRecommendationsAsync() {
        if (recommendationsModel == null) return;
        setBusy(true, "Calculating role recommendations...");
        AppExecutor.runWorker(
            "refresh-recommendations",
            () -> recommendationService.getRecommendations(currentStudent.getStudentId(), skillGapService.getConfig()),
            this,
            recs -> {
                setBusy(false, "Ready");
                cachedRecommendations = recs;
                filterAndDisplayRecommendations();
            },
            error -> {
                setBusy(false, "Ready");
                JOptionPane.showMessageDialog(this, "Error generating recommendations: " + error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        );
    }

    private void refreshRecommendations() {
        if (recommendationsModel == null) return;
        try {
            cachedRecommendations = recommendationService.getRecommendations(currentStudent.getStudentId(), skillGapService.getConfig());
            filterAndDisplayRecommendations();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating recommendations: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterAndDisplayRecommendations() {
        if (recommendationsModel == null) return;
        recommendationsModel.setRowCount(0);
        if (cachedRecommendations == null) return;

        String filter = recFilterCombo != null ? (String) recFilterCombo.getSelectedItem() : "All Recommendations";
        int rank = 1;

        for (RoleRecommendation rec : cachedRecommendations) {
            if ("High Readiness (>=80%)".equals(filter) && rec.getMatchPercentage() < 80) {
                continue;
            }
            if ("Moderate Readiness (>=50%)".equals(filter) && rec.getMatchPercentage() < 50) {
                continue;
            }
            if ("Not Yet Applied".equals(filter) && rec.isHasApplied()) {
                continue;
            }

            String appStateStr = rec.isHasApplied() ? "Applied [" + rec.getApplicationStatus() + "]" : "Not Applied";
            recommendationsModel.addRow(new Object[]{
                rank++,
                rec.getMatchPercentage() + "%",
                rec.getCompanyName(),
                rec.getRoleTitle(),
                rec.getVacancyStatus(),
                appStateStr,
                rec.getMissingMandatoryNames(),
                rec.getEffectiveCareersUrl()
            });
        }

        if (recommendationsTable != null && recommendationsTable.getRowCount() > 0) {
            recommendationsTable.setRowSelectionInterval(0, 0);
            updateRecommendationDetail(0);
        } else {
            clearRecommendationDetail();
        }
    }

    private RoleRecommendation getSelectedRecommendation() {
        if (recommendationsTable == null) return null;
        int row = recommendationsTable.getSelectedRow();
        if (row < 0 || cachedRecommendations == null || cachedRecommendations.isEmpty()) return null;

        String compName = (String) recommendationsModel.getValueAt(row, 2);
        String roleTitle = (String) recommendationsModel.getValueAt(row, 3);

        for (RoleRecommendation rec : cachedRecommendations) {
            if (rec.getCompanyName().equalsIgnoreCase(compName) && rec.getRoleTitle().equalsIgnoreCase(roleTitle)) {
                return rec;
            }
        }
        return null;
    }

    private void updateRecommendationDetail(int row) {
        RoleRecommendation rec = getSelectedRecommendation();
        if (rec == null) {
            clearRecommendationDetail();
            return;
        }

        recDetailHeader.setText(rec.getCompanyName() + " — " + rec.getRoleTitle() + " (" + rec.getMatchPercentage() + "% Match Score)");
        recDetailReason.setText(rec.getRecommendationReason());
        recDetailVacancy.setText("Status: " + rec.getVacancyStatus() + 
                                "  |  Source: " + rec.getVacancySource() + 
                                "  |  Info Source: " + rec.getInformationSource() +
                                "  |  Last Verified: " + (rec.getLastVerified() != null ? rec.getLastVerified().toString() : "N/A"));

        recDetailMatching.setText("Satisfied Skills (" + rec.getMatchingSkills().size() + "): " + rec.getMatchingSkillNames());
        String upgrades = rec.getPartiallyMatchedSkills().isEmpty() ? "None" : rec.getPartiallyMatchedNames();
        recDetailUpgrades.setText("Upgrades Needed (" + rec.getPartiallyMatchedSkills().size() + "): " + upgrades);
        recDetailMissingMandatory.setText("Missing Mandatory (" + rec.getMissingMandatorySkills().size() + "): " + rec.getMissingMandatoryNames());
        recDetailMissingOptional.setText("Missing Optional (" + rec.getMissingOptionalSkills().size() + "): " + rec.getMissingOptionalNames());

        if (rec.isHasApplied()) {
            recApplyBtn.setText("Already Applied (" + rec.getApplicationStatus() + ")");
            recApplyBtn.setEnabled(false);
        } else {
            recApplyBtn.setText("+ Record Application for " + rec.getRoleTitle());
            recApplyBtn.setEnabled(true);
        }

        String careersUrl = rec.getEffectiveCareersUrl();
        if (careersUrl != null && !careersUrl.isEmpty()) {
            recOpenCareersBtn.setText("Open Careers Portal (" + rec.getCompanyName() + ")");
            recOpenCareersBtn.setEnabled(true);
        } else {
            recOpenCareersBtn.setText("No Careers Portal URL");
            recOpenCareersBtn.setEnabled(false);
        }
    }

    private void clearRecommendationDetail() {
        if (recDetailHeader != null) recDetailHeader.setText("No recommendations available for the selected filter.");
        if (recDetailReason != null) recDetailReason.setText("");
        if (recDetailVacancy != null) recDetailVacancy.setText("");
        if (recDetailMatching != null) recDetailMatching.setText("Satisfied Skills: -");
        if (recDetailUpgrades != null) recDetailUpgrades.setText("Upgrades Needed: -");
        if (recDetailMissingMandatory != null) recDetailMissingMandatory.setText("Missing Mandatory: -");
        if (recDetailMissingOptional != null) recDetailMissingOptional.setText("Missing Optional: -");
        if (recApplyBtn != null) recApplyBtn.setEnabled(false);
        if (recOpenCareersBtn != null) recOpenCareersBtn.setEnabled(false);
    }

    private void applyToSelectedRecommendation() {
        RoleRecommendation rec = getSelectedRecommendation();
        if (rec != null) {
            showAddApplicationDialog(rec.getCompanyId(), rec.getRoleId());
        }
    }

    private void openCareersPortalForSelected() {
        RoleRecommendation rec = getSelectedRecommendation();
        if (rec == null) return;
        String url = rec.getEffectiveCareersUrl();
        openWebUrl(this, url);
    }

    private void jumpToSkillGapForSelected() {
        RoleRecommendation rec = getSelectedRecommendation();
        if (rec == null) return;
        mainTabbedPane.setSelectedIndex(3); // Switch to Skill Gap tab
        if (gapRoleCombo != null) {
            for (int i = 0; i < gapRoleCombo.getItemCount(); i++) {
                Role r = gapRoleCombo.getItemAt(i);
                if (r != null && r.getRoleId() == rec.getRoleId()) {
                    gapRoleCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void showMatchFormulaDialog() {
        String msg = "<html><body style='width: 420px; font-family: Segoe UI;'>" +
            "<h3>SINST Recommendation & Alignment Formula</h3>" +
            "<p>Recommendations calculate curricular qualification match using the documented transparent formula:</p>" +
            "<p style='background-color: #f1f5f9; padding: 6px; font-family: Consolas;'><b>Match Score</b> = (&Sigma; (Skill Match Value &times; Weight) / &Sigma; Weights) &times; 100</p>" +
            "<ul>" +
            "<li><b>Mandatory Skills:</b> 2.0x weight (essential core competence)</li>" +
            "<li><b>Optional Skills:</b> 1.0x weight (supplementary competency)</li>" +
            "<li><b>Full Credit (1.0):</b> Demonstrated proficiency &ge; required benchmark</li>" +
            "<li><b>Partial Credit:</b> Demonstrated Level / Required Level</li>" +
            "<li><b>Missing Skill:</b> 0.0 credit</li>" +
            "</ul>" +
            "<p><b>Ranking Priority:</b> Match Score (Desc) &rarr; Missing Mandatory Count (Asc) &rarr; Verified Evidence Count (Desc).</p>" +
            "<p style='color: #b45309;'><i>Critical Distinction: SINST labels results as Potential Role Matches based on company hiring profiles and does not fabricate active job postings or hiring probabilities.</i></p>" +
            "</body></html>";
        JOptionPane.showMessageDialog(this, msg, "Explainable Match Formula", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createCatalogsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel help = new JLabel("Manage the company and internship role catalogs used when recording applications.");
        help.setBorder(new EmptyBorder(0, 4, 4, 4));
        panel.add(help, BorderLayout.NORTH);

        JTabbedPane catalogs = new JTabbedPane();
        companiesModel = new DefaultTableModel(new String[]{"ID", "Company", "Industry", "Location", "Website", "Contact Email"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        companiesTable = new JTable(companiesModel);
        companiesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        companiesTable.getColumnModel().getColumn(4).setCellRenderer(new HyperlinkRenderer());
        companiesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = companiesTable.rowAtPoint(e.getPoint());
                int col = companiesTable.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 4) {
                    Object val = companiesModel.getValueAt(row, 4);
                    if (val != null) {
                        openWebUrl(MainFrame.this, val.toString());
                    }
                }
            }
        });
        companiesTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = companiesTable.columnAtPoint(e.getPoint());
                companiesTable.setCursor(col == 4 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }
        });
        catalogs.addTab("Companies", createCatalogPanel(companiesTable, true));

        rolesModel = new DefaultTableModel(new String[]{"ID", "Role", "Experience", "Description"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        rolesTable = new JTable(rolesModel);
        rolesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        catalogs.addTab("Roles", createCatalogPanel(rolesTable, false));

        panel.add(catalogs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCatalogPanel(JTable table, boolean companyCatalog) {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton add = createStyledButton("Add", new Color(39, 174, 96), Color.WHITE);
        JButton edit = createStyledButton("Edit Selected", new Color(24, 100, 180), Color.WHITE);
        JButton openWebBtn = new JButton("Open Website");
        openWebBtn.addActionListener(e -> {
            int row = companiesTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(MainFrame.this, "Please select a company first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Object val = companiesModel.getValueAt(row, 4);
            openWebUrl(MainFrame.this, val != null ? val.toString() : "");
        });
        JButton remove = new JButton("Remove Selected");
        add.addActionListener(e -> { if (companyCatalog) showCompanyEditor(null); else showRoleEditor(null); });
        edit.addActionListener(e -> editCatalogSelection(companyCatalog));
        remove.addActionListener(e -> deleteCatalogSelection(companyCatalog));
        buttons.add(add);
        buttons.add(edit);
        if (companyCatalog) {
            buttons.add(openWebBtn);
        }
        buttons.add(remove);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void editCatalogSelection(boolean companyCatalog) {
        JTable table = companyCatalog ? companiesTable : rolesTable;
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a record first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int id = ((Number) table.getValueAt(row, 0)).intValue();
        setBusy(true, "Loading catalog record...");
        AppExecutor.runWorker(
            "load-catalog-item",
            () -> {
                if (companyCatalog) {
                    for (Company c : companyService.getAllCompanies()) {
                        if (c.getCompanyId() == id) return c;
                    }
                } else {
                    for (Role role : roleService.getAllRoles()) {
                        if (role.getRoleId() == id) return role;
                    }
                }
                return null;
            },
            this,
            item -> {
                setBusy(false, "Ready");
                if (item instanceof Company) {
                    showCompanyEditor((Company) item);
                } else if (item instanceof Role) {
                    showRoleEditor((Role) item);
                }
            },
            ex -> {
                setBusy(false, "Ready");
                showCatalogError(ex.getMessage());
            }
        );
    }

    private void showCompanyEditor(Company current) {
        JTextField name = new JTextField(current == null ? "" : current.getCompanyName());
        JTextField industry = new JTextField(current == null ? "" : nullToEmpty(current.getIndustry()));
        JTextField location = new JTextField(current == null ? "" : nullToEmpty(current.getLocation()));
        JTextField website = new JTextField(current == null ? "" : nullToEmpty(current.getWebsite()));
        JTextField email = new JTextField(current == null ? "" : nullToEmpty(current.getContactEmail()));
        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("Company name *")); form.add(name);
        form.add(new JLabel("Industry")); form.add(industry);
        form.add(new JLabel("Location")); form.add(location);
        form.add(new JLabel("Website")); form.add(website);
        form.add(new JLabel("Contact email")); form.add(email);
        if (JOptionPane.showConfirmDialog(this, form, current == null ? "Add Company" : "Edit Company", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        Company value = new Company(current == null ? 0 : current.getCompanyId(), name.getText().trim(), industry.getText().trim(), location.getText().trim(), website.getText().trim(), email.getText().trim());

        setBusy(true, "Saving company...");
        AppExecutor.runWorker(
            "save-company",
            () -> {
                companyService.saveCompany(value);
                return null;
            },
            this,
            res -> {
                setBusy(false, "Company saved");
                refreshCatalogsAsync();
            },
            ex -> {
                setBusy(false, "Ready");
                showCatalogError(ex.getMessage());
            }
        );
    }

    private void showRoleEditor(Role current) {
        setBusy(true, "Loading role skills...");
        AppExecutor.runWorker(
            "load-role-skills",
            () -> {
                List<Skill> skills = skillService.getAllSkills();
                List<Integer> selectedIds = (current != null) ? roleService.getRoleSkillIds(current.getRoleId()) : new ArrayList<>();
                return new RoleEditorData(skills, selectedIds);
            },
            this,
            data -> {
                setBusy(false, "Ready");
                openRoleEditorDialog(current, data.skills, data.selectedIds);
            },
            ex -> {
                setBusy(false, "Ready");
                showCatalogError(ex.getMessage());
            }
        );
    }

    private void openRoleEditorDialog(Role current, List<Skill> skills, List<Integer> selectedIds) {
        JTextField title = new JTextField(current == null ? "" : current.getRoleTitle());
        JComboBox<String> level = new JComboBox<>(new String[]{"Intern", "Entry Level", "Intermediate", "Experienced"});
        if (current != null) level.setSelectedItem(current.getExperienceLevel());
        JTextArea description = new JTextArea(current == null ? "" : nullToEmpty(current.getDescription()), 5, 28);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        JList<Skill> skillList = new JList<>(skills.toArray(new Skill[0]));
        skillList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        skillList.setVisibleRowCount(7);
        for (int i = 0; i < skills.size(); i++) {
            if (selectedIds.contains(skills.get(i).getSkillId())) {
                skillList.addSelectionInterval(i, i);
            }
        }
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Role title *")); form.add(title);
        form.add(new JLabel("Experience level")); form.add(level);
        form.add(new JLabel("Description")); form.add(new JScrollPane(description));
        form.add(new JLabel("Required skills (Ctrl/Shift-click)")); form.add(new JScrollPane(skillList));
        if (JOptionPane.showConfirmDialog(this, form, current == null ? "Add Role" : "Edit Role", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        Role value = new Role(current == null ? 0 : current.getRoleId(), title.getText().trim(), description.getText().trim(), (String) level.getSelectedItem());
        List<Integer> requiredSkillIds = new ArrayList<>();
        for (Skill skill : skillList.getSelectedValuesList()) {
            requiredSkillIds.add(skill.getSkillId());
        }

        setBusy(true, "Saving role...");
        AppExecutor.runWorker(
            "save-role",
            () -> {
                roleService.saveRole(value, requiredSkillIds);
                return null;
            },
            this,
            res -> {
                setBusy(false, "Role saved");
                refreshCatalogsAsync();
            },
            ex -> {
                setBusy(false, "Ready");
                showCatalogError(ex.getMessage());
            }
        );
    }

    private void deleteCatalogSelection(boolean companyCatalog) {
        JTable table = companyCatalog ? companiesTable : rolesTable;
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a record first.", "No Selection", JOptionPane.INFORMATION_MESSAGE); return; }
        int id = ((Number) table.getValueAt(row, 0)).intValue();
        String label = String.valueOf(table.getValueAt(row, 1));
        if (JOptionPane.showConfirmDialog(this, "Remove '" + label + "' from the catalog? Records used by applications cannot be removed.", "Confirm Removal", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        setBusy(true, "Removing from catalog...");
        AppExecutor.runWorker(
            "delete-catalog",
            () -> {
                if (companyCatalog) companyService.deleteCompany(id); else roleService.deleteRole(id);
                return null;
            },
            this,
            res -> {
                setBusy(false, "Ready");
                refreshCatalogsAsync();
            },
            ex -> {
                setBusy(false, "Ready");
                showCatalogError(ex.getMessage());
            }
        );
    }

    private String nullToEmpty(String value) { return value == null ? "" : value; }

    private void showCatalogError(String message) {
        if (message == null) message = "Catalog error occurred.";
        JOptionPane.showMessageDialog(this, message, "Catalog Error", JOptionPane.ERROR_MESSAGE);
    }

    public void refreshCatalogsAsync() {
        AppExecutor.runWorker(
            "refresh-catalogs",
            () -> {
                List<Company> companies = companyService.getAllCompanies();
                List<Role> roles = roleService.getAllRoles();
                return new CatalogsData(companies, roles);
            },
            this,
            data -> updateCatalogsUI(data.companies, data.roles),
            error -> showCatalogError(error.getMessage())
        );
    }

    private void refreshCatalogs() {
        try {
            List<Company> companies = companyService.getAllCompanies();
            List<Role> roles = roleService.getAllRoles();
            updateCatalogsUI(companies, roles);
        } catch (DatabaseException ex) { showCatalogError(ex.getMessage()); }
    }

    private void updateCatalogsUI(List<Company> companies, List<Role> roles) {
        companiesModel.setRowCount(0);
        if (companies != null) {
            for (Company c : companies) {
                companiesModel.addRow(new Object[]{c.getCompanyId(), c.getCompanyName(), c.getIndustry(), c.getLocation(), c.getWebsite(), c.getContactEmail()});
            }
        }
        rolesModel.setRowCount(0);
        if (roles != null) {
            for (Role r : roles) {
                rolesModel.addRow(new Object[]{r.getRoleId(), r.getRoleTitle(), r.getExperienceLevel(), r.getDescription()});
            }
        }
        gapRoleCombo.removeAllItems();
        if (roles != null) {
            for (Role r : roles) {
                gapRoleCombo.addItem(r);
            }
            if (!roles.isEmpty() && currentGapResults == null) {
                performSkillGapAnalysisAsync();
            }
        }
    }

    // =========================================================================
    // DATA REFRESH LOGIC
    // =========================================================================

    public void refreshAllData() {
        refreshDashboard();
        refreshApplications();
        refreshSkills();
        refreshCertsAndProjects();
        refreshRecommendations();
        refreshCatalogs();
    }

    public void refreshAllDataAsync() {
        refreshDashboardAsync();
        refreshApplicationsAsync();
        refreshSkillsAsync();
        refreshCertsAndProjectsAsync();
        refreshRecommendationsAsync();
        refreshCatalogsAsync();
    }

    public void refreshDashboardAsync() {
        AppExecutor.runWorker(
            "refresh-dashboard",
            () -> {
                DashboardStats stats = dashboardService.getDashboardStats(currentStudent.getStudentId());
                List<Interview> upcoming = interviewService.getUpcomingInterviews(currentStudent.getStudentId());
                return new DashboardData(stats, upcoming);
            },
            this,
            data -> updateDashboardUI(data.stats, data.upcoming),
            error -> System.err.println("Dashboard refresh error: " + error.getMessage())
        );
    }

    private void refreshDashboard() {
        try {
            DashboardStats stats = dashboardService.getDashboardStats(currentStudent.getStudentId());
            List<Interview> upcoming = interviewService.getUpcomingInterviews(currentStudent.getStudentId());
            updateDashboardUI(stats, upcoming);
        } catch (Exception e) {
            System.err.println("Dashboard refresh error: " + e.getMessage());
        }
    }

    private void updateDashboardUI(DashboardStats stats, List<Interview> upcoming) {
        if (stats == null) return;
        totalAppsVal.setText(String.valueOf(stats.getTotal()));
        underReviewVal.setText(String.valueOf(stats.getUnderReview()));
        interviewsVal.setText(String.valueOf(stats.getUpcomingInterviews()));
        selectedVal.setText(String.valueOf(stats.getSelected()));

        // Status Breakdown
        statusModel.setRowCount(0);
        Map<String, Integer> counts = stats.asMap();
        for (String key : new String[]{"Applied", "Shortlisted", "Interview", "Selected", "Rejected"}) {
            statusModel.addRow(new Object[]{key, counts.getOrDefault(key, 0)});
        }

        // Upcoming Interviews
        upcomingInterviewsModel.setRowCount(0);
        if (upcoming != null) {
            for (Interview inv : upcoming) {
                upcomingInterviewsModel.addRow(new Object[]{
                    ValidationUtil.formatDisplayDateTime(inv.getInterviewDate()),
                    inv.getCompanyName(),
                    inv.getRoleTitle(),
                    inv.getRoundType(),
                    inv.getMode()
                });
            }
        }
    }

    public void refreshApplicationsAsync() {
        AppExecutor.runWorker(
            "refresh-applications",
            () -> applicationService.getApplications(currentStudent.getStudentId()),
            this,
            apps -> {
                loadedApplications = apps;
                filterApplicationsTable();
            },
            error -> JOptionPane.showMessageDialog(this, "Could not load applications: " + error.getMessage())
        );
    }

    private void refreshApplications() {
        try {
            loadedApplications = applicationService.getApplications(currentStudent.getStudentId());
            filterApplicationsTable();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not load applications: " + e.getMessage());
        }
    }

    private void filterApplicationsTable() {
        if (loadedApplications == null) return;
        String filter = (String) appFilterCombo.getSelectedItem();
        appsModel.setRowCount(0);

        for (Application a : loadedApplications) {
            if ("All".equalsIgnoreCase(filter) || a.getStatus().equalsIgnoreCase(filter)) {
                appsModel.addRow(new Object[]{
                    a.getApplicationId(),
                    a.getCompanyName(),
                    a.getRoleTitle(),
                    ValidationUtil.formatDisplayDate(a.getApplicationDate()),
                    a.getDeadline() != null ? ValidationUtil.formatDisplayDate(a.getDeadline()) : "N/A",
                    a.getStatus(),
                    a.getStipend() != null ? a.getStipend() : "-",
                    a.getNotes() != null ? a.getNotes() : ""
                });
            }
        }
    }

    public void refreshSkillsAsync() {
        AppExecutor.runWorker(
            "refresh-skills",
            () -> {
                List<Skill> master = skillService.getAllSkills();
                List<Skill> studentSkills = skillService.getStudentSkills(currentStudent.getStudentId());
                return new SkillsData(master, studentSkills);
            },
            this,
            data -> updateSkillsUI(data.master, data.studentSkills),
            error -> System.err.println("Skills refresh error: " + error.getMessage())
        );
    }

    private void refreshSkills() {
        try {
            List<Skill> master = skillService.getAllSkills();
            List<Skill> studentSkills = skillService.getStudentSkills(currentStudent.getStudentId());
            updateSkillsUI(master, studentSkills);
        } catch (Exception e) {
            System.err.println("Skills refresh error: " + e.getMessage());
        }
    }

    private void updateSkillsUI(List<Skill> master, List<Skill> studentSkills) {
        masterSkillsCombo.removeAllItems();
        if (master != null) {
            for (Skill s : master) {
                masterSkillsCombo.addItem(s);
            }
        }

        studentSkillsModel.setRowCount(0);
        if (studentSkills != null) {
            for (Skill s : studentSkills) {
                studentSkillsModel.addRow(new Object[]{
                    s.getSkillId(),
                    s.getSkillName(),
                    s.getCategory(),
                    s.getProficiencyLevel()
                });
            }
        }
    }

    public void refreshCertsAndProjectsAsync() {
        AppExecutor.runWorker(
            "refresh-certs-projects",
            () -> {
                List<Certification> certs = certificationService.getCertifications(currentStudent.getStudentId());
                List<Project> projs = projectService.getProjects(currentStudent.getStudentId());
                return new CertsAndProjectsData(certs, projs);
            },
            this,
            data -> updateCertsAndProjectsUI(data.certs, data.projs),
            error -> System.err.println("Cert/Project refresh error: " + error.getMessage())
        );
    }

    private void refreshCertsAndProjects() {
        try {
            List<Certification> certs = certificationService.getCertifications(currentStudent.getStudentId());
            List<Project> projs = projectService.getProjects(currentStudent.getStudentId());
            updateCertsAndProjectsUI(certs, projs);
        } catch (Exception e) {
            System.err.println("Cert/Project refresh error: " + e.getMessage());
        }
    }

    private void updateCertsAndProjectsUI(List<Certification> certs, List<Project> projs) {
        certsModel.setRowCount(0);
        if (certs != null) {
            for (Certification c : certs) {
                certsModel.addRow(new Object[]{c.getCertificationId(), c.getCertificateName(), c.getIssuingOrganization(), c.getIssueDate()});
            }
        }

        projectsModel.setRowCount(0);
        if (projs != null) {
            for (Project p : projs) {
                projectsModel.addRow(new Object[]{p.getProjectId(), p.getTitle(), p.getTechnologiesUsed(), p.getProjectUrl() != null ? p.getProjectUrl() : ""});
            }
        }
    }

    private void refreshRolesAndCompanies() {
        try {
            List<Role> roles = roleService.getAllRoles();
            gapRoleCombo.removeAllItems();
            for (Role r : roles) {
                gapRoleCombo.addItem(r);
            }
        } catch (Exception e) {
            System.err.println("Roles refresh error: " + e.getMessage());
        }
    }

    // =========================================================================
    // SKILL GAP EXECUTION
    // =========================================================================

    public void performSkillGapAnalysisAsync() {
        Role selectedRole = (Role) gapRoleCombo.getSelectedItem();
        if (selectedRole == null) return;

        setBusy(true, "Analyzing skill gap for " + selectedRole.getRoleTitle() + "...");
        final int roleId = selectedRole.getRoleId();
        final String roleTitle = selectedRole.getRoleTitle();

        AppExecutor.runWorker(
            "skill-gap-analysis",
            () -> skillGapService.analyzeSkillGap(currentStudent.getStudentId(), roleId),
            this,
            results -> {
                setBusy(false, "Ready");
                Role currentSel = (Role) gapRoleCombo.getSelectedItem();
                if (currentSel != null && currentSel.getRoleId() != roleId) {
                    return; // Stale result discard
                }
                currentGapResults = results;
                updateSkillGapUI(results, roleTitle);
            },
            error -> {
                setBusy(false, "Ready");
                JOptionPane.showMessageDialog(this, "Error analyzing skill gap: " + error.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        );
    }

    private void performSkillGapAnalysis() {
        Role selectedRole = (Role) gapRoleCombo.getSelectedItem();
        if (selectedRole == null) return;

        try {
            currentGapResults = skillGapService.analyzeSkillGap(currentStudent.getStudentId(), selectedRole.getRoleId());
            updateSkillGapUI(currentGapResults, selectedRole.getRoleTitle());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error analyzing skill gap: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSkillGapUI(List<SkillGapResult> results, String roleTitle) {
        gapMatchedModel.setRowCount(0);
        gapMissingModel.setRowCount(0);

        if (skillGapService.isInsufficientRoleData(results)) {
            gapProgressBar.setValue(0);
            gapProgressBar.setString("Insufficient role data (0 requirements defined)");
            gapProgressBar.setForeground(new Color(128, 128, 128));
            gapSummaryLabel.setText("Insufficient role data: No skill requirements are defined for " + roleTitle + ".");
            return;
        }

        int matchedCount = 0;
        int totalRequired = results.size();

        for (SkillGapResult r : results) {
            String reqBenchmark = r.getRequiredLevel() + (r.isMandatory() ? " [Mandatory]" : " [Optional]");

            if (r.isMatched()) {
                matchedCount++;
                gapMatchedModel.addRow(new Object[]{r.getSkillName(), r.getCategory(), reqBenchmark, r.getStudentLevel()});
            } else if (r.isUpgradeNeeded()) {
                int gap = r.getProficiencyGap();
                String gapText = gap > 0 ? " (Gap: +" + gap + (gap == 1 ? " level" : " levels") + ")" : "";
                String statusDesc = "Upgrade Needed: " + r.getStudentLevel() + " \u2192 " + r.getRequiredLevel() + gapText;
                gapMissingModel.addRow(new Object[]{r.getSkillName(), r.getCategory(), reqBenchmark, statusDesc});
            } else if (r.isInsufficientEvidence()) {
                gapMissingModel.addRow(new Object[]{r.getSkillName(), r.getCategory(), reqBenchmark, "Insufficient Evidence (Unspecified Level)"});
            } else {
                gapMissingModel.addRow(new Object[]{r.getSkillName(), r.getCategory(), reqBenchmark, "Not Possessed (Missing)"});
            }
        }

        int pct = skillGapService.calculateMatchPercentage(results);
        gapProgressBar.setValue(pct);
        gapProgressBar.setString(pct + "% Match (" + matchedCount + " of " + totalRequired + " required skills met)");

        if (pct >= 80) {
            gapProgressBar.setForeground(new Color(39, 174, 96));
        } else if (pct >= 50) {
            gapProgressBar.setForeground(new Color(243, 156, 18));
        } else {
            gapProgressBar.setForeground(new Color(217, 83, 79));
        }
        gapSummaryLabel.setText(skillGapService.getReadinessAssessment(pct, matchedCount, totalRequired, roleTitle));
    }

    private static class DashboardData {
        final DashboardStats stats;
        final List<Interview> upcoming;
        DashboardData(DashboardStats stats, List<Interview> upcoming) {
            this.stats = stats;
            this.upcoming = upcoming;
        }
    }

    private static class SkillsData {
        final List<Skill> master;
        final List<Skill> studentSkills;
        SkillsData(List<Skill> master, List<Skill> studentSkills) {
            this.master = master;
            this.studentSkills = studentSkills;
        }
    }

    private static class CertsAndProjectsData {
        final List<Certification> certs;
        final List<Project> projs;
        CertsAndProjectsData(List<Certification> certs, List<Project> projs) {
            this.certs = certs;
            this.projs = projs;
        }
    }

    private static class CatalogsData {
        final List<Company> companies;
        final List<Role> roles;
        CatalogsData(List<Company> companies, List<Role> roles) {
            this.companies = companies;
            this.roles = roles;
        }
    }

    private static class RoleEditorData {
        final List<Skill> skills;
        final List<Integer> selectedIds;
        RoleEditorData(List<Skill> skills, List<Integer> selectedIds) {
            this.skills = skills;
            this.selectedIds = selectedIds;
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
        String targetLevel = "Intermediate";
        if (currentGapResults != null) {
            for (SkillGapResult r : currentGapResults) {
                if (r.getSkillName().equalsIgnoreCase(skillName)) {
                    targetLevel = r.getRequiredLevel();
                    break;
                }
            }
        }
        addSkillByNameAndLevel(skillName, targetLevel);
    }

    private void addSkillByNameAndLevel(String skillName, String targetLevel) {
        setBusy(true, "Adding missing skill...");
        AppExecutor.runWorker(
            "add-missing-skill",
            () -> {
                List<Skill> all = skillService.getAllSkills();
                for (Skill s : all) {
                    if (s.getSkillName().equalsIgnoreCase(skillName)) {
                        skillService.addOrUpdateStudentSkill(currentStudent.getStudentId(), s.getSkillId(), targetLevel);
                        return s;
                    }
                }
                return null;
            },
            this,
            matchedSkill -> {
                setBusy(false, "Ready");
                if (matchedSkill != null) {
                    JOptionPane.showMessageDialog(this, "Successfully saved '" + skillName + "' (" + targetLevel + ") to your profile!", "Skill Acquired", JOptionPane.INFORMATION_MESSAGE);
                    refreshSkillsAsync();
                    refreshDashboardAsync();
                    performSkillGapAnalysisAsync();
                } else {
                    JOptionPane.showMessageDialog(this, "Skill '" + skillName + "' not found in catalog.", "Not Found", JOptionPane.WARNING_MESSAGE);
                }
            },
            error -> {
                setBusy(false, "Ready");
                JOptionPane.showMessageDialog(this, "Error adding skill: " + error.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        );
    }

    private void addStudentSkill() {
        Skill selected = (Skill) masterSkillsCombo.getSelectedItem();
        String prof = (String) proficiencyCombo.getSelectedItem();
        if (selected == null || prof == null) return;

        setBusy(true, "Saving skill...");
        AppExecutor.runWorker(
            "save-student-skill",
            () -> {
                skillService.addOrUpdateStudentSkill(currentStudent.getStudentId(), selected.getSkillId(), prof);
                try {
                    SkillEvidence ev = new SkillEvidence(
                        0,
                        currentStudent.getStudentId(),
                        selected.getSkillId(),
                        selected.getSkillName(),
                        "Self-Reported",
                        null,
                        "Self-Reported Skill (" + prof + ")",
                        "Self-declared competence level: " + prof + ".",
                        "Unverified",
                        null
                    );
                    skillService.addEvidence(ev);
                } catch (Exception ignored) {}
                return selected;
            },
            this,
            savedSkill -> {
                setBusy(false, "Skill saved");
                refreshSkillsAsync();
                refreshDashboardAsync();
                JOptionPane.showMessageDialog(this, "Skill '" + savedSkill.getSkillName() + "' (" + prof + ") saved to your profile!", "Success", JOptionPane.INFORMATION_MESSAGE);
            },
            error -> {
                setBusy(false, "Ready");
                JOptionPane.showMessageDialog(this, "Error saving skill: " + error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        );
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
            setBusy(true, "Removing skill...");
            AppExecutor.runWorker(
                "remove-student-skill",
                () -> {
                    skillService.removeStudentSkill(currentStudent.getStudentId(), skillId);
                    return null;
                },
                this,
                res -> {
                    setBusy(false, "Skill removed");
                    refreshSkillsAsync();
                    refreshDashboardAsync();
                },
                error -> {
                    setBusy(false, "Ready");
                    JOptionPane.showMessageDialog(this, "Error: " + error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            );
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

    private void showEditProfileDialog() {
        JTextField rollField = new JTextField(currentStudent.getRollNumber());
        rollField.setEditable(false);
        JTextField nameField = new JTextField(currentStudent.getFullName());
        JTextField emailField = new JTextField(currentStudent.getEmail());
        JTextField deptField = new JTextField(currentStudent.getDepartment() != null ? currentStudent.getDepartment() : "CSE");
        JComboBox<Integer> yearCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        yearCombo.setSelectedItem(currentStudent.getYearOfStudy() > 0 ? currentStudent.getYearOfStudy() : 2);
        JTextField phoneField = new JTextField(currentStudent.getPhone() != null ? currentStudent.getPhone() : "");

        JPanel panel = new JPanel(new GridLayout(6, 2, 8, 8));
        panel.add(new JLabel("Roll Number (ID):"));
        panel.add(rollField);
        panel.add(new JLabel("Full Name *:"));
        panel.add(nameField);
        panel.add(new JLabel("Email Address *:"));
        panel.add(emailField);
        panel.add(new JLabel("Department *:"));
        panel.add(deptField);
        panel.add(new JLabel("Year of Study *:"));
        panel.add(yearCombo);
        panel.add(new JLabel("Phone:"));
        panel.add(phoneField);

        int res = JOptionPane.showConfirmDialog(this, panel, "Edit Student Profile", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String dept = deptField.getText().trim();
            int year = (Integer) yearCombo.getSelectedItem();
            String phone = phoneField.getText().trim();

            if (!ValidationUtil.isValidName(name)) {
                JOptionPane.showMessageDialog(this, "Please enter a valid full name (letters, spaces, dots, hyphens only).", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!ValidationUtil.isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!ValidationUtil.isNonEmpty(dept)) {
                JOptionPane.showMessageDialog(this, "Department cannot be blank.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!ValidationUtil.isValidYearOfStudy(year)) {
                JOptionPane.showMessageDialog(this, "Year of study must be between 1 and 4.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!ValidationUtil.isValidPhone(phone)) {
                JOptionPane.showMessageDialog(this, "Please enter a valid phone number (10-15 digits) or leave it blank.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            setBusy(true, "Updating profile...");
            AppExecutor.runWorker(
                "update-profile",
                () -> {
                    currentStudent.setFullName(name);
                    currentStudent.setEmail(email);
                    currentStudent.setDepartment(dept);
                    currentStudent.setYearOfStudy(year);
                    currentStudent.setPhone(phone.isEmpty() ? null : phone);

                    studentService.updateProfile(currentStudent, currentStudent.getStudentId());
                    return null;
                },
                this,
                resUpdate -> {
                    setBusy(false, "Profile updated");
                    updateWelcomeLabelText();
                    refreshDashboardAsync();
                    JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                },
                ex -> {
                    setBusy(false, "Ready");
                    if (ex instanceof UnauthorizedAccessException) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Authorization Error", JOptionPane.ERROR_MESSAGE);
                    } else if (ex instanceof DuplicateEntityException) {
                        JOptionPane.showMessageDialog(this, "The email address '" + email + "' is already registered to another account.", "Duplicate Email", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Error updating profile: " + ValidationUtil.sanitizeErrorMessage((Exception) ex), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            );
        }
    }

    private void showAddApplicationDialog() {
        showAddApplicationDialog(0, 0);
    }

    private void showAddApplicationDialog(int preselectedCompanyId, int preselectedRoleId) {
        setBusy(true, "Preparing application form...");
        AppExecutor.runWorker(
            "load-companies-roles",
            () -> {
                List<Company> companies = companyService.getAllCompanies();
                List<Role> roles = roleService.getAllRoles();
                return new CatalogsData(companies, roles);
            },
            this,
            data -> {
                setBusy(false, "Ready");
                openAddApplicationDialog(data.companies, data.roles, preselectedCompanyId, preselectedRoleId);
            },
            ex -> {
                setBusy(false, "Ready");
                JOptionPane.showMessageDialog(this, "Error preparing form: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        );
    }

    private void openAddApplicationDialog(List<Company> companies, List<Role> roles, int preselectedCompanyId, int preselectedRoleId) {
        if (companies == null || companies.isEmpty() || roles == null || roles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please ensure companies and roles are seeded in the database.", "Data Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new GridLayout(6, 2, 8, 8));
        JComboBox<Company> compCombo = new JComboBox<>(companies.toArray(new Company[0]));
        JComboBox<Role> roleCombo = new JComboBox<>(roles.toArray(new Role[0]));

        if (preselectedCompanyId > 0) {
            for (int i = 0; i < compCombo.getItemCount(); i++) {
                if (compCombo.getItemAt(i).getCompanyId() == preselectedCompanyId) {
                    compCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        if (preselectedRoleId > 0) {
            for (int i = 0; i < roleCombo.getItemCount(); i++) {
                if (roleCombo.getItemAt(i).getRoleId() == preselectedRoleId) {
                    roleCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        JTextField stipendField = new JTextField("₹ 35,000 / mo");
        JTextField deadlineField = new JTextField(ValidationUtil.futureDateString(30));
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
            if (c == null || r == null) {
                JOptionPane.showMessageDialog(this, "Please select both a company and a role.", "Selection Missing", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String deadlineText = deadlineField.getText().trim();
            if (!ValidationUtil.isValidDate(deadlineText)) {
                JOptionPane.showMessageDialog(this, "Please enter a valid deadline in YYYY-MM-DD format (valid calendar date).", "Invalid Date", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String stipendText = stipendField.getText().trim();
            if (ValidationUtil.hasNegativeStipend(stipendText)) {
                JOptionPane.showMessageDialog(this, "Stipend amount cannot be negative.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Date appDate = new Date(System.currentTimeMillis());
            Date deadline = ValidationUtil.parseDate(deadlineText);
            String status = (String) statusCombo.getSelectedItem();
            String notes = notesField.getText().trim();

            Application app = new Application(0, currentStudent.getStudentId(), c.getCompanyId(), r.getRoleId(), appDate, deadline, status, stipendText, notes);
            setBusy(true, "Recording application...");
            AppExecutor.runWorker(
                "submit-application",
                () -> {
                    applicationService.submitApplication(app);
                    return null;
                },
                this,
                res -> {
                    setBusy(false, "Application saved");
                    refreshApplicationsAsync();
                    refreshDashboardAsync();
                    refreshRecommendationsAsync();
                    JOptionPane.showMessageDialog(this, "Application recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                },
                ex -> {
                    setBusy(false, "Ready");
                    if (ex instanceof DuplicateEntityException) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Duplicate Application", JOptionPane.WARNING_MESSAGE);
                    } else if (ex instanceof ValidationException) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
                    } else if (ex instanceof DatabaseException) {
                        JOptionPane.showMessageDialog(this, "Database error adding application: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Error adding application: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            );
        }
    }

    private void showEditApplicationDialog() {
        int appId = getSelectedApplicationId();
        if (appId == -1) return;

        Application target = null;
        if (loadedApplications != null) {
            for (Application a : loadedApplications) {
                if (a.getApplicationId() == appId) {
                    target = a;
                    break;
                }
            }
        }
        if (target == null) {
            JOptionPane.showMessageDialog(this, "Selected application could not be found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        JTextField compField = new JTextField(target.getCompanyName());
        compField.setEditable(false);
        JTextField roleField = new JTextField(target.getRoleTitle());
        roleField.setEditable(false);

        JTextField deadlineField = new JTextField(target.getDeadline() != null ? ValidationUtil.formatDate(target.getDeadline()) : "");
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Applied", "Shortlisted", "Interview", "Selected", "Rejected"});
        statusCombo.setSelectedItem(target.getStatus());
        JTextField stipendField = new JTextField(target.getStipend() != null ? target.getStipend() : "");
        JTextField notesField = new JTextField(target.getNotes() != null ? target.getNotes() : "");

        panel.add(new JLabel("Company:"));
        panel.add(compField);
        panel.add(new JLabel("Role:"));
        panel.add(roleField);
        panel.add(new JLabel("Deadline (YYYY-MM-DD):"));
        panel.add(deadlineField);
        panel.add(new JLabel("Application Status:"));
        panel.add(statusCombo);
        panel.add(new JLabel("Stipend:"));
        panel.add(stipendField);
        panel.add(new JLabel("Notes:"));
        panel.add(notesField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Application Details", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            final Application appToUpdate = target;
            String deadlineText = deadlineField.getText().trim();
            Date deadline = null;
            if (!deadlineText.isEmpty()) {
                if (!ValidationUtil.isValidDate(deadlineText)) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid deadline in YYYY-MM-DD format.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                deadline = ValidationUtil.parseDate(deadlineText);
            }

            String stipendText = stipendField.getText().trim();
            if (ValidationUtil.hasNegativeStipend(stipendText)) {
                JOptionPane.showMessageDialog(this, "Stipend amount cannot be negative.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            appToUpdate.setDeadline(deadline);
            appToUpdate.setStatus((String) statusCombo.getSelectedItem());
            appToUpdate.setStipend(stipendText);
            appToUpdate.setNotes(notesField.getText().trim());

            setBusy(true, "Updating application...");
            AppExecutor.runWorker(
                "update-application",
                () -> {
                    applicationService.updateApplication(appToUpdate, currentStudent.getStudentId());
                    return null;
                },
                this,
                res -> {
                    setBusy(false, "Application updated");
                    refreshApplicationsAsync();
                    refreshDashboardAsync();
                    JOptionPane.showMessageDialog(this, "Application updated successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
                },
                ex -> {
                    setBusy(false, "Ready");
                    if (ex instanceof UnauthorizedAccessException) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Authorization Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Error updating application: " + ValidationUtil.sanitizeErrorMessage((Exception) ex), "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            );
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
            String newStatus = (String) combo.getSelectedItem();
            setBusy(true, "Updating status...");
            AppExecutor.runWorker(
                "update-status",
                () -> {
                    applicationService.updateStatus(appId, currentStudent.getStudentId(), newStatus);
                    return newStatus;
                },
                this,
                resStatus -> {
                    setBusy(false, "Status updated");
                    refreshApplicationsAsync();
                    refreshDashboardAsync();
                    JOptionPane.showMessageDialog(this, "Status updated to '" + resStatus + "'!", "Updated", JOptionPane.INFORMATION_MESSAGE);
                },
                ex -> {
                    setBusy(false, "Ready");
                    if (ex instanceof UnauthorizedAccessException) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Authorization Error", JOptionPane.ERROR_MESSAGE);
                    } else if (ex instanceof ValidationException) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Error updating status: " + ValidationUtil.sanitizeErrorMessage((Exception) ex), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            );
        }
    }

    private void showScheduleInterviewDialog() {
        int appId = getSelectedApplicationId();
        if (appId == -1) return;

        int row = appsTable.getSelectedRow();
        String company = (String) appsModel.getValueAt(row, 1);
        String role = (String) appsModel.getValueAt(row, 2);

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        JTextField dateField = new JTextField(ValidationUtil.futureDateTimeString(3));
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
            String rawDate = dateField.getText().trim();
            if (!rawDate.contains(":")) rawDate += " 10:00:00";
            if (!ValidationUtil.isValidTimestamp(rawDate)) {
                JOptionPane.showMessageDialog(this, "Please enter a valid date and time (format: YYYY-MM-DD HH:MM:SS or YYYY-MM-DD HH:MM).", "Invalid Date/Time", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Timestamp ts = ValidationUtil.parseTimestamp(rawDate);
            Interview inv = new Interview(0, appId, ts, (String) roundCombo.getSelectedItem(), (String) modeCombo.getSelectedItem(), "Scheduled", "Pending", feedbackField.getText().trim());

            setBusy(true, "Scheduling interview...");
            AppExecutor.runWorker(
                "schedule-interview",
                () -> {
                    interviewService.scheduleInterview(inv, currentStudent.getStudentId());
                    return null;
                },
                this,
                res -> {
                    setBusy(false, "Interview scheduled");
                    refreshApplicationsAsync();
                    refreshDashboardAsync();
                    JOptionPane.showMessageDialog(this, "Interview scheduled for " + company + "!", "Scheduled", JOptionPane.INFORMATION_MESSAGE);
                },
                ex -> {
                    setBusy(false, "Ready");
                    if (ex instanceof UnauthorizedAccessException) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Authorization Error", JOptionPane.ERROR_MESSAGE);
                    } else if (ex instanceof DuplicateEntityException) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Duplicate Record", JOptionPane.WARNING_MESSAGE);
                    } else if (ex instanceof ValidationException) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Error scheduling interview: " + ValidationUtil.sanitizeErrorMessage((Exception) ex), "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            );
        }
    }

    private void showViewInterviewsDialog() {
        JDialog dlg = new JDialog(this, "All Scheduled & Logged Interviews", true);
        dlg.setSize(850, 480);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));

        String[] cols = {"ID", "Company", "Role", "Date & Time", "Round", "Mode", "Status", "Outcome"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = new JTable(m);
        t.setRowHeight(26);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton updateOutcomeBtn = createStyledButton("Update Outcome / Status", new Color(24, 100, 180), Color.WHITE);
        JButton deleteBtn = createStyledButton("Delete Interview", new Color(217, 83, 79), Color.WHITE);
        JButton closeBtn = new JButton("Close");

        Runnable loadData = () -> {
            AppExecutor.runWorker(
                "load-interviews-dialog",
                () -> interviewService.getInterviews(currentStudent.getStudentId()),
                dlg,
                interviews -> {
                    m.setRowCount(0);
                    for (Interview i : interviews) {
                        m.addRow(new Object[]{
                            i.getInterviewId(),
                            i.getCompanyName(),
                            i.getRoleTitle(),
                            ValidationUtil.formatDisplayDateTime(i.getInterviewDate()),
                            i.getRoundType(),
                            i.getMode(),
                            i.getStatus(),
                            i.getOutcome()
                        });
                    }
                },
                err -> JOptionPane.showMessageDialog(dlg, "Error loading interviews: " + err.getMessage()),
                updateOutcomeBtn, deleteBtn
            );
        };
        loadData.run();

        updateOutcomeBtn.addActionListener(e -> {
            int sel = t.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(dlg, "Please select an interview first.");
                return;
            }
            int interviewId = (int) m.getValueAt(sel, 0);
            String currentStatus = (String) m.getValueAt(sel, 6);
            String currentOutcome = (String) m.getValueAt(sel, 7);

            JComboBox<String> stCombo = new JComboBox<>(new String[]{"Scheduled", "Completed", "Cancelled", "Rescheduled"});
            stCombo.setSelectedItem(currentStatus);
            JComboBox<String> outCombo = new JComboBox<>(new String[]{"Pending", "Passed", "Failed"});
            outCombo.setSelectedItem(currentOutcome);
            JTextField fbField = new JTextField();

            JPanel p = new JPanel(new GridLayout(3, 2, 8, 8));
            p.add(new JLabel("Interview Status:"));
            p.add(stCombo);
            p.add(new JLabel("Outcome:"));
            p.add(outCombo);
            p.add(new JLabel("Feedback / Notes:"));
            p.add(fbField);

            if (JOptionPane.showConfirmDialog(dlg, p, "Update Interview Outcome", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                String newSt = (String) stCombo.getSelectedItem();
                String newOut = (String) outCombo.getSelectedItem();
                String fb = fbField.getText().trim();
                AppExecutor.runWorker(
                    "update-interview-outcome",
                    () -> {
                        interviewService.updateInterviewOutcome(interviewId, currentStudent.getStudentId(), newSt, newOut, fb);
                        return null;
                    },
                    dlg,
                    res -> {
                        loadData.run();
                        refreshDashboardAsync();
                    },
                    ex -> {
                        if (ex instanceof UnauthorizedAccessException) {
                            JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Authorization Error", JOptionPane.ERROR_MESSAGE);
                        } else if (ex instanceof ValidationException) {
                            JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(dlg, "Error updating interview: " + ValidationUtil.sanitizeErrorMessage((Exception) ex), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    },
                    updateOutcomeBtn, deleteBtn
                );
            }
        });

        deleteBtn.addActionListener(e -> {
            int sel = t.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(dlg, "Please select an interview to delete.");
                return;
            }
            int interviewId = (int) m.getValueAt(sel, 0);
            String company = (String) m.getValueAt(sel, 1);
            String round = (String) m.getValueAt(sel, 4);

            if (JOptionPane.showConfirmDialog(dlg, "Delete interview (" + company + " - " + round + ")?", "Confirm Deletion", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                AppExecutor.runWorker(
                    "delete-interview",
                    () -> {
                        interviewService.deleteInterview(interviewId, currentStudent.getStudentId());
                        return null;
                    },
                    dlg,
                    res -> {
                        loadData.run();
                        refreshDashboardAsync();
                    },
                    ex -> {
                        if (ex instanceof UnauthorizedAccessException) {
                            JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Authorization Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(dlg, "Error deleting interview: " + ValidationUtil.sanitizeErrorMessage((Exception) ex), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    },
                    updateOutcomeBtn, deleteBtn
                );
            }
        });

        closeBtn.addActionListener(e -> dlg.dispose());

        btnPanel.add(updateOutcomeBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(closeBtn);

        dlg.add(new JScrollPane(t), BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void deleteSelectedApplication() {
        int appId = getSelectedApplicationId();
        if (appId == -1) return;

        int row = appsTable.getSelectedRow();
        String company = (String) appsModel.getValueAt(row, 1);
        String role = (String) appsModel.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete application for " + company + " (" + role + ")?", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            setBusy(true, "Deleting application...");
            AppExecutor.runWorker(
                "delete-application",
                () -> {
                    applicationService.deleteApplication(appId, currentStudent.getStudentId());
                    return null;
                },
                this,
                res -> {
                    setBusy(false, "Application deleted");
                    refreshApplicationsAsync();
                    refreshDashboardAsync();
                    JOptionPane.showMessageDialog(this, "Application deleted successfully.");
                },
                ex -> {
                    setBusy(false, "Ready");
                    if (ex instanceof UnauthorizedAccessException) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Authorization Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Error deleting application: " + ValidationUtil.sanitizeErrorMessage((Exception) ex), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            );
        }
    }

    private void showAddCertDialog() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        JTextField nameField = new JTextField();
        JTextField orgField = new JTextField("Oracle / AWS / HackerRank");
        JTextField dateField = new JTextField(ValidationUtil.currentDateString());
        JTextField credField = new JTextField();

        panel.add(new JLabel("Certification Name *:"));
        panel.add(nameField);
        panel.add(new JLabel("Issuing Organization *:"));
        panel.add(orgField);
        panel.add(new JLabel("Issue Date (YYYY-MM-DD) *:"));
        panel.add(dateField);
        panel.add(new JLabel("Credential ID:"));
        panel.add(credField);

        int res = JOptionPane.showConfirmDialog(this, panel, "Add Certification", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String org = orgField.getText().trim();
            String dateStr = dateField.getText().trim();

            if (!validateCertInputs(name, org, dateStr)) {
                return;
            }

            Date d = ValidationUtil.parseDate(dateStr);
            Certification c = new Certification(0, currentStudent.getStudentId(), name, org, d, credField.getText().trim());

            setBusy(true, "Searching skill suggestions...");
            AppExecutor.runWorker(
                "find-cert-suggestions",
                () -> {
                    List<CertificationSkillMapping> suggestions = certificationService.findSuggestedSkills(name);
                    List<Skill> allSkills = skillService.getAllSkills();
                    return new CertInitData(suggestions, allSkills);
                },
                this,
                data -> {
                    setBusy(false, "Ready");
                    showReviewCertificationSkillsDialog(c, data.suggestions, data.allSkills);
                },
                ex -> {
                    setBusy(false, "Ready");
                    showReviewCertificationSkillsDialog(c, new ArrayList<>(), new ArrayList<>());
                }
            );
        }
    }

    private void showReviewCertificationSkillsDialog(Certification cert, List<CertificationSkillMapping> suggestions) {
        showReviewCertificationSkillsDialog(cert, suggestions, null);
    }

    private void showReviewCertificationSkillsDialog(Certification cert, List<CertificationSkillMapping> suggestions, List<Skill> allSkills) {
        JDialog reviewDialog = new JDialog(this, "Review & Map Skills: " + cert.getCertificateName(), true);
        reviewDialog.setLayout(new BorderLayout(10, 10));
        reviewDialog.setSize(750, 480);
        reviewDialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(8, 8));
        mainPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        JLabel titleLbl = new JLabel("Certification: " + cert.getCertificateName() + " (" + cert.getIssuingOrganization() + ")");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel infoLbl;
        if (suggestions != null && !suggestions.isEmpty()) {
            infoLbl = new JLabel("Found " + suggestions.size() + " verified skill mappings from curriculum catalog. Review, modify, or add additional skills:");
        } else {
            infoLbl = new JLabel("No pre-configured skills found in the catalog. You may manually associate competencies or save without skills:");
        }
        infoLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLbl.setForeground(new Color(90, 95, 105));
        headerPanel.add(titleLbl);
        headerPanel.add(infoLbl);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Table Model for reviewed skills
        DefaultTableModel reviewModel = new DefaultTableModel(
            new Object[]{"Include", "Skill ID", "Skill Name", "Learning Outcome / Competency", "Assessed Proficiency"}, 0
        ) {
            @Override
            public Class<?> getColumnClass(int col) {
                if (col == 0) return Boolean.class;
                if (col == 1) return Integer.class;
                return String.class;
            }
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 0 || col == 3 || col == 4;
            }
        };

        if (suggestions != null) {
            for (CertificationSkillMapping m : suggestions) {
                reviewModel.addRow(new Object[]{
                    Boolean.TRUE,
                    m.getSkillId(),
                    m.getSkillName(),
                    m.getLearningOutcome(),
                    m.getSuggestedProficiency() != null ? m.getSuggestedProficiency() : "Intermediate"
                });
            }
        }

        JTable reviewTable = new JTable(reviewModel);
        reviewTable.setRowHeight(28);
        reviewTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        reviewTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        reviewTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        reviewTable.getColumnModel().getColumn(3).setPreferredWidth(280);
        reviewTable.getColumnModel().getColumn(4).setPreferredWidth(120);

        JComboBox<String> levelCombo = new JComboBox<>(new String[]{"Beginner", "Intermediate", "Advanced", "Expert"});
        reviewTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(levelCombo));

        mainPanel.add(new JScrollPane(reviewTable), BorderLayout.CENTER);

        // Bottom panel: manual add + action buttons
        JPanel bottomContainer = new JPanel(new BorderLayout(5, 5));

        JPanel manualAddPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        manualAddPanel.setBorder(BorderFactory.createTitledBorder("Add Competency Manually"));
        JComboBox<Skill> addSkillCombo = new JComboBox<>();
        if (allSkills != null) {
            for (Skill s : allSkills) {
                addSkillCombo.addItem(s);
            }
        } else {
            try {
                for (Skill s : skillService.getAllSkills()) {
                    addSkillCombo.addItem(s);
                }
            } catch (Exception ignored) {}
        }
        JComboBox<String> addLevelCombo = new JComboBox<>(new String[]{"Beginner", "Intermediate", "Advanced", "Expert"});
        addLevelCombo.setSelectedItem("Intermediate");
        JTextField addOutcomeField = new JTextField("Practical coursework & certification evaluation", 20);
        JButton addManualBtn = new JButton("+ Add Skill");

        addManualBtn.addActionListener(ev -> {
            Skill sel = (Skill) addSkillCombo.getSelectedItem();
            if (sel == null) return;
            for (int r = 0; r < reviewModel.getRowCount(); r++) {
                int sid = (int) reviewModel.getValueAt(r, 1);
                if (sid == sel.getSkillId()) {
                    JOptionPane.showMessageDialog(reviewDialog, "Skill '" + sel.getSkillName() + "' is already in the review list.", "Duplicate Skill", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            reviewModel.addRow(new Object[]{
                Boolean.TRUE,
                sel.getSkillId(),
                sel.getSkillName(),
                addOutcomeField.getText().trim(),
                addLevelCombo.getSelectedItem()
            });
        });

        manualAddPanel.add(new JLabel("Skill:"));
        manualAddPanel.add(addSkillCombo);
        manualAddPanel.add(new JLabel("Level:"));
        manualAddPanel.add(addLevelCombo);
        manualAddPanel.add(new JLabel("Outcome:"));
        manualAddPanel.add(addOutcomeField);
        manualAddPanel.add(addManualBtn);
        bottomContainer.add(manualAddPanel, BorderLayout.NORTH);

        // Action Buttons
        JPanel actionBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        JButton saveBtn = createStyledButton("Confirm & Save Certification", new Color(24, 100, 180), Color.WHITE);
        JButton saveCertOnlyBtn = new JButton("Save Without Skills");
        JButton cancelBtn = new JButton("Cancel");

        saveBtn.addActionListener(ev -> {
            if (reviewTable.isEditing()) {
                reviewTable.getCellEditor().stopCellEditing();
            }

            List<StudentCertificationSkill> reviewedList = new ArrayList<>();
            for (int r = 0; r < reviewModel.getRowCount(); r++) {
                Boolean inc = (Boolean) reviewModel.getValueAt(r, 0);
                if (Boolean.TRUE.equals(inc)) {
                    int sid = (int) reviewModel.getValueAt(r, 1);
                    String sname = (String) reviewModel.getValueAt(r, 2);
                    String outcome = (String) reviewModel.getValueAt(r, 3);
                    String prof = (String) reviewModel.getValueAt(r, 4);

                    reviewedList.add(new StudentCertificationSkill(
                        0,
                        currentStudent.getStudentId(),
                        0,
                        sid,
                        sname,
                        outcome,
                        "Approved",
                        prof,
                        null
                    ));
                }
            }

            AppExecutor.runWorker(
                "save-cert-with-skills",
                () -> {
                    certificationService.saveCertificationWithReviewedSkills(cert, reviewedList, currentStudent.getStudentId());
                    return null;
                },
                reviewDialog,
                res -> {
                    reviewDialog.dispose();
                    refreshCertsAndProjectsAsync();
                    refreshSkillsAsync();
                    refreshDashboardAsync();
                    refreshCatalogsAsync();
                    JOptionPane.showMessageDialog(
                        this,
                        "Certification and " + reviewedList.size() + " verified skill(s) recorded with supporting evidence!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                },
                ex -> {
                    if (ex instanceof UnauthorizedAccessException) {
                        JOptionPane.showMessageDialog(reviewDialog, ex.getMessage(), "Authorization Error", JOptionPane.ERROR_MESSAGE);
                    } else if (ex instanceof DuplicateEntityException) {
                        JOptionPane.showMessageDialog(reviewDialog, ex.getMessage(), "Duplicate Record", JOptionPane.WARNING_MESSAGE);
                    } else if (ex instanceof ValidationException) {
                        JOptionPane.showMessageDialog(reviewDialog, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(reviewDialog, "Error saving certification: " + ValidationUtil.sanitizeErrorMessage((Exception) ex), "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                },
                saveBtn, saveCertOnlyBtn, cancelBtn
            );
        });

        saveCertOnlyBtn.addActionListener(ev -> {
            AppExecutor.runWorker(
                "save-cert-only",
                () -> {
                    certificationService.saveCertificationWithReviewedSkills(cert, new ArrayList<>(), currentStudent.getStudentId());
                    return null;
                },
                reviewDialog,
                res -> {
                    reviewDialog.dispose();
                    refreshCertsAndProjectsAsync();
                    refreshSkillsAsync();
                    refreshDashboardAsync();
                    refreshCatalogsAsync();
                    JOptionPane.showMessageDialog(this, "Certification saved successfully (without skill mappings).", "Success", JOptionPane.INFORMATION_MESSAGE);
                },
                ex -> {
                    if (ex instanceof UnauthorizedAccessException) {
                        JOptionPane.showMessageDialog(reviewDialog, ex.getMessage(), "Authorization Error", JOptionPane.ERROR_MESSAGE);
                    } else if (ex instanceof DuplicateEntityException) {
                        JOptionPane.showMessageDialog(reviewDialog, ex.getMessage(), "Duplicate Record", JOptionPane.WARNING_MESSAGE);
                    } else if (ex instanceof ValidationException) {
                        JOptionPane.showMessageDialog(reviewDialog, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(reviewDialog, "Error saving certification: " + ValidationUtil.sanitizeErrorMessage((Exception) ex), "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                },
                saveBtn, saveCertOnlyBtn, cancelBtn
            );
        });

        cancelBtn.addActionListener(ev -> reviewDialog.dispose());

        actionBtnPanel.add(saveCertOnlyBtn);
        actionBtnPanel.add(cancelBtn);
        actionBtnPanel.add(saveBtn);
        bottomContainer.add(actionBtnPanel, BorderLayout.SOUTH);

        mainPanel.add(bottomContainer, BorderLayout.SOUTH);
        reviewDialog.add(mainPanel);
        reviewDialog.setVisible(true);
    }

    private void showViewCertSkillsDialog() {
        int row = certsTable.getSelectedRow();
        if (row == -1) {
            if (certsTable.getRowCount() > 0) {
                certsTable.setRowSelectionInterval(0, 0);
                row = 0;
            } else {
                JOptionPane.showMessageDialog(this, "Please select a certification to view.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        int certId = (int) certsModel.getValueAt(row, 0);
        String certName = (String) certsModel.getValueAt(row, 1);
        String org = (String) certsModel.getValueAt(row, 2);

        setBusy(true, "Loading certification skills...");
        AppExecutor.runWorker(
            "load-cert-skills",
            () -> certificationService.getCertificationSkills(certId),
            this,
            skills -> {
                setBusy(false, "Ready");
                openViewCertSkillsDialog(certName, org, skills);
            },
            ex -> {
                setBusy(false, "Ready");
                JOptionPane.showMessageDialog(this, "Error retrieving certification skills: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        );
    }

    private void openViewCertSkillsDialog(String certName, String org, List<StudentCertificationSkill> skills) {
        JDialog dialog = new JDialog(this, "Verified Skills & Evidence: " + certName, true);
        dialog.setSize(680, 380);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Certification: " + certName + " (" + org + ")");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(title, BorderLayout.NORTH);

        if (skills == null || skills.isEmpty()) {
            JLabel emptyLbl = new JLabel("No skill mappings or learning outcomes recorded for this certification.");
            emptyLbl.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(emptyLbl, BorderLayout.CENTER);
        } else {
            DefaultTableModel model = new DefaultTableModel(
                new String[]{"Skill Name", "Assessed Proficiency", "Learning Outcome / Competency", "Status"}, 0
            ) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            for (StudentCertificationSkill s : skills) {
                model.addRow(new Object[]{
                    s.getSkillName(),
                    s.getAssessedProficiency(),
                    s.getLearningOutcome(),
                    s.getReviewStatus()
                });
            }
            JTable table = new JTable(model);
            table.setRowHeight(26);
            table.getColumnModel().getColumn(0).setPreferredWidth(130);
            table.getColumnModel().getColumn(1).setPreferredWidth(120);
            table.getColumnModel().getColumn(2).setPreferredWidth(280);
            table.getColumnModel().getColumn(3).setPreferredWidth(80);
            panel.add(new JScrollPane(table), BorderLayout.CENTER);
        }

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(closeBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showSkillEvidenceDialog() {
        int row = studentSkillsTable.getSelectedRow();
        if (row == -1) {
            if (studentSkillsTable.getRowCount() > 0) {
                studentSkillsTable.setRowSelectionInterval(0, 0);
                row = 0;
            } else {
                JOptionPane.showMessageDialog(this, "Please select a skill from the list to view its evidence.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        int skillId = (int) studentSkillsModel.getValueAt(row, 0);
        String skillName = (String) studentSkillsModel.getValueAt(row, 1);
        String currentLevel = (String) studentSkillsModel.getValueAt(row, 3);

        setBusy(true, "Loading skill evidence...");
        AppExecutor.runWorker(
            "load-skill-evidence",
            () -> skillService.getSkillEvidence(currentStudent.getStudentId(), skillId),
            this,
            evidenceList -> {
                setBusy(false, "Ready");
                openSkillEvidenceDialog(skillName, currentLevel, evidenceList);
            },
            ex -> {
                setBusy(false, "Ready");
                JOptionPane.showMessageDialog(this, "Error retrieving skill evidence: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        );
    }

    private void openSkillEvidenceDialog(String skillName, String currentLevel, List<SkillEvidence> evidenceList) {
        JDialog dialog = new JDialog(this, "Skill Evidence & Verification: " + skillName, true);
        dialog.setSize(720, 380);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Supporting Evidence for: " + skillName + " (Demonstrated Level: " + currentLevel + ")");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(title, BorderLayout.NORTH);

        if (evidenceList == null || evidenceList.isEmpty()) {
            JLabel emptyLbl = new JLabel("<html><i>No formal evidence records found for this skill.<br>Current status: Self-Reported only (Unverified).</i></html>");
            emptyLbl.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(emptyLbl, BorderLayout.CENTER);
        } else {
            DefaultTableModel model = new DefaultTableModel(
                new String[]{"Evidence Type", "Evidence Source / Title", "Details", "Status", "Date"}, 0
            ) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            for (SkillEvidence ev : evidenceList) {
                model.addRow(new Object[]{
                    ev.getEvidenceType(),
                    ev.getEvidenceTitle(),
                    ev.getEvidenceDetails(),
                    ev.getVerificationStatus(),
                    ev.getCreatedAt() != null ? ValidationUtil.formatDate(ev.getCreatedAt()) : "-"
                });
            }
            JTable table = new JTable(model);
            table.setRowHeight(26);
            table.getColumnModel().getColumn(0).setPreferredWidth(100);
            table.getColumnModel().getColumn(1).setPreferredWidth(170);
            table.getColumnModel().getColumn(2).setPreferredWidth(230);
            table.getColumnModel().getColumn(3).setPreferredWidth(80);
            table.getColumnModel().getColumn(4).setPreferredWidth(90);
            panel.add(new JScrollPane(table), BorderLayout.CENTER);
        }

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(closeBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showEditCertDialog() {
        int row = certsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a certification to edit.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int certId = (int) certsModel.getValueAt(row, 0);

        setBusy(true, "Loading certification details...");
        AppExecutor.runWorker(
            "load-cert-edit",
            () -> {
                for (Certification c : certificationService.getCertifications(currentStudent.getStudentId())) {
                    if (c.getCertificationId() == certId) return c;
                }
                return null;
            },
            this,
            currentCert -> {
                setBusy(false, "Ready");
                if (currentCert == null) {
                    JOptionPane.showMessageDialog(this, "Certification could not be found.");
                    return;
                }
                openEditCertDialog(currentCert);
            },
            ex -> {
                setBusy(false, "Ready");
                JOptionPane.showMessageDialog(this, "Error retrieving certification: " + ex.getMessage());
            }
        );
    }

    private void openEditCertDialog(Certification currentCert) {
        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        JTextField nameField = new JTextField(currentCert.getCertificateName());
        JTextField orgField = new JTextField(currentCert.getIssuingOrganization());
        JTextField dateField = new JTextField(currentCert.getIssueDate() != null ? ValidationUtil.formatDate(currentCert.getIssueDate()) : "");
        JTextField credField = new JTextField(currentCert.getCredentialId() != null ? currentCert.getCredentialId() : "");

        panel.add(new JLabel("Certification Name *:"));
        panel.add(nameField);
        panel.add(new JLabel("Issuing Organization *:"));
        panel.add(orgField);
        panel.add(new JLabel("Issue Date (YYYY-MM-DD) *:"));
        panel.add(dateField);
        panel.add(new JLabel("Credential ID:"));
        panel.add(credField);

        int res = JOptionPane.showConfirmDialog(this, panel, "Edit Certification", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String org = orgField.getText().trim();
            String dateStr = dateField.getText().trim();

            if (!validateCertInputs(name, org, dateStr)) {
                return;
            }

            Date parsedDate = ValidationUtil.parseDate(dateStr);
            currentCert.setCertificateName(name);
            currentCert.setIssuingOrganization(org);
            currentCert.setIssueDate(parsedDate);
            currentCert.setCredentialId(credField.getText().trim());

            setBusy(true, "Updating certification...");
            AppExecutor.runWorker(
                "update-cert",
                () -> {
                    certificationService.updateCertification(currentCert);
                    return null;
                },
                this,
                r -> {
                    setBusy(false, "Certification updated");
                    refreshCertsAndProjectsAsync();
                    refreshCatalogsAsync();
                    JOptionPane.showMessageDialog(this, "Certification updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                },
                ex -> {
                    setBusy(false, "Ready");
                    if (ex instanceof ValidationException) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Error updating certification: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            );
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
            setBusy(true, "Removing certification...");
            AppExecutor.runWorker(
                "delete-cert",
                () -> {
                    certificationService.deleteCertification(certId, currentStudent.getStudentId());
                    return null;
                },
                this,
                res -> {
                    setBusy(false, "Certification removed");
                    refreshCertsAndProjectsAsync();
                },
                ex -> {
                    setBusy(false, "Ready");
                    if (ex instanceof UnauthorizedAccessException) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Authorization Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Error: " + ValidationUtil.sanitizeErrorMessage((Exception) ex), "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            );
        }
    }

    private void showAddProjectDialog() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        JTextField titleField = new JTextField();
        JTextField descField = new JTextField();
        JTextField techField = new JTextField("Java, Swing, MySQL");
        JTextField urlField = new JTextField("https://github.com/...");

        panel.add(new JLabel("Project Title *:"));
        panel.add(titleField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        panel.add(new JLabel("Technologies Used *:"));
        panel.add(techField);
        panel.add(new JLabel("Project Link / Repo URL:"));
        panel.add(urlField);

        int res = JOptionPane.showConfirmDialog(this, panel, "Add Academic Project", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String tech = techField.getText().trim();

            if (!validateProjectInputs(title, tech)) {
                return;
            }

            Project p = new Project(0, currentStudent.getStudentId(), title, descField.getText().trim(), tech, urlField.getText().trim());
            setBusy(true, "Adding academic project...");
            AppExecutor.runWorker(
                "add-project",
                () -> {
                    projectService.addProject(p);
                    return null;
                },
                this,
                r -> {
                    setBusy(false, "Project added");
                    refreshCertsAndProjectsAsync();
                    JOptionPane.showMessageDialog(this, "Academic project added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                },
                ex -> {
                    setBusy(false, "Ready");
                    if (ex instanceof ValidationException) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Error adding project: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            );
        }
    }

    private void showEditProjectDialog() {
        int row = projTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an academic project to edit.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int pid = (int) projectsModel.getValueAt(row, 0);

        setBusy(true, "Loading project details...");
        AppExecutor.runWorker(
            "load-proj-edit",
            () -> {
                for (Project p : projectService.getProjects(currentStudent.getStudentId())) {
                    if (p.getProjectId() == pid) return p;
                }
                return null;
            },
            this,
            currentProj -> {
                setBusy(false, "Ready");
                if (currentProj == null) {
                    JOptionPane.showMessageDialog(this, "Project could not be found.");
                    return;
                }
                openEditProjectDialog(currentProj);
            },
            ex -> {
                setBusy(false, "Ready");
                JOptionPane.showMessageDialog(this, "Error retrieving project: " + ex.getMessage());
            }
        );
    }

    private void openEditProjectDialog(Project currentProj) {
        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        JTextField titleField = new JTextField(currentProj.getTitle());
        JTextField descField = new JTextField(currentProj.getDescription() != null ? currentProj.getDescription() : "");
        JTextField techField = new JTextField(currentProj.getTechnologiesUsed() != null ? currentProj.getTechnologiesUsed() : "");
        JTextField urlField = new JTextField(currentProj.getProjectUrl() != null ? currentProj.getProjectUrl() : "");

        panel.add(new JLabel("Project Title *:"));
        panel.add(titleField);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        panel.add(new JLabel("Technologies Used *:"));
        panel.add(techField);
        panel.add(new JLabel("Project Link / Repo URL:"));
        panel.add(urlField);

        int res = JOptionPane.showConfirmDialog(this, panel, "Edit Academic Project", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String tech = techField.getText().trim();

            if (!validateProjectInputs(title, tech)) {
                return;
            }

            currentProj.setTitle(title);
            currentProj.setDescription(descField.getText().trim());
            currentProj.setTechnologiesUsed(tech);
            currentProj.setProjectUrl(urlField.getText().trim());

            setBusy(true, "Updating academic project...");
            AppExecutor.runWorker(
                "update-project",
                () -> {
                    projectService.updateProject(currentProj, currentStudent.getStudentId());
                    return null;
                },
                this,
                r -> {
                    setBusy(false, "Project updated");
                    refreshCertsAndProjectsAsync();
                    JOptionPane.showMessageDialog(this, "Academic project updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                },
                ex -> {
                    setBusy(false, "Ready");
                    if (ex instanceof UnauthorizedAccessException) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Authorization Error", JOptionPane.ERROR_MESSAGE);
                    } else if (ex instanceof ValidationException) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Error updating project: " + ValidationUtil.sanitizeErrorMessage((Exception) ex), "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            );
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
            setBusy(true, "Removing academic project...");
            AppExecutor.runWorker(
                "delete-project",
                () -> {
                    projectService.deleteProject(pid, currentStudent.getStudentId());
                    return null;
                },
                this,
                res -> {
                    setBusy(false, "Project removed");
                    refreshCertsAndProjectsAsync();
                },
                ex -> {
                    setBusy(false, "Ready");
                    if (ex instanceof UnauthorizedAccessException) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Authorization Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Error: " + ValidationUtil.sanitizeErrorMessage((Exception) ex), "Database Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            );
        }
    }

    private static class CertInitData {
        final List<CertificationSkillMapping> suggestions;
        final List<Skill> allSkills;
        CertInitData(List<CertificationSkillMapping> suggestions, List<Skill> allSkills) {
            this.suggestions = suggestions;
            this.allSkills = allSkills;
        }
    }

    private boolean validateCertInputs(String name, String org, String dateStr) {
        if (!ValidationUtil.isNonEmpty(name)) {
            JOptionPane.showMessageDialog(this, "Certification Name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!ValidationUtil.isNonEmpty(org)) {
            JOptionPane.showMessageDialog(this, "Issuing Organization is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!ValidationUtil.isValidDate(dateStr)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid issue date in YYYY-MM-DD format.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean validateProjectInputs(String title, String tech) {
        if (!ValidationUtil.isNonEmpty(title)) {
            JOptionPane.showMessageDialog(this, "Project Title is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!ValidationUtil.isNonEmpty(tech)) {
            JOptionPane.showMessageDialog(this, "Technologies Used is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
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

    public static class HyperlinkRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value != null && !value.toString().trim().isEmpty()) {
                String val = value.toString().trim();
                lbl.setText("<html><u><font color='#2563eb'>" + escapeHtml(val) + "</font></u></html>");
                lbl.setToolTipText("Click to open " + val + " in browser");
            } else {
                lbl.setText("-");
                lbl.setToolTipText(null);
            }
            return lbl;
        }
    }

    private void showResumeDialog() {
        setBusy(true, "Compiling professional resume...");
        AppExecutor.runWorker(
            "generate-resume",
            () -> resumeService.buildResumeData(currentStudent.getStudentId()),
            this,
            data -> {
                setBusy(false, "Ready");
                new ResumeDialog(this, data, resumeService).setVisible(true);
            },
            ex -> {
                setBusy(false, "Ready");
                JOptionPane.showMessageDialog(this, "Error compiling resume: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        );
    }

    public static void openWebUrl(Component parent, String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No web link provided for this entry.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String url = rawUrl.trim();
        if (!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")) {
            url = "https://" + url;
        }
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                JTextField urlField = new JTextField(url);
                urlField.setEditable(false);
                JOptionPane.showMessageDialog(parent, urlField, "Website Link", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JTextField urlField = new JTextField(url);
            urlField.setEditable(false);
            JOptionPane.showMessageDialog(parent, urlField, "Website Link", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
