package com.zzw.github.graphql.client;

import com.zzw.github.graphql.builder.GraphQLArgument;
import com.zzw.github.graphql.builder.GraphQLBuilder;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zzw on 2018/8/2.
 */
public class GraphQLArgComponent extends JLabel implements MouseListener {
    private static final Border EXITED_BORDER = new EmptyBorder(2, 2, 2, 2);
    private static final Border ENTERED_BORDER = new LineBorder(Color.RED, 2);
    private GraphQLArgument mData;
    private SchemaArgsPanel mComponent;
    private GraphQLEntryPanel mContainer;
    private List<GraphQLArgComponentListener> mGraphQLArgComponentListenerList = new ArrayList<>();

    public void addGraphQLArgComponentListener(GraphQLArgComponentListener listener) {
        if ((listener != null) && (!mGraphQLArgComponentListenerList.contains(listener))) {
            mGraphQLArgComponentListenerList.add(listener);
        }
    }

    public void removeGraphQLArgComponentListener(GraphQLArgComponentListener listener) {
        if ((listener != null) && mGraphQLArgComponentListenerList.contains(listener)) {
            mGraphQLArgComponentListenerList.remove(listener);
        }
    }

    public interface GraphQLArgComponentListener {
        void onArgClicked(GraphQLArgument data, SchemaArgsPanel component);
    }

    public GraphQLArgComponent(GraphQLArgument data, GraphQLEntryPanel container) {
        mData = data;
        Object component = (mData == null) ? null : data.getTag("component");
        mComponent = (component == null) ? null : ((SchemaArgsPanel) component);
        mContainer = container;
        setOpaque(true);
        setBackground(Color.WHITE);
        setBorder(EXITED_BORDER);
        addMouseListener(this);
    }

    public GraphQLArgument getData() { return mData; }

    public SchemaArgsPanel getComponent() { return mComponent; }

    @Override
    public String getText() {
        if (mData == null) {
            return null;
        } else {
            String text = null;
            try {
                Method method = GraphQLBuilder.class.getDeclaredMethod("buildArgument", GraphQLArgument.class, int.class);
                method.setAccessible(true);
                text = (String) method.invoke(new GraphQLBuilder(), mData, 0);
            } catch (NoSuchMethodException e1) {
                e1.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            } catch (InvocationTargetException e3) {
                e3.printStackTrace();
            }
            return text;
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
            if ((component != null) && (component != GraphQLArgComponent.this)) {
                component.setBackground(Color.WHITE);
            }
            mContainer.setSelectedComponent(GraphQLArgComponent.this);
            GraphQLArgComponent.this.setBackground(Color.YELLOW);
            for (GraphQLArgComponentListener listener : mGraphQLArgComponentListenerList) {
                listener.onArgClicked(mData, mComponent);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}
}
