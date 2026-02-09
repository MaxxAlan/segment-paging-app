package GUI;

import model.Segments;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SegmentationMemoryGUI extends JFrame {

    private boolean isFullScreen = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SegmentationMemoryGUI().setVisible(true));
    }

    public SegmentationMemoryGUI() {
        setTitle("Segmentation Memory Simulator");
        setSize(1200, 600);
        setResizable(true); // Enabled resizing
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        MainPanel mainPanel = new MainPanel();
        add(mainPanel);

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

    public class MainPanel extends JPanel {

        private JList<String> rowHeader;
        private DefaultTableModel tableModel;
        private final String[] columnNames = {"Content"};
        private JButton createButton;
        private final List<Segments> segments;
        private int nextSID = 1;
        private JTable activeSegmentsTable;

        public MainPanel() {
            segments = new ArrayList<>();
            setBorder(new EmptyBorder(10, 10, 10, 10));
            setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weighty = 1.0;

            JPanel tablePanel = createTablePanel();
            JPanel settingsPanel = createSettingsAndAddSegmentPanel();
            JPanel processesPanel = createSegmentsPanel();

            gbc.gridx = 0; gbc.weightx = 0.3;
            add(tablePanel, gbc);

            gbc.gridx = 1; gbc.weightx = 0.2;
            add(settingsPanel, gbc);

            gbc.gridx = 2; gbc.weightx = 0.5;
            add(processesPanel, gbc);
        }

        private JPanel createTablePanel() {
            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.setBorder(new TitledBorder("Main Memory"));

            tableModel = new DefaultTableModel(0, 1) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

            JTable table = new JTable(tableModel) {
                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                    Component c = super.prepareRenderer(renderer, row, column);
                    Object value = getModel().getValueAt(row, column);
                    c.setBackground(Color.WHITE);

                    if ("OS".equals(value)) {
                        c.setBackground(Color.LIGHT_GRAY);
                    } else if (value != null && value.toString().contains("SID ")) {
                        try {
                            String sidStr = value.toString().split(" ")[1];
                            int sid = Integer.parseInt(sidStr);
                            Segments s = findSegmentsBySID(sid);
                            if (s != null) c.setBackground(s.getColor());
                        } catch (Exception ignored) {}
                    }
                    return c;
                }
            };

            table.setFillsViewportHeight(true);
            table.setRowHeight(20);

            rowHeader = new JList<>();
            rowHeader.setFixedCellWidth(50);
            rowHeader.setFixedCellHeight(table.getRowHeight());
            rowHeader.setCellRenderer(new RowHeaderRenderer(table));

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setRowHeaderView(rowHeader);
            tablePanel.add(scrollPane, BorderLayout.CENTER);

            return tablePanel;
        }

        private void updateTableData(int memorySize, int osSize) {
            Object[][] newData = new Object[memorySize][1];
            for (int i = 0; i < memorySize; i++) newData[i][0] = "Free";
            tableModel.setDataVector(newData, columnNames);

            initializeOSSegments(osSize);
            updateSegmentsTable();
            updateRowHeaders(memorySize);
        }

        private void initializeOSSegments(int osSize) {
            Segments osSegment = new Segments(0, "OS", osSize, Color.LIGHT_GRAY, 0);
            for (int i = 0; i < osSize; i++) {
                updateMemoryTable(i, osSegment);
            }
            segments.add(osSegment);
        }

        private void updateRowHeaders(int memorySize) {
            String[] headers = new String[memorySize];
            for (int i = 0; i < memorySize; i++) headers[i] = "@" + (i + 1);
            rowHeader.setListData(headers);
        }

        private class RowHeaderRenderer extends JLabel implements ListCellRenderer<String> {
            RowHeaderRenderer(JTable table) {
                setOpaque(true);
                setBorder(UIManager.getBorder("TableHeader.cellBorder"));
                setHorizontalAlignment(CENTER);
                setBackground(table.getTableHeader().getBackground());
            }
            @Override
            public Component getListCellRendererComponent(JList<? extends String> l, String v, int i, boolean s, boolean f) {
                setText(v);
                return this;
            }
        }

        private JPanel createSettingsAndAddSegmentPanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JPanel configPanel = new JPanel(new GridBagLayout());
            configPanel.setBorder(new TitledBorder("General Settings"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 4, 4, 4); gbc.fill = GridBagConstraints.HORIZONTAL;

            final JSpinner memSpinner = new JSpinner(new SpinnerNumberModel(64, 1, 256, 1));
            final JSpinner osSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 64, 1));

            gbc.gridx=0; gbc.gridy=0; configPanel.add(new JLabel("Mem Size:"), gbc);
            gbc.gridx=1; configPanel.add(memSpinner, gbc);
            gbc.gridx=0; gbc.gridy=1; configPanel.add(new JLabel("OS Size:"), gbc);
            gbc.gridx=1; configPanel.add(osSpinner, gbc);

            JButton genBtn = new JButton("Generate");
            genBtn.addActionListener(e -> {
                segments.clear(); nextSID = 1;
                updateTableData((Integer) memSpinner.getValue(), (Integer) osSpinner.getValue());
                createButton.setEnabled(true);
            });
            gbc.gridx=0; gbc.gridy=2; gbc.gridwidth=2; configPanel.add(genBtn, gbc);
            panel.add(configPanel);

            JPanel addPanel = new JPanel(new GridBagLayout());
            addPanel.setBorder(new TitledBorder("Add Segment"));
            final JTextField nameField = new JTextField(10);
            final JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 64, 1));
            final JButton colorBtn = new JButton(" ");
            colorBtn.setBackground(new Color(255, 130, 0));
            colorBtn.setOpaque(true);
            colorBtn.setBorderPainted(false);
            colorBtn.addActionListener(e -> {
                Color c = JColorChooser.showDialog(null, "Choose Color", colorBtn.getBackground());
                if(c != null) colorBtn.setBackground(c);
            });

            gbc.gridwidth=1; gbc.gridy=0; gbc.gridx=0; addPanel.add(new JLabel("Name:"), gbc);
            gbc.gridx=1; addPanel.add(nameField, gbc);
            gbc.gridy=1; gbc.gridx=0; addPanel.add(new JLabel("Size:"), gbc);
            gbc.gridx=1; addPanel.add(sizeSpinner, gbc);
            gbc.gridy=2; gbc.gridx=0; addPanel.add(new JLabel("Color:"), gbc);
            gbc.gridx=1; addPanel.add(colorBtn, gbc);

            createButton = new JButton("Create Segment");
            createButton.setEnabled(false);
            createButton.addActionListener(e -> {
                int size = (Integer) sizeSpinner.getValue();
                String name = nameField.getText().trim();
                if (name.isEmpty()) return;

                int start = findFirstFitAddress(size);
                if (start == -1) {
                    JOptionPane.showMessageDialog(null, "No contiguous space found!");
                    return;
                }

                Segments s = new Segments(nextSID++, name, size, colorBtn.getBackground(), start);
                for (int i = 0; i < size; i++) updateMemoryTable(start + i, s);
                segments.add(s);
                updateSegmentsTable();
            });
            gbc.gridy=3; gbc.gridx=0; gbc.gridwidth=2; addPanel.add(createButton, gbc);
            panel.add(addPanel);

            JButton fullScreenBtn = new JButton("Full Screen (F11)");
            fullScreenBtn.addActionListener(e -> toggleFullScreen());
            panel.add(fullScreenBtn);

            return panel;
        }

        private int findFirstFitAddress(int size) {
            int rowCount = tableModel.getRowCount();
            int contiguous = 0;
            int start = -1;
            for (int i = 0; i < rowCount; i++) {
                if ("Free".equals(tableModel.getValueAt(i, 0))) {
                    if (contiguous == 0) start = i;
                    contiguous++;
                    if (contiguous == size) return start;
                } else {
                    contiguous = 0;
                    start = -1;
                }
            }
            return -1;
        }

        private void updateMemoryTable(int address, Segments s) {
            String val = (s.getSID() == 0) ? "OS" : "SID " + s.getSID() + " (" + s.getName() + ")";
            tableModel.setValueAt(val, address, 0);
        }

        private Segments findSegmentsBySID(int sid) {
            for (Segments s : segments) if (s.getSID() == sid) return s;
            return null;
        }

        private JPanel createSegmentsPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(new TitledBorder("Segments Table"));

            String[] cols = {"Addr", "Base", "SID", "Name", "Limit", "Color"};
            activeSegmentsTable = new JTable(new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            });

            activeSegmentsTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                    Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                    if(v instanceof Color) {
                        comp.setBackground((Color)v);
                        comp.setForeground((Color)v);
                    }
                    return comp;
                }
            });

            panel.add(new JScrollPane(activeSegmentsTable), BorderLayout.CENTER);

            JButton back = new JButton("Back to Menu");
            back.addActionListener(e -> {
                new StartForm().setVisible(true);
                dispose();
            });
            panel.add(back, BorderLayout.SOUTH);

            return panel;
        }

        private void updateSegmentsTable() {
            DefaultTableModel model = (DefaultTableModel) activeSegmentsTable.getModel();
            model.setRowCount(0);
            for (Segments s : segments) {
                if (s.getSID() == 0) continue;
                model.addRow(new Object[]{s.getBase_address() + 1, s.getBase_address() + 1, s.getSID(), s.getName(), s.getLimit(), s.getColor()});
            }
        }
    }
}
