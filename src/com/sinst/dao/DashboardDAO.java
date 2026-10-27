package com.sinst.dao;

import com.sinst.model.DashboardStats;
import java.sql.SQLException;
import java.util.Map;

public interface DashboardDAO {
    DashboardStats getStats(int studentId) throws SQLException;
    Map<String, Integer> getCountsMap(int studentId) throws SQLException;
}
