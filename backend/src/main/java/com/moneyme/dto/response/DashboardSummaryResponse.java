package com.moneyme.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSummaryResponse {

    private List<AccountResponse> accounts;
    private BigDecimal totalBalance;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private List<TransactionResponse> recentTransactions;

    public DashboardSummaryResponse() {}

    public DashboardSummaryResponse(List<AccountResponse> accounts, BigDecimal totalBalance,
                                    BigDecimal totalDeposits, BigDecimal totalWithdrawals,
                                    List<TransactionResponse> recentTransactions) {
        this.accounts = accounts;
        this.totalBalance = totalBalance != null ? totalBalance : BigDecimal.ZERO;
        this.totalDeposits = totalDeposits != null ? totalDeposits : BigDecimal.ZERO;
        this.totalWithdrawals = totalWithdrawals != null ? totalWithdrawals : BigDecimal.ZERO;
        this.recentTransactions = recentTransactions;
    }

    public List<AccountResponse> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountResponse> accounts) {
        this.accounts = accounts;
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }

    public BigDecimal getTotalDeposits() {
        return totalDeposits;
    }

    public void setTotalDeposits(BigDecimal totalDeposits) {
        this.totalDeposits = totalDeposits;
    }

    public BigDecimal getTotalWithdrawals() {
        return totalWithdrawals;
    }

    public void setTotalWithdrawals(BigDecimal totalWithdrawals) {
        this.totalWithdrawals = totalWithdrawals;
    }

    public List<TransactionResponse> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<TransactionResponse> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }
}

