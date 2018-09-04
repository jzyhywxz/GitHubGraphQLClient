package com.zzw.github.graphql.client;

import com.zzw.github.graphql.builder.GraphQLNode;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zzw on 2018/8/2.
 */
public class GraphQLNodeComponent extends JLabel implements MouseListener {
    private static final Border EXITED_BORDER = new EmptyBorder(2, 2, 2, 2);
    private static final Border ENTERED_BORDER = new LineBorder(Color.RED, 2);
    private GraphQLNode mData;
    private SchemaTypePanel mComponent;
    private GraphQLEntryPanel mContainer;
    private List<GraphQLNodeComponentListener> mGraphQLNodeComponentListenerList = new ArrayList<>();

    public void addGraphQLNodeComponentListener(GraphQLNodeComponentListener listener) {
        if ((listener != null) && (!mGraphQLNodeComponentListenerList.contains(listener))) {
            mGraphQLNodeComponentListenerList.add(listener);
        }
    }

    public void removeGraphQLNodeComponentListener(GraphQLNodeComponentListener listener) {
        if ((listener != null) && mGraphQLNodeComponentListenerList.contains(listener)) {
            mGraphQLNodeComponentListenerList.remove(listener);
        }
    }

    public interface GraphQLNodeComponentListener {
        void onNodeClicked(GraphQLNode data, SchemaTypePanel component);
    }

    public GraphQLNodeComponent(GraphQLNode data, GraphQLEntryPanel container) {
        mData = data;
        Object component = (mData == null) ? null : data.getTag("component");
        mComponent = (component == null) ? null : ((SchemaTypePanel) component);
        mContainer = container;
        setOpaque(true);
        setBackground(Color.WHITE);
        setBorder(EXITED_BORDER);
        addMouseListener(this);
    }

    public GraphQLNode getData() { return mData; }

    public SchemaTypePanel getComponent() { return mComponent; }

    @Override
    public String getText() {
        if (mData == null) {
            return null;
        } else if (mData.isFragment()) {
            return "... on " + mData.getNodeType();
        } else {
            return mData.getNodeName();
        }
    }

    @Override
    public Icon getIcon() {
        if (mComponent == null) {
            return null;
        } else {
            return mComponent.getIdentifierIcon();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (mComponent != null) {
            setBorder(ENTERED_BORDER);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (mComponent != null) {
            setBorder(EXITED_BORDER);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (mComponent != null) {
            Component component = mContainer.getSelectedComponent();
            if ((component != null) && (component != GraphQLNodeComponent.this)) {
                component.setBackground(Color.WHITE);
            }
            mContainer.setSelectedComponent(GraphQLNodeComponent.this);
            GraphQLNodeComponent.this.setBackground(Color.YELLOW);
            for (GraphQLNodeComponentListener listener : mGraphQLNodeComponentListenerList) {
                listener.onNodeClicked(mData, mComponent);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}
}
