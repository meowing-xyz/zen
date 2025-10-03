package xyz.meowing.zen;

import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URI;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.net.URLDecoder;

public class Installer extends JFrame {
    private static final Color BACKGROUND = new Color(23, 25, 31);
    private static final Color CARD_BACKGROUND = new Color(32, 35, 42);
    private static final Color ACCENT = new Color(88, 166, 255);
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255);
    private static final Color TEXT_SECONDARY = new Color(156, 163, 175);
    private static final Color BUTTON_HOVER = new Color(67, 56, 202);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new Installer().setVisible(true);
        });
    }

    public Installer() {
        setTitle("Zen Mod");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 450);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 600, 450, 20, 20));

        final Point[] mouseDownCompCoords = {null};
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                mouseDownCompCoords[0] = e.getPoint();
            }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent e) {
                Point currCoords = e.getLocationOnScreen();
                setLocation(currCoords.x - mouseDownCompCoords[0].x, currCoords.y - mouseDownCompCoords[0].y);
            }
        });

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createContentPanel(), BorderLayout.CENTER);
        mainPanel.add(createFooterPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.setBackground(BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BACKGROUND);
        header.setBorder(new EmptyBorder(20, 20, 15, 20));

        JLabel titleLabel = new JLabel("⚡ Zen Mod");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_PRIMARY);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(createCloseButton(), BorderLayout.EAST);

        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setPreferredSize(new Dimension(0, 1));
        separator.setForeground(new Color(75, 85, 99));
        separator.setBackground(new Color(75, 85, 99));
        separator.setOpaque(true);

        headerContainer.add(header, BorderLayout.CENTER);
        headerContainer.add(separator, BorderLayout.SOUTH);

        return headerContainer;
    }

    private JButton createCloseButton() {
        JButton closeButton = new JButton("×") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(new Color(248, 113, 113));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(239, 68, 68));
                } else {
                    g2.setColor(TEXT_SECONDARY);
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        closeButton.setForeground(Color.WHITE);
        closeButton.setPreferredSize(new Dimension(32, 32));
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);
        closeButton.setBorder(BorderFactory.createLineBorder(new Color(75, 85, 99), 1));
        closeButton.setContentAreaFilled(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> System.exit(0));

        return closeButton;
    }

    private JPanel createContentPanel() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel warningLabel = new JLabel("This mod cannot be run directly!");
        warningLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        warningLabel.setForeground(new Color(248, 113, 113));
        warningLabel.setHorizontalAlignment(SwingConstants.CENTER);
        warningLabel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel stepsPanel = new JPanel();
        stepsPanel.setLayout(new BoxLayout(stepsPanel, BoxLayout.Y_AXIS));
        stepsPanel.setBackground(BACKGROUND);

        String[] steps = {
                "1. Install Minecraft Forge 1.8.9-11.15.1.2318",
                "",
                "2. Place this JAR in your mods folder:",
                "    • Windows: %appdata%/.minecraft/mods/",
                "    • macOS: ~/Library/Application Support/minecraft/mods/",
                "    • Linux: ~/.minecraft/mods/",
                "",
                "3. Launch Minecraft with Forge profile"
        };

        for (String step : steps) {
            JLabel stepLabel = createStepLabel(step);
            stepsPanel.add(stepLabel);
        }

        content.add(warningLabel, BorderLayout.NORTH);
        content.add(stepsPanel, BorderLayout.CENTER);
        return content;
    }

    private static @NotNull JLabel createStepLabel(String step) {
        JLabel stepLabel = new JLabel(step.isEmpty() ? " " : step);
        stepLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        stepLabel.setForeground(step.startsWith("    •") ? TEXT_SECONDARY : TEXT_PRIMARY);
        stepLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        stepLabel.setBorder(new EmptyBorder(1, 0, 1, 0));
        return stepLabel;
    }

    private JPanel createFooterPanel() {
        JPanel footerContainer = new JPanel(new BorderLayout());
        footerContainer.setBackground(BACKGROUND);

        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setPreferredSize(new Dimension(0, 1));
        separator.setForeground(new Color(75, 85, 99));
        separator.setBackground(new Color(75, 85, 99));
        separator.setOpaque(true);

        JPanel footer = new JPanel(new GridBagLayout());
        footer.setBackground(BACKGROUND);
        footer.setBorder(new EmptyBorder(20, 40, 25, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 8, 0, 8);
        gbc.gridy = 0;

        JButton installButton = createModernButton("Install", "📁");
        JButton discordButton = createModernButton("Discord", "💬");
        JButton githubButton = createModernButton("GitHub", "🔗");

        installButton.addActionListener(e -> autoInstallMod());
        discordButton.addActionListener(e -> openURL("https://discord.gg/KPmHQUC97G"));
        githubButton.addActionListener(e -> openURL("https://github.com/kiwidotzip/zen"));

        gbc.gridx = 0;
        footer.add(installButton, gbc);
        gbc.gridx = 1;
        footer.add(discordButton, gbc);
        gbc.gridx = 2;
        footer.add(githubButton, gbc);

        footerContainer.add(separator, BorderLayout.NORTH);
        footerContainer.add(footer, BorderLayout.CENTER);

        return footerContainer;
    }

    private JButton createModernButton(String text, String icon) {
        JButton button = new JButton(icon + " " + text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(BUTTON_HOVER.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(BUTTON_HOVER);
                } else {
                    g2.setColor(ACCENT);
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(110, 45));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private void showThemedDialog(String message, String title, int messageType) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(400, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setShape(new RoundRectangle2D.Double(0, 0, 400, 150, 15, 15));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel messageLabel = new JLabel("<html><center>" + message.replace("\n", "<br>") + "</center></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(TEXT_PRIMARY);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton okButton = new JButton("OK");
        okButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        okButton.setForeground(Color.WHITE);
        okButton.setBackground(ACCENT);
        okButton.setPreferredSize(new Dimension(80, 35));
        okButton.setFocusPainted(false);
        okButton.setBorderPainted(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(CARD_BACKGROUND);
        buttonPanel.add(okButton);

        panel.add(messageLabel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private boolean showThemedConfirmDialog() {
        JDialog dialog = new JDialog(this, "File Exists", true);
        dialog.setSize(450, 180);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setShape(new RoundRectangle2D.Double(0, 0, 450, 180, 15, 15));

        final boolean[] result = {false};

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BACKGROUND);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel messageLabel = new JLabel("<html><center>A mod with this name already exists.<br>Replace it?</center></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(TEXT_PRIMARY);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(CARD_BACKGROUND);

        JButton yesButton = createConfirmButton("Yes", result, dialog, true);
        JButton noButton = createConfirmButton("No", result, dialog, false);

        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);

        panel.add(messageLabel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);

        return result[0];
    }

    private JButton createConfirmButton(String text, boolean[] result, JDialog dialog, boolean setValue) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(setValue ? ACCENT : new Color(107, 114, 128));
        button.setPreferredSize(new Dimension(80, 35));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> {
            if (setValue) result[0] = true;
            dialog.dispose();
        });
        return button;
    }

    private void autoInstallMod() {
        try {
            String jarPath = Installer.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            if (jarPath.startsWith("file:")) {
                jarPath = jarPath.substring(5);
            }
            jarPath = URLDecoder.decode(jarPath, "UTF-8");

            String os = System.getProperty("os.name").toLowerCase();
            String modsFolder;

            if (os.contains("win")) {
                modsFolder = System.getenv("APPDATA") + "\\.minecraft\\mods\\";
            } else if (os.contains("mac")) {
                modsFolder = System.getProperty("user.home") + "/Library/Application Support/minecraft/mods/";
            } else {
                modsFolder = System.getProperty("user.home") + "/.minecraft/mods/";
            }

            File modsDir = new File(modsFolder);
            if (!modsDir.exists()) {
                modsDir.mkdirs();
            }

            File sourceFile = new File(jarPath);
            File targetFile = new File(modsFolder + sourceFile.getName());

            if (targetFile.exists() && !showThemedConfirmDialog()) {
                return;
            }

            Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            showThemedDialog(
                    "Mod installed successfully to:\n" + modsFolder,
                    "Installation Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {
            showThemedDialog(
                    "Failed to install mod:\n" + e.getMessage(),
                    "Installation Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static void openURL(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Could not open browser. Please visit: " + url,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}