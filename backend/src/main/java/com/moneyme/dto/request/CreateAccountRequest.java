package com.moneyme.dto.request;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public class CreateAccountRequest {

    @DecimalMin(value = "0.0", inclusive = true, message = "Initial deposit cannot be negative")
    private BigDecimal initialDeposit = BigDecimal.ZERO;

    private String currency = "USD";

    public CreateAccountRequest() {}

    public CreateAccountRequest(BigDecimal initialDeposit, String currency) {
        this.initialDeposit = initialDeposit != null ? initialDeposit : BigDecimal.ZERO;
        this.currency = currency != null ? currency : "USD";
    }

    public BigDecimal getInitialDeposit() {
        return initialDeposit;
    }

    public void setInitialDeposit(BigDecimal initialDeposit) {
        this.initialDeposit = initialDeposit;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}

