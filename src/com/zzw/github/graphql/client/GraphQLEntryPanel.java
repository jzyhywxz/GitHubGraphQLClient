package com.zzw.github.graphql.client;

import com.zzw.github.graphql.builder.GraphQLArgument;
import com.zzw.github.graphql.builder.GraphQLNode;
import com.zzw.github.graphql.builder.TypeUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by zzw on 2018/8/3.
 */
public class GraphQLEntryPanel extends JPanel implements SchemaTypePanel.SchemaTypeSelectionListener, SchemaArgsPanel.SchemaArgsSelectionListener {
    private GraphQLNode mRootData;
    private Component mSelectedComponent;
    private Component mPreparedComponent;
    private List<List<Component>> mComponents = new ArrayList<>();

    private GraphQLNodeComponent.GraphQLNodeComponentListener mGraphQLNodeComponentListener;
    private GraphQLArgComponent.GraphQLArgComponentListener mGraphQLArgComponentListener;
    private List<GraphQLChangeListener> mGraphQLChangeListenerList = new ArrayList<>();

    public void addGraphQLChangeListener(GraphQLChangeListener listener) {
        if ((listener != null) && (!mGraphQLChangeListenerList.contains(listener))) {
            mGraphQLChangeListenerList.add(listener);
        }
    }

    public void removeGraphQLChangeListener(GraphQLChangeListener listener) {
        if ((listener != null) && mGraphQLChangeListenerList.contains(listener)) {
            mGraphQLChangeListenerList.remove(listener);
        }
    }

    public interface GraphQLChangeListener {
        void onGraphQLChanged(GraphQLNode node);
    }

    public Component getSelectedComponent() { return mSelectedComponent; }
    public void setSelectedComponent(Component component) { mSelectedComponent = component; }

    public GraphQLEntryPanel(GraphQLNode data, GraphQLNodeComponent.GraphQLNodeComponentListener nl, GraphQLArgComponent.GraphQLArgComponentListener al) {
        mRootData = data;
        mGraphQLNodeComponentListener = nl;
        mGraphQLArgComponentListener = al;
        setLayout(new GraphQLLayout());
        setBackground(Color.WHITE);
        updateComponents();
    }

    private void updateComponents() {
        List<List<Component>> newComponents = new ArrayList<>();
        addComponents(mRootData, mComponents, newComponents);
        mComponents = newComponents;
        removeAll();
        for (List<Component> componentRow : mComponents) {
            for (Component component : componentRow) {
                add(component);
            }
        }
        updateUI();
    }

    private void addComponents(GraphQLNode node, List<List<Component>> oldComponentMatrix, List<List<Component>> newComponentMatrix) {
        List<Component> componentRow = new ArrayList<>();

        GraphQLNodeComponent gnc = getGraphQLNodeComponent(node, oldComponentMatrix);
        componentRow.add(gnc);

        Set<GraphQLArgument> arguments = node.getArguments();
        if ((arguments != null) && (arguments.size() > 0)) {
            componentRow.add(new OpaqueLabel(" ("));
            Iterator<GraphQLArgument> iterator = arguments.iterator();
            while (iterator.hasNext()) {
                GraphQLArgComponent gac = getGraphQLArgComponent(iterator.next(), oldComponentMatrix);
                componentRow.add(gac);
                if (iterator.hasNext()) {
                    componentRow.add(new OpaqueLabel(", "));
                }
            }
            componentRow.add(new OpaqueLabel(")"));
        }

        Set<GraphQLNode> children = node.getChildren();
        if ((children != null) && (children.size() > 0)) {
            componentRow.add(new OpaqueLabel(" {"));
            newComponentMatrix.add(componentRow);

            Iterator<GraphQLNode> iterator = children.iterator();
            while (iterator.hasNext()) {
                addComponents(iterator.next(), oldComponentMatrix, newComponentMatrix);
            }

            componentRow = new ArrayList<>();
            componentRow.add(new OpaqueLabel("}"));
            newComponentMatrix.add(componentRow);
        } else {
            newComponentMatrix.add(componentRow);
        }
    }

