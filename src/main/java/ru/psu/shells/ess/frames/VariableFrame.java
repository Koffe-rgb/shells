package ru.psu.shells.ess.frames;

import ru.psu.shells.ess.misc.DialogResult;
import ru.psu.shells.ess.misc.ModalFrame;
import ru.psu.shells.ess.misc.Mode;
import ru.psu.shells.ess.components.Storage;
import ru.psu.shells.ess.model.entity.Domain;
import ru.psu.shells.ess.model.entity.VarType;
import ru.psu.shells.ess.model.entity.Variable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class VariableFrame extends JDialog implements ModalFrame {
    private JPanel rPanel;
    private JButton btnCancel;
    private JButton btnOK;
    private JTextArea txtaQuestion;
    private JTextField txtfName;
    private JComboBox<Domain> cbxDomain;
    private JButton btnAddDomain;
    private JRadioButton rbtnRequested;
    private JRadioButton rbtnInferred;
    private JRadioButton rbtnInferRequested;

    private DialogResult dialogResult = DialogResult.Cancel;
    private final int idx;
    private final Mode mode;
    private boolean isNameChanged = false;
    private final String oldName;

    private final ArrayList<Variable> variables;
    private final ArrayList<Domain> domains;
    private final DefaultComboBoxModel<Domain> domainCbxModel = new DefaultComboBoxModel<>();

    private final JRadioButton[] rbtnsAr = new JRadioButton[] {rbtnInferred, rbtnInferRequested, rbtnRequested};

    public VariableFrame(JFrame owner, int idx, Mode mode) {
        super(owner);
        this.setContentPane(rPanel);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);


        this.idx = idx;
        this.mode = mode;
        this.variables = Storage.getVariables();
        this.domains = Storage.getDomains();
        this.oldName = (mode == Mode.TO_UPDATE) ? variables.get(idx).getName() : "";

        cbxDomain.setModel(domainCbxModel);
        this.domains.forEach(domainCbxModel::addElement);

        createRadioGroup();

        txtfName.getDocument().addDocumentListener(txtfNameDL);
        btnOK.addActionListener(btnOkAL);
        ActionListener btnCancelAL = e -> dispose();
        btnCancel.addActionListener(btnCancelAL);
        btnAddDomain.addActionListener(btnAddDomainAL);

        rootPane.setDefaultButton(btnOK);

        if (mode == Mode.TO_CREATE) {
            this.setTitle("Добавить переменную");
        } else {
            this.setTitle("Изменить переменную");

            Variable variable = variables.get(idx);
            txtfName.setText(variable.getName());
            txtaQuestion.setText(variable.getQuestion());
            cbxDomain.setSelectedIndex(domainCbxModel.getIndexOf(variable.getDomain()));

            switch (variable.getType()) {
                case REQUESTED: rbtnRequested.setSelected(true); break;
                case INFERRED: rbtnInferred.setSelected(true); break;
                case INFER_REQUESTED: rbtnInferRequested.setSelected(true); break;
            }
        }

        this.setSize(400, 300);
        this.setModal(true);
        this.setLocationRelativeTo(owner);
        this.setResizable(false);
        this.setVisible(true);
    }

    private void createRadioGroup() {
        ButtonGroup group = new ButtonGroup();
        Arrays.stream(rbtnsAr).forEach(rbtn -> {
            group.add(rbtn);
            rbtn.addActionListener(rbtnGroupAL);
        });
        rbtnRequested.setSelected(true);
    }

    @Override
    public DialogResult getDialogResult() {
        return dialogResult;
    }

    private final ActionListener rbtnGroupAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JRadioButton rbtn = (JRadioButton) e.getSource();
            if (rbtn == rbtnInferred) {
                txtaQuestion.setEnabled(false);
                txtaQuestion.setText("");
            } else {
                txtaQuestion.setEnabled(true);
                txtfName.setText(txtfName.getText());
            }
        }
    };

    private final DocumentListener txtfNameDL = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) { makeQuestion(); }
        @Override
        public void removeUpdate(DocumentEvent e) { makeQuestion(); }
        @Override
        public void changedUpdate(DocumentEvent e) { makeQuestion(); }

        private void makeQuestion() {
            if (!rbtnInferred.isSelected()) {
                txtaQuestion.setText(txtfName.getText().trim() + "?");
                isNameChanged = !oldName.equalsIgnoreCase(txtfName.getText().trim());
            }
        }

    };

    private final ActionListener btnOkAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = txtfName.getText().trim();
            String question = txtaQuestion.getText().trim();
            Domain domain = (Domain) cbxDomain.getSelectedItem();
            VarType type = VarType.REQUESTED;

            if (rbtnRequested.isSelected())
                type = VarType.REQUESTED;
            else if (rbtnInferred.isSelected())
                type = VarType.INFERRED;
            else if (rbtnInferRequested.isSelected())
                type = VarType.INFER_REQUESTED;

            if (name.isEmpty()) {
                txtfName.grabFocus();
                return;
            } else if (domain == null) {
                cbxDomain.grabFocus();
                cbxDomain.showPopup();
                return;
            } else if (question.isEmpty() && type != VarType.INFERRED) {
                txtaQuestion.grabFocus();
                return;
            }

            Optional<Variable> isDuplicate = Storage.getVariables().stream()
                    .filter(v -> v.getName().equalsIgnoreCase(name))
                    .findFirst();
            if (!isDuplicate.equals(Optional.empty()) && isNameChanged) {
                JOptionPane.showMessageDialog(VariableFrame.this, "Переменная с таким именем уже существует", "Ошибка", JOptionPane.ERROR_MESSAGE);
                txtfName.grabFocus();
                txtfName.selectAll();
                return;
            }

            if (mode == Mode.TO_CREATE) {
                Variable variable = new Variable(name, domain, question, type);
                variables.add(idx + 1, variable);
            } else {
                Variable variable = variables.get(idx);
                variable.setName(name);
                variable.setDomain(domain);
                variable.setQuestion(question);
                variable.setType(type);
            }


            dialogResult = DialogResult.Ok;
            dispose();
        }
    };

    private final ActionListener btnAddDomainAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            DomainFrame addDomainFrame = new DomainFrame(null, -1, Mode.TO_CREATE);
            if (addDomainFrame.getDialogResult() == DialogResult.Ok) {
                domainCbxModel.removeAllElements();
                domains.forEach(domainCbxModel::addElement);
                cbxDomain.updateUI();
                cbxDomain.invalidate();
            }
            btnOK.grabFocus();
        }
    };
}
