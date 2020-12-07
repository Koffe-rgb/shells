package ru.psu.shells.ess.frames;

import ru.psu.shells.ess.misc.DialogResult;
import ru.psu.shells.ess.misc.FactMode;
import ru.psu.shells.ess.misc.ModalFrame;
import ru.psu.shells.ess.misc.Mode;
import ru.psu.shells.ess.components.Storage;
import ru.psu.shells.ess.model.entity.Fact;
import ru.psu.shells.ess.model.entity.Rule;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class RuleFrame extends JDialog implements ModalFrame {

    private JTextField txtfName;
    private JList<Fact> lstCondition;
    private JButton btnAddCondition;
    private JButton btnDeleteCondition;
    private JList<Fact> lstConclusion;
    private JButton btnAddConclusion;
    private JButton btnDeleteConclusion;
    private JTextArea txtaExplain;
    private JButton btnOK;
    private JButton btnCancel;
    private JPanel rPanel;

    private DialogResult dialogResult = DialogResult.Cancel;
    private final ArrayList<Rule> rules;
    private final Mode mode;
    private final int idx;
    private boolean isNameChanged = false;

    private final DefaultListModel<Fact> conditionModel = new DefaultListModel<>();
    private final DefaultListModel<Fact> conclusionModel = new DefaultListModel<>();

    private final String oldName;

    public RuleFrame(JFrame owner, int idx, Mode mode) {
        super(owner);
        this.setContentPane(rPanel);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        lstCondition.setModel(conditionModel);
        lstConclusion.setModel(conclusionModel);

        this.rules = Storage.getRules();
        this.idx = idx;
        this.mode = mode;
        this.oldName = (mode == Mode.TO_UPDATE) ? rules.get(idx).getName() : "";

        if (mode == Mode.TO_CREATE) {
            this.setTitle("Добавить правило");
        } else {
            Rule rule = rules.get(idx);
            txtfName.setText(rule.getName());
            txtaExplain.setText(rule.getExplanation());
            rule.getCondition().forEach(conditionModel::addElement);
            conclusionModel.addElement(rule.getConclusion());
            this.setTitle("Обновить правило");
        }

        customizeButtons();


        txtfName.getDocument().addDocumentListener(txtfNameDL);
        txtaExplain.getDocument().addDocumentListener(txtaExplanationDL);

        rootPane.setDefaultButton(btnOK);
        this.pack();
        this.setModal(true);
        this.setLocationRelativeTo(owner);
        this.setResizable(false);
        this.setVisible(true);
    }

    private void customizeButtons() {
        btnOK.addActionListener(btnOkAL);
        btnCancel.addActionListener(btnCancelAL);

        btnAddCondition.addActionListener(btnAddConditionAL);
        btnDeleteCondition.addActionListener(btnDeleteConditionAL);

        btnAddConclusion.addActionListener(btnAddConclusionAL);
        btnDeleteConclusion.addActionListener(btnDeleteConclusionAL);
    }

    @Override
    public DialogResult getDialogResult() {
        return dialogResult;
    }

    private final ActionListener btnOkAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = txtfName.getText().trim();
            String explain = txtaExplain.getText().trim();

            if (name.isEmpty()) {
                txtfName.grabFocus();
                return;
            } else if (conditionModel.size() == 0) {
                btnAddCondition.grabFocus();
                return;
            } else if (conclusionModel.size() == 0) {
                btnAddConclusion.grabFocus();
                return;
            } else {
                btnOK.grabFocus();
            }

            Fact[] condition = new Fact[conditionModel.size()];
            conditionModel.copyInto(condition);
            Fact conclusion = conclusionModel.get(0);


            Optional<Rule> isDuplicate = Storage.getRules().stream()
                    .filter(r -> r.getName().equalsIgnoreCase(name))
                    .findFirst();
            if (!isDuplicate.equals(Optional.empty()) && isNameChanged) {
                JOptionPane.showMessageDialog(RuleFrame.this, "Правило с таким именем уже существует", "Ошибка", JOptionPane.ERROR_MESSAGE);
                txtfName.grabFocus();
                txtfName.selectAll();
                return;
            }

            if (mode == Mode.TO_CREATE) {
                Rule rule = new Rule(name, conclusion, explain);
                rule.setCondition(new ArrayList<>(Arrays.asList(condition)));
                rules.add(idx + 1, rule);
            } else {
                Rule rule = rules.get(idx);
                rule.setExplanation(explain);
                rule.setName(name);
                rule.setConclusion(conclusion);
                rule.setCondition(new ArrayList<>(Arrays.asList(condition)));
            }

            dialogResult = DialogResult.Ok;
            dispose();
        }
    };

    private final DocumentListener txtfNameDL = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) { check(); }
        @Override
        public void removeUpdate(DocumentEvent e) { check(); }
        @Override
        public void changedUpdate(DocumentEvent e) { check(); }

        private void check() { isNameChanged = !oldName.equalsIgnoreCase(txtfName.getText().trim()); }
    };

    private final DocumentListener txtaExplanationDL = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
        }
    };

    private final ActionListener btnCancelAL = e -> dispose();

    private final ActionListener btnAddConditionAL = e -> {
        FactFrame addFactFrame = new FactFrame(RuleFrame.this, "Добавить факт посылки", FactMode.CONDITION);
        DialogResult dialogResult = addFactFrame.getDialogResult();
        if (dialogResult == DialogResult.Ok) {
            Fact fact = addFactFrame.returnFact();
            for(int i = 0; i < conditionModel.size(); ++i) {
                Fact f = conditionModel.get(i);
                if (f.getVariable() == fact.getVariable()) {
                    JOptionPane.showMessageDialog(RuleFrame.this, "Факт с данной переменной уже добавлен в посылку", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            conditionModel.addElement(fact);
            lstCondition.updateUI();
            lstCondition.invalidate();
        }
    };

    private final ActionListener btnDeleteConditionAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedIndex = lstCondition.getSelectedIndex();
            if (selectedIndex != -1) {
                //rules.get(idx).getCondition().remove(selectedIndex);
                //conditionModel.removeElement(selectedIndex);
                conditionModel.removeElementAt(selectedIndex);
                lstCondition.updateUI();
                lstCondition.invalidate();
            }
        }
    };

    private final ActionListener btnAddConclusionAL = e -> {
        FactFrame addFactFrame = new FactFrame(RuleFrame.this, "Добавить факт заключения", FactMode.CONCLUSION);
        DialogResult dialogResult = addFactFrame.getDialogResult();
        if (dialogResult == DialogResult.Ok) {
            Fact fact = addFactFrame.returnFact();
            conclusionModel.clear();
            conclusionModel.addElement(fact);
            lstConclusion.updateUI();
            lstConclusion.invalidate();
        }
    };

    private final ActionListener btnDeleteConclusionAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Fact factToRemove = conclusionModel.get(0);
            if (factToRemove == null)
                return;
            rules.get(idx).setConclusion(null);
            conclusionModel.clear();
            lstConclusion.updateUI();
            lstConclusion.invalidate();
        }
    };
}
