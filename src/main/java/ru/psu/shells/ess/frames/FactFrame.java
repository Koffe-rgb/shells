package ru.psu.shells.ess.frames;

import ru.psu.shells.ess.misc.DialogResult;
import ru.psu.shells.ess.misc.FactMode;
import ru.psu.shells.ess.misc.ModalFrame;
import ru.psu.shells.ess.misc.Mode;
import ru.psu.shells.ess.components.Storage;
import ru.psu.shells.ess.model.entity.DomainValue;
import ru.psu.shells.ess.model.entity.Fact;
import ru.psu.shells.ess.model.entity.VarType;
import ru.psu.shells.ess.model.entity.Variable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;




public class FactFrame extends JDialog implements ModalFrame {
    private JButton btnCancel;
    private JButton btnOK;
    private JComboBox<DomainValue> cbxDomainValues;
    private JComboBox<Variable> cbxVariable;
    private JButton btnAddVariable;
    private JPanel rPanel;

    private DialogResult dialogResult = DialogResult.Cancel;

    private final ArrayList<Variable> variables;

    DefaultComboBoxModel<Variable> variableCbxModel = new DefaultComboBoxModel<>();
    DefaultComboBoxModel<DomainValue> domainValueCbxModel = new DefaultComboBoxModel<>();

    private Fact returnedFact;

    public FactFrame(JDialog owner, String name, FactMode factMode) {
        super(owner);
        this.setContentPane(rPanel);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        this.variables = Storage.getVariables();

        cbxVariable.setModel(variableCbxModel);
        cbxDomainValues.setModel(domainValueCbxModel);

        if (factMode == FactMode.CONDITION) {
            this.variables.forEach(variable -> variableCbxModel.addElement(variable));
        } else {
            this.variables.stream()
                    .filter(variable -> variable.getType() != VarType.REQUESTED)
                    .forEach(variable -> variableCbxModel.addElement(variable));
        }

        if (cbxVariable.getSelectedItem() != null) {
            Variable var = (Variable) cbxVariable.getSelectedItem();
            ArrayList<DomainValue> values = var.getDomain().getValues();
            values.forEach(value -> domainValueCbxModel.addElement(value));
        }

        customizeButtons();

        rootPane.setDefaultButton(btnOK);
        this.setTitle(name);
        this.setSize(400, 250);
        this.setModal(true);
        this.setLocationRelativeTo(owner);
        this.setResizable(false);
        this.setVisible(true);
    }

    private void customizeButtons() {
        cbxVariable.addActionListener(cbxVariableAL);
        btnCancel.addActionListener(btnCancelAL);
        btnOK.addActionListener(btnOkAL);
        btnAddVariable.addActionListener(btnAddVariableAL);
    }

    @Override
    public DialogResult getDialogResult() {
        return dialogResult;
    }

    public Fact returnFact() {
        return returnedFact;
    }

    private final ActionListener cbxVariableAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Variable selectedVariable = (Variable) cbxVariable.getSelectedItem();
            domainValueCbxModel.removeAllElements();
            if (selectedVariable != null) {
                selectedVariable.getDomain()
                        .getValues()
                        .forEach(domainValue -> domainValueCbxModel.addElement(domainValue));
                cbxDomainValues.updateUI();
                cbxDomainValues.invalidate();
            }
        }
    };

    private final ActionListener btnAddVariableAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            VariableFrame addVariableFrame = new VariableFrame(null, -1, Mode.TO_CREATE);
            if (addVariableFrame.getDialogResult() == DialogResult.Ok) {
                variableCbxModel.removeAllElements();
                variables.forEach(variable -> variableCbxModel.addElement(variable));
                cbxVariable.updateUI();
                cbxVariable.invalidate();
                cbxDomainValues.updateUI();
                cbxDomainValues.invalidate();
            }
            btnOK.grabFocus();
        }
    };

    private final ActionListener btnOkAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Variable variable = (Variable) cbxVariable.getSelectedItem();
            DomainValue domainValue = (DomainValue) cbxDomainValues.getSelectedItem();

            if (variable == null) {
                cbxVariable.showPopup();
                return;
            } else if (domainValue == null) {
                cbxDomainValues.showPopup();
                return;
            }

            returnedFact = new Fact(variable, domainValue);
            dialogResult = DialogResult.Ok;
            dispose();
        }
    };

    private final ActionListener btnCancelAL = e -> dispose();
}