    private GraphQLNodeComponent getGraphQLNodeComponent(GraphQLNode node, List<List<Component>> componentMatrix) {
        GraphQLNodeComponent gnc = removeGraphQLNodeComponent(node, componentMatrix);
        if (gnc == null) {
            gnc = new GraphQLNodeComponent(node, this);
            gnc.addGraphQLNodeComponentListener(mGraphQLNodeComponentListener);
        }
        return gnc;
    }

    private GraphQLArgComponent getGraphQLArgComponent(GraphQLArgument argument, List<List<Component>> componentMatrix) {
        GraphQLArgComponent gac = removeGraphQLArgComponent(argument, componentMatrix);
        if (gac == null) {
            gac = new GraphQLArgComponent(argument, this);
            gac.addGraphQLArgComponentListener(mGraphQLArgComponentListener);
        }
        return gac;
    }

    private GraphQLNodeComponent removeGraphQLNodeComponent(GraphQLNode node, List<List<Component>> componentMatrix) {
        for (int i = 0; i < componentMatrix.size(); i++) {
            List<Component> componentRow = componentMatrix.get(i);
            for (int j = 0; j < componentRow.size(); j++) {
                Component component = componentRow.get(j);
                if (component instanceof GraphQLNodeComponent) {
                    GraphQLNodeComponent graphQLNodeComponent = (GraphQLNodeComponent) component;
                    if (graphQLNodeComponent.getData() == node) {
                        componentRow.remove(j);
                        if (componentRow.isEmpty()) {
                            componentMatrix.remove(i);
                        }
                        return graphQLNodeComponent;
                    }
                }
            }
        }
        return null;
    }

    private GraphQLArgComponent removeGraphQLArgComponent(GraphQLArgument argument, List<List<Component>> componentMatrix) {
        for (int i = 0; i < componentMatrix.size(); i++) {
            List<Component> componentRow = componentMatrix.get(i);
            for (int j = 0; j < componentRow.size(); j++) {
                Component component = componentRow.get(j);
                if (component instanceof GraphQLArgComponent) {
                    GraphQLArgComponent graphQLArgComponent = (GraphQLArgComponent) component;
                    if (graphQLArgComponent.getData() == argument) {
                        componentRow.remove(j);
                        if (componentRow.isEmpty()) {
                            componentMatrix.remove(i);
                        }
                        return graphQLArgComponent;
                    }
                }
            }
        }
        return null;
    }

    private GraphQLNodeComponent findGraphQLNodeComponent(GraphQLNode node, List<List<Component>> componentMatrix) {
        for (List<Component> componentRow : componentMatrix) {
            for (Component component : componentRow) {
                if (component instanceof GraphQLNodeComponent) {
                    GraphQLNodeComponent graphQLNodeComponent = (GraphQLNodeComponent) component;
                    if (graphQLNodeComponent.getData() == node) {
                        return graphQLNodeComponent;
                    }
                }
            }
        }
        return null;
    }

    private GraphQLArgComponent findGraphQLArgComponent(GraphQLArgument argument, List<List<Component>> componentMatrix) {
        for (List<Component> componentRow : componentMatrix) {
            for (Component component : componentRow) {
                if (component instanceof GraphQLArgComponent) {
                    GraphQLArgComponent graphQLArgComponent = (GraphQLArgComponent) component;
                    if (graphQLArgComponent.getData() == argument) {
                        return graphQLArgComponent;
                    }
                }
            }
        }
        return null;
    }

