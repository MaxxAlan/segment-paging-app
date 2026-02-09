import GUI.PagingMemoryGUI;
import GUI.SegmentationMemoryGUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String[] options = {"Paging Simulator", "Segmentation Simulator"};
            int choice = JOptionPane.showOptionDialog(
                    null,
                    "Select Memory Management Technique:",
                    "Paging & Segmentation Simulator",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice == 0) {
                new PagingMemoryGUI().setVisible(true);
            } else if (choice == 1) {
                new SegmentationMemoryGUI().setVisible(true);
            }
        });
    }
}
