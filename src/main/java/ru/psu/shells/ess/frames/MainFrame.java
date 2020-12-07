package ru.psu.shells.ess.frames;

import ru.psu.shells.ess.misc.*;
import ru.psu.shells.ess.components.Storage;
import ru.psu.shells.ess.misc.MultilineTableCellRenderer;
import ru.psu.shells.ess.misc.TableRowTransferHandler;
import ru.psu.shells.ess.model.entity.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.function.Consumer;

public class MainFrame extends JFrame {
    private JPanel rPanel;
    private JPanel pnlCondition;
    private JPanel pnlConclusion;
    private JPanel pnlQuestion;
    private JPanel pnlDomainValues;
    private JPanel pnlExplanation;

    private JList<Fact> lstCondition;
    private JList<Fact> lstConclusion;
    private JList<DomainValue> lstDomainValues;
    private JTextArea txtaQuestion;
    private JTextArea txtaExplanation;

    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnRules;
    private JButton btnVariables;
    private JButton btnDomains;

    private JTable tblSpecial;
    private JScrollPane spnlTbl;



    // массивы компонентов для быстрого редактирования
    private final JButton[] btnsBrowse = new JButton[] {btnRules, btnVariables, btnDomains};
    private final JPanel[] panels = new JPanel[] {pnlDomainValues, pnlQuestion, pnlCondition, pnlConclusion, pnlExplanation};

    // листы сырых данных
    private ArrayList<Rule> rules = Storage.getRules();
    private ArrayList<Variable> variables = Storage.getVariables();
    private ArrayList<Domain> domains = Storage.getDomains();

    // модели отображения сырых данных на таблицу
    private RuleTableModel ruleTableModel = new RuleTableModel(rules);
    private VariableTableModel variableTableModel = new VariableTableModel(variables);
    private DomainTableModel domainTableModel = new DomainTableModel(domains);

    // модели отображения данных на листы
    private final DefaultListModel<Fact> conditionListModel = new DefaultListModel<>();
    private final DefaultListModel<Fact> conclusionListModel = new DefaultListModel<>();
    private final DefaultListModel<DomainValue> domainValueListModel = new DefaultListModel<>();

    private boolean needSave;

    private enum SelectedTab {
        RULES,
        VARIABLES,
        DOMAINS
    }
    private SelectedTab selectedTab;


    public MainFrame() throws HeadlessException {
        this.setContentPane(rPanel);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setTitle("Expert System Shell");
        this.needSave = false;

        txtaExplanation.setEditable(false);

        // меню
        makeMenu();

        // кнопки переключения таблицы
        customizeBrowseButtons();

        // кнопки редактирования
        customizeEditButtons();

        customizeTable();

        lstCondition.setModel(conditionListModel);
        lstConclusion.setModel(conclusionListModel);
        lstDomainValues.setModel(domainValueListModel);

        this.pack();
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setVisible(true);
    }

    private void customizeTable() {
        tblSpecial.getSelectionModel().addListSelectionListener(tblSpecialSL);
        tblSpecial.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblSpecial.setModel(ruleTableModel);
        tblSpecial.setDragEnabled(true);
        tblSpecial.setDropMode(DropMode.INSERT_ROWS);
        tblSpecial.setTransferHandler(new TableRowTransferHandler(tblSpecial));
        setMultilineRows();
    }

    private void setMultilineRows() {
        Enumeration<TableColumn> columns = tblSpecial.getColumnModel().getColumns();
        MultilineTableCellRenderer cellRenderer = new MultilineTableCellRenderer();

        while (columns.hasMoreElements()) {
            TableColumn tableColumn = columns.nextElement();
            tableColumn.setCellRenderer(cellRenderer);
        }
    }

    private void setFirstColumnWidth() {
        if (selectedTab == SelectedTab.RULES) {
            tblSpecial.getColumnModel().getColumn(0).setWidth(80);
            tblSpecial.getColumnModel().getColumn(0).setMaxWidth(100);
        } else {
            tblSpecial.getColumnModel().getColumn(0).setWidth(150);
            tblSpecial.getColumnModel().getColumn(0).setMaxWidth(150);
        }
    }

