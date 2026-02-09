package GUI;

import model.Page;
import model.Process;

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

public class PagingMemoryGUI extends JFrame {

    private boolean isFullScreen = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PagingMemoryGUI().setVisible(true));
    }

    public PagingMemoryGUI() {
        setTitle("Paging Memory Simulator");
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
        private final String[] columnNames = {"Memory Content"};
        private JButton createButton;
        private final List<Process> processes;
        private int nextPID = 1;
        private JTable activeProcessesTable;
        private boolean[] freeFrames;

        public MainPanel() {
            processes = new ArrayList<>();
            setBorder(new EmptyBorder(10, 10, 10, 10));
            setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weighty = 1.0;

            JPanel tablePanel = createTablePanel();
            JPanel settingsPanel = createSettingsAndAddProcessPanel();
            JPanel processesPanel = createProcessesPanel();

            gbc.gridx = 0; gbc.weightx = 0.3;
            add(tablePanel, gbc);

            gbc.gridx = 1; gbc.weightx = 0.2;
            add(settingsPanel, gbc);

            gbc.gridx = 2; gbc.weightx = 0.5;
            add(processesPanel, gbc);
        }

        private JPanel createTablePanel() {
            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.setBorder(new TitledBorder("Physical Memory"));

            tableModel = new DefaultTableModel(0, 1) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

            JTable table = new JTable(tableModel) {
                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                    Component c = super.prepareRenderer(renderer, row, column);
                    Object value = getValueAt(row, column);
                    c.setBackground(Color.WHITE);

                    if ("OS".equals(value)) {
                        c.setBackground(Color.LIGHT_GRAY);
                    } else if (value != null && value.toString().startsWith("PID ")) {
                        try {
                            String pidStr = value.toString().split(" ")[1];
                            int pid = Integer.parseInt(pidStr);
                            Process p = findProcessByPID(pid);
                            if (p != null) c.setBackground(p.getColor());
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
            tableModel.setRowCount(0);
            freeFrames = new boolean[memorySize];
            for (int i = 0; i < memorySize; i++) {
                tableModel.addRow(new Object[]{"Free"});
                freeFrames[i] = true;
            }
            initializeOSProcess(osSize);
            updateRowHeaders(memorySize);
        }

        private void initializeOSProcess(int osSize) {
            Process osProcess = new Process(0, "OS", osSize, Color.LIGHT_GRAY);
            for (int i = 0; i < osSize; i++) {
                osProcess.addPage(new Page(i, i, i));
                tableModel.setValueAt("OS", i, 0);
                freeFrames[i] = false;
            }
            processes.add(osProcess);
        }

        private void updateRowHeaders(int memorySize) {
            String[] headers = new String[memorySize];
            for (int i = 0; i < memorySize; i++) headers[i] = "F" + i;
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

        private JPanel createSettingsAndAddProcessPanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JPanel genPanel = new JPanel(new GridBagLayout());
            genPanel.setBorder(new TitledBorder("Configuration"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 4, 4, 4); gbc.fill = GridBagConstraints.HORIZONTAL;

            final JSpinner memSpinner = new JSpinner(new SpinnerNumberModel(64, 1, 512, 1));
            final JSpinner osSpinner = new JSpinner(new SpinnerNumberModel(4, 0, 64, 1));
            
            gbc.gridx=0; gbc.gridy=0; genPanel.add(new JLabel("Mem Size:"), gbc);
            gbc.gridx=1; genPanel.add(memSpinner, gbc);
            gbc.gridx=0; gbc.gridy=1; genPanel.add(new JLabel("OS Size:"), gbc);
            gbc.gridx=1; genPanel.add(osSpinner, gbc);

            JButton genBtn = new JButton("Initialize Memory");
            genBtn.addActionListener(e -> {
                processes.clear(); nextPID = 1;
                updateTableData((Integer)memSpinner.getValue(), (Integer)osSpinner.getValue());
                updateProcessesTable();
                createButton.setEnabled(true);
            });
            gbc.gridx=0; gbc.gridy=2; gbc.gridwidth=2; genPanel.add(genBtn, gbc);
            panel.add(genPanel);

            JPanel addPanel = new JPanel(new GridBagLayout());
            addPanel.setBorder(new TitledBorder("Add Process"));
            final JTextField nameFld = new JTextField("P" + nextPID);
            final JSpinner sizeSpn = new JSpinner(new SpinnerNumberModel(4, 1, 128, 1));
            final JButton colorBtn = new JButton(" ");
            colorBtn.setBackground(new Color(100, 150, 255));
            colorBtn.setOpaque(true);
            colorBtn.setBorderPainted(false);
            colorBtn.addActionListener(e -> {
                Color c = JColorChooser.showDialog(null, "Select Color", colorBtn.getBackground());
                if(c != null) colorBtn.setBackground(c);
            });

            gbc.gridwidth=1; gbc.gridy=0; gbc.gridx=0; addPanel.add(new JLabel("Name:"), gbc);
            gbc.gridx=1; addPanel.add(nameFld, gbc);
            gbc.gridy=1; gbc.gridx=0; addPanel.add(new JLabel("Size:"), gbc);
            gbc.gridx=1; addPanel.add(sizeSpn, gbc);
            gbc.gridy=2; gbc.gridx=0; addPanel.add(new JLabel("Color:"), gbc);
            gbc.gridx=1; addPanel.add(colorBtn, gbc);

            createButton = new JButton("Allocate Process");
            createButton.setEnabled(false);
            createButton.addActionListener(e -> {
                int size = (Integer)sizeSpn.getValue();
                String name = nameFld.getText().trim();
                if(name.isEmpty()) return;

                List<Integer> free = getFreeFrameIndices();
                if(size > free.size()) {
                    JOptionPane.showMessageDialog(null, "Insufficient Memory!");
                    return;
                }

                Process p = new Process(nextPID++, name, size, colorBtn.getBackground());
                Random r = new Random();
                for(int i=0; i<size; i++) {
                    int idx = r.nextInt(free.size());
                    int frame = free.remove(idx);
                    p.addPage(new Page(frame, frame, i));
                    tableModel.setValueAt("PID " + p.getPID() + " - P" + i + " (" + name + ")", frame, 0);
                    freeFrames[frame] = false;
                }
                processes.add(p);
                updateProcessesTable();
                nameFld.setText("P" + nextPID);
            });
            gbc.gridy=3; gbc.gridx=0; gbc.gridwidth=2; addPanel.add(createButton, gbc);
            panel.add(addPanel);

            JButton fullScreenBtn = new JButton("Full Screen (F11)");
            fullScreenBtn.addActionListener(e -> toggleFullScreen());
            panel.add(fullScreenBtn);

            return panel;
        }

        private List<Integer> getFreeFrameIndices() {
            List<Integer> list = new ArrayList<>();
            for(int i=0; i<freeFrames.length; i++) if(freeFrames[i]) list.add(i);
            return list;
        }

        private Process findProcessByPID(int pid) {
            for(Process p : processes) if(p.getPID() == pid) return p;
            return null;
        }

        private JPanel createProcessesPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(new TitledBorder("Process List / Page Table"));
            
            String[] cols = {"Addr", "Frame", "PID", "Page", "Name", "Size", "Color"};
            activeProcessesTable = new JTable(new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            });
            
            activeProcessesTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
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

            panel.add(new JScrollPane(activeProcessesTable), BorderLayout.CENTER);
            
            JButton back = new JButton("Back to Menu");
            back.addActionListener(e -> {
                new StartForm().setVisible(true);
                dispose();
            });
            panel.add(back, BorderLayout.SOUTH);

            return panel;
        }

        private void updateProcessesTable() {
            DefaultTableModel model = (DefaultTableModel) activeProcessesTable.getModel();
            model.setRowCount(0);
            for (Process p : processes) {
                if(p.getPID() == 0) continue;
                for (Page pg : p.getPages()) {
                    model.addRow(new Object[]{ pg.getAddress(), pg.getFrame(), p.getPID(), pg.getPageNr(), p.getName(), p.getSize(), p.getColor() });
                }
            }
        }
    }
}
