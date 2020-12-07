package ru.psu.shells.ess.frames;

import ru.psu.shells.ess.components.ExplainComponent;
import ru.psu.shells.ess.components.WorkingMemory;
import ru.psu.shells.ess.model.entity.DomainValue;
import ru.psu.shells.ess.model.entity.Fact;
import ru.psu.shells.ess.model.entity.Rule;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ConsultationResultFrame extends JDialog {
    private JTree tvRules;
    private JList<Fact> lstFacts;
    private JButton btnOk;
    private JButton btnCollapse;
    private JLabel lblResult;
    private JPanel rPanel;

    private boolean isExpanded = true;

    DefaultListModel<Fact> listModel = new DefaultListModel<>();

    List<Fact> selected = new ArrayList<>();

    public ConsultationResultFrame(Frame owner, WorkingMemory workingMemory, DomainValue answer) {
        super(owner);
        this.setContentPane(rPanel);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        DefaultTreeModel treeModel = new DefaultTreeModel(null);


        tvRules.setModel(treeModel);
        lstFacts.setModel(listModel);

        tvRules.addTreeSelectionListener(tvRulesSL);
        lstFacts.setCellRenderer(new MyCellRender(selected));

        ExplainComponent explainComponent = new ExplainComponent(workingMemory, tvRules, listModel);
        explainComponent.explain();
        expandTree(tvRules, isExpanded);
        explainComponent.collapseExplanation();

        String answerText = "ОТВЕТ: "  + workingMemory.getGoal().getName() + " = ";
        answerText += (answer != null) ? answer.getValue() : "НЕИЗВЕСТНО";
        this.lblResult.setText(answerText);

        btnCollapse.addActionListener(btnCollapseAL);
        btnOk.addActionListener(e -> dispose());

        this.setTitle("Режим объяснения");
        this.setSize(600, 500);
        this.setModal(true);
        this.setLocationRelativeTo(owner);
        this.setResizable(false);
        this.setVisible(true);
    }

    private final ActionListener btnCollapseAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            isExpanded = !isExpanded;
            String text = isExpanded ? "Свернуть" : "Развернуть";
            btnCollapse.setText(text);
            expandTree(tvRules, isExpanded);
        }
    };

    private final TreeSelectionListener tvRulesSL = new TreeSelectionListener() {
        @Override
        public void valueChanged(TreeSelectionEvent e) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tvRules.getLastSelectedPathComponent();
            if (node == null)
                return;

            lstFacts.clearSelection();
            selected.clear();

            Object nodeInfo = node.getUserObject();
            if (nodeInfo instanceof Rule) {
                List<Fact> condition = ((Rule) nodeInfo).getCondition();

                Rule rule = (Rule) nodeInfo;

                int j = 0;
                for (int i = 0; i < listModel.size() && j < condition.size(); ++i) {
                    Fact listFact = listModel.get(i);
                    Fact ruleFact = condition.get(j);

                    if (listFact.getVariable().equals(ruleFact.getVariable()) &&
                            listFact.getValue().equals(ruleFact.getValue())) {
                        selected.add(listFact);
                        j++;
                    }
                }

                int idx = listModel.indexOf(rule.getConclusion());
                selected.add(listModel.get(idx));

            }
            lstFacts.updateUI();
        }
    };

    private static class MyCellRender extends DefaultListCellRenderer {
        List<Fact> selected;

        public MyCellRender(List<Fact> selected) {
            this.selected = selected;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Fact fact = (Fact) value;

            if (selected.contains(fact)) {
                setBackground(Color.GRAY);
                if (selected.indexOf(fact) != selected.size() - 1) {
                    setForeground(Color.GREEN);
                    setFont(list.getFont().deriveFont(Font.BOLD));
                } else {
                    setForeground(Color.YELLOW);
                    setFont(list.getFont().deriveFont(Font.BOLD));
                }
            } else {
                setForeground(list.getForeground());
                setBackground(list.getBackground());
                setFont(list.getFont());
            }

            return this;
        }
    }

    private void expandTree(JTree tree, boolean expand) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        expandAll(tree, new TreePath(root), expand);
    }

    private void expandAll(JTree tree, TreePath path, boolean expand) {
        TreeNode node = (TreeNode) path.getLastPathComponent();

        if (node.getChildCount() >= 0) {
            Enumeration enumeration = node.children();
            while (enumeration.hasMoreElements()) {
                TreeNode n = (TreeNode) enumeration.nextElement();
                TreePath p = path.pathByAddingChild(n);

                expandAll(tree, p, expand);
            }
        }

        if (expand) {
            tree.expandPath(path);
        } else {
            tree.collapsePath(path);
        }
    }
}
