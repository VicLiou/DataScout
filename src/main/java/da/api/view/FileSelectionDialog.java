package da.api.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileSelectionDialog extends JDialog {
    private String selectedFilePath;
    private JList<String> recentFileList;

    public FileSelectionDialog(List<String> recentFiles) {
        setTitle("選擇資料來源");
        setModal(true);
        setSize(500, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Handle closing manually
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        headerPanel.add(new JLabel("請選擇或開啟現有的 Excel 資料檔案 (.xlsx):"));
        add(headerPanel, BorderLayout.NORTH);

        // Recent Files List
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("最近開啟的項目"));

        recentFileList = new JList<>(recentFiles.toArray(new String[0]));
        recentFileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recentFileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selected = recentFileList.getSelectedValue();
                    if (selected != null) {
                        confirmSelection(selected);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(recentFileList);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton browseButton = new JButton("瀏覽檔案...");
        browseButton.addActionListener(e -> browseFile());

        JButton okButton = new JButton("確定");
        okButton.addActionListener(e -> {
            String selected = recentFileList.getSelectedValue();
            if (selected != null) {
                confirmSelection(selected);
            } else {
                JOptionPane.showMessageDialog(this, "請選擇一個檔案或點擊瀏覽", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton cancelButton = new JButton("離開");
        cancelButton.addActionListener(e -> {
            System.exit(0);
        });

        buttonPanel.add(browseButton);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void browseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            confirmSelection(selectedFile.getAbsolutePath());
        }
    }

    private void confirmSelection(String path) {
        File file = new File(path);
        if (!file.exists()) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "檔案不存在: " + path + "\n是否要建立新檔案?",
                    "檔案不存在",
                    JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }
        this.selectedFilePath = path;
        dispose();
    }

    public String getSelectedFilePath() {
        return selectedFilePath;
    }
}
