package com.log.capital.trial.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.formdev.flatlaf.FlatDarkLaf;

public class TelegramCloneClient extends JFrame {

    private WebSocketClient client;

    private final DefaultListModel<String> messageModel;

    private final JTextField messageField;

    public TelegramCloneClient() {
        setTitle("Telegram MVP");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // Основной контейнер: Список чатов (слева) и Окно чата (справа)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setDividerSize(1);

        // Левая панель (Заглушка списка контактов)
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(new Color(33, 33, 33)); // Цвет TG Dark
        sidebar.add(new JLabel("  Чаты (MVP)", JLabel.LEFT), BorderLayout.NORTH);
        splitPane.setLeftComponent(sidebar);

        // Правая панель (Окно сообщений)
        JPanel chatPanel = new JPanel(new BorderLayout());

        // Область сообщений
        messageModel = new DefaultListModel<>();
        JList<String> messageList = new JList<>(messageModel);
        messageList.setBackground(new Color(14, 22, 33)); // Фон чата TG
        chatPanel.add(new JScrollPane(messageList), BorderLayout.CENTER);

        // Поле ввода
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messageField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        messageField.addActionListener(e -> sendMessage());
        JButton sendBtn = new JButton("ОТПРАВИТЬ");
        sendBtn.addActionListener(e -> sendMessage());

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        splitPane.setRightComponent(chatPanel);
        add(splitPane);
    }

    public void connectWebSocket(String username, String password) {
        try {
            URI serverUri = new URI("ws://localhost:8080/chat-server");
            String auth = username +":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Basic " + encodedAuth);
            client = new WebSocketClient(serverUri, headers) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                        send("CONNECT\naccept-version:1.1,1.2\nheart-beat:10000,10000\n\n\u0000");
                        send("SUBSCRIBE\nid:sub-0\ndestination:/topic/messages\n\n\u0000");
                }

                @Override
                public void onMessage(String message) {
                    SwingUtilities.invokeLater(() -> {
                        messageModel.addElement(message);
                    });
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Соединение закрыто: " + reason + "code: " + code);
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };
            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String text = messageField.getText();
        if (client != null && client.isOpen()) {
            String stompFrame = "SEND\ndestination:/topic/messages\ncontent-type:text/plain\n\n" + text + "\u0000";
            client.send(stompFrame);
            messageField.setText("");
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
        } catch (Exception ex) {
        }

        SwingUtilities.invokeLater(() -> {
            TelegramCloneClient mainFrame = new TelegramCloneClient();
            LoginDialog loginDlg = new LoginDialog(mainFrame);
            loginDlg.setVisible(true);

            if (loginDlg.isSucceeded()) {
                mainFrame.connectWebSocket(loginDlg.getUsername(), loginDlg.getPassword());
                mainFrame.setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}
