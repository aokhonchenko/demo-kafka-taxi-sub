package ru.x5.demo.kafka.saga.model;

import ru.x5.demo.kafka.saga.enums.TaxiStatus;

public class Result {

    private String author = "transfer";
    private Integer ticketId;
    private TaxiStatus status;
    private Integer orderId;

    // region g/s

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getTicketId() {
        return ticketId;
    }

    public void setTicketId(Integer ticketId) {
        this.ticketId = ticketId;
    }

    public TaxiStatus getStatus() {
        return status;
    }

    public void setStatus(TaxiStatus status) {
        this.status = status;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    // endregion

}
