package com.sinst.service;

import com.sinst.exception.DatabaseException;
import com.sinst.exception.UnauthorizedAccessException;
import com.sinst.exception.ValidationException;
import com.sinst.model.Skill;
import com.sinst.model.SkillEvidence;

import java.util.List;

public interface SkillService {
    List<Skill> getAllSkills() throws DatabaseException;
    List<Skill> getStudentSkills(int studentId) throws DatabaseException;
    void addOrUpdateStudentSkill(int studentId, int skillId, String proficiency) throws ValidationException, DatabaseException, UnauthorizedAccessException;
    void removeStudentSkill(int studentId, int skillId) throws DatabaseException, UnauthorizedAccessException;

    // Skill evidence methods
    List<SkillEvidence> getSkillEvidence(int studentId, int skillId) throws DatabaseException;
    List<SkillEvidence> getStudentEvidence(int studentId) throws DatabaseException;
    void addEvidence(SkillEvidence evidence) throws ValidationException, DatabaseException, UnauthorizedAccessException;
}
