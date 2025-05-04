package com.library.gui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import com.library.model.User;
import javax.swing.border.EmptyBorder;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;

public abstract class BaseDashboard extends JFrame {
    protected User user;
    protected JPanel mainPanel;
    protected JPanel headerPanel;
    protected JPanel contentPanel;
    protected JPanel sidebarPanel;
    protected JButton logoutButton;
    protected JLabel welcomeLabel;
    protected JLabel userTypeLabel;
    protected boolean darkMode = false;
    protected JButton darkModeToggleButton;
    protected Color lightBg = Color.WHITE;
    protected Color darkBg = new Color(36, 41, 46);
    protected Color lightRow = new Color(240, 240, 240);
    protected Color darkRow = new Color(44, 62, 80);
    protected Color lightText = Color.BLACK;
    protected Color darkText = Color.WHITE;

    public BaseDashboard(User user) {
        this.user = user;
        setTitle("Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 800));

        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        initializeBaseComponents();
        layoutBaseComponents();
    }

    protected void initializeBaseComponents() {
        // Main panel with modern gradient background
        mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(52, 152, 219);
                Color color2 = new Color(41, 128, 185);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header panel with modern design
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(44, 62, 80));
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Welcome label with modern font
        welcomeLabel = new JLabel("Welcome, " + user.getName());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        // Stylish, centered heading with modern gradient effect
        JLabel titleLabel = new JLabel("Smart Library Management System") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Create gradient paint with vibrant cyan and blue
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(0, 255, 255), // Bright cyan
                    getWidth(), getHeight(), new Color(0, 191, 255) // Deep sky blue
                );
                g2d.setPaint(gp);
                
                // Draw text with gradient
                Font font = new Font("Segoe UI", Font.BOLD, 36);
                g2d.setFont(font);
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2d.drawString(getText(), x, y);
                
