package ru.psu.shells.ess.misc;

import ru.psu.shells.ess.model.entity.DomainValue;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class DomainValueTableModel extends AbstractTableModel implements Reorderable {
    private List<DomainValue> datalist;
    private final String[] columnNames = new String[] {"Значение"};
    private final Class[] columnClass = new Class[] {String.class};

    public DomainValueTableModel(List<DomainValue> domainValueList) {
        this.datalist = domainValueList;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
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

    public DomainValue getRowObject(int idx) { return datalist.get(idx); }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        DomainValue domainValue = datalist.get(rowIndex);
        if (columnIndex == 0) {
            return domainValue.getValue();
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        DomainValue domainValue = datalist.get(rowIndex);
        domainValue.setValue((String) aValue);
    }

    public void removeRow(int idx) {
        datalist.remove(idx);
        fireTableRowsDeleted(idx, idx);
    }

    public List<DomainValue> getDatalist() {
        return datalist;
    }

    public void setDatalist(List<DomainValue> datalist) {
        this.datalist = datalist;
    }

    @Override
    public void reorder(int fromIndex, int toIndex) {
        DomainValue from = datalist.get(fromIndex);
        toIndex = Math.min(toIndex, datalist.size() - 1);
        DomainValue to = datalist.get(toIndex);
        datalist.set(fromIndex, to);
        datalist.set(toIndex, from);
    }
}