    /* *********************************************************************************************
     * SchemaTypePanel.SchemaTypeSelectionListener
     * *********************************************************************************************/
    @Override
    public void onValueChanged(Class clazz, String identifier, Class type, boolean selected) {
        if ((mSelectedComponent == null) || (!(mSelectedComponent instanceof GraphQLNodeComponent))) {
            return;
        }

        GraphQLNode selectedNode = ((GraphQLNodeComponent) mSelectedComponent).getData();

        if (selected) {
            GraphQLNode data = null;
            if (TypeUtil.isInterface(clazz)) {
                if (type != null) {
                    Class[] interfaceClasses = type.getInterfaces();
                    if ((interfaceClasses != null) && (interfaceClasses.length > 0)) {
                        for (Class interfaceClass : interfaceClasses) {
                            if (interfaceClass.getName().equals(clazz.getName())) {
                                data = GraphQLNode.asFragment(type);
                                break;
                            }
                        }
                    }
                }
                if (data == null) {
                    data = GraphQLNode.asNode(identifier);
                }
            } else if (TypeUtil.isUnion(clazz)) {
                if (type != null) {
                    data = GraphQLNode.asFragment(type);
                } else {
                    data = GraphQLNode.asNode(identifier);
                }
            } else {
                data = GraphQLNode.asNode(identifier);
            }

            SchemaTypePanel component = null;
            if ((type != null) && (type.isArray())) {
                type = type.getComponentType();
            }
            if ((type == null) || TypeUtil.isEnum(type) || TypeUtil.isInputObject(type) || TypeUtil.isScalar(type)) {
                // do nothing
            } else {
                component = new SchemaTypePanel(type, true);
                component.setGraphQLEntryPanel(GraphQLEntryPanel.this);
            }

            if (component != null) {
                data.addTag("component", component);
            }
            selectedNode.addNode(data);
        } else {
            GraphQLNode data = null;
            if (TypeUtil.isInterface(clazz)) {
                Class[] interfaceClasses = type.getInterfaces();
                if ((interfaceClasses != null) && (interfaceClasses.length > 0)) {
                    for (Class interfaceClass : interfaceClasses) {
                        if (interfaceClass.getName().equals(clazz.getName())) {
                            data = selectedNode.getNode(type);
                            break;
                        }
                    }
                }
                if (data == null) {
                    data = selectedNode.getNode(identifier);
                }
            } else if (TypeUtil.isUnion(clazz)) {
                data = selectedNode.getNode(type);
            } else {
                data = selectedNode.getNode(identifier);
            }
            selectedNode.removeNode(data);
        }

        updateComponents();
        onSelectionChanged(clazz, identifier, type, true, GraphQLEntryPanel.this);

        for (GraphQLChangeListener listener : mGraphQLChangeListenerList) {
            listener.onGraphQLChanged(mRootData);
        }
    }

    @Override
    public void onFieldSelected(Class clazz, String identifier) {}

    @Override
    public void onTypeSelected(Class type) {}

    @Override
    public void onSelectionChanged(Class clazz, String identifier, Class type, boolean isEditable, GraphQLEntryPanel graphQLEntryPanel) {
        if ((mSelectedComponent == null) || (!(mSelectedComponent instanceof GraphQLNodeComponent))) {
            return;
        }

        GraphQLNode selectedNode = ((GraphQLNodeComponent) mSelectedComponent).getData();

        GraphQLNode data = null;
        if (TypeUtil.isInterface(clazz)) {
            if (type != null) {
                Class[] interfaceClasses = type.getInterfaces();
                if ((interfaceClasses != null) && (interfaceClasses.length > 0)) {
                    for (Class interfaceClass : interfaceClasses) {
                        if (interfaceClass.getName().equals(clazz.getName())) {
                            data = selectedNode.getNode(type);
                            break;
                        }
                    }
                }
            }
            if (data == null) {
                data = selectedNode.getNode(identifier);
            }
        } else if (TypeUtil.isUnion(clazz)) {
            data = selectedNode.getNode(type);
        } else {
            data = selectedNode.getNode(identifier);
        }

        if (data != null) {
            GraphQLNodeComponent gnc = findGraphQLNodeComponent(data, mComponents);
            if (gnc == mPreparedComponent) {
                return;
            }
            if ((gnc != null) && (mPreparedComponent != null)) {
                mPreparedComponent.setBackground(Color.WHITE);
            }
            mPreparedComponent = gnc;
            if (mPreparedComponent != null) {
                mPreparedComponent.setBackground(Color.PINK);
            }
        } else {
            if (mPreparedComponent != null) {
                mPreparedComponent.setBackground(Color.WHITE);
                mPreparedComponent = null;
            }
        }
    }

