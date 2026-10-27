package com.sinst.db;

import com.sinst.dao.*;
import com.sinst.model.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * AppDAO provides a backward-compatible Facade delegating database operations
 * to the focused DAO implementations (StudentDAO, SkillDAO, ApplicationDAO, etc.).
 */
public class AppDAO {

    private static final StudentDAO studentDAO = StudentDAOImpl.getInstance();
    private static final SkillDAO skillDAO = SkillDAOImpl.getInstance();
    private static final ApplicationDAO applicationDAO = ApplicationDAOImpl.getInstance();
    private static final InterviewDAO interviewDAO = InterviewDAOImpl.getInstance();
    private static final CertificationDAO certificationDAO = CertificationDAOImpl.getInstance();
    private static final ProjectDAO projectDAO = ProjectDAOImpl.getInstance();
    private static final CompanyDAO companyDAO = CompanyDAOImpl.getInstance();
    private static final RoleDAO roleDAO = RoleDAOImpl.getInstance();
    private static final DashboardDAO dashboardDAO = DashboardDAOImpl.getInstance();

    // =========================================================================
    // 1. STUDENT AUTHENTICATION & PROFILE
    // =========================================================================

    public static Student login(String identifier, String password) throws SQLException {
        return studentDAO.login(identifier, password);
    }

    public static Student getStudentById(int studentId) throws SQLException {
        return studentDAO.getById(studentId);
    }

    public static boolean registerStudent(Student s) throws SQLException {
        return studentDAO.register(s);
    }

    public static boolean updateStudentProfile(Student s) throws SQLException {
        return studentDAO.updateProfile(s);
    }

    public static boolean updateStudentProfile(Student s, int authenticatedStudentId) throws SQLException {
        return studentDAO.updateProfile(s, authenticatedStudentId);
    }

    // =========================================================================
    // 2. DASHBOARD AGGREGATES & UPCOMING INTERVIEWS
    // =========================================================================

    public static Map<String, Integer> getDashboardCounts(int studentId) throws SQLException {
        return dashboardDAO.getCountsMap(studentId);
    }

    public static List<Interview> getUpcomingInterviews(int studentId) throws SQLException {
        return interviewDAO.getUpcoming(studentId);
    }

    // =========================================================================
    // 3. APPLICATIONS MANAGEMENT
    // =========================================================================

    public static List<Application> getApplications(int studentId) throws SQLException {
        return applicationDAO.getByStudentId(studentId);
    }

    public static boolean hasApplication(int studentId, int companyId, int roleId) throws SQLException {
        return applicationDAO.hasApplication(studentId, companyId, roleId);
    }

    public static boolean addApplication(Application a) throws SQLException {
        return applicationDAO.add(a);
    }

    public static boolean updateApplication(Application a) throws SQLException {
        return applicationDAO.update(a);
    }

    public static boolean updateApplication(Application a, int studentId) throws SQLException {
        return applicationDAO.update(a, studentId);
    }

    public static boolean updateApplicationStatus(int applicationId, String status) throws SQLException {
        return applicationDAO.updateStatus(applicationId, status);
    }

    public static boolean updateApplicationStatus(int applicationId, int studentId, String status) throws SQLException {
        return applicationDAO.updateStatus(applicationId, studentId, status);
    }

    public static boolean deleteApplication(int applicationId) throws SQLException {
        return applicationDAO.delete(applicationId);
    }

    public static boolean deleteApplication(int applicationId, int studentId) throws SQLException {
        return applicationDAO.delete(applicationId, studentId);
    }

    // =========================================================================
    // 4. INTERVIEWS MANAGEMENT
    // =========================================================================

    public static List<Interview> getInterviews(int studentId) throws SQLException {
        return interviewDAO.getByStudentId(studentId);
    }

    public static List<Interview> getInterviewsForApplication(int applicationId) throws SQLException {
        return interviewDAO.getByApplicationId(applicationId);
    }

    public static List<Interview> getInterviewsForApplication(int applicationId, int studentId) throws SQLException {
        return interviewDAO.getByApplicationId(applicationId, studentId);
    }

    public static boolean scheduleInterview(Interview i, int applicationId, String newStatus) throws SQLException {
        return interviewDAO.schedule(i, applicationId, newStatus);
    }

    public static boolean scheduleInterview(Interview i, int applicationId, int studentId, String newStatus) throws SQLException {
        return interviewDAO.schedule(i, applicationId, studentId, newStatus);
    }

