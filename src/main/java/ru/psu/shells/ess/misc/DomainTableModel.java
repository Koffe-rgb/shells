package ru.psu.shells.ess.misc;

import ru.psu.shells.ess.model.entity.Domain;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class DomainTableModel extends AbstractTableModel implements Reorderable {
    private final List<Domain> datalist;
    private final String[] columnNames = new String[] {"Имя"};
    private final Class[] columnClass = new Class[] {String.class};

    public DomainTableModel(List<Domain> domainList) {
        this.datalist = domainList;
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

    public Domain getRowObject(int idx) { return datalist.get(idx); }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Domain domain = datalist.get(rowIndex);
        if (columnIndex == 0) {
            return domain.getName();
        }
        return null;
    }

    @Override
    public void reorder(int fromIndex, int toIndex) {
        Domain from = datalist.get(fromIndex);
        toIndex = Math.min(toIndex, datalist.size() - 1);
        Domain to = datalist.get(toIndex);
        datalist.set(fromIndex, to);
        datalist.set(toIndex, from);
    }
}
