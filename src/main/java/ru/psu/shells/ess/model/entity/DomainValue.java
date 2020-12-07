package ru.psu.shells.ess.model.entity;

import java.io.Serializable;

public class DomainValue implements Serializable {
    private String value;

    public DomainValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
