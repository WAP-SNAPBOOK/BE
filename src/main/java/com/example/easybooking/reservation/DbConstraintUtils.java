package com.example.easybooking.reservation;

public final class DbConstraintUtils {
    private DbConstraintUtils() {
    }

    public static boolean isUniqueConstraintViolation(Throwable t, String constraintName) {
        for (Throwable cur = t; cur != null; cur = cur.getCause()) {

            if (cur instanceof org.hibernate.exception.ConstraintViolationException h) {
                String name = h.getConstraintName();
                if (name != null && name.equalsIgnoreCase(constraintName)) {
                    return true;
                }
            }

            if (cur instanceof java.sql.SQLException sql) {
                // H2 unique
                if ("23505".equals(sql.getSQLState()) && containsIgnoreCase(sql.getMessage(), constraintName)) {
                    return true;
                }
                // MySQL duplicate key (more specific than SQLState 23000)
                if (sql.getErrorCode() == 1062 && containsIgnoreCase(sql.getMessage(), constraintName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean containsIgnoreCase(String s, String needle) {
        return s != null && needle != null && s.toLowerCase().contains(needle.toLowerCase());
    }
}