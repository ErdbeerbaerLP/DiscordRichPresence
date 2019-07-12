package de.erdbeerbaerlp.discordrpc;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Window which gets called when mod is first used (will be prevented when another mod disables this mod first)
 */
public class FirstLaunchWindow extends JDialog {
    private final JTextField textField = new JTextField();

    protected FirstLaunchWindow() {
        setModal(true);
        setTitle("DiscordRPC FistLaunch");
        setSize(253, 228);
        setLocationRelativeTo(null);
        getContentPane().setLayout(null);

        JButton btnOk = new JButton("OK");

        btnOk.setBounds(69, 153, 89, 23);
        getContentPane().add(btnOk);


        textField.setBounds(37, 108, 148, 20);
        getContentPane().add(textField);
        JLabel lblEmpty = new JLabel("<html><font color=red>Please enter some text");
        lblEmpty.setVisible(false);
        lblEmpty.setBounds(37, 128, 148, 14);
        getContentPane().add(lblEmpty);
        textField.setColumns(10);
        btnOk.addActionListener(arg0 -> {
            if (textField.getText().isEmpty()) lblEmpty.setVisible(true);
            else {
                setVisible(false);
            }
        });
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent arg0) {
                lblEmpty.setVisible(false);
            }
        });
        JLabel lblpleaseEnterThe = new JLabel("<html>Please enter the Client/Modpack name below.<br>\r\n(The first line of your Discord RichPresence)<br>");
        lblpleaseEnterThe.setBounds(12, 11, 213, 84);
        getContentPane().add(lblpleaseEnterThe);


    }

    protected String getClientName() {
        if (this.textField.getText().isEmpty()) return "Vanilla";
        else return this.textField.getText();
    }
}
