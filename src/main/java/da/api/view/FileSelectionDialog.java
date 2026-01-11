package da.api.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileSelectionDialog extends JDialog {
    private String selectedFilePath;
    private JList<String> recentFileList;
    private Consumer<String> onRemoveCallback;
    private Runnable onCreateNewCallback;
    private boolean isCreateNewRequested = false;

    public FileSelectionDialog(List<String> recentFiles) {
        this(recentFiles, null, null);
    }

    public FileSelectionDialog(List<String> recentFiles, Consumer<String> onRemove, Runnable onCreateNew) {
        this.onRemoveCallback = onRemove;
        this.onCreateNewCallback = onCreateNew;

        setTitle("選擇資料來源");
        setModal(true);
        setSize(650, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // 設定圖示
        try {
            java.net.URL iconUrl = getClass().getResource("/app_icon.png");
            if (iconUrl != null) {
                setIconImage(new javax.swing.ImageIcon(iconUrl).getImage());
            }
        } catch (Exception e) {
            // 忽略
        }

        initComponents(recentFiles);
    }

    private void initComponents(List<String> recentFiles) {
        // 主容器
        JPanel mainContainer = new JPanel(new BorderLayout(0, 15));
        mainContainer.setBackground(new Color(248, 249, 250));
        mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ============ 標題區域 ============
        JPanel headerPanel = new JPanel(new BorderLayout(0, 8));
        headerPanel.setBackground(new Color(224, 242, 254));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(125, 211, 252), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel titleLabel = new JLabel("DataScout - 資料來源選擇");
        titleLabel.setFont(new Font("微軟正黑體", Font.BOLD, 18));
        titleLabel.setForeground(new Color(3, 105, 161));

        JLabel descLabel = new JLabel("請選擇或開啟現有的 Excel 資料檔案 (.xlsx)");
        descLabel.setFont(new Font("微軟正黑體", Font.PLAIN, 13));
        descLabel.setForeground(new Color(7, 89, 133));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(descLabel, BorderLayout.CENTER);

        // ============ 最近檔案列表區域 ============
        JPanel listSection = new JPanel(new BorderLayout(0, 10));
        listSection.setBackground(Color.WHITE);
        listSection.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        // 列表標題
        JPanel listHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        listHeader.setBackground(new Color(249, 250, 251));
        JLabel listTitle = new JLabel("最近開啟的項目");
        listTitle.setFont(new Font("微軟正黑體", Font.BOLD, 14));
        listTitle.setForeground(new Color(31, 41, 55));

        JLabel listHint = new JLabel("（雙擊開啟 / 右鍵移除）");
        listHint.setFont(new Font("微軟正黑體", Font.PLAIN, 12));
        listHint.setForeground(new Color(107, 114, 128));

        listHeader.add(listTitle);
        listHeader.add(listHint);
        listSection.add(listHeader, BorderLayout.NORTH);

        // 列表內容
        javax.swing.DefaultListModel<String> listModel = new javax.swing.DefaultListModel<>();
        for (String file : recentFiles) {
            listModel.addElement(file);
        }

        recentFileList = new JList<>(listModel);
        recentFileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recentFileList.setFont(new Font("微軟正黑體", Font.PLAIN, 13));
        recentFileList.setBackground(Color.WHITE);
        recentFileList.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // 自訂 cell renderer 增加行高
        recentFileList.setFixedCellHeight(32);

        recentFileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    String selected = recentFileList.getSelectedValue();
                    if (selected != null) {
                        confirmSelection(selected);
                    }
                }
                if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                    int index = recentFileList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        recentFileList.setSelectedIndex(index);
                        showContextMenu(e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                    int index = recentFileList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        recentFileList.setSelectedIndex(index);
                        showContextMenu(e.getX(), e.getY());
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(recentFileList);
        scrollPane.setBorder(null);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        listSection.add(scrollPane, BorderLayout.CENTER);

        // 移除按鈕
        JPanel removePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        removePanel.setBackground(new Color(249, 250, 251));

        JButton removeButton = new JButton("移除所選項目");
        removeButton.setFont(new Font("微軟正黑體", Font.PLAIN, 13));
        removeButton.setForeground(new Color(220, 38, 38));
        removeButton.setBackground(new Color(254, 226, 226));
        removeButton.setPreferredSize(new Dimension(130, 35));
        removeButton.setFocusPainted(false);
        removeButton.setBorderPainted(false);
        removeButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        removeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                removeButton.setBackground(new Color(254, 202, 202));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                removeButton.setBackground(new Color(254, 226, 226));
            }
        });

        removeButton.addActionListener(e -> removeSelectedItem());
        removePanel.add(removeButton);

        listSection.add(removePanel, BorderLayout.SOUTH);

        // ============ 按鈕區域 ============
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 15));
        buttonPanel.setBackground(new Color(248, 249, 250));

        // 建立空白檔案按鈕
        if (onCreateNewCallback != null) {
            JButton createNewButton = createStyledButton("建立空白檔案", new Color(16, 185, 129), Color.WHITE);
            createNewButton.addActionListener(e -> {
                isCreateNewRequested = true;
                dispose();
            });
            buttonPanel.add(createNewButton);
        }

        // 瀏覽檔案按鈕
        JButton browseButton = createStyledButton("瀏覽檔案...", new Color(249, 115, 22), Color.WHITE);
        browseButton.addActionListener(e -> browseFile());
        buttonPanel.add(browseButton);

        // 確定按鈕
        JButton okButton = createStyledButton("確定", new Color(37, 99, 235), Color.WHITE);
        okButton.addActionListener(e -> {
            String selected = recentFileList.getSelectedValue();
            if (selected != null) {
                confirmSelection(selected);
            } else {
                JOptionPane.showMessageDialog(this, "請選擇一個檔案或點擊瀏覽", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });
        buttonPanel.add(okButton);

        // 取消按鈕
        JButton cancelButton = createStyledButton("取消", new Color(229, 231, 235), new Color(55, 65, 81));
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        // 組合面板
        mainContainer.add(headerPanel, BorderLayout.NORTH);
        mainContainer.add(listSection, BorderLayout.CENTER);
        mainContainer.add(buttonPanel, BorderLayout.SOUTH);

        add(mainContainer);
    }

    /**
     * 創建美化按鈕
     */
    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setFont(new Font("微軟正黑體", Font.BOLD, 14));
        button.setForeground(fg);
        button.setBackground(bg);
        button.setPreferredSize(new Dimension(130, 42));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Hover 效果
        Color originalBg = bg;
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (originalBg.equals(new Color(37, 99, 235))) {
                    button.setBackground(new Color(29, 78, 216));
                } else if (originalBg.equals(new Color(16, 185, 129))) {
                    button.setBackground(new Color(5, 150, 105));
                } else if (originalBg.equals(new Color(249, 115, 22))) {
                    button.setBackground(new Color(234, 88, 12));
                } else {
                    button.setBackground(new Color(209, 213, 219));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalBg);
            }
        });

        return button;
    }

    private void showContextMenu(int x, int y) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem removeItem = new JMenuItem("從清單中移除");
        removeItem.setFont(new Font("微軟正黑體", Font.PLAIN, 13));
        removeItem.addActionListener(e -> removeSelectedItem());
        popupMenu.add(removeItem);
        popupMenu.show(recentFileList, x, y);
    }

    private void removeSelectedItem() {
        String selected = recentFileList.getSelectedValue();
        if (selected != null) {
            // 從 UI 移除
            ((javax.swing.DefaultListModel<String>) recentFileList.getModel()).removeElement(selected);
            // 回呼移除設定
            if (onRemoveCallback != null) {
                onRemoveCallback.accept(selected);
            }
        }
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
        this.selectedFilePath = path;
        dispose();
    }

    public String getSelectedFilePath() {
        return selectedFilePath;
    }

    public boolean isCreateNewRequested() {
        return isCreateNewRequested;
    }
}
