package ru.psu.shells.ess.components;

import ru.psu.shells.ess.model.entity.Fact;
import ru.psu.shells.ess.model.entity.Rule;
import ru.psu.shells.ess.model.entity.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkingMemory {
    private final Variable goal;
    private final List<Rule> untriggeredRules;
    private final List<Fact> knownFacts;
    private final Map<Variable, Rule> inferenceRulesMap;

    public WorkingMemory(Variable goal) {
        this.goal = goal;
        this.untriggeredRules = new ArrayList<>();
        this.knownFacts = new ArrayList<>();
        this.inferenceRulesMap = new HashMap<>();
    }

    public Variable getGoal() {
        return goal;
    }

    public List<Rule> getUntriggeredRules() {
        return untriggeredRules;
    }

    public List<Fact> getKnownFacts() {
        return knownFacts;
    }

    public Map<Variable, Rule> getInferenceRulesMap() {
        return inferenceRulesMap;
    }
}
