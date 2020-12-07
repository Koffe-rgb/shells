package ru.psu.shells.ess.model.entity;

import java.io.Serializable;
import java.util.ArrayList;

public class Rule implements Serializable {
    private String name;
    private ArrayList<Fact> condition;
    private Fact conclusion;
    private String explanation;

    public Rule(String name, Fact conclusion, String explanation) {
        this.name = name;
        this.condition = new ArrayList<>();
        this.conclusion = conclusion;
        this.explanation = explanation;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Fact> getCondition() {
        return condition;
    }

    public void setCondition(ArrayList<Fact> condition) {
        this.condition = condition;
    }

    public Fact getConclusion() {
        return conclusion;
    }

    public void setConclusion(Fact conclusion) {
        this.conclusion = conclusion;
    }

    @Override
    public String toString() {
        return name;
    }
}
