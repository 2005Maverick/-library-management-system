package com.library.gui;

import com.library.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class UserProfileDialog extends JDialog {
    private JLabel avatarLabel;
    private JButton uploadButton;
    private User user;
    private static final int AVATAR_SIZE = 96;

    public UserProfileDialog(JFrame parent, User user) {
        super(parent, "User Profile", true);
        this.user = user;
        setLayout(new BorderLayout(20, 20));
        setSize(400, 350);
        setLocationRelativeTo(parent);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Avatar
        avatarLabel = new JLabel();
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setPreferredSize(new Dimension(AVATAR_SIZE, AVATAR_SIZE));
        loadAvatar();
        infoPanel.add(avatarLabel);
        infoPanel.add(Box.createVerticalStrut(10));

        // Upload button
        uploadButton = new JButton("Upload/Change Avatar");
        uploadButton.addActionListener(this::onUploadAvatar);
        infoPanel.add(uploadButton);
        infoPanel.add(Box.createVerticalStrut(20));

        // User info
        infoPanel.add(new JLabel("Name: " + user.getName()));
        infoPanel.add(new JLabel("Email: " + user.getEmail()));
        infoPanel.add(new JLabel("User Type: " + user.getUserType()));

        add(infoPanel, BorderLayout.CENTER);
    }

    private void loadAvatar() {
        File avatarFile = getAvatarFile();
        if (avatarFile.exists()) {
            try {
                Image img = ImageIO.read(avatarFile).getScaledInstance(AVATAR_SIZE, AVATAR_SIZE, Image.SCALE_SMOOTH);
                avatarLabel.setIcon(new ImageIcon(img));
            } catch (IOException e) {
                avatarLabel.setIcon(defaultAvatarIcon());
            }
        } else {
            avatarLabel.setIcon(defaultAvatarIcon());
        }
    }

    private void onUploadAvatar(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            try {
                Image img = ImageIO.read(selected).getScaledInstance(AVATAR_SIZE, AVATAR_SIZE, Image.SCALE_SMOOTH);
                ImageIO.write(ImageIO.read(selected), "png", getAvatarFile());
                avatarLabel.setIcon(new ImageIcon(img));
                JOptionPane.showMessageDialog(this, "Avatar updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to load image!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private File getAvatarFile() {
        return new File("avatars", "avatar_" + user.getId() + ".png");
    }

    private Icon defaultAvatarIcon() {
        // Simple default avatar (circle with initials)
        BufferedImage img = new BufferedImage(AVATAR_SIZE, AVATAR_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(new Color(200, 200, 200));
        g2.fillOval(0, 0, AVATAR_SIZE, AVATAR_SIZE);
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 32));
        String initials = user.getName().length() > 0 ? user.getName().substring(0, 1).toUpperCase() : "?";
        FontMetrics fm = g2.getFontMetrics();
        int x = (AVATAR_SIZE - fm.stringWidth(initials)) / 2;
        int y = (AVATAR_SIZE - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(initials, x, y);
        g2.dispose();
        return new ImageIcon(img);
    }
} 