                // Add bright white highlight effect
                g2d.setColor(new Color(255, 255, 255, 40)); // Bright white glow
                g2d.drawString(getText(), x - 1, y - 1);
            }
        };
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setPreferredSize(new Dimension(0, 60));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Modern logout button
        logoutButton = new JButton("Logout") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(231, 76, 60));
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                super.paintComponent(g);
            }
        };
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setPreferredSize(new Dimension(100, 40));
        logoutButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        // Profile button (header, left of Settings)
        JButton profileButton = new JButton("Profile");
        profileButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        profileButton.setFocusPainted(false);
        profileButton.setContentAreaFilled(false);
        profileButton.setBorderPainted(false);
        profileButton.setForeground(Color.WHITE);
        profileButton.setPreferredSize(new Dimension(100, 40));
        profileButton.addActionListener(e -> {
            UserProfileDialog dialog = new UserProfileDialog(this, user);
            dialog.setVisible(true);
        });

        // Settings button (header, left of logout)
        JButton settingsButton = new JButton("Settings");
        settingsButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        settingsButton.setFocusPainted(false);
        settingsButton.setContentAreaFilled(false);
        settingsButton.setBorderPainted(false);
        settingsButton.setForeground(Color.WHITE);
        settingsButton.setPreferredSize(new Dimension(100, 40));
        settingsButton.addActionListener(e -> {
            SettingsDialog dialog = new SettingsDialog(this, darkMode, this::toggleDarkMode);
            dialog.setVisible(true);
        });

        // Panel for right-aligned header buttons (Profile, Settings, Logout)
        JPanel headerButtonPanel = new JPanel();
        headerButtonPanel.setOpaque(false);
        headerButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerButtonPanel.add(profileButton);
        headerButtonPanel.add(settingsButton);
        headerButtonPanel.add(logoutButton);
        headerPanel.add(headerButtonPanel, BorderLayout.EAST);

        // Sidebar panel with modern design and content
        sidebarPanel = new JPanel();
        sidebarPanel.setBackground(new Color(44, 62, 80));
        sidebarPanel.setPreferredSize(new Dimension(270, 0));
        sidebarPanel.setBorder(new EmptyBorder(30, 20, 30, 20));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));

        // Library logo (provided image, loaded from file path)
        try {
            ImageIcon logoIcon = new ImageIcon("src/main/java/com/library/9043296.png");
            Image logoImg = logoIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(logoImg));
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            sidebarPanel.add(logoLabel);
        } catch (Exception e) {
            JLabel logoLabel = new JLabel("📚");
            logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            sidebarPanel.add(logoLabel);
        }
        sidebarPanel.add(Box.createVerticalStrut(30));

        // Modern colorful buttons
        JButton returnBookButton = createModernButton("Return Book at Time", new Color(52, 152, 219));
        returnBookButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        returnBookButton.setMaximumSize(new Dimension(220, 45));
        returnBookButton.setFocusable(false);
        sidebarPanel.add(returnBookButton);
        sidebarPanel.add(Box.createVerticalStrut(25));

        JButton fineButton = createModernButton("Fine for Late Return", new Color(39, 174, 96));
        fineButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        fineButton.setMaximumSize(new Dimension(220, 45));
        fineButton.setFocusable(false);
        sidebarPanel.add(fineButton);
        sidebarPanel.add(Box.createVerticalStrut(40));

        // Quotes section (Einstein and Borges only, with professional spacing)
        Object[][] quotes = {
            {"The only thing that you absolutely have to know, is the location of the library.", "Albert Einstein", "src/main/java/com/library/735.jpg"},
            {"I have always imagined that Paradise will be a kind of library.", "Jorge Luis Borges", "src/main/java/com/library/OIP (2).jpeg"}
        };
        boolean first = true;
        for (Object[] quotePair : quotes) {
            if (!first) sidebarPanel.add(Box.createVerticalStrut(32)); // Large gap between quotes
            first = false;
            JPanel quotePanel = new JPanel();
            quotePanel.setOpaque(false);
            quotePanel.setLayout(new BoxLayout(quotePanel, BoxLayout.Y_AXIS));
            // Author photo (larger, above quote)
            try {
                ImageIcon authorIcon = new ImageIcon((String)quotePair[2]);
                Image authorImg = authorIcon.getImage().getScaledInstance(96, 96, Image.SCALE_SMOOTH);
                JLabel authorPicLabel = new JLabel(new ImageIcon(authorImg));
                authorPicLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                quotePanel.add(authorPicLabel);
            } catch (Exception e) {
                JLabel authorPicLabel = new JLabel("👤");
                authorPicLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
                authorPicLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                quotePanel.add(authorPicLabel);
            }
            quotePanel.add(Box.createVerticalStrut(16)); // Gap between photo and quote
            // Quote and author text
            JLabel quoteLabel = new JLabel("<html><center><i>\"" + quotePair[0] + "\"</i></center></html>");
            quoteLabel.setFont(new Font("Georgia", Font.ITALIC, 14));
            quoteLabel.setForeground(new Color(236, 240, 241));
            quoteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            quotePanel.add(quoteLabel);
            JLabel authorLabel = new JLabel("- " + quotePair[1]);
            authorLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            authorLabel.setForeground(new Color(189, 195, 199));
            authorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            quotePanel.add(authorLabel);
            quotePanel.setBorder(new EmptyBorder(0, 0, 0, 0));
            sidebarPanel.add(quotePanel);
        }

        // Add filler to push content to the top
        sidebarPanel.add(Box.createVerticalGlue());
        // Add dark mode toggle button at the bottom of the sidebar
        darkModeToggleButton = new JButton("🌙");
        darkModeToggleButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        darkModeToggleButton.setFocusPainted(false);
        darkModeToggleButton.setContentAreaFilled(false);
        darkModeToggleButton.setBorderPainted(false);
        darkModeToggleButton.setForeground(Color.WHITE);
        darkModeToggleButton.addActionListener(e -> toggleDarkMode());
        sidebarPanel.add(darkModeToggleButton);

        // Content panel with modern design
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
    }

    protected void layoutBaseComponents() {
        // Add components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Add main panel to frame
        add(mainPanel);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    // Helper method to create modern buttons
    protected JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(color);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                super.paintComponent(g);
            }
        };
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setPreferredSize(new Dimension(200, 40));
        button.setMargin(new Insets(10, 10, 10, 10));
        return button;
    }

    // Helper method to create modern panels
    protected JPanel createModernPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        return panel;
    }

    // Enhanced modern table with zebra striping, sorting, filtering, and actions
    protected JTable createModernTable(Object[][] data, String[] columns) {
        // Add actions column if not present
        String[] colsWithActions = new String[columns.length + 1];
        System.arraycopy(columns, 0, colsWithActions, 0, columns.length);
        colsWithActions[columns.length] = "Actions";

        Object[][] dataWithActions = new Object[data.length][colsWithActions.length];
        for (int i = 0; i < data.length; i++) {
            System.arraycopy(data[i], 0, dataWithActions[i], 0, columns.length);
            dataWithActions[i][columns.length] = ""; // Placeholder for actions
        }

        JTable table = new JTable(dataWithActions, colsWithActions) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (isRowSelected(row)) {
                    c.setBackground(new Color(52, 152, 219));
                    c.setForeground(Color.WHITE);
                } else {
                    if (darkMode) {
                        c.setBackground(row % 2 == 0 ? darkBg : darkRow);
                        c.setForeground(darkText);
                    } else {
                        c.setBackground(row % 2 == 0 ? lightBg : lightRow);
                        c.setForeground(lightText);
                    }
                }
                // Add left padding for the first column
                if (column == 0 && c instanceof JComponent) {
                    ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
                } else if (c instanceof JComponent) {
                    ((JComponent) c).setBorder(null);
                }
                return c;
            }
        };
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(darkMode ? darkRow : new Color(52, 73, 94));
        table.getTableHeader().setForeground(darkMode ? darkText : Color.BLACK);
        table.getTableHeader().setPreferredSize(new Dimension(0, 50));

        // Enable sorting
        table.setAutoCreateRowSorter(true);

        // Add action buttons (edit/delete) as icons
        table.getColumn("Actions").setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
                panel.setOpaque(false);
                JButton editBtn = new JButton(new ImageIcon(getClass().getResource("/edit_icon.png")));
                JButton delBtn = new JButton(new ImageIcon(getClass().getResource("/delete_icon.png")));
                editBtn.setPreferredSize(new Dimension(24, 24));
                delBtn.setPreferredSize(new Dimension(24, 24));
                editBtn.setBorderPainted(false);
                delBtn.setBorderPainted(false);
                editBtn.setContentAreaFilled(false);
                delBtn.setContentAreaFilled(false);
                panel.add(editBtn);
                panel.add(delBtn);
                return panel;
            }
        });
        return table;
    }

    protected void toggleDarkMode() {
        darkMode = !darkMode;
        updateTheme();
    }

    protected void updateTheme() {
        Color bg = darkMode ? darkBg : lightBg;
        Color fg = darkMode ? darkText : lightText;
        mainPanel.setBackground(bg);
        headerPanel.setBackground(darkMode ? new Color(30, 34, 40) : new Color(44, 62, 80));
        sidebarPanel.setBackground(darkMode ? new Color(30, 34, 40) : new Color(44, 62, 80));
        contentPanel.setBackground(bg);
        welcomeLabel.setForeground(Color.WHITE);
        if (userTypeLabel != null) userTypeLabel.setForeground(darkMode ? Color.WHITE : new Color(44, 62, 80));
        darkModeToggleButton.setForeground(fg);
        repaint();
    }

    protected abstract void initializeComponents();
    protected abstract void layoutComponents();
} 