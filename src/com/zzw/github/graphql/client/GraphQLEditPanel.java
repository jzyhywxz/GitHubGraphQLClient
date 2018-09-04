package com.zzw.github.graphql.client;

import com.zzw.github.graphql.builder.GraphQLBuilder;
import com.zzw.github.graphql.builder.GraphQLNode;

import javax.swing.*;
import java.awt.*;

/**
 * Created by zzw on 2018/8/7.
 */
public class GraphQLEditPanel extends JPanel implements GraphQLEntryPanel.GraphQLChangeListener {
    private JTextArea mTextArea = new JTextArea();
    private GraphQLBuilder mBuilder = new GraphQLBuilder();
    private GraphQLNode mNode;

    public GraphQLEditPanel() {
        mTextArea.setEditable(false);
        setLayout(new BorderLayout(0, 0));
        add(new JScrollPane(mTextArea), BorderLayout.CENTER);
    }

    public String visualize() {
        return (mNode != null) ? mBuilder.visualize() : null;
    }

    public String serialize() {
        return (mNode != null) ? mBuilder.serialize() : null;
    }

    public String getText() {
        return mTextArea.getText();
    }

    @Override
    public void onGraphQLChanged(GraphQLNode node) {
        mNode = node;
        if (mNode != null) {
            mBuilder.prepare(mNode);
            String payload = mBuilder.visualize();
            mTextArea.setText(payload);
        }
    }
}
