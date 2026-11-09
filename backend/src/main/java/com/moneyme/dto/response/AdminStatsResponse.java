package com.moneyme.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class AdminStatsResponse {

    private long totalUsers;
    private long totalAccounts;
    private long totalTransactions;
    private BigDecimal totalVolume;
    private List<TransactionResponse> recentTransactions;
    private List<UserResponse> users;

    public AdminStatsResponse() {}

    public AdminStatsResponse(long totalUsers, long totalAccounts, long totalTransactions,
                              BigDecimal totalVolume, List<TransactionResponse> recentTransactions,
                              List<UserResponse> users) {
        this.totalUsers = totalUsers;
        this.totalAccounts = totalAccounts;
        this.totalTransactions = totalTransactions;
        this.totalVolume = totalVolume != null ? totalVolume : BigDecimal.ZERO;
        this.recentTransactions = recentTransactions;
        this.users = users;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalAccounts() {
        return totalAccounts;
    }

    public void setTotalAccounts(long totalAccounts) {
        this.totalAccounts = totalAccounts;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public BigDecimal getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(BigDecimal totalVolume) {
        this.totalVolume = totalVolume;
    }

    public List<TransactionResponse> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<TransactionResponse> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }

    public List<UserResponse> getUsers() {
        return users;
    }

    public void setUsers(List<UserResponse> users) {
        this.users = users;
    }
}
