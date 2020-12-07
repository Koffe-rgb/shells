package ru.psu.shells.ess.frames;

import ru.psu.shells.ess.components.Storage;
import ru.psu.shells.ess.misc.DialogResult;
import ru.psu.shells.ess.misc.ModalFrame;
import ru.psu.shells.ess.misc.Mode;
import ru.psu.shells.ess.model.entity.Domain;
import ru.psu.shells.ess.model.entity.DomainValue;
import ru.psu.shells.ess.model.entity.Rule;
import ru.psu.shells.ess.misc.TableRowTransferHandler;
import ru.psu.shells.ess.misc.DomainValueTableModel;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DomainFrame extends JDialog implements ModalFrame {
    private JPanel rPanel;
    private JButton btnCancel;
    private JButton btnOK;
    private JTextField txtfName;
    private JButton btnAdd;
    private JButton btnDelete;
    private JLabel lbMsg;
    private JTextField txtfValue;
    private JLabel lbMsgValue;
    private JTable tblValues;
    private JScrollPane spnlTbl;

    private DialogResult dialogResult = DialogResult.Cancel;
    private final int idx;
    private final Mode mode;
    private boolean isNameChanged = false;
    private String oldName;
    private Domain createdDomain;
    private ArrayList<DomainValue> oldValues = null;

    private final ArrayList<Domain> domains;

    private final DomainValueTableModel domainValueTableModel = new DomainValueTableModel(new ArrayList<>());

    public DomainFrame(JFrame owner, int idx, Mode mode) {
        super(owner);
        this.setContentPane(rPanel);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        this.idx = idx;
        this.mode = mode;
        this.domains = Storage.getDomains();
        this.oldName = (mode == Mode.TO_UPDATE) ? domains.get(idx).getName() : "";

        tblValues.setModel(domainValueTableModel);
        tblValues.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblValues.setDragEnabled(true);
        tblValues.setDropMode(DropMode.INSERT_ROWS);
        tblValues.setTransferHandler(new TableRowTransferHandler(tblValues));

        JPopupMenu popupMenu = new JPopupMenu();
        tblValues.setComponentPopupMenu(popupMenu);
        spnlTbl.setComponentPopupMenu(popupMenu);

        JMenuItem clearSelectionMI = new JMenuItem("Снять выделение");
        clearSelectionMI.addActionListener(e -> tblValues.clearSelection());
        popupMenu.add(clearSelectionMI);

        customizeButtons();

        txtfName.getDocument().addDocumentListener(txtfNameDL);

        rootPane.setDefaultButton(btnOK);

        if (mode == Mode.TO_CREATE) {
            this.setTitle("Добавить домен");
            createdDomain = new Domain("");
        } else {
            this.setTitle("Изменить домен");
            createdDomain = domains.get(idx);
            txtfName.setText(createdDomain.getName());
            oldValues = new ArrayList<>(createdDomain.getValues());
            domainValueTableModel.setDatalist(createdDomain.getValues());
            tblValues.updateUI();
            tblValues.invalidate();
        }



        this.setSize(450, 350);
        this.setModal(true);
        this.setLocationRelativeTo(owner);
        this.setResizable(false);
        this.setVisible(true);
    }

    private void customizeButtons() {
        btnOK.addActionListener(btnOkAL);
        btnCancel.addActionListener(btnCancelAL);
        btnDelete.addActionListener(btnDeleteAL);
        btnAdd.addActionListener(btnAddAL);
    }

    @Override
    public DialogResult getDialogResult() {
        return dialogResult;
    }

    private final DocumentListener txtfNameDL = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) { check(); }
        @Override
        public void removeUpdate(DocumentEvent e) { check(); }
        @Override
        public void changedUpdate(DocumentEvent e) { check(); }

        private void check() { isNameChanged = !oldName.equalsIgnoreCase(txtfName.getText().trim()); }
    };

    private final ActionListener btnAddAL = e -> {
        String text = txtfValue.getText().trim();
        if (!text.isEmpty()) {

            List<DomainValue> dataList = domainValueTableModel.getDatalist();

            boolean isUnique = dataList.isEmpty() ||
                    !dataList.stream()
                            .map(DomainValue::getValue)
                            .collect(Collectors.toList())
                            .contains(text);

            if (isUnique) {
                lbMsgValue.setText("Добавлено!");
                lbMsgValue.setForeground(Color.GREEN);
                txtfValue.selectAll();
                txtfValue.grabFocus();
            } else {
                lbMsgValue.setText("Уже существует!");
                lbMsgValue.setForeground(Color.RED);
                txtfValue.selectAll();
                txtfValue.grabFocus();
                return;
            }

            int selectedRow = tblValues.getSelectedRow();
            dataList.add(selectedRow + 1, new DomainValue(text));
            tblValues.updateUI();

        }
    };


    private final ActionListener btnDeleteAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedIndex = tblValues.getSelectedRow();
            if (selectedIndex == -1)
                return;

            DomainValue selectedDomainValue = domainValueTableModel.getRowObject(selectedIndex);

            boolean isUsed =
                    Storage.getRules().stream()
                            .flatMap(rule -> rule.getCondition().stream())
                            .anyMatch(fact -> fact.getValue().equals(selectedDomainValue)) ||
                    Storage.getRules().stream()
                            .map(Rule::getConclusion)
                            .anyMatch(fact -> fact.getValue().equals(selectedDomainValue));

            if (isUsed) {
                JOptionPane.showMessageDialog(
                        DomainFrame.this,
                        "Невозможно удалить данное значение, " +
                                "поскольку оно используется в правилах",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            domainValueTableModel.removeRow(selectedIndex);
        }
    };

    private final ActionListener btnOkAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = txtfName.getText().trim();

            ArrayList<DomainValue> domainValues = (ArrayList<DomainValue>) domainValueTableModel.getDatalist();

            if (name.isEmpty()) {
                txtfName.grabFocus();
                return;
            } else if (domainValues.size() == 0) {
                btnAdd.grabFocus();
                return;
            }

            Optional<Domain> isDuplicate = Storage.getDomains().stream()
                    .filter(d -> d.getName().equalsIgnoreCase(name))
                    .findFirst();
            if (!isDuplicate.equals(Optional.empty()) && isNameChanged) {
                lbMsg.setText("Уже существует!");
                lbMsg.setForeground(Color.RED);
                txtfName.grabFocus();
                txtfName.selectAll();
                return;
            }

            createdDomain.setName(name);
            createdDomain.setValues(domainValues);

            if (mode == Mode.TO_CREATE) {
                domains.add(idx + 1, createdDomain);
            }

            dialogResult = DialogResult.Ok;
            dispose();
        }
    };

    private final ActionListener btnCancelAL = e -> {
        createdDomain.setName(oldName);
        createdDomain.setValues(oldValues);
        dispose();
    };
}
