package com.sinst.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardStats {
    private int totalApplications;
    private int applied;
    private int shortlisted;
    private int interview;
    private int selected;
    private int rejected;
    private int upcomingInterviews;
    private int totalSkills;

    public DashboardStats() {}

    public DashboardStats(int totalApplications, int applied, int shortlisted, int interview, int selected, int rejected, int upcomingInterviews, int totalSkills) {
        this.totalApplications = totalApplications;
        this.applied = applied;
        this.shortlisted = shortlisted;
        this.interview = interview;
        this.selected = selected;
        this.rejected = rejected;
        this.upcomingInterviews = upcomingInterviews;
        this.totalSkills = totalSkills;
    }

    public int getTotalApplications() { return totalApplications; }
    public int getTotal() { return totalApplications; }
    public void setTotalApplications(int totalApplications) { this.totalApplications = totalApplications; }

    public int getApplied() { return applied; }
    public void setApplied(int applied) { this.applied = applied; }

    public int getShortlisted() { return shortlisted; }
    public void setShortlisted(int shortlisted) { this.shortlisted = shortlisted; }

    public int getInterview() { return interview; }
    public void setInterview(int interview) { this.interview = interview; }

    public int getSelected() { return selected; }
    public void setSelected(int selected) { this.selected = selected; }

    public int getRejected() { return rejected; }
    public void setRejected(int rejected) { this.rejected = rejected; }

    public int getUpcomingInterviews() { return upcomingInterviews; }
    public void setUpcomingInterviews(int upcomingInterviews) { this.upcomingInterviews = upcomingInterviews; }

    public int getTotalSkills() { return totalSkills; }
    public void setTotalSkills(int totalSkills) { this.totalSkills = totalSkills; }

    public int getUnderReview() {
        return applied + shortlisted;
    }

    public Map<String, Integer> asMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("Total", totalApplications);
        map.put("Applied", applied);
        map.put("Shortlisted", shortlisted);
        map.put("Interview", interview);
        map.put("Selected", selected);
        map.put("Rejected", rejected);
        map.put("UpcomingInterviews", upcomingInterviews);
        map.put("TotalSkills", totalSkills);
        return map;
    }

    public static DashboardStats fromMap(Map<String, Integer> map) {
        if (map == null) return new DashboardStats();
        return new DashboardStats(
            map.getOrDefault("Total", 0),
            map.getOrDefault("Applied", 0),
            map.getOrDefault("Shortlisted", 0),
            map.getOrDefault("Interview", 0),
            map.getOrDefault("Selected", 0),
            map.getOrDefault("Rejected", 0),
            map.getOrDefault("UpcomingInterviews", 0),
            map.getOrDefault("TotalSkills", 0)
        );
    }
}
