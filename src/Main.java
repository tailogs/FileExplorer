import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Stack;

public class Main {

    private JFrame frame;
    private JTree fileTree;
    private JTable fileTable;
    private JButton backButton;
    private JTextField pathField;

    private File currentDirectory;
    private Stack<File> pathHistory = new Stack<>();

    public Main() {
        // Установка LookAndFeel для темной темы
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame = new JFrame("File Explorer");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(Color.DARK_GRAY); // Цвет фона основного контейнера

        // Загрузка иконки из ресурсов внутри JAR-файла
        ClassLoader classLoader = Main.class.getClassLoader();
        URL iconUrl = classLoader.getResource("resources/FileExplorer.png"); // Правильный путь к ресурсу в JAR

        try (InputStream inputStream = iconUrl.openStream()) {
            if (inputStream != null) {
                ImageIcon icon = new ImageIcon(ImageIO.read(inputStream));
                frame.setIconImage(icon.getImage());
            } else {
                System.err.println("Не удалось загрузить иконку FileExplorer.png");
            }
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке иконки: " + e.getMessage());
        }

        // Создаем панель для дерева и таблицы
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.DARK_GRAY); // Цвет фона панели

        // Панель для кнопки "Назад" и текстовых полей пути
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.DARK_GRAY); // Цвет фона панели

        backButton = new JButton("Назад");
        backButton.setBackground(new Color(60, 60, 60)); // Цвет кнопки (темно-серый)
        backButton.setForeground(Color.WHITE); // Цвет текста кнопки
        backButton.setFocusPainted(false); // Убираем рамку при фокусе
        backButton.setBorderPainted(false); // Убираем отрисовку границы
        backButton.setOpaque(true); // Делаем кнопку непрозрачной
        pathField = new JTextField();
        pathField.setBackground(new Color(60, 60, 60)); // Цвет фона поля (темно-серый)
        pathField.setForeground(Color.WHITE); // Цвет текста поля
        pathField.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Шрифт текстового поля
        pathField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Устанавливаем отступы

        // Стилизация кнопки "Назад" при наведении
        backButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                backButton.setBackground(new Color(80, 80, 80));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                backButton.setBackground(new Color(60, 60, 60));
            }
        });

        // Слушатель для текстового поля пути
        pathField.addActionListener(e -> openDirectory(pathField.getText()));

        topPanel.add(backButton, BorderLayout.WEST);
        topPanel.add(pathField, BorderLayout.CENTER);

        // Создаем корневой узел для дерева файловой системы
        File[] roots = File.listRoots();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Компьютер");
        for (File file : roots) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(file);
            root.add(node);
        }
        DefaultTreeModel treeModel = new DefaultTreeModel(root);
        fileTree = new JTree(treeModel);
        fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        fileTree.setRootVisible(false); // Чтобы не показывать корневой узел "Компьютер"
        fileTree.setBackground(Color.DARK_GRAY); // Цвет фона дерева
        fileTree.setForeground(Color.WHITE); // Цвет текста в дереве
        fileTree.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Шрифт дерева

        // Добавляем обработчик для открытия каталога при двойном клике
        fileTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Двойной клик
                    TreePath path = fileTree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        Object nodeInfo = node.getUserObject();
                        if (nodeInfo instanceof File) {
                            File selectedFile = (File) nodeInfo;
                            if (selectedFile.isDirectory()) {
                                showFiles(selectedFile); // Открыть содержимое каталога
                            }
                        }
                    }
                }
            }
        });

        // Создаем таблицу для отображения содержимого папки
        String[] columnNames = {"Имя файла", "Тип", "Размер", "Дата изменения"};
        Object[][] data = {};
        fileTable = new JTable(data, columnNames);
        fileTable.setBackground(Color.DARK_GRAY); // Цвет фона таблицы
        fileTable.setForeground(Color.WHITE); // Цвет текста в таблице
        fileTable.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Шрифт таблицы
        JScrollPane tableScrollPane = new JScrollPane(fileTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder()); // Убираем границы у скролл-панели

        // Добавляем обработчик для обновления пути при клике по таблице
        fileTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = fileTable.getSelectedRow();
                int column = fileTable.getSelectedColumn();
                if (row != -1 && column == 0) { // Клик по имени файла/каталога
                    String selectedName = (String) fileTable.getValueAt(row, column);
                    String newPath = currentDirectory.getAbsolutePath() + File.separator + selectedName;
                    if (new File(newPath).isDirectory()) {
                        newPath += File.separator;
                    }
                    pathField.setText(newPath);
                    openDirectory(newPath);
                }
            }
        });

        // Добавляем верхнюю панель над деревом
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(fileTree), BorderLayout.WEST);
        panel.add(tableScrollPane, BorderLayout.CENTER);

        // Добавляем обработчик событий для кнопки "Назад"
        backButton.addActionListener(e -> goBack());

        // Отображаем окно
        frame.getContentPane().add(panel);
        frame.setVisible(true);

        // Показываем корневые диски по умолчанию
        showFiles(File.listRoots()[0]);
    }

    private void showFiles(File directory) {
        pathHistory.push(directory); // Добавляем текущую директорию в историю
        currentDirectory = directory;
        pathField.setText(directory.getAbsolutePath());

        // Обновляем содержимое JTable для отображения файлов и папок в выбранной директории
        File[] files = directory.listFiles();
        if (files != null) {
            Object[][] newData = new Object[files.length][4];
            for (int i = 0; i < files.length; i++) {
                newData[i][0] = files[i].getName();
                newData[i][1] = files[i].isDirectory() ? "Папка" : "Файл";
                newData[i][2] = files[i].isDirectory() ? "-" : formatFileSize(files[i].length());
                newData[i][3] = new Date(files[i].lastModified());
            }
            DefaultTableModel tableModel = new DefaultTableModel(newData, new String[]{"Имя файла", "Тип", "Размер", "Дата изменения"});
            fileTable.setModel(tableModel);
        }
    }

    private void openDirectory(String path) {
        File directory = new File(path);
        if (directory.exists() && directory.isDirectory()) {
            showFiles(directory);
        }
        // else {
        //    JOptionPane.showMessageDialog(frame, "Неверный путь к каталогу", "Ошибка", JOptionPane.ERROR_MESSAGE);
        //}
    }

    private void goBack() {
        if (!pathHistory.isEmpty()) {
            pathHistory.pop(); // Удаляем текущую директорию из истории
        }

        // Получаем предыдущую директорию из истории, если она есть
        File previousDirectory = !pathHistory.isEmpty() ? pathHistory.peek() : null;

        // Если предыдущая директория не null, открываем её
        if (previousDirectory != null) {
            showFilesWithoutAddingToHistory(previousDirectory);
        } else {
            // Если история пуста, показываем корневые диски
            showFilesWithoutAddingToHistory(File.listRoots()[0]);
        }
    }

    private void showFilesWithoutAddingToHistory(File directory) {
        currentDirectory = directory;
        pathField.setText(directory.getAbsolutePath());

        // Обновляем содержимое JTable для отображения файлов и папок в выбранной директории
        File[] files = directory.listFiles();
        if (files != null) {
            Object[][] newData = new Object[files.length][4];
            for (int i = 0; i < files.length; i++) {
                newData[i][0] = files[i].getName();
                newData[i][1] = files[i].isDirectory() ? "Папка" : "Файл";
                newData[i][2] = files[i].isDirectory() ? "-" : formatFileSize(files[i].length());
                newData[i][3] = new Date(files[i].lastModified());
            }
            DefaultTableModel tableModel = new DefaultTableModel(newData, new String[]{"Имя файла", "Тип", "Размер", "Дата изменения"});
            fileTable.setModel(tableModel);
        }
    }

    // Метод для форматирования размера файла в читаемый формат
    private String formatFileSize(long size) {
        String hrSize;
        double bytes = size;
        double kilobytes = bytes / 1024;
        double megabytes = kilobytes / 1024;
        double gigabytes = megabytes / 1024;
        double terabytes = gigabytes / 1024;

        if (terabytes > 1) {
            hrSize = String.format("%.2f Тб", terabytes);
        } else if (gigabytes > 1) {
            hrSize = String.format("%.2f Гб", gigabytes);
        } else if (megabytes > 1) {
            hrSize = String.format("%.2f Мб", megabytes);
        } else if (kilobytes > 1) {
            hrSize = String.format("%.2f Кб", kilobytes);
        } else {
            hrSize = String.format("%.0f байт", bytes);
        }

        return hrSize;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}

