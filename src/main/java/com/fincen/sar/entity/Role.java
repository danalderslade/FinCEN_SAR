package com.fincen.sar.entity;

public enum Role {
    ANALYST,    // Creates and edits SAR filings
    REVIEWER,   // Reviews filings before submission
    APPROVER,   // Approves and submits filings to FinCEN
    ADMIN       // System administration, user management
}
