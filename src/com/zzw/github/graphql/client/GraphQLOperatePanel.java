package com.zzw.github.graphql.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Created by zzw on 2018/8/8.
 */
public class GraphQLOperatePanel extends JPanel {
    private JLabel mAccessTokenLabel = new JLabel(new ImageIcon(getClass().getResource("image/access.png")));
    private JTextField mAccessTokenField = new JTextField("24c7963d1bea8a280869514fb02e4be0fc95038f");
    private JButton mInputButton = new JButton(new ImageIcon(getClass().getResource("image/import.png")));
    private JTextArea mInputArea = new JTextArea();
    private JButton mOutputButton = new JButton(new ImageIcon(getClass().getResource("image/execute.png")));
    private JTextArea mOutputArea = new JTextArea();
    private NetworkAgent mNetworkAgent = new NetworkAgent();
    private GraphQLEditPanel mGraphQLEditPanel;

    public GraphQLOperatePanel(GraphQLEditPanel graphQLEditPanel) {
        mGraphQLEditPanel = graphQLEditPanel;

        Color color = new Color(0xffeeeeee);

        mAccessTokenLabel.setOpaque(true);
        mAccessTokenLabel.setBackground(color);
        mAccessTokenField.setHorizontalAlignment(SwingConstants.LEFT);

        mInputButton.setBorderPainted(false);
        mInputButton.setBackground(color);
        mInputArea.setLineWrap(true);
        mInputArea.setWrapStyleWord(true);

        mOutputButton.setBorderPainted(false);
        mOutputButton.setBackground(color);
        mOutputArea.setLineWrap(true);
        mOutputArea.setWrapStyleWord(true);

        initEvent();

        JPanel leftPanel = new JPanel(new GridLayout(3, 1, 0, 5));
        leftPanel.setBackground(color);
        leftPanel.add(mAccessTokenLabel);
        leftPanel.add(mInputButton);
        leftPanel.add(mOutputButton);

        JPanel rightPanel = new JPanel(new GridLayout(3, 1, 0, 5));
        rightPanel.setBackground(color);
        rightPanel.add(mAccessTokenField);
        rightPanel.add(new JScrollPane(mInputArea));
        rightPanel.add(new JScrollPane(mOutputArea));

        setLayout(new BorderLayout(0, 5));
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

    private void initEvent() {
        mInputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mGraphQLEditPanel != null) {
                    String payload = mGraphQLEditPanel.serialize();
                    if (payload != null) {
                        mInputArea.setText(payload);
                    }
                }
            }
        });

        mOutputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String accessToken = mAccessTokenField.getText();
                if ((accessToken == null) || (accessToken.trim().length() <= 0)) {
                    mOutputArea.setText("Invalid Access Token!");
                    mOutputArea.setEditable(false);
                    return;
                }

                String payload = mInputArea.getText();
                if ((payload == null) || (payload.trim().length() <= 0)) {
                    mOutputArea.setText("Invalid Input!");
                    mOutputArea.setEditable(false);
                    return;
                }

                mOutputButton.setEnabled(false);
                mOutputArea.setText("");
                mOutputArea.setEditable(false);
                mNetworkAgent.execute(accessToken, payload);
            }
        });
    }

    public class NetworkAgent {
        public void execute(final String accessToken, final String payload) {
            new Thread() {
                @Override
                public void run() {
                    com.zzw.github.graphql.network.GGClient client = new com.zzw.github.graphql.network.GGClient(
                            "https://api.github.com/graphql", accessToken);
                    try {
                        client.connect(payload, false);
                        String result = client.result();
                        mOutputButton.setEnabled(true);
                        mOutputArea.setText(result);
                        mOutputArea.setEditable(true);
                    } catch (IOException e) {
                        mOutputButton.setEnabled(true);
                        mOutputArea.setText(e.getMessage());
                        mOutputArea.setEditable(false);
                    } finally {
                        client.disconnect();
                    }
                }
            }.start();
        }
    }
}
