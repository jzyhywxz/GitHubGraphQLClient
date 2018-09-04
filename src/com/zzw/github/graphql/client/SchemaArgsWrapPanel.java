package com.zzw.github.graphql.client;

import com.zzw.github.graphql.builder.GraphQLArgument;

import javax.swing.*;
import java.awt.*;

/**
 * Created by zzw on 2018/8/2.
 */
public class SchemaArgsWrapPanel extends JPanel implements SchemaTypePanel.SchemaTypeSelectionListener, GraphQLArgComponent.GraphQLArgComponentListener {
    private SchemaArgsPanel mSchemaArgsPanel;

    public SchemaArgsWrapPanel() {
        setLayout(new BorderLayout(0, 0));
    }

    @Override
    public void onValueChanged(Class clazz, String identifier, Class type, boolean isSelected) {
//        System.out.println("onSelectionChanged: " + clazz.getName() + "#" + identifier + "," + isSelected);
    }

    @Override
    public void onFieldSelected(Class clazz, String identifier) {
//        System.out.println("onFieldSelected: " + clazz.getName() + "#" + identifier);
    }

    @Override
    public void onTypeSelected(Class type) {
//        System.out.println("onTypeSelected: " + type.getName());
    }

    @Override
    public void onSelectionChanged(Class clazz, String identifier, Class type, boolean isEditable, GraphQLEntryPanel graphQLEntryPanel) {
        if (mSchemaArgsPanel != null) {
            SchemaArgsWrapPanel.this.remove(mSchemaArgsPanel);
        }
        mSchemaArgsPanel = new SchemaArgsPanel(clazz, identifier, isEditable);
        mSchemaArgsPanel.setGraphQLEntryPanel(graphQLEntryPanel);
        SchemaArgsWrapPanel.this.add(mSchemaArgsPanel);
        SchemaArgsWrapPanel.this.revalidate();
    }

    @Override
    public void onArgClicked(GraphQLArgument data, SchemaArgsPanel component) {
        if (mSchemaArgsPanel != null) {
            SchemaArgsWrapPanel.this.remove(mSchemaArgsPanel);
        }
        mSchemaArgsPanel = component;
        SchemaArgsWrapPanel.this.add(mSchemaArgsPanel);
        SchemaArgsWrapPanel.this.revalidate();
    }
}
