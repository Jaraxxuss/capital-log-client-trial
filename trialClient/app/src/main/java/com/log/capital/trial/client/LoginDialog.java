package com.log.capital.trial.client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.formdev.flatlaf.FlatClientProperties;

public class LoginDialog extends JDialog {
    private JTextField usernameField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();
    private boolean succeeded;

    public LoginDialog(Frame parent) {
        super(parent, "Вход в Telegram MVP", true);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 20, 10, 20);

        JLabel title = new JLabel("Введите данные", JLabel.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(title, gbc);

        usernameField = new JTextField(20);
        usernameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Имя пользователя");
        gbc.gridy = 1;
        add(usernameField, gbc);

        passwordField = new JPasswordField(20);
        passwordField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Пароль");
        gbc.gridy = 2;
        add(passwordField, gbc);

        JButton loginButton = new JButton("ВОЙТИ");
        loginButton.setBackground(new Color(43, 82, 120)); // Синий цвет TG
        loginButton.setForeground(Color.WHITE);
        gbc.gridy = 3;
        gbc.insets = new Insets(20, 20, 10, 20);
        add(loginButton, gbc);

        loginButton.addActionListener(e -> {
            if (authenticate(getUsername(), getPassword())) {
                succeeded = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Неверное имя или пароль",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private boolean authenticate(String user, String pass) {
        try {
            String auth = user + ":" + pass;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/auth/me")) // Наш новый эндпоинт
                    .header("Authorization", "Basic " + encodedAuth)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsername() { return usernameField.getText().trim(); }
    public String getPassword() { return new String(passwordField.getPassword()); }
    public boolean isSucceeded() { return succeeded; }
}
