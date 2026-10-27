package com.sinst.service;

import com.sinst.dao.DashboardDAO;
import com.sinst.dao.DashboardDAOImpl;
import com.sinst.exception.DatabaseException;
import com.sinst.model.DashboardStats;

import java.sql.SQLException;
import java.util.Map;

public class DashboardServiceImpl implements DashboardService {

    private final DashboardDAO dashboardDAO;

    public DashboardServiceImpl() {
        this(DashboardDAOImpl.getInstance());
    }

    public DashboardServiceImpl(DashboardDAO dashboardDAO) {
        this.dashboardDAO = dashboardDAO;
    }

    @Override
    public DashboardStats getDashboardStats(int studentId) throws DatabaseException {
        try {
            return dashboardDAO.getStats(studentId);
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving dashboard stats: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Integer> getDashboardCounts(int studentId) throws DatabaseException {
        try {
            return dashboardDAO.getCountsMap(studentId);
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving dashboard counts: " + e.getMessage(), e);
        }
    }
}
