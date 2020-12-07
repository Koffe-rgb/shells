package ru.psu.shells.ess.model.entity;

import java.io.Serializable;
import java.util.ArrayList;

public class Domain implements Serializable {
    private String name;
    private ArrayList<DomainValue> values;

    public Domain(String name) {
        this.name = name;
        this.values = new ArrayList<DomainValue>();
    }

    public void setValues(ArrayList<DomainValue> values) {
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<DomainValue> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return name;
    }
}
