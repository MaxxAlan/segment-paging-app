package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class StartForm extends JFrame {

    private boolean isFullScreen = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StartForm().setVisible(true));
    }

    public StartForm() {
        setTitle("Memory Management Simulator");
        setSize(600, 300);
        setResizable(true); // Enabled resizing
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        MenuPane menuPane = new MenuPane();
        add(menuPane);
        
        // Add Full Screen Toggle Shortcut (F11)
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F11) {
                    toggleFullScreen();
                }
            }
        });
        setFocusable(true);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void toggleFullScreen() {
        dispose();
        if (!isFullScreen) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setUndecorated(true);
            isFullScreen = true;
        } else {
            setExtendedState(JFrame.NORMAL);
            setUndecorated(false);
            isFullScreen = false;
        }
        setVisible(true);
    }

    public class MenuPane extends JPanel {

        public MenuPane() {
            setBorder(new EmptyBorder(20, 20, 20, 20));
            setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.insets = new Insets(10, 10, 10, 10);

            JLabel title = new JLabel("<html><div style='text-align: center;'><h1><strong>Paging & Segmentation Simulator</strong></h1><hr></div></html>");
            add(title, gbc);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
            JButton pagingButton = new JButton("Paging Memory");
            JButton segmentationButton = new JButton("Segmentation Memory");
            JButton fullScreenButton = new JButton("Toggle Full Screen (F11)");

            pagingButton.setPreferredSize(new Dimension(180, 40));
            segmentationButton.setPreferredSize(new Dimension(180, 40));

            pagingButton.addActionListener(e -> {
                new PagingMemoryGUI().setVisible(true);
                StartForm.this.dispose();
            });

            segmentationButton.addActionListener(e -> {
                new SegmentationMemoryGUI().setVisible(true);
                StartForm.this.dispose();
            });

            fullScreenButton.addActionListener(e -> toggleFullScreen());

            buttons.add(pagingButton);
            buttons.add(segmentationButton);
            buttons.add(fullScreenButton);

            add(buttons, gbc);
        }
    }
}
