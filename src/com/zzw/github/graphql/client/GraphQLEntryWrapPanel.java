package com.zzw.github.graphql.client;

import com.zzw.github.graphql.builder.GraphQLNode;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Created by zzw on 2018/8/3.
 */
public class GraphQLEntryWrapPanel extends JPanel {
    private JTabbedPane mTabbedPane = new JTabbedPane();

    public GraphQLEntryWrapPanel(SchemaTypeWrapPanel nl, SchemaArgsWrapPanel al, GraphQLEditPanel cl) {
        mTabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        SchemaTreePanel schemaTreePanel = new SchemaTreePanel();
        schemaTreePanel.addSchemaTreeSelectionListener(nl);
        mTabbedPane.addTab("schema", null, schemaTreePanel, "GitHub GraphQL Schema System");

        SchemaTypePanel queryComponent = new SchemaTypePanel(com.zzw.github.graphql.schema.queries.Query.class, true);
        GraphQLNode queryNode = GraphQLNode.asNode("query");
        queryNode.addTag("component", queryComponent);
        GraphQLEntryPanel queryPanel = new GraphQLEntryPanel(queryNode, nl, al);
        queryPanel.addGraphQLChangeListener(cl);
        queryComponent.setGraphQLEntryPanel(queryPanel);
        JPanel queryWrapper = new JPanel(new BorderLayout(0, 0));
        queryWrapper.add(new JScrollPane(queryPanel));
        mTabbedPane.addTab("query", null, queryWrapper, com.zzw.github.graphql.schema.queries.Query.class.getName());

        SchemaTypePanel mutationComponent = new SchemaTypePanel(com.zzw.github.graphql.schema.mutations.Mutation.class, true);
        GraphQLNode mutationNode = GraphQLNode.asNode("mutation");
        mutationNode.addTag("component", mutationComponent);
        GraphQLEntryPanel mutationPanel = new GraphQLEntryPanel(mutationNode, nl, al);
        mutationPanel.addGraphQLChangeListener(cl);
        mutationComponent.setGraphQLEntryPanel(mutationPanel);
        JPanel mutationWrapper = new JPanel(new BorderLayout(0, 0));
        mutationWrapper.add(new JScrollPane(mutationPanel));
        mTabbedPane.addTab("mutation", null, mutationWrapper, com.zzw.github.graphql.schema.mutations.Mutation.class.getName());

        setLayout(new BorderLayout(0, 0));
        add(mTabbedPane, BorderLayout.CENTER);
    }
}
