package com.sinst.service;

import com.sinst.exception.DatabaseException;
import com.sinst.model.ResumeData;

/**
 * Service for aggregating student profile data and compiling professional,
 * ATS-compliant resumes across multiple presentation formats.
 */
public interface ResumeService {

    /**
     * Aggregates profile, skills, projects, certifications, and experience data for a student.
     *
     * @param studentId the unique student identifier
     * @return the compiled ResumeData model
     * @throws DatabaseException if a database error occurs
     */
    ResumeData buildResumeData(int studentId) throws DatabaseException;

    /**
     * Generates a clean, ATS-compliant Plain Text resume.
     *
     * @param data the compiled resume data
     * @return plain text resume string
     */
    String generatePlainText(ResumeData data);

    /**
     * Generates a structured, formatted Markdown resume suitable for developer portfolios,
     * GitHub profiles, and documentation engines.
     *
     * @param data the compiled resume data
     * @return markdown resume string
     */
    String generateMarkdown(ResumeData data);

    /**
     * Generates an elegant, print-ready HTML resume with embedded CSS styling.
     *
     * @param data the compiled resume data
     * @return standalone HTML document string
     */
    String generateHtml(ResumeData data);
}
