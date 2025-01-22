import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductManagementApp {
    private static List<String> laboratories = new ArrayList<>();
    private static List<Product> products = new ArrayList<>();
    private static final String PRODUCTS_FILE = "products.txt";
    private static final String LABORATORIES_FILE = "laboratories.txt";

    public static void main(String[] args) {
        loadData();
        showMainMenu();
    }

    private static void showMainMenu() {
        JFrame mainFrame = new JFrame("Sistema de Gestão de Produtos");
        mainFrame.setSize(400, 300);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        mainFrame.add(panel);

        JButton registerButton = new JButton("Cadastro de Produto");
        JButton reportButton = new JButton("Relatório com Filtros");
        JButton searchButton = new JButton("Buscar Produto");
        JButton exitButton = new JButton("Sair");

        panel.add(registerButton);
        panel.add(reportButton);
        panel.add(searchButton);
        panel.add(exitButton);

        registerButton.addActionListener(e -> showPasswordPrompt());
        reportButton.addActionListener(e -> showReportWithFilters());
        searchButton.addActionListener(e -> showSearchScreen());
        exitButton.addActionListener(e -> System.exit(0));

        mainFrame.setVisible(true);
    }

    private static void showPasswordPrompt() {
        JPasswordField passwordField = new JPasswordField();
        int option = JOptionPane.showConfirmDialog(null, passwordField, "Digite a senha:", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String password = new String(passwordField.getPassword());
            if ("1234".equals(password)) {
                showProductRegistration();
            } else {
                JOptionPane.showMessageDialog(null, "Senha incorreta!");
            }
        }
    }

    private static void showProductRegistration() {
        JFrame registerFrame = new JFrame("Cadastro de Produto");
        registerFrame.setSize(600, 400);

        JPanel panel = new JPanel(new GridBagLayout());
        registerFrame.add(panel);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);

        JTextField descriptionField = addField(panel, "Descrição:", c, 0);
        JTextField posologyField = addField(panel, "Posologia:", c, 1);
        JTextField usageField = addField(panel, "Serve para:", c, 2);
        JTextField activeField = addField(panel, "Princípio Ativo:", c, 3);

        JLabel labLabel = new JLabel("Laboratório:");
        JComboBox<String> labComboBox = new JComboBox<>(laboratories.toArray(new String[0]));
        if (!laboratories.isEmpty()) {
            labComboBox.setSelectedIndex(laboratories.size() - 1); // Seleciona o último laboratório
        }
        JButton newLabButton = new JButton("Criar Novo");
        c.gridx = 0;
        c.gridy = 4;
        panel.add(labLabel, c);
        c.gridx = 1;
        panel.add(labComboBox, c);
        c.gridx = 2;
        panel.add(newLabButton, c);

        newLabButton.addActionListener(e -> {
            String newLab = JOptionPane.showInputDialog("Digite o nome do novo laboratório:");
            if (newLab != null && !newLab.trim().isEmpty()) {
                laboratories.add(newLab);
                labComboBox.addItem(newLab);
                labComboBox.setSelectedItem(newLab); // Seleciona automaticamente o novo laboratório
                saveLaboratories();
            }
        });

        JButton saveButton = new JButton("Salvar Produto");
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 2;
        panel.add(saveButton, c);

        saveButton.addActionListener(e -> {
            String description = descriptionField.getText().trim();
            if (description.isEmpty() || isDuplicate(description)) {
                JOptionPane.showMessageDialog(null, "Descrição inválida ou duplicada.");
                return;
            }

            Product product = new Product(
                    description,
                    posologyField.getText().trim(),
                    usageField.getText().trim(),
                    activeField.getText().trim(),
                    (String) labComboBox.getSelectedItem()
            );

            products.add(product);
            saveProducts();
            JOptionPane.showMessageDialog(null, "Produto cadastrado com sucesso!");
        });

        registerFrame.setVisible(true);
    }

    private static JTextField addField(JPanel panel, String label, GridBagConstraints c, int row) {
        JLabel fieldLabel = new JLabel(label);
        JTextField textField = new JTextField(20);
        c.gridx = 0;
        c.gridy = row;
        panel.add(fieldLabel, c);
        c.gridx = 1;
        panel.add(textField, c);
        return textField;
    }

    private static void showReportWithFilters() {
        JFrame reportFrame = new JFrame("Relatório com Filtros");
        reportFrame.setSize(800, 500);

        JPanel panel = new JPanel(new BorderLayout());
        reportFrame.add(panel);

        JPanel filterPanel = new JPanel(new GridLayout(2, 2));
        JTextField descFilter = new JTextField();
        JComboBox<String> labFilter = new JComboBox<>(laboratories.toArray(new String[0]));
        labFilter.insertItemAt("Todos", 0);
        labFilter.setSelectedIndex(0);

        filterPanel.add(new JLabel("Descrição:"));
        filterPanel.add(descFilter);
        filterPanel.add(new JLabel("Laboratório:"));
        filterPanel.add(labFilter);

        JButton filterButton = new JButton("Filtrar");
        filterPanel.add(filterButton);
        panel.add(filterPanel, BorderLayout.NORTH);

        JTable table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        filterButton.addActionListener(e -> {
            String desc = descFilter.getText().toLowerCase();
            String lab = (String) labFilter.getSelectedItem();

            List<Product> filteredProducts = products.stream()
                    .filter(p -> desc.isEmpty() || p.getDescription().toLowerCase().contains(desc))
                    .filter(p -> "Todos".equals(lab) || p.getLaboratory().equals(lab))
                    .collect(Collectors.toList());

            String[] columns = {"Descrição", "Posologia", "Serve Para", "Princípio Ativo", "Laboratório"};
            String[][] data = filteredProducts.stream()
                    .map(p -> new String[]{p.getDescription(), p.getPosology(), p.getUsage(), p.getActive(), p.getLaboratory()})
                    .toArray(String[][]::new);
            table.setModel(new javax.swing.table.DefaultTableModel(data, columns));
        });

        reportFrame.setVisible(true);
    }

    private static void showSearchScreen() {
        JFrame searchFrame = new JFrame("Buscar Produto");
        searchFrame.setSize(400, 300);

        JPanel panel = new JPanel(new BorderLayout());
        searchFrame.add(panel);

        JTextField searchField = new JTextField();
        JList<String> productList = new JList<>();
        panel.add(searchField, BorderLayout.NORTH);
        panel.add(new JScrollPane(productList), BorderLayout.CENTER);

        searchField.addCaretListener(e -> {
            String keyword = searchField.getText().toLowerCase();
            List<String> productDescriptions = products.stream()
                    .filter(p -> p.getDescription().toLowerCase().contains(keyword))
                    .map(Product::getDescription)
                    .collect(Collectors.toList());
            productList.setListData(productDescriptions.toArray(new String[0]));
        });

        productList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedProduct = productList.getSelectedValue();
                Product product = products.stream()
                        .filter(p -> p.getDescription().equals(selectedProduct))
                        .findFirst()
                        .orElse(null);
                if (product != null) {
                    JOptionPane.showMessageDialog(null, product);
                }
            }
        });

        searchFrame.setVisible(true);
    }

    private static boolean isDuplicate(String description) {
        return products.stream().anyMatch(p -> p.getDescription().equalsIgnoreCase(description));
    }

    private static void saveProducts() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PRODUCTS_FILE))) {
            for (Product product : products) {
                writer.write(product.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveLaboratories() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LABORATORIES_FILE))) {
            for (String lab : laboratories) {
                writer.write(lab);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadData() {
        loadProducts();
        loadLaboratories();
    }

    private static void loadProducts() {
        try (BufferedReader reader = new BufferedReader(new FileReader(PRODUCTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                products.add(Product.fromCSV(line));
            }
        } catch (IOException e) {
            products = new ArrayList<>();
        }
    }

    private static void loadLaboratories() {
        try (BufferedReader reader = new BufferedReader(new FileReader(LABORATORIES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                laboratories.add(line);
            }
        } catch (IOException e) {
            laboratories = new ArrayList<>();
        }
    }
}

class Product {
    private String description;
    private String posology;
    private String usage;
    private String active;
    private String laboratory;

    public Product(String description, String posology, String usage, String active, String laboratory) {
        this.description = description;
        this.posology = posology;
        this.usage = usage;
        this.active = active;
        this.laboratory = laboratory;
    }

    public String getDescription() {
        return description;
    }

    public String getPosology() {
        return posology;
    }

    public String getUsage() {
        return usage;
    }

    public String getActive() {
        return active;
    }

    public String getLaboratory() {
        return laboratory;
    }

    public String toCSV() {
        return String.join(",", description, posology, usage, active, laboratory);
    }

    public static Product fromCSV(String csv) {
        String[] parts = csv.split(",");
        return new Product(parts[0], parts[1], parts[2], parts[3], parts[4]);
    }

    @Override
    public String toString() {
        return "Descrição: " + description + ", Posologia: " + posology + ", Serve Para: " + usage + ", Princípio Ativo: " + active + ", Laboratório: " + laboratory;
    }
}