    private void customizeEditButtons() {
        btnAdd.addActionListener(btnAddAL);
        btnUpdate.addActionListener(btnUpdateAL);
        btnDelete.addActionListener(btnDeleteAL);
    }

    private void customizeBrowseButtons() {
        Consumer<JButton> consumer = btn -> {
            btn.setActionCommand(btn.getName());
            btn.addActionListener(btnsBrowseAL);
        };
        Arrays.stream(btnsBrowse).forEach(consumer);
        btnRules.doClick();
    }

    private void makeMenu() {
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("Файл");
        JMenu consultationMenu = new JMenu("Консультация");
        menuBar.add(fileMenu);
        menuBar.add(consultationMenu);

        JMenuItem createMI = new JMenuItem("Создать новую базу знаний");
        JMenuItem saveMI = new JMenuItem("Сохранить базу знаний");
        JMenuItem loadMI = new JMenuItem("Загрузить базу знаний");
        JMenuItem consultMI = new JMenuItem("Проконсультироваться");

        createMI.addActionListener(createAL);
        loadMI.addActionListener(loadFileAL);
        saveMI.addActionListener(saveFileAL);
        consultMI.addActionListener(consultAL);

        fileMenu.add(createMI);
        fileMenu.add(saveMI);
        fileMenu.add(new JToolBar.Separator());
        fileMenu.add(loadMI);

        consultationMenu.add(consultMI);

        JPopupMenu popupMenu = new JPopupMenu();
        tblSpecial.setComponentPopupMenu(popupMenu);
        spnlTbl.setComponentPopupMenu(popupMenu);

        JMenuItem clearSelectionMI = new JMenuItem("Снять выделение");
        clearSelectionMI.addActionListener(e -> tblSpecial.clearSelection());
        popupMenu.add(clearSelectionMI);
    }

    private void openRuleFrame(int idx, Mode mode) {
        RuleFrame ruleFrame = new RuleFrame(MainFrame.this, idx, mode);
        DialogResult dialogResult = ruleFrame.getDialogResult();
        if (dialogResult == DialogResult.Ok) {
            tblSpecial.updateUI();
            tblSpecial.invalidate();
            ruleTableModel.fireTableStructureChanged();
            setMultilineRows();
            setFirstColumnWidth();
            needSave = true;
        }
    }

    private void openDomainFrame(int idx, Mode mode) {
        DomainFrame domainFrame = new DomainFrame(MainFrame.this, idx, mode);
        DialogResult dialogResult = domainFrame.getDialogResult();
        if (dialogResult == DialogResult.Ok) {
            tblSpecial.updateUI();
            tblSpecial.invalidate();
            domainTableModel.fireTableStructureChanged();
            setMultilineRows();
            needSave = true;
        }
    }

    private void openVariableFrame(int idx, Mode mode) {
        VariableFrame variableFrame = new VariableFrame(MainFrame.this, idx, mode);
        DialogResult dialogResult = variableFrame.getDialogResult();
        if (dialogResult == DialogResult.Ok) {
            tblSpecial.updateUI();
            tblSpecial.invalidate();
            variableTableModel.fireTableStructureChanged();
            setMultilineRows();
            setFirstColumnWidth();
            needSave = true;
        }
    }

    private void loadFile(String path) {
        Storage.load(path);
        rules = Storage.getRules();
        variables = Storage.getVariables();
        domains = Storage.getDomains();
        ruleTableModel = new RuleTableModel(rules);
        variableTableModel = new VariableTableModel(variables);
        domainTableModel = new DomainTableModel(domains);
        btnRules.doClick();
        tblSpecial.setModel(ruleTableModel);
        setMultilineRows();
        setFirstColumnWidth();
    }

    private void saveFile() {
        FileDialog fileDialog = new FileDialog(MainFrame.this, "Сохранить базу знаний");
        fileDialog.setDirectory(Paths.get("").toAbsolutePath().toString());
        fileDialog.setFile("*.ess");
        fileDialog.setMode(FileDialog.SAVE);
        fileDialog.setVisible(true);

        String file = fileDialog.getFile();
        if (file != null)
            Storage.save(fileDialog.getDirectory() + file);
    }

