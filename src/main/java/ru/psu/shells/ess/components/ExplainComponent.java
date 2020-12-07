package ru.psu.shells.ess.components;

import ru.psu.shells.ess.model.entity.Fact;
import ru.psu.shells.ess.model.entity.Rule;
import ru.psu.shells.ess.model.entity.VarType;
import ru.psu.shells.ess.model.entity.Variable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.Enumeration;
import java.util.Optional;

public class ExplainComponent {
    private final WorkingMemory workingMemory;
    private final JTree tree;
    private final DefaultListModel<Fact> listModel;

    public ExplainComponent(WorkingMemory workingMemory, JTree tree, DefaultListModel<Fact> listModel) {
        this.workingMemory = workingMemory;
        this.tree = tree;
        this.listModel = listModel;
    }

    public void explain() {
        buildExplainTree(workingMemory.getGoal(), null, tree);
        // в одну строчку добавляем все означенные переменные в лист
        workingMemory.getKnownFacts().forEach(listModel::addElement);
    }

    // строит дерево объяснения доказательства
    private void buildExplainTree(Variable variable, DefaultMutableTreeNode parent, JTree tree) {
        String strGoal = "Цель: " + variable.getName();
        // полуаем факт, который был доказан
        // optional типы - аналог ? типов из сишарпа (например int?)
        Optional<Fact> optionalAnswerFact = workingMemory.getKnownFacts().stream()
                .filter(fact -> fact.getVariable().equals(variable))
                .findFirst();
        String strAnswer = optionalAnswerFact.map(fact -> strGoal + " = " + fact.getValue().getValue())
                .orElseGet(() -> strGoal + " НЕ УДАЛОСЬ НАЙТИ");

        if (variable.getType() == VarType.REQUESTED) {
            strAnswer += " [ЗАПРОШЕНА]";
        }

        // нода дерева
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(strAnswer);
        if (parent != null) {
            parent.add(node);
        } else {
            ((DefaultTreeModel) tree.getModel()).setRoot(node);
        }

        // правило, которое использовалось для доказательства этой переменной
        Rule inferenceRule = workingMemory.getInferenceRulesMap().get(variable);
        if (inferenceRule != null) {
            node.add(new DefaultMutableTreeNode(inferenceRule));

            // для всех фактов посылки повторяем
            for (Fact fact : inferenceRule.getCondition()) {
                buildExplainTree(fact.getVariable(), node, tree);
            }

            // объяснение
            DefaultMutableTreeNode explanationNode = new DefaultMutableTreeNode("Объяснение " + inferenceRule);
            explanationNode.add(new DefaultMutableTreeNode(inferenceRule.getExplanation()));
            node.add(explanationNode);
        }

    }

    // перед тем, как отобразить дерево на форме, закрываем ноды с объяснениями - длинные строчки
    public void collapseExplanation() {
        Enumeration<DefaultMutableTreeNode> e = ((DefaultMutableTreeNode) tree.getModel().getRoot()).depthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = e.nextElement();
            if (node.toString().startsWith("Объяснение R")) {
                tree.collapsePath(new TreePath(node.getPath()));
            }
        }
    }
}
