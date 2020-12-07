package ru.psu.shells.ess.frames;

import ru.psu.shells.ess.misc.DialogResult;
import ru.psu.shells.ess.misc.ModalFrame;
import ru.psu.shells.ess.components.Storage;
import ru.psu.shells.ess.model.entity.VarType;
import ru.psu.shells.ess.model.entity.Variable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GoalFrame extends JDialog implements ModalFrame {
    private JButton btnCancel;
    private JButton btnOk;
    private JComboBox<Variable> cbxGoal;
    private JPanel rPanel;

    private DialogResult dialogResult = DialogResult.Cancel;
    private Variable goal;

    public GoalFrame(Frame owner) {
        super(owner);
        this.setContentPane(rPanel);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        rootPane.setDefaultButton(btnOk);

        DefaultComboBoxModel<Variable> cbxModel = new DefaultComboBoxModel<>();
        cbxGoal.setModel(cbxModel);
        Storage.getVariables().stream()
                .filter(variable -> variable.getType() != VarType.REQUESTED)
                .forEach(cbxModel::addElement);

        btnOk.addActionListener(btnOkAL);
        ActionListener btnCancelAL = e -> dispose();
        btnCancel.addActionListener(btnCancelAL);


        this.setTitle("Выбор цели консультации");
        this.setSize(300, 150);
        this.setModal(true);
        this.setLocationRelativeTo(owner);
        this.setResizable(false);
        this.setVisible(true);
    }

    private final ActionListener btnOkAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            goal = (Variable) cbxGoal.getSelectedItem();
            dialogResult = DialogResult.Ok;
            dispose();
        }
    };

    public Variable getGoal() {
        return goal;
    }

    @Override
    public DialogResult getDialogResult() {
        return dialogResult;
    }
}
