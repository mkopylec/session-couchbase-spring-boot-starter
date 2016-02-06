package com.github.mkopylec.sessioncouchbase;

import java.io.Serializable;
import java.util.Objects;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private String text;
    private Integer number;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(text, message.text) &&
                Objects.equals(number, message.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, number);
    }
}
