package com.zzw.github.graphql.client;

import com.zzw.github.graphql.builder.TypeUtil;
import com.zzw.github.graphql.schema.annotations.Argument;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by zzw on 2018/7/29.
 */
public class SchemaArgsPanel extends JPanel {
    private Class mClazz;
    private String mIdentifier;
    private boolean mIsEditable;
    private GraphQLEntryPanel mGraphQLEntryPanel;

    private Object[] mColumnIdentifiers = new Object[]{ "Argument", "Value", "Type", "Required", "Description" };
    private JTable mTable;
    private DefaultTableModel mTableModel;
    private ListSelectionModel mListSelectionModel;
    private List<SchemaArgsSelectionListener> mSchemaArgsSelectionListenerList = new ArrayList<>();

    public void addSchemaArgsSelectionListener(SchemaArgsSelectionListener listener) {
        if ((listener != null) && (!mSchemaArgsSelectionListenerList.contains(listener))) {
            mSchemaArgsSelectionListenerList.add(listener);
        }
    }

    public void removeSchemaArgsSelectionListener(SchemaArgsSelectionListener listener) {
        if ((listener != null) && mSchemaArgsSelectionListenerList.contains(listener)) {
            mSchemaArgsSelectionListenerList.remove(listener);
        }
    }

    public interface SchemaArgsSelectionListener {
        void onArgValueChanged(SchemaArgsPanel panel, Class clazz, String identifier, String argument, Class type, String value, String required);
        void onArgSelectionChanged(Class clazz, String identifier, String argument, Class type);
        void onArgFieldSelected(Class clazz, String identifier, String argument);
        void onArgTypeSelected(Class type);
    }

    public void setGraphQLEntryPanel(GraphQLEntryPanel graphQLEntryPanel) {
        if (mGraphQLEntryPanel != null) {
            this.removeSchemaArgsSelectionListener(mGraphQLEntryPanel);
        }
        mGraphQLEntryPanel = graphQLEntryPanel;
        if (mGraphQLEntryPanel != null) {
            this.addSchemaArgsSelectionListener(mGraphQLEntryPanel);
        }
    }

    public SchemaArgsPanel(Class clazz, String identifier) {
        this(clazz, identifier, false);
    }

    public SchemaArgsPanel(Class clazz, String identifier, boolean isEditable) {
        mClazz = clazz;
        mIdentifier = identifier;
        mIsEditable = isEditable;

        mTableModel = new ArgsTableModel();
        mTable = new JTable(mTableModel);
        mListSelectionModel = mTable.getSelectionModel();

        mTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        mTable.getTableHeader().setReorderingAllowed(false);
        mTable.setRowHeight(30);

        initData();
        initView();
        initEvent();

        setLayout(new BorderLayout(0, 0));
        add(new JScrollPane(mTable));
    }

    private void initData() {
        Argument[] arguments = TypeUtil.getArguments(mClazz, mIdentifier);
        if ((arguments == null) || (arguments.length <= 0)) {
            return;
        }

        Object[][] dataVector = new Object[arguments.length][5];

        for (int i = 0; i < arguments.length; i++) {
            String argName = arguments[i].name();
            String argType = arguments[i].type();
            String nonNull = arguments[i].nonNull().value();
            String argDesc = arguments[i].description().value();

            argType = argType.substring(argType.lastIndexOf(".") + 1);
            argDesc = (argDesc.length() <= 0) ? "No description." : argDesc;

            dataVector[i] = new Object[]{ argName, "", argType, nonNull, argDesc };
        }

        mTableModel.setDataVector(dataVector, mColumnIdentifiers);
    }