    private final ActionListener saveFileAL = e -> {
        if (domains.isEmpty()) {
            JOptionPane.showMessageDialog(null, "База знаний пуста - нечего сохранять", "Неудача", JOptionPane.WARNING_MESSAGE);
            return;
        }

        saveFile();
        needSave = false;
    };

    private final ActionListener loadFileAL = e -> {
        if (checkSave(false)) return;

        FileDialog fileDialog = new FileDialog(MainFrame.this, "Загрузить базу знаний");
        fileDialog.setDirectory(Paths.get("").toAbsolutePath().toString());
        fileDialog.setFile("*.ess");
        fileDialog.setMode(FileDialog.LOAD);
        fileDialog.setVisible(true);

        String file = fileDialog.getFile();
        if (file != null) {
            loadFile(fileDialog.getDirectory() + file);
            MainFrame.this.setTitle("Expert System Shell - " + file);
        }
        needSave = false;
    };

    private final ActionListener createAL = e -> {
        if (checkSave(false)) return;

        MainFrame.this.setTitle("Expert System Shell");
        Storage.clear();

        tblSpecial.updateUI();
        tblSpecial.invalidate();

        needSave = false;
    };

    public boolean checkSave(boolean onClosing) {
        if (needSave) {
            int dialog = JOptionPane.showConfirmDialog(
                    MainFrame.this,
                    "Хотите сохранить изменения?",
                    "", (onClosing ? JOptionPane.YES_NO_OPTION : JOptionPane.YES_NO_CANCEL_OPTION),
                    JOptionPane.QUESTION_MESSAGE
            );
            if (dialog == JOptionPane.YES_OPTION)
                saveFile();
            else return dialog == JOptionPane.CANCEL_OPTION;
        }
        return false;
    }

