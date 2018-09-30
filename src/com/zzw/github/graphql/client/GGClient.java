package com.zzw.github.graphql.client;

import javax.swing.*;
import java.awt.*;

/**
 * Created by zzw on 2018/7/27.
 */
public class GGClient extends JFrame {
    public GGClient(String accessToken) {
        SchemaTypeWrapPanel schemaTypeWrapPanel = new SchemaTypeWrapPanel();
        SchemaArgsWrapPanel schemaArgsWrapPanel = new SchemaArgsWrapPanel();
        GraphQLEditPanel graphQLEditPanel = new GraphQLEditPanel();
        GraphQLEntryWrapPanel graphQLEntryWrapPanel = new GraphQLEntryWrapPanel(schemaTypeWrapPanel, schemaArgsWrapPanel, graphQLEditPanel);
        GraphQLOperatePanel graphQLOperatePanel = new GraphQLOperatePanel(graphQLEditPanel, accessToken);

        schemaTypeWrapPanel.setSchemaArgsWrapPanel(schemaArgsWrapPanel);

        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        leftPanel.add(graphQLEntryWrapPanel);
        leftPanel.add(graphQLEditPanel);

        JSplitPane mainPanel = new JSplitPane();
        mainPanel.setLeftComponent(schemaTypeWrapPanel);
        mainPanel.setRightComponent(schemaArgsWrapPanel);

        JSplitPane centerPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        centerPanel.setLeftComponent(mainPanel);
        centerPanel.setRightComponent(graphQLOperatePanel);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(centerPanel);

        add(splitPane);
    }

    public static void main(String[] args) {
        String accessToken = null;
        if (args != null && args.length > 0) {
            accessToken = args[0];
        }

        JFrame frame = new GGClient(accessToken);
        frame.setTitle("GitHub GraphQL Client");
        frame.setUndecorated(false);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());
        Dimension dimension = new Dimension(
                screenSize.width - screenInsets.left - screenInsets.right,
                screenSize.height - screenInsets.top - screenInsets.bottom);
        frame.setSize(dimension);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
