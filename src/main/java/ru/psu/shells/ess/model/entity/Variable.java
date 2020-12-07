package ru.psu.shells.ess.model.entity;

import java.io.Serializable;

public class Variable implements Serializable {
    private String name;
    private Domain domain;
    private String question;
    private VarType type;

    public Variable(String name, Domain domain, String question, VarType type) {
        this.name = name;
        this.domain = domain;
        this.question = question;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public VarType getType() {
        return type;
    }

    public void setType(VarType type) {
        this.type = type;
    }

    public String getStringType() {
        String stringType = "Запрашиваемая";
        switch (type) {
            case REQUESTED: stringType = "Запрашиваемая"; break;
            case INFERRED: stringType = "Выводимая"; break;
            case INFER_REQUESTED: stringType = "Выводимо-запрашиваемая"; break;
        }
        return stringType;
    }

    @Override
    public String toString() {
        return name;
    }
}
