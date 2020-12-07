package ru.psu.shells.ess.model.entity;

import java.io.Serializable;

public class Fact implements Serializable {
    private Variable variable;
    private DomainValue value;

    public Fact(Variable variable, DomainValue value) {
        this.variable = variable;
        this.value = value;
    }

    public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }

    public DomainValue getValue() {
        return value;
    }

    public void setValue(DomainValue value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return variable + " = " + value;
    }
}
