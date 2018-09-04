package com.zzw.github.graphql.client;

import com.zzw.github.graphql.builder.GraphQLNode;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by zzw on 2018/8/2.
 */
public class SchemaTypeWrapPanel extends JPanel implements SchemaTreePanel.SchemaTreeSelectionListener, GraphQLNodeComponent.GraphQLNodeComponentListener {
    private JTabbedPane mTabbedPane = new JTabbedPane();

    private SchemaArgsWrapPanel mSchemaArgsWrapPanel;

    public void setSchemaArgsWrapPanel(SchemaArgsWrapPanel panel) { this.mSchemaArgsWrapPanel = panel; }

    public SchemaTypeWrapPanel() {
        mTabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        setLayout(new BorderLayout(0, 0));
        add(mTabbedPane, BorderLayout.CENTER);
    }

    @Override
    public void onSchemaTreeNodeSelected(Class clazz) {
        String clazzName = clazz.getName();
        if ((mTabbedPane.getTabCount() > 0) && clazzName.equals(mTabbedPane.getToolTipTextAt(0))) {
            return;
        }

        if (mTabbedPane.getTabCount() > 0) {
            mTabbedPane.removeTabAt(0);
        }
        SchemaTypePanel schemaTypePanel = new SchemaTypePanel(clazz);
        String title = schemaTypePanel.getIdentifierTitle();
        Icon icon = schemaTypePanel.getIdentifierIcon();
        String tip = schemaTypePanel.getIdentifierString();
        schemaTypePanel.addSchemaTypeSelectionListener(mSchemaArgsWrapPanel);
        mTabbedPane.insertTab(title, icon, schemaTypePanel, tip, 0);
        mTabbedPane.setTabComponentAt(0, new ButtonTabComponent(mTabbedPane));
        mTabbedPane.setSelectedIndex(0);
    }

    @Override
    public void onNodeClicked(GraphQLNode data, SchemaTypePanel component) {
        String identifier = component.getIdentifierString();
        if ((mTabbedPane.getTabCount() > 0) && identifier.equals(mTabbedPane.getToolTipTextAt(0))) {
            return;
        }

        if (mTabbedPane.getTabCount() > 0) {
            mTabbedPane.removeTabAt(0);
        }
        String title = component.getIdentifierTitle();
        Icon icon = component.getIdentifierIcon();
        component.addSchemaTypeSelectionListener(mSchemaArgsWrapPanel);
        mTabbedPane.insertTab(title, icon, component, identifier, 0);
        mTabbedPane.setTabComponentAt(0, new ButtonTabComponent(mTabbedPane));
        mTabbedPane.setSelectedIndex(0);
    }

    private static class ButtonTabComponent extends JPanel {
        private final JTabbedPane pane;

        public ButtonTabComponent(final JTabbedPane pane) {
            //unset default FlowLayout' gaps
            super(new FlowLayout(FlowLayout.LEFT, 0, 0));
            if (pane == null) {
                throw new NullPointerException("TabbedPane is null");
            }
            this.pane = pane;
            setOpaque(false);

            //make JLabel read titles from JTabbedPane
            JLabel label = new JLabel() {
                public String getText() {
                    int i = pane.indexOfTabComponent(ButtonTabComponent.this);
                    if (i != -1) {
                        return pane.getTitleAt(i);
                    }
                    return null;
                }

                public Icon getIcon() {
                    int i = pane.indexOfTabComponent(ButtonTabComponent.this);
                    if (i != -1) {
                        return pane.getIconAt(i);
                    }
                    return null;
                }

                public String getToolTipText() {
                    int i = pane.indexOfTabComponent(ButtonTabComponent.this);
                    if (i != -1) {
                        return pane.getToolTipTextAt(i);
                    }
                    return null;
                }
            };

            add(label);
            //add more space between the label and the button
            label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            //tab button
            JButton button = new ButtonTabComponent.TabButton();
            add(button);
            //add more space to the top of the component
            setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        }

        private class TabButton extends JButton implements ActionListener {
            public TabButton() {
                int size = 17;
                setPreferredSize(new Dimension(size, size));
                setToolTipText("close this tab");
                //Make the button looks the same for all Laf's
                setUI(new BasicButtonUI());
                //Make it transparent
                setContentAreaFilled(false);
                //No need to be focusable
                setFocusable(false);
                setBorder(BorderFactory.createEtchedBorder());
                setBorderPainted(false);
                //Making nice rollover effect
                //we use the same listener for all buttons
                addMouseListener(buttonMouseListener);
                setRolloverEnabled(true);
                //Close the proper tab by clicking the button
                addActionListener(this);
            }

            public void actionPerformed(ActionEvent e) {
                int i = pane.indexOfTabComponent(ButtonTabComponent.this);
                if (i != -1) {
                    pane.remove(i);
                }
            }

            //we don't want to update UI for this button
            public void updateUI() {
            }

            //paint the cross
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                //shift the image for pressed buttons
                if (getModel().isPressed()) {
                    g2.translate(1, 1);
                }
                g2.setStroke(new BasicStroke(2));
                g2.setColor(Color.BLACK);
                if (getModel().isRollover()) {
                    g2.setColor(Color.MAGENTA);
                }
                int delta = 6;
                g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
                g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
                g2.dispose();
            }
        }

        private final static MouseListener buttonMouseListener = new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                Component component = e.getComponent();
                if (component instanceof AbstractButton) {
                    AbstractButton button = (AbstractButton) component;
                    button.setBorderPainted(true);
                }
            }

            public void mouseExited(MouseEvent e) {
                Component component = e.getComponent();
                if (component instanceof AbstractButton) {
                    AbstractButton button = (AbstractButton) component;
                    button.setBorderPainted(false);
                }
            }
        };
    }
}
