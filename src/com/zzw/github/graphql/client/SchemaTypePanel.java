package com.zzw.github.graphql.client;

import com.zzw.github.graphql.builder.TypeUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zzw on 2018/7/29.
 */
public class SchemaTypePanel extends JPanel {
    private Class mClazz;
    private boolean mIsEditable;
    private GraphQLEntryPanel mGraphQLEntryPanel;

    private Object[] mColumnIdentifiers = new Object[]{ "Selected", "Field", "Type", "Required", "Description" };
    private JTable mTable;
    private DefaultTableModel mTableModel;
    private ListSelectionModel mListSelectionModel;

    private List<SchemaTypeSelectionListener> mSchemaTypeSelectionListenerList = new ArrayList<>();

    public void addSchemaTypeSelectionListener(SchemaTypeSelectionListener listener) {
        if ((listener != null) && (!mSchemaTypeSelectionListenerList.contains(listener))) {
            mSchemaTypeSelectionListenerList.add(listener);
        }
    }

    public void removeSchemaTypeSelectionListener(SchemaTypeSelectionListener listener) {
        if ((listener != null) && mSchemaTypeSelectionListenerList.contains(listener)) {
            mSchemaTypeSelectionListenerList.remove(listener);
        }
    }

    public interface SchemaTypeSelectionListener {
        void onValueChanged(Class clazz, String identifier, Class type, boolean selected);
        void onFieldSelected(Class clazz, String identifier);
        void onTypeSelected(Class type);
        void onSelectionChanged(Class clazz, String identifier, Class type, boolean isEditable, GraphQLEntryPanel graphQLEntryPanel);
    }

    public Icon getIdentifierIcon() {
        if (mClazz == null) {
            return null;
        }

        String className = mClazz.getName();
        if (className.startsWith("com.zzw.github.graphql.schema.query")) {
            return new IdentifierIcon('Q', Color.WHITE, Color.GREEN, 8);
        } else if (className.startsWith("com.zzw.github.graphql.schema.mutations")) {
            return new IdentifierIcon('M', Color.WHITE, Color.GREEN, 8);
        } else if (className.startsWith("com.zzw.github.graphql.schema.objects")) {
            return new IdentifierIcon('O', Color.WHITE, Color.MAGENTA, 8);
        } else if (className.startsWith("com.zzw.github.graphql.schema.interfaces")) {
            return new IdentifierIcon('I', Color.WHITE, Color.GRAY, 8);
        } else if (className.startsWith("com.zzw.github.graphql.schema.enums")) {
            return new IdentifierIcon('E', Color.WHITE, Color.BLUE, 8);
        } else if (className.startsWith("com.zzw.github.graphql.schema.unions")) {
            return new IdentifierIcon('U', Color.WHITE, Color.GRAY, 8);
        } else if (className.startsWith("com.zzw.github.graphql.schema.inputobjects")) {
            return new IdentifierIcon('I', Color.WHITE, Color.MAGENTA, 8);
        } else if (className.startsWith("com.zzw.github.graphql.schema.scalars")) {
            return new IdentifierIcon('S', Color.WHITE, Color.BLUE, 8);
        } else {
            return null;
        }
    }

    public String getIdentifierString() {
        if (mIsEditable) {
            return mClazz.getName() + "#" + this.hashCode();
        } else {
            return mClazz.getName();
        }
    }

    public String getIdentifierTitle() {
        return mClazz.getSimpleName();
    }

    public void setGraphQLEntryPanel(GraphQLEntryPanel graphQLEntryPanel) {
        if (mGraphQLEntryPanel != null) {
            this.removeSchemaTypeSelectionListener(mGraphQLEntryPanel);
        }
        mGraphQLEntryPanel = graphQLEntryPanel;
        if (mGraphQLEntryPanel != null) {
            this.addSchemaTypeSelectionListener(mGraphQLEntryPanel);
        }
    }

    public SchemaTypePanel(Class clazz) {
        this(clazz, false);
    }

