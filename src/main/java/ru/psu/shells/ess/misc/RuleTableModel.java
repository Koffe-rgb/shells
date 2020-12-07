package ru.psu.shells.ess.misc;

import ru.psu.shells.ess.model.entity.Fact;
import ru.psu.shells.ess.model.entity.Rule;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.stream.Collectors;

public class RuleTableModel extends AbstractTableModel implements Reorderable {
    private final List<Rule> datalist;
    private final String[] columnNames = new String[] {"Имя", "Описание"};
    private final Class[] columnClass = new Class[] {String.class, String.class};

    public RuleTableModel(List<Rule> ruleList) {
        this.datalist = ruleList;
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

    public Rule getRowObject(int idx) { return datalist.get(idx); }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Rule rule = datalist.get(rowIndex);
        if (columnIndex == 0) {
            return rule.getName();
        } else if (columnIndex == 1) {
            List<String> strings = rule.getCondition().stream().map(Fact::toString).collect(Collectors.toList());
            StringBuilder sb = new StringBuilder("ЕСЛИ ");
            strings.forEach(s -> sb.append(s).append(" И "));
            sb.delete(sb.length() - 3, sb.length()).append(", \r\nТО ").append(rule.getConclusion());
            return sb.toString();
        }
        return null;
    }

    @Override
    public void reorder(int fromIndex, int toIndex) {
        Rule from = datalist.get(fromIndex);
        toIndex = Math.min(toIndex, datalist.size() - 1);
        Rule to = datalist.get(toIndex);
        datalist.set(fromIndex, to);
        datalist.set(toIndex, from);
    }
}
