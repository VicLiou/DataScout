package da.api.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;

/**
 * 現代化檔案選擇對話框
 * 專為 DataScout 設計，提供一致的 UI 體驗
 */
public class ModernFileChooser extends JDialog {

    public static final int MODE_OPEN = 0;
    public static final int MODE_SAVE = 1;

    private static final String FONT_NAME = "微軟正黑體";
    private static final Color BG_COLOR = new Color(248, 249, 250);
    private static final Color PRIMARY_BTN_COLOR = new Color(37, 99, 235);
    private static final Color BORDER_COLOR = new Color(209, 213, 219);

    private int mode;
    private File currentDirectory;
    private File selectedFile;
    private boolean approved = false;

    private JTextField pathField;
    private JTextField fileNameField;
    private JList<File> fileList;
    private DefaultListModel<File> listModel;
    private JButton confirmButton;
    private transient FileSystemView fileSystemView;

    public ModernFileChooser(Window parent, String title, int mode) {
        super(resolveDialogOwner(parent), title, java.awt.Dialog.ModalityType.APPLICATION_MODAL);

        this.mode = mode;
        this.fileSystemView = FileSystemView.getFileSystemView();
        this.currentDirectory = fileSystemView.getHomeDirectory();

        setSize(750, 520);
        setLocationRelativeTo(parent);
        setResizable(true);

        // 設定應用程式圖示
        try {
            java.net.URL iconUrl = getClass().getResource("/app_icon.png");
            if (iconUrl != null) {
                setIconImage(new javax.swing.ImageIcon(iconUrl).getImage());
            }
        } catch (Exception e) {
            // 忽略
        }

        initComponents();
        refreshFileList();
    }

    private static java.awt.Frame resolveDialogOwner(Window parent) {
        if (parent instanceof java.awt.Frame) {
            return (java.awt.Frame) parent;
        }
        if (parent == null) {
            return null;
        }
        return (java.awt.Frame) SwingUtilities.getWindowAncestor(parent);
    }

