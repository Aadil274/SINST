package com.sinst.dao;

import com.sinst.model.Skill;
import com.sinst.model.SkillGapResult;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface SkillDAO {
    List<Skill> getAllSkills() throws SQLException;
    List<Skill> getStudentSkills(int studentId) throws SQLException;
    boolean hasStudentSkill(int studentId, int skillId) throws SQLException;
    boolean addOrUpdateStudentSkill(int studentId, int skillId, String proficiency) throws SQLException;
    boolean addOrUpdateStudentSkill(int studentId, int skillId, String proficiency, Connection conn) throws SQLException;
    String getStudentSkillProficiency(int studentId, int skillId, Connection conn) throws SQLException;
    boolean deleteStudentSkill(int studentId, int skillId) throws SQLException;
    List<SkillGapResult> getSkillGapAnalysis(int studentId, int roleId) throws SQLException;
}