    private void initView() {
        if (mTableModel.getRowCount() <= 0) {
            return;
        }

        TableColumn fieldColumn = mTable.getColumn(mColumnIdentifiers[0]);
        fieldColumn.setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel fieldLabel = new JLabel((String) value);
                Font font = fieldLabel.getFont();
                fieldLabel.setFont(new Font(font.getName(), font.getStyle(), (int) (font.getSize() * 1.2)));
                fieldLabel.setOpaque(true);
                fieldLabel.setBackground(isSelected ? mTable.getSelectionBackground() : mTable.getBackground());
                return fieldLabel;
            }
        });

        TableColumn selectedColumn = mTable.getColumn(mColumnIdentifiers[1]);
        selectedColumn.setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JTextField valueField = new JTextField((String) value);
                valueField.setHorizontalAlignment(SwingConstants.RIGHT);
                valueField.setForeground(Color.RED);
                valueField.setBackground(isSelected ? mTable.getSelectionBackground() : mTable.getBackground());
                return valueField;
            }
        });

        TableColumn typeColumn = mTable.getColumn(mColumnIdentifiers[2]);
        typeColumn.setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel typeLabel = new JLabel((String) value);
                Font font = typeLabel.getFont();
                typeLabel.setFont(new Font(font.getName(), Font.ITALIC, font.getSize()));
                typeLabel.setOpaque(true);
                typeLabel.setBackground(isSelected ? mTable.getSelectionBackground() : mTable.getBackground());
                return typeLabel;
            }
        });

        TableColumn requiredColumn = mTable.getColumn(mColumnIdentifiers[3]);
        requiredColumn.setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel requireLabel = new JLabel((String) value);
                requireLabel.setHorizontalAlignment(SwingConstants.CENTER);
                Font font = requireLabel.getFont();
                requireLabel.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
                requireLabel.setOpaque(true);
                requireLabel.setBackground(isSelected ? mTable.getSelectionBackground() : mTable.getBackground());
                return requireLabel;
            }
        });

        TableColumn descColumn = mTable.getColumn(mColumnIdentifiers[4]);
        descColumn.setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel descLabel = new JLabel((String) value);
                descLabel.setOpaque(true);
                descLabel.setBackground(isSelected ? mTable.getSelectionBackground() : mTable.getBackground());
                return descLabel;
            }
        });
    }

    private void initEvent() {
        if (mTableModel.getRowCount() <= 0) {
            return;
        }

        mTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                int row = mTable.getSelectedRow();
                if (row == -1) {
                    return;
                }
                String argument = (String) mTable.getValueAt(row, 0);

                switch (e.getKeyChar()) {
                    case 'f':
                        for (SchemaArgsSelectionListener listener : mSchemaArgsSelectionListenerList) {
                            listener.onArgFieldSelected(mClazz, mIdentifier, argument);
                        }
                        break;
                    case 't':
                        for (SchemaArgsSelectionListener listener : mSchemaArgsSelectionListenerList) {
                            listener.onArgTypeSelected(TypeUtil.getArgType(mClazz, mIdentifier, argument));
                        }
                        break;
                    default: break;
                }
            }
        });

        mTableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (col == 1) {
                    String argument = (String) mTable.getValueAt(row, 0);
                    Class type = TypeUtil.getArgType(mClazz, mIdentifier, argument);
                    String value = (String) mTable.getValueAt(row, 1);
                    String required = (String) mTable.getValueAt(row, 3);
                    for (SchemaArgsSelectionListener listener : mSchemaArgsSelectionListenerList) {
                        listener.onArgValueChanged(SchemaArgsPanel.this, mClazz, mIdentifier, argument, type, value, required);
                    }
                }
            }
        });

        mListSelectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                String argument = (String) mTable.getValueAt(mTable.getSelectedRow(), 0);
                Class type = TypeUtil.getArgType(mClazz, mIdentifier, argument);
                for (SchemaArgsSelectionListener listener : mSchemaArgsSelectionListenerList) {
                    listener.onArgSelectionChanged(mClazz, mIdentifier, argument, type);
                }
            }
        });
    }

    private class ArgsTableModel extends DefaultTableModel {
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return getValueAt(0, columnIndex).getClass();
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return (mIsEditable && (column == 1));
        }
    }
}
