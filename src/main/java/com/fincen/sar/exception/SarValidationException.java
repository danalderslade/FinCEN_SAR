package com.fincen.sar.exception;

import java.util.List;

/**
 * Thrown when FinCEN SAR business-rule validation fails.
 */
public class SarValidationException extends RuntimeException {

    private final List<String> violations;

    public SarValidationException(List<String> violations) {
        super(String.join("; ", violations));
        this.violations = List.copyOf(violations);
    }

    public SarValidationException(String violation) {
        this(List.of(violation));
    }

    public List<String> getViolations() {
        return violations;
    }
}
