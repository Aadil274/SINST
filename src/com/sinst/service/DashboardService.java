package com.sinst.service;

import com.sinst.exception.DatabaseException;
import com.sinst.model.DashboardStats;

import java.util.Map;

public interface DashboardService {
    DashboardStats getDashboardStats(int studentId) throws DatabaseException;
    Map<String, Integer> getDashboardCounts(int studentId) throws DatabaseException;
}
