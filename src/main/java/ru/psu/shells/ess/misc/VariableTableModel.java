package ru.psu.shells.ess.misc;

import ru.psu.shells.ess.model.entity.Variable;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class VariableTableModel extends AbstractTableModel implements Reorderable {
    private final List<Variable> datalist;
    private final String[] columnNames = new String[] {"Имя", "Тип", "Домен"};
    private final Class[] columnClass = new Class[] {String.class, String.class, String.class};

    public VariableTableModel(List<Variable> variableList) {
        this.datalist = variableList;
    }

    public String getColumnName(int column) {
        return columnNames[column];
    }

    public Class<?> getColumnClass(int column) {
        return columnClass[column];
    }

    @Override
    public int getRowCount() {
        return datalist.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    public Variable getRowObject(int idx) { return datalist.get(idx); }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Variable variable = datalist.get(rowIndex);
        if (columnIndex == 0) {
            return variable.getName();
        } else if (columnIndex == 1) {
            return variable.getStringType();
        } else if (columnIndex == 2) {
            return variable.getDomain().getName();
        }
        return null;
    }

    @Override
    public void reorder(int fromIndex, int toIndex) {
        Variable from = datalist.get(fromIndex);
        toIndex = Math.min(toIndex, datalist.size() - 1);
        Variable to = datalist.get(toIndex);
        datalist.set(fromIndex, to);
        datalist.set(toIndex, from);
    }
}
