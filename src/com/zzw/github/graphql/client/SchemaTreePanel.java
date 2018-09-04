package com.zzw.github.graphql.client;

import com.zzw.github.graphql.builder.PackageUtil;
import com.zzw.github.graphql.spider.GitHubGraphQLParser;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zzw on 2018/7/27.
 */
public class SchemaTreePanel extends JPanel {
    private JTree mSchemaTree;

    private List<SchemaTreeSelectionListener> mSchemaTreeSelectionListenerList = new ArrayList<>();

    public void addSchemaTreeSelectionListener(SchemaTreeSelectionListener listener) {
        if ((listener != null) && (!mSchemaTreeSelectionListenerList.contains(listener))) {
            mSchemaTreeSelectionListenerList.add(listener);
        }
    }

    public void removeSchemaTreeSelectionListener(SchemaTreeSelectionListener listener) {
        if ((listener != null) && mSchemaTreeSelectionListenerList.contains(listener)) {
            mSchemaTreeSelectionListenerList.remove(listener);
        }
    }

    public interface SchemaTreeSelectionListener {
        public void onSchemaTreeNodeSelected(Class clazz);
    }

    public SchemaTreePanel() {
        initData();
        initEvent();
        setLayout(new BorderLayout(0, 0));
        add(new JScrollPane(mSchemaTree), BorderLayout.CENTER);
    }

    private void initData() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("GitHub GraphQL Schema System");
        root.add(constructSchemaTree("Query", GitHubGraphQLParser.PACKAGE_PREFIX + "query"));
        root.add(constructSchemaTree("Mutations", GitHubGraphQLParser.PACKAGE_PREFIX + "mutations"));
        root.add(constructSchemaTree("Objects", GitHubGraphQLParser.PACKAGE_PREFIX + "objects"));
        root.add(constructSchemaTree("Interfaces", GitHubGraphQLParser.PACKAGE_PREFIX + "interfaces"));
        root.add(constructSchemaTree("Enums", GitHubGraphQLParser.PACKAGE_PREFIX + "enums"));
        root.add(constructSchemaTree("Unions", GitHubGraphQLParser.PACKAGE_PREFIX + "unions"));
        root.add(constructSchemaTree("Input Objects", GitHubGraphQLParser.PACKAGE_PREFIX + "inputobjects"));
        root.add(constructSchemaTree("Scalars", GitHubGraphQLParser.PACKAGE_PREFIX + "scalars"));
        mSchemaTree = new JTree(root);
    }

    private void initEvent() {
        mSchemaTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) e.getNewLeadSelectionPath().getLastPathComponent();
                Object userObject = treeNode.getUserObject();
                if ((userObject instanceof SchemaTreeNode) && (!mSchemaTreeSelectionListenerList.isEmpty())) {
                    Class clazz = ((SchemaTreeNode) userObject).getType();
                    for (SchemaTreeSelectionListener listener : mSchemaTreeSelectionListenerList) {
                        listener.onSchemaTreeNodeSelected(clazz);
                    }
                }
            }
        });
    }

    private DefaultMutableTreeNode constructSchemaTree(String title, String pkg) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(title);
        List<Class> classes = PackageUtil.getClasses(pkg);
        if ((classes != null) && (classes.size() > 0)) {
            for (Class clazz : classes) {
                root.add(new DefaultMutableTreeNode(new SchemaTreeNode(clazz)));
            }
        }
        return root;
    }

    public static class SchemaTreeNode {
        private Class mType;

        public SchemaTreeNode(Class clazz) {
            mType = clazz;
        }

        public Class getType() { return mType; }

        @Override
        public String toString() {
            return (mType == null) ? "null" : mType.getSimpleName();
        }
    }
}