    private final ActionListener consultAL = e -> {
        boolean isPresent = Storage.getVariables().stream().anyMatch(variable -> variable.getType() != VarType.REQUESTED);
        if (!isPresent) {
            JOptionPane.showMessageDialog(
                    MainFrame.this,
                    "Невозможно выбрать цель консультации!",
                    "Предупреждение",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        GoalFrame goalFrame = new GoalFrame(MainFrame.this);
        if (goalFrame.getDialogResult() != DialogResult.Cancel) {
            Variable goal = goalFrame.getGoal();
            ConsultationFrame consultationFrame = new ConsultationFrame(MainFrame.this, goal);
            consultationFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    super.windowClosed(e);
                    consultationFrame.close();
                }

                @Override
                public void windowClosing(WindowEvent e)
                {
                    super.windowClosing(e);
                    consultationFrame.close();
                }
            });
        }
    };

    private final ListSelectionListener tblSpecialSL = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            int selectedRow = tblSpecial.getSelectedRow();
            conditionListModel.clear();
            conclusionListModel.clear();
            domainValueListModel.clear();
            txtaQuestion.setText("");
            txtaExplanation.setText("");

            if (selectedTab == SelectedTab.RULES && selectedRow != -1) {
                Rule selectedRule = ruleTableModel.getRowObject(selectedRow);
                selectedRule.getCondition().forEach(conditionListModel::addElement);
                conclusionListModel.addElement(selectedRule.getConclusion());
                txtaExplanation.setText(selectedRule.getExplanation());
            } else if (selectedTab == SelectedTab.VARIABLES && selectedRow != -1) {
                Variable selectedVariable = variableTableModel.getRowObject(selectedRow);
                selectedVariable.getDomain()
                        .getValues()
                        .forEach(domainValueListModel::addElement);
                txtaQuestion.setText(selectedVariable.getQuestion());
            } else if (selectedTab == SelectedTab.DOMAINS && selectedRow != -1) {
                Domain selectedDomain = domainTableModel.getRowObject(selectedRow);
                selectedDomain.getValues()
                        .forEach(domainValueListModel::addElement);
            }
        }
    };

    private final ActionListener btnsBrowseAL = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            JButton btn = (JButton) e.getSource();
            Arrays.stream(btnsBrowse).forEach(b -> b.setEnabled(true));
            btn.setEnabled(false);
            Arrays.stream(panels).forEach(pnl -> pnl.setVisible(false));

            if (btn == btnRules) {
                pnlCondition.setVisible(true);
                pnlConclusion.setVisible(true);
                pnlExplanation.setVisible(true);
                tblSpecial.setModel(ruleTableModel);
                setMultilineRows();
                setFirstColumnWidth();
                selectedTab = SelectedTab.RULES;
            } else if (btn == btnVariables) {
                pnlQuestion.setVisible(true);
                pnlDomainValues.setVisible(true);
                tblSpecial.setModel(variableTableModel);
                setMultilineRows();
                setFirstColumnWidth();
                selectedTab = SelectedTab.VARIABLES;
            } else if (btn == btnDomains) {
                pnlDomainValues.setVisible(true);
                tblSpecial.setModel(domainTableModel);
                setMultilineRows();
                selectedTab = SelectedTab.DOMAINS;
            }
        }
    };

    private final ActionListener btnAddAL = e -> {
        int selectedRow = tblSpecial.getSelectedRow();
        if (selectedRow == -1)
            selectedRow = tblSpecial.getRowCount() - 1;
        switch (selectedTab) {
            case RULES: openRuleFrame(selectedRow, Mode.TO_CREATE); break;
            case VARIABLES: openVariableFrame(selectedRow, Mode.TO_CREATE); break;
            case DOMAINS: openDomainFrame(selectedRow, Mode.TO_CREATE); break;
        }
    };

    private final ActionListener btnUpdateAL = e -> {
        int selectedRow = tblSpecial.getSelectedRow();
        if (selectedRow == -1) return;
        switch (selectedTab) {
            case RULES: openRuleFrame(selectedRow, Mode.TO_UPDATE); break;
            case VARIABLES: openVariableFrame(selectedRow, Mode.TO_UPDATE); break;
            case DOMAINS: openDomainFrame(selectedRow, Mode.TO_UPDATE); break;
        }
    };

    private final ActionListener btnDeleteAL = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = tblSpecial.getSelectedRow();
            if (selectedRow == -1) return;

            if (validate(selectedRow)) return;

            switch (selectedTab) {
                case RULES: rules.remove(selectedRow); break;
                case DOMAINS: domains.remove(selectedRow); break;
                case VARIABLES: variables.remove(selectedRow); break;
            }

            conditionListModel.clear();
            conclusionListModel.clear();
            domainValueListModel.clear();
            txtaQuestion.setText("");
            txtaExplanation.setText("");

            tblSpecial.updateUI();
            tblSpecial.invalidate();
        }

        private boolean validate(int selectedRow) {
            boolean isUsed = false;
            String error = "";

            if (selectedTab == SelectedTab.DOMAINS) {
                Domain selectedDomain = domainTableModel.getRowObject(selectedRow);
                isUsed = variables.stream()
                        .map(Variable::getDomain)
                        .anyMatch(domain -> domain.equals(selectedDomain));
                error = "Невозможно удалить данный домен, поскольку он используется в переменных";
            } else if (selectedTab == SelectedTab.VARIABLES) {
                Variable selectedVariable = variableTableModel.getRowObject(selectedRow);
                isUsed = rules.stream()
                                .flatMap(rule -> rule.getCondition().stream())
                                .map(Fact::getVariable)
                                .anyMatch(variable -> variable.equals(selectedVariable)) ||
                        rules.stream()
                                .map(Rule::getConclusion)
                                .map(Fact::getVariable)
                                .anyMatch(variable -> variable.equals(selectedVariable));
                error = "Невозможно удалить данную переменную, поскольку она используется в правилах";
            }

            if (isUsed) {
                JOptionPane.showMessageDialog(
                        MainFrame.this,
                        error,
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE
                );
                return true;
            }
            return false;
        }
    };
}