    public SchemaTypePanel(Class clazz, boolean isEditable) {
        mClazz = clazz;
        mIsEditable = isEditable;

        mTableModel = new TypeTableModel(mIsEditable);
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
        boolean hasImplementedBys = false;
        boolean hasIdentifiers = false;

        if (TypeUtil.isInterface(mClazz)) {
            hasImplementedBys = true;
            hasIdentifiers = true;
        } else if (TypeUtil.isUnion(mClazz)) {
            hasImplementedBys = true;
        } else {
            hasIdentifiers = true;
        }

        List<Object[]> dataList = new ArrayList<>();

        if (hasImplementedBys) {
            String[] implementBys = TypeUtil.getImplementedBy(mClazz);
            if ((implementBys != null) && (implementBys.length > 0)) {
                dataList.add(new Object[]{ false, "__typename", "", "", "Meta field." });
                for (String implementedBy : implementBys) {
                    try {
                        String simpleName = implementedBy.substring(implementedBy.lastIndexOf(".") + 1);
                        String description = TypeUtil.getDescription(Class.forName(implementedBy), null);
                        description = (description == null) ? "No description." : description;
                        dataList.add(new Object[]{ false, simpleName, simpleName, "", description });
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (hasIdentifiers) {
            List<String> identifiers = TypeUtil.getDeclaredIdentifiers(mClazz);
            if ((identifiers != null) && (identifiers.size() > 0)) {
                for (String identifier : identifiers) {
                    String typeName = TypeUtil.getTypeName(mClazz, identifier);
                    String nonNull = TypeUtil.getNonNull(mClazz, identifier);
                    String description = TypeUtil.getDescription(mClazz, identifier);
                    typeName = (typeName == null) ? "null" : typeName;
                    nonNull = (nonNull == null) ? "" : nonNull;
                    description = (description == null) ? "No description." : description;
                    dataList.add(new Object[]{ false, identifier, typeName, nonNull, description });
                }
            }
        }

        if (dataList.isEmpty()) {
            return;
        }

        Object[][] dataVector = new Object[dataList.size()][5];
        for (int i = 0; i < dataList.size(); i++) {
            dataVector[i] = dataList.get(i);
        }
        mTableModel.setDataVector(dataVector, mColumnIdentifiers);
    }

    private void initView() {
        if (mTableModel.getRowCount() <= 0) {
            return;
        }

        TableColumn selectedColumn = mTable.getColumn(mColumnIdentifiers[0]);
        selectedColumn.setCellRenderer(new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    boolean selected = (boolean) mTable.getValueAt(row, 0);
                    JCheckBox selectedCheckBox = new JCheckBox();
                    selectedCheckBox.setSelected(selected);
                    selectedCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
                    setTableCellBackground(selectedCheckBox, selected, isSelected);
                    return selectedCheckBox;
                }
            });

        TableColumn fieldColumn = mTable.getColumn(mColumnIdentifiers[1]);
        fieldColumn.setCellRenderer(new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    boolean selected = (boolean) mTable.getValueAt(row, 0);
                    JLabel fieldLabel = new JLabel();
                    fieldLabel.setText((String) value);
                    if (TypeUtil.isOverride(mClazz, (String) value)) {
                        fieldLabel.setIcon(new CircleIcon(5, 20, 30, Color.MAGENTA));
                    } else {
                        fieldLabel.setIcon(new CircleIcon(5, 20, 30, Color.GREEN));
                    }
                    fieldLabel.setIconTextGap(0);
                    Font font = fieldLabel.getFont();
                    fieldLabel.setFont(new Font(font.getName(), font.getStyle(), (int) (font.getSize() * 1.2)));
                    fieldLabel.setOpaque(true);
                    setTableCellBackground(fieldLabel, selected, isSelected);
                    return fieldLabel;
                }
            });

        TableColumn typeColumn = mTable.getColumn(mColumnIdentifiers[2]);
        typeColumn.setCellRenderer(new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    boolean selected = (boolean) mTable.getValueAt(row, 0);
                    JLabel typeLabel = new JLabel((String) value);
                    Font font = typeLabel.getFont();
                    typeLabel.setFont(new Font(font.getName(), Font.ITALIC, font.getSize()));
                    typeLabel.setOpaque(true);
                    setTableCellBackground(typeLabel, selected, isSelected);
                    return typeLabel;
                }
            });

        TableColumn requiredColumn = mTable.getColumn(mColumnIdentifiers[3]);
        requiredColumn.setCellRenderer(new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    boolean selected = (boolean) mTable.getValueAt(row, 0);
                    JLabel requireLabel = new JLabel((String) value);
                    requireLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    Font font = requireLabel.getFont();
                    requireLabel.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
                    requireLabel.setOpaque(true);
                    setTableCellBackground(requireLabel, selected, isSelected);
                    return requireLabel;
                }
            });

        TableColumn descColumn = mTable.getColumn(mColumnIdentifiers[4]);
        descColumn.setCellRenderer(new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    boolean selected = (boolean) mTable.getValueAt(row, 0);
                    JLabel descLabel = new JLabel((String) value);
                    descLabel.setOpaque(true);
                    setTableCellBackground(descLabel, selected, isSelected);
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
                String identifier = (String) mTable.getValueAt(row, 1);

                switch (e.getKeyChar()) {
                    case 'f':
                        for (SchemaTypeSelectionListener listener : mSchemaTypeSelectionListenerList) {
                            listener.onFieldSelected(mClazz, identifier);
                        }
                        break;
                    case 't':
                        for (SchemaTypeSelectionListener listener : mSchemaTypeSelectionListenerList) {
                            listener.onTypeSelected(TypeUtil.getType(mClazz, identifier));
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

                if (col == 0) {
                    String identifier = (String) mTable.getValueAt(row, 1);
                    Class type = TypeUtil.getType(mClazz, identifier);
                    boolean selected = (boolean) mTable.getValueAt(row, 0);

                    for (SchemaTypeSelectionListener listener : mSchemaTypeSelectionListenerList) {
                        listener.onValueChanged(mClazz, identifier, type, selected);
                    }

                    mTable.validate();
                }
            }
        });

        mListSelectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                String identifier = (String) mTable.getValueAt(mTable.getSelectedRow(), 1);
                Class type = TypeUtil.getType(mClazz, identifier);
                for (SchemaTypeSelectionListener listener : mSchemaTypeSelectionListenerList) {
                    listener.onSelectionChanged(mClazz, identifier, type, mIsEditable, mGraphQLEntryPanel);
                }
            }
        });
    }

    private void setTableCellBackground(Component component, boolean selected, boolean isSelected) {
        if (isSelected) {
            component.setBackground(mTable.getSelectionBackground());
        } else if (selected) {
            component.setBackground(Color.YELLOW);
        } else {
            component.setBackground(mTable.getBackground());
        }
    }

    private static class TypeTableModel extends DefaultTableModel {
        private boolean isEditable;

        public TypeTableModel(boolean isEditable) {
            this.isEditable = isEditable;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return getValueAt(0, columnIndex).getClass();
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return isEditable ? (column == 0) : false;
        }
    }

    private static class CircleIcon implements Icon {
        private int radius;
        private int width;
        private int height;
        private Color color;

        public CircleIcon(int radius, int width, int height, Color color) {
            this.radius = radius;
            this.width = width;
            this.height = height;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillOval(width / 2 - radius, height / 2 - radius,
                    radius * 2, radius * 2);
        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }
    }

    private static class IdentifierIcon implements Icon {
        private char identifier;
        private Color foreground;
        private Color background;
        private int radius;

        public IdentifierIcon(char identifier, Color foreground, Color background, int radius) {
            this.identifier = identifier;
            this.foreground = foreground;
            this.background = background;
            this.radius = radius;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(background);
            g.fillOval(0, 0, radius * 2, radius * 2);
            g.setColor(foreground);
            Font oldFont = g.getFont();
            Font newFont = new Font(oldFont.getName(), Font.BOLD, oldFont.getSize());
            g.setFont(newFont);
            FontMetrics fontMetrics = g.getFontMetrics();
            int idWidth = fontMetrics.charWidth(identifier) / 2;
            int idHeight = fontMetrics.getAscent() / 2;
            g.drawString(String.valueOf(identifier), radius - idWidth, radius + idHeight);
        }

        @Override
        public int getIconWidth() {
            return radius * 2;
        }

        @Override
        public int getIconHeight() {
            return radius * 2;
        }
    }
}
