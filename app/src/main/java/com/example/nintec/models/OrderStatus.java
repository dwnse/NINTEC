package com.example.nintec.models;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    COMPLETED,
    CANCELLED,
    REJECTED;

    public String toApiString() {
        return name().toLowerCase();
    }

    public static OrderStatus fromApiString(String s) {
        if (s == null) return PENDING;
        switch (s.toLowerCase()) {
            case "completed":
            case "delivered":
                return COMPLETED;
            case "confirmed":
                return CONFIRMED;
            case "processing":
                return PROCESSING;
            case "shipped":
                return SHIPPED;
            case "cancelled":
            case "rejected":
                return CANCELLED;
            case "pending":
            default:
                return PENDING;
        }
    }

    public String getDisplayName() {
        switch (this) {
            case COMPLETED: return "Finalizado";
            case CONFIRMED: return "Confirmado";
            case PROCESSING: return "En Preparación";
            case SHIPPED: return "Enviado";
            case DELIVERED: return "Entregado";
            case CANCELLED: return "Cancelado";
            case REJECTED: return "Rechazado";
            case PENDING:
            default: return "Pendiente";
        }
    }

    /** Returns the step index (0-3) for the timeline stepper */
    public int getStepIndex() {
        switch (this) {
            case CONFIRMED: return 1;
            case PROCESSING: return 2;
            case SHIPPED:
            case DELIVERED:
            case COMPLETED: return 3;
            case CANCELLED:
            case REJECTED: return -1;
            case PENDING:
            default: return 0;
        }
    }
}