    public static boolean addInterview(Interview i) throws SQLException {
        return interviewDAO.schedule(i, i.getApplicationId(), "Interview");
    }

    public static boolean updateInterview(int interviewId, String status, String outcome, String feedback) throws SQLException {
        return interviewDAO.update(interviewId, status, outcome, feedback);
    }

    public static boolean updateInterview(int interviewId, int studentId, String status, String outcome, String feedback) throws SQLException {
        return interviewDAO.update(interviewId, studentId, status, outcome, feedback);
    }

    public static boolean updateInterviewFull(Interview i) throws SQLException {
        return interviewDAO.updateFull(i);
    }

    public static boolean updateInterviewFull(Interview i, int studentId) throws SQLException {
        return interviewDAO.updateFull(i, studentId);
    }

    public static boolean deleteInterview(int interviewId) throws SQLException {
        return interviewDAO.delete(interviewId);
    }

    public static boolean deleteInterview(int interviewId, int studentId) throws SQLException {
        return interviewDAO.delete(interviewId, studentId);
    }

    // =========================================================================
    // 5. SKILLS & PROFILE MANAGEMENT
    // =========================================================================

    public static List<Skill> getAllSkills() throws SQLException {
        return skillDAO.getAllSkills();
    }

    public static List<Skill> getStudentSkills(int studentId) throws SQLException {
        return skillDAO.getStudentSkills(studentId);
    }

    public static boolean addOrUpdateStudentSkill(int studentId, int skillId, String proficiency) throws SQLException {
        return skillDAO.addOrUpdateStudentSkill(studentId, skillId, proficiency);
    }

    public static boolean deleteStudentSkill(int studentId, int skillId) throws SQLException {
        return skillDAO.deleteStudentSkill(studentId, skillId);
    }

    // =========================================================================
    // 6. CERTIFICATIONS & ACADEMIC PROJECTS
    // =========================================================================

    public static List<Certification> getCertifications(int studentId) throws SQLException {
        return certificationDAO.getByStudentId(studentId);
    }

    public static boolean addCertification(Certification c) throws SQLException {
        return certificationDAO.add(c);
    }

    public static boolean updateCertification(Certification c) throws SQLException {
        return certificationDAO.update(c);
    }

    public static boolean updateCertification(Certification c, int studentId) throws SQLException {
        return certificationDAO.update(c, studentId);
    }

    public static boolean deleteCertification(int certId) throws SQLException {
        return certificationDAO.delete(certId);
    }

    public static boolean deleteCertification(int certId, int studentId) throws SQLException {
        return certificationDAO.delete(certId, studentId);
    }

    public static List<Project> getProjects(int studentId) throws SQLException {
        return projectDAO.getByStudentId(studentId);
    }

    public static boolean addProject(Project p) throws SQLException {
        return projectDAO.add(p);
    }

    public static boolean updateProject(Project p) throws SQLException {
        return projectDAO.update(p);
    }

    public static boolean updateProject(Project p, int studentId) throws SQLException {
        return projectDAO.update(p, studentId);
    }

    public static boolean deleteProject(int projectId) throws SQLException {
        return projectDAO.delete(projectId);
    }

    public static boolean deleteProject(int projectId, int studentId) throws SQLException {
        return projectDAO.delete(projectId, studentId);
    }

    // =========================================================================
    // 7. ROLES & COMPANIES CATALOG
    // =========================================================================

    public static List<Role> getAllRoles() throws SQLException {
        return roleDAO.getAll();
    }

    public static List<Integer> getRoleSkillIds(int roleId) throws SQLException {
        return roleDAO.getRoleSkillIds(roleId);
    }

    public static boolean saveRole(Role role, List<Integer> requiredSkillIds) throws SQLException {
        return roleDAO.save(role, requiredSkillIds);
    }

    public static boolean deleteRole(int roleId) throws SQLException {
        return roleDAO.delete(roleId);
    }

    public static List<Company> getAllCompanies() throws SQLException {
        return companyDAO.getAll();
    }

    public static boolean saveCompany(Company company) throws SQLException {
        return companyDAO.save(company);
    }

    public static boolean deleteCompany(int companyId) throws SQLException {
        return companyDAO.delete(companyId);
    }

    // =========================================================================
    // 8. SKILL-GAP ENGINE
    // =========================================================================

    public static List<SkillGapResult> getSkillGapAnalysis(int studentId, int roleId) throws SQLException {
        return skillDAO.getSkillGapAnalysis(studentId, roleId);
    }
}