    /* *********************************************************************************************
     * SchemaArgsPanel.SchemaArgsSelectionListener
     * *********************************************************************************************/
    @Override
    public void onArgValueChanged(SchemaArgsPanel panel, Class clazz, String identifier, String argument, Class type, String value, String required) {
        if (mPreparedComponent == null) {
            return;
        }

        GraphQLNode preparedNode = ((GraphQLNodeComponent) mPreparedComponent).getData();
        GraphQLArgument preparedArgument = preparedNode.getArgument(argument);
        Object argValue = TypeUtil.getArgValue(type, value);
        int nonNull = TypeUtil.getNonNull(required);

        if (preparedArgument == null) {
            GraphQLArgument data;
            if (value.startsWith("$")) {
                data = GraphQLArgument.asVariable(argument, argValue, type, value.substring(1), nonNull);
            } else {
                data = GraphQLArgument.asArgument(argument, argValue, type);
            }

            data.addTag("component", panel);
            preparedNode.addArgument(data);
        } else {
            if (value.startsWith("$")) {
                String varName = value.substring(1);
                preparedArgument.setVarName(varName, nonNull);
            } else {
                preparedArgument.setArgValue(argValue);
            }
        }

        updateComponents();

        for (GraphQLChangeListener listener : mGraphQLChangeListenerList) {
            listener.onGraphQLChanged(mRootData);
        }
    }

    @Override
    public void onArgFieldSelected(Class clazz, String identifier, String argument) {}

    @Override
    public void onArgTypeSelected(Class type) {}

    @Override
    public void onArgSelectionChanged(Class clazz, String identifier, String argument, Class type) {}

    private static class OpaqueLabel extends JLabel {
        public OpaqueLabel(String text) {
            super(text);
            setOpaque(true);
            setBackground(Color.WHITE);
        }
    }

    private class GraphQLLayout implements LayoutManager {
        private int leftPadding = 5;
        private int rightPadding = 5;
        private int topPadding = 5;
        private int bottomPadding = 5;
        private int horiGap = 10;
        private int vertGap = 5;

        @Override
        public void addLayoutComponent(String name, Component comp) {}

        @Override
        public void removeLayoutComponent(Component comp) {}

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            if (mComponents.isEmpty()) {
                return new Dimension(0, 0);
            }

            int width = 0;
            int height = 0;
            int indent = 0;
            for (List<Component> componentRow : mComponents) {
                int x = indent * horiGap;
                int y = 0;
                for (Component component : componentRow) {
                    Dimension size = component.getPreferredSize();
                    if (component instanceof OpaqueLabel) {
                        String text = ((OpaqueLabel) component).getText();
                        if (text.equals(" {")) {
                            indent += 2;
                        } else if (text.equals("}")) {
                            indent -= 2;
                        }
                    }
                    x += size.width;
                    if (y < size.height) {
                        y = size.height;
                    }
                }
                if (width < x) {
                    width = x;
                }
                height += (y + vertGap);
            }

            width += (leftPadding + rightPadding);
            height += (topPadding + bottomPadding - vertGap);
            return new Dimension(width, height);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            if (mComponents.isEmpty()) {
                return new Dimension(0, 0);
            }

            int width = 0;
            int height = 0;
            int indent = 0;
            for (List<Component> componentRow : mComponents) {
                int x = indent * horiGap;
                int y = 0;
                for (Component component : componentRow) {
                    Dimension size = component.getMinimumSize();
                    if (component instanceof OpaqueLabel) {
                        String text = ((OpaqueLabel) component).getText();
                        if (text.equals(" {")) {
                            indent += 2;
                        } else if (text.equals("}")) {
                            indent -= 2;
                        }
                    }
                    x += size.width;
                    if (y < size.height) {
                        y = size.height;
                    }
                }
                if (width < x) {
                    width = x;
                }
                height += (y + vertGap);
            }

            width += (leftPadding + rightPadding);
            height += (topPadding + bottomPadding - vertGap);
            return new Dimension(width, height);
        }

        @Override
        public void layoutContainer(Container parent) {
            if (mComponents.isEmpty()) {
                return;
            }

            int y = topPadding;
            int indent = 0;
            for (List<Component> componentRow : mComponents) {
                int x = leftPadding + indent * horiGap;
                int maxHeight = 0;
                for (Component component : componentRow) {
                    if (component instanceof OpaqueLabel) {
                        String text = ((OpaqueLabel) component).getText();
                        if (text.equals(" {")) {
                            indent += 2;
                        } else if (text.equals("}")) {
                            indent -= 2;
                            x -= 2 * horiGap;
                        }
                    }
                    Dimension size = component.getPreferredSize();
                    component.setBounds(x, y, size.width, size.height);
                    x += size.width;
                    if (size.height > maxHeight) {
                        maxHeight = size.height;
                    }
                }
                y += (maxHeight + vertGap);
            }
        }
    }
}
