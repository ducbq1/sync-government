package org.webflux.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.webflux.enumerate.Status;

@Data
public class Toast {

    public Toast(Status status) {
        this.status = status.name().toLowerCase();
    }

    public Toast(Status status, Object data) {
        this.status = status.name().toLowerCase();
        this.data = data;
    }

    public Toast(Status status, String title, String message) {
        this.status = status.name().toLowerCase();
        this.title = title;
        this.message = message;
    }

    public Toast(Status status, String title, String message, Object data) {
        this.status = status.name().toLowerCase();
        this.title = title;
        this.message = message;
        this.data = data;
    }

    private String status;
    private String title;
    private String message;
    private Object data;
}
