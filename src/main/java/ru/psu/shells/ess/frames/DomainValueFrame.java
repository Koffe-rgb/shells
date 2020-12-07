package ru.psu.shells.ess.frames;

import ru.psu.shells.ess.misc.DialogResult;
import ru.psu.shells.ess.misc.ModalFrame;
import ru.psu.shells.ess.model.entity.DomainValue;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class DomainValueFrame extends JDialog implements ModalFrame {
    private JPanel rPanel;
    private JButton btnCancel;
    private JButton btnOK;
    private JTextField txtfName;

    private DialogResult dialogResult = DialogResult.Cancel;

    private final ArrayList<DomainValue> domainValues;

    public DomainValueFrame(JDialog owner) {
        super(owner);
        this.setContentPane(rPanel);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        this.domainValues = new ArrayList<>();

        ActionListener btnCancelAL = e -> dispose();
        btnCancel.addActionListener(btnCancelAL);
        btnOK.addActionListener(btnOkAL);

        rootPane.setDefaultButton(btnOK);
        this.setTitle("Добавить значение домена");
        this.setSize(200, 150);
        this.setModal(true);
        this.setLocationRelativeTo(owner);
        this.setResizable(false);
        this.setVisible(true);
    }

    @Override
    public DialogResult getDialogResult() {
        return dialogResult;
    }

    public ArrayList<DomainValue> getDomainValues() {
        return domainValues;
    }

    private final ActionListener btnOkAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = txtfName.getText().trim();

            if (name.isEmpty()) {
                dispose();
                return;
            }

            DomainValue domainValue = new DomainValue(name);

            domainValues.add(domainValue);
            dialogResult = DialogResult.Ok;
            txtfName.setText("");
            txtfName.grabFocus();
        }
    };

}
