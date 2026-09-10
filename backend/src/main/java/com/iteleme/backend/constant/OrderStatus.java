package com.iteleme.backend.constant;

public final class OrderStatus {
    public static final int CANCELED = -1;
    public static final int UNPAID = 0;
    public static final int PAID = 1;
    public static final int COMPLETED = 2;

    private OrderStatus() {
    }
}