    private void initComponents() {
        JPanel mainContainer = new JPanel(new BorderLayout(0, 15));
        mainContainer.setBackground(BG_COLOR);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ============ 頂部導航區 ============
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBackground(BG_COLOR);

        // 導航按鈕群組
        JPanel navButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        navButtons.setBackground(BG_COLOR);

        // 上一頁按鈕
        JButton upButton = createNavButton("↑ 上一層", "回上一層目錄");
        upButton.setPreferredSize(new Dimension(100, 32)); // 加寬以容納文字
        upButton.addActionListener(e -> navigateUp());

        JButton homeButton = createNavButton("桌面", "回桌面");
        homeButton.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        homeButton.addActionListener(e -> navigateTo(fileSystemView.getHomeDirectory()));

        JButton pcButton = createNavButton("本機", "顯示磁碟機");
        pcButton.setFont(new Font(FONT_NAME, Font.BOLD, 12));
        pcButton.addActionListener(e -> {
            currentDirectory = null;
            refreshFileList();
        });

        navButtons.add(upButton);
        navButtons.add(homeButton);
        navButtons.add(pcButton);

        // 路徑顯示
        pathField = new JTextField();
        pathField.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        pathField.setEditable(true); // 允許使用者手動輸入路徑
        pathField.setBackground(Color.WHITE);
        pathField.setPreferredSize(new Dimension(300, 32));
        pathField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));

        // 處理手動輸入路徑
        pathField.addActionListener(e -> {
            String inputPath = pathField.getText().trim();
            if (!inputPath.isEmpty()) {
                File target = new File(inputPath);
                if (target.exists()) {
                    if (target.isDirectory()) {
                        navigateTo(target);
                    } else {
                        // 如果是檔案，則進入該目錄並選中該檔案
                        File parent = target.getParentFile();
                        if (parent != null && parent.exists()) {
                            navigateTo(parent);
                            setSelectedFile(target);
                            // 滾動到選中項目
                            fileList.setSelectedValue(target, true);
                        }
                    }
                }
            }
        });

        topPanel.add(navButtons, BorderLayout.WEST);
        topPanel.add(pathField, BorderLayout.CENTER);
        mainContainer.add(topPanel, BorderLayout.NORTH);

        // ============ 檔案列表區 ============
        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileList.setCellRenderer(new FileListRenderer());
        fileList.setLayoutOrientation(JList.VERTICAL_WRAP);
        fileList.setVisibleRowCount(-1);
        fileList.setBackground(Color.WHITE);

        // 設定固定儲存格大小，讓排列更整齊
        fileList.setFixedCellHeight(28);
        fileList.setFixedCellWidth(220); // 這裡可以根據視窗大小動態調整，目前設固定寬度

        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    File file = fileList.getSelectedValue();
                    if (file != null) {
                        if (file.isDirectory()) {
                            navigateTo(file);
                        } else {
                            setSelectedFile(file);
                            confirmSelection();
                        }
                    }
                } else {
                    File file = fileList.getSelectedValue();
                    if (file != null && !file.isDirectory()) {
                        setSelectedFile(file);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(fileList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);

        mainContainer.add(scrollPane, BorderLayout.CENTER);

        // ============ 底部操作區 ============
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(BG_COLOR);

        JPanel inputsPanel = new JPanel(new GridBagLayout());
        inputsPanel.setBackground(BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 8); // 元件間距

        // 檔名標籤
        JLabel nameLabel = new JLabel("檔案名稱(N):");
        nameLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        inputsPanel.add(nameLabel, gbc);

        // 檔名輸入框
        fileNameField = new JTextField();
        fileNameField.setFont(new Font(FONT_NAME, Font.PLAIN, 13));
        fileNameField.setPreferredSize(new Dimension(200, 36)); // 增加高度
        fileNameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));

        fileNameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    confirmSelection();
                }
            }
        });

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        inputsPanel.add(fileNameField, gbc);

        // 下拉選單風格的檔案類型顯示
        JLabel typeLabel = new JLabel(" Excel 活頁簿 (*.xlsx) ");
        typeLabel.setFont(new Font(FONT_NAME, Font.PLAIN, 12));
        typeLabel.setForeground(new Color(55, 65, 81));
        typeLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        typeLabel.setOpaque(true);
        typeLabel.setBackground(Color.WHITE);

        gbc.gridx = 2;
        gbc.weightx = 0;
        inputsPanel.add(typeLabel, gbc);

        bottomPanel.add(inputsPanel, BorderLayout.CENTER);

        // 按鈕區
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BG_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        confirmButton = new JButton(mode == MODE_SAVE ? "存檔" : "開啟");
        confirmButton.setPreferredSize(new Dimension(90, 36));
        styleButton(confirmButton, PRIMARY_BTN_COLOR, Color.WHITE);
        confirmButton.addActionListener(e -> confirmSelection());

        JButton cancelButton = new JButton("取消");
        cancelButton.setPreferredSize(new Dimension(90, 36));
        styleButton(cancelButton, Color.WHITE, new Color(55, 65, 81));
        cancelButton.setBorder(BorderFactory.createLineBorder(BORDER_COLOR)); // 取消按鈕加邊框
        cancelButton.addActionListener(e -> {
            approved = false;
            dispose();
        });

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainContainer.add(bottomPanel, BorderLayout.SOUTH);
        add(mainContainer);
    }

    private void navigateTo(File dir) {
        currentDirectory = dir;
        refreshFileList();
    }

    private void navigateUp() {
        if (currentDirectory == null)
            return;

        File parent = fileSystemView.getParentDirectory(currentDirectory);
        if (parent != null) {
            navigateTo(parent);
        } else {
            currentDirectory = null; // 根目錄
            refreshFileList();
        }
    }

    private void refreshFileList() {
        listModel.clear();
        File[] files;

        if (currentDirectory == null) {
            files = File.listRoots();
            pathField.setText("本機");
        } else {
            files = fileSystemView.getFiles(currentDirectory, true);
            pathField.setText(currentDirectory.getAbsolutePath());
        }

        Vector<File> fileVector = new Vector<>();
        if (files != null) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    boolean d1 = f1.isDirectory();
                    boolean d2 = f2.isDirectory();
                    if (d1 && !d2)
                        return -1;
                    if (!d1 && d2)
                        return 1;
                    return f1.getName().compareToIgnoreCase(f2.getName());
                }
            });

            for (File file : files) {
                if (file.isDirectory()) {
                    fileVector.add(file);
                } else {
                    String name = file.getName().toLowerCase();
                    if (name.endsWith(".xlsx")) {
                        fileVector.add(file);
                    }
                }
            }
        }

        for (File f : fileVector) {
            listModel.addElement(f);
        }
    }

    private void setSelectedFile(File file) {
        this.selectedFile = file;
        if (file != null) {
            fileNameField.setText(file.getName());
        }
    }

    private void confirmSelection() {
        String inputName = fileNameField.getText().trim();
        if (inputName.isEmpty())
            return;

        if (!inputName.toLowerCase().endsWith(".xlsx")) {
            inputName += ".xlsx";
        }

        if (currentDirectory != null) {
            selectedFile = new File(currentDirectory, inputName);
        } else {
            selectedFile = new File(fileSystemView.getHomeDirectory(), inputName);
        }

        approved = true;
        dispose();
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    public boolean isApproved() {
        return approved;
    }

    // ============ 客製化 Renderer ============
    class FileListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof File) {
                File file = (File) value;
                setText(" " + fileSystemView.getSystemDisplayName(file)); // 增加文字間距
                setIcon(fileSystemView.getSystemIcon(file));
                setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5)); // 增加上下間距

                if (isSelected) {
                    setBackground(new Color(219, 234, 254));
                    setForeground(new Color(30, 64, 175));
                } else {
                    setBackground(Color.WHITE);
                    setForeground(new Color(31, 41, 55));
                }
            }
            return this;
        }

        // 解決選取背景繪製問題
        @Override
        public boolean isOpaque() {
            return true;
        }
    }

    private JButton createNavButton(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setToolTipText(tooltip);
        btn.setMargin(new Insets(2, 12, 2, 12));
        btn.setPreferredSize(new Dimension(80, 32)); // 固定高度，寬度由字數決定
        styleButton(btn, Color.WHITE, new Color(55, 65, 81));
        return btn;
    }

    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setFont(new Font(FONT_NAME, Font.BOLD, 13));
        button.setForeground(fgColor);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 扁平化樣式(除取消按鈕外預設無邊框)
        if (bgColor == PRIMARY_BTN_COLOR) {
            button.setBorderPainted(false);
        } else {
            // 導航按鈕使用柔和邊框
            button.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
        }

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                if (bgColor == Color.WHITE) {
                    button.setBackground(new Color(243, 244, 246));
                } else if (bgColor == PRIMARY_BTN_COLOR) {
                    button.setBackground(new Color(29, 78, 216));
                }
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
    }
}
