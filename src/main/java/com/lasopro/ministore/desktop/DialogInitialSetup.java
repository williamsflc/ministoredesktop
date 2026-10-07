package com.lasopro.ministore.desktop;

import com.lasopro.ministore.util.CryptoUtils;
import com.lasopro.ministore.util.InitialSetupService;
import com.lasopro.ministore.util.InitialSetupService.DatabaseConfiguration;
import com.lasopro.ministore.util.InitialSetupService.DatabaseType;
import com.lasopro.ministore.util.Resources;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.File;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/**
 * Asistente modal para preparar una instalación nueva.
 */
public final class DialogInitialSetup extends JDialog {

    private static final String[] STEP_TITLES = {
        "Base de datos", "Configuración principal", "Usuario administrador", "Listo"
    };

    private final InitialSetupService service = new InitialSetupService();
    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);
    private final JLabel stepLabel = new JLabel();
    private final JButton backButton = new JButton("Atrás");
    private final JButton nextButton = new JButton("Siguiente");
    private final JButton cancelButton = new JButton("Cancelar");

    private final JRadioButton localOption = new JRadioButton("Utilizar base de datos local (SQLite)", true);
    private final JRadioButton remoteOption = new JRadioButton("Conectar a Base de datos");
    private final JTextField localDatabaseName = new JTextField("ministore", 28);
    private final JPanel localFields = new JPanel(new GridBagLayout());
    private final JPanel remoteFields = new JPanel(new GridBagLayout());
    private final JComboBox<DatabaseType> databaseType = new JComboBox<>(new DatabaseType[]{
        DatabaseType.POSTGRESQL, DatabaseType.SQL_SERVER, DatabaseType.ORACLE, DatabaseType.MYSQL
    });
    private final JTextField jdbcUrl = new JTextField(38);
    private final JTextField databaseUser = new JTextField(25);
    private final JPasswordField databasePassword = new JPasswordField(25);
    private final JLabel driverClass = new JLabel();
    private final JButton testButton = new JButton("Probar conexión");

    private final JTextField appName = new JTextField("MiniStore", 32);
    private final JTextField appLogo = new JTextField("res/logo.png", 32);
    private final JTextArea invoiceHeader = new JTextArea(8, 34);

    private final JTextField adminUser = new JTextField(28);
    private final JPasswordField adminPassword = new JPasswordField(28);
    private final JPasswordField adminConfirmation = new JPasswordField(28);

    private int step;
    private boolean completed;
    private DatabaseConfiguration configuration;

    public DialogInitialSetup(Window owner) {
        super(owner, "Configuración inicial", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(720, 560));
        buildUi();
        loadExistingValues();
        updateDatabaseMode();
        updateStep();
        pack();
        setLocationRelativeTo(owner);
    }

    public boolean showWizard() {
        setVisible(true);
        return completed;
    }

    private void buildUi() {
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 12, 16));

        stepLabel.setFont(stepLabel.getFont().deriveFont(Font.BOLD, 20f));
        content.add(stepLabel, BorderLayout.NORTH);

        cardPanel.add(buildDatabaseStep(), "0");
        cardPanel.add(buildApplicationStep(), "1");
        cardPanel.add(buildAdministratorStep(), "2");
        cardPanel.add(buildHelpStep(), "3");
        content.add(cardPanel, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(cancelButton);
        actions.add(backButton);
        actions.add(nextButton);
        content.add(actions, BorderLayout.SOUTH);

        backButton.addActionListener(event -> {
            if (step > 0 && step < 3) {
                step--;
                updateStep();
            }
        });
        nextButton.addActionListener(event -> next());
        cancelButton.addActionListener(event -> {
            int answer = JOptionPane.showConfirmDialog(this,
                    "La aplicación necesita completar la configuración inicial para continuar.\n¿Desea salir?",
                    "Cancelar configuración", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer == JOptionPane.YES_OPTION) {
                dispose();
            }
        });

        setContentPane(content);
    }

    private JPanel buildDatabaseStep() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = constraints();
        gbc.gridwidth = 2;

        ButtonGroup group = new ButtonGroup();
        group.add(localOption);
        group.add(remoteOption);
        panel.add(localOption, gbc);
        gbc.gridy++;
        panel.add(remoteOption, gbc);

        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        localFields.setBorder(BorderFactory.createTitledBorder("Base de datos local"));
        addRow(localFields, 0, "Nombre de la Base de datos (Sin espacios o caracteres especiales)", localDatabaseName);
        panel.add(localFields, gbc);

        gbc.gridy++;
        remoteFields.setBorder(BorderFactory.createTitledBorder("Conexión"));
        addRow(remoteFields, 0, "Tipo de base de datos", databaseType);
        addRow(remoteFields, 1, "JDBC URL", jdbcUrl);
        addRow(remoteFields, 2, "Nombre de Usuario", databaseUser);
        addRow(remoteFields, 3, "Contraseña", databasePassword);
        addRow(remoteFields, 4, "Driver class", driverClass);
        GridBagConstraints buttonConstraints = constraints();
        buttonConstraints.gridx = 1;
        buttonConstraints.gridy = 5;
        buttonConstraints.anchor = GridBagConstraints.EAST;
        remoteFields.add(testButton, buttonConstraints);
        panel.add(remoteFields, gbc);

        gbc.gridy++;
        gbc.weighty = 1;
        panel.add(new JPanel(), gbc);

        localOption.addActionListener(event -> updateDatabaseMode());
        remoteOption.addActionListener(event -> updateDatabaseMode());
        databaseType.addActionListener(event -> updateSelectedDatabaseType());
        testButton.addActionListener(event -> testConnection());
        return panel;
    }

    private JPanel buildApplicationStep() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Datos indispensables"));
        addRow(panel, 0, "Nombre de la aplicación", appName);

        JPanel logoRow = new JPanel(new BorderLayout(4, 0));
        logoRow.add(appLogo, BorderLayout.CENTER);
        JButton chooseLogo = new JButton("Examinar...");
        logoRow.add(chooseLogo, BorderLayout.EAST);
        chooseLogo.addActionListener(event -> chooseLogo());
        addRow(panel, 1, "Logo", logoRow);

        invoiceHeader.setLineWrap(true);
        invoiceHeader.setWrapStyleWord(true);
        addRow(panel, 2, "Encabezado de factura", new JScrollPane(invoiceHeader));
        return panel;
    }

    private JPanel buildAdministratorStep() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Primer usuario"));
        addRow(panel, 0, "Usuario", adminUser);
        addRow(panel, 1, "Contraseña", adminPassword);
        addRow(panel, 2, "Confirmar contraseña", adminConfirmation);

        GridBagConstraints gbc = constraints();
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Este usuario se registrará con el tipo Administrador."), gbc);
        return panel;
    }

    private JPanel buildHelpStep() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JLabel title = new JLabel("¿Qué sigue?");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        JTextArea tips = new JTextArea("""
                1. Registre las categorías de sus productos en la pestaña "Categorías".

                2. Registre uno o más proveedores en la pestaña de "Proveedores".

                3. Agregue sus productos en la pestaña de "Inventario".

                Al finalizar se abrirá la pantalla de ingreso. Use el usuario administrador
                que acaba de registrar.
                """);
        tips.setEditable(false);
        tips.setLineWrap(true);
        tips.setWrapStyleWord(true);
        tips.setOpaque(false);
        tips.setFont(tips.getFont().deriveFont(16f));
        panel.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        panel.add(title, BorderLayout.NORTH);
        panel.add(tips, BorderLayout.CENTER);
        return panel;
    }

    private void next() {
        try {
            switch (step) {
                case 0 -> configureDatabase();
                case 1 -> {
                    service.saveApplicationConfiguration(
                            appName.getText(), appLogo.getText(), invoiceHeader.getText());
                    step++;
                    updateStep();
                }
                case 2 -> {
                    service.createAdministrator(
                            adminUser.getText(), adminPassword.getPassword(), adminConfirmation.getPassword());
                    service.complete(configuration);
                    step++;
                    updateStep();
                }
                case 3 -> {
                    completed = true;
                    dispose();
                }
                default -> throw new IllegalStateException("Paso desconocido");
            }
        } catch (Throwable error) {
            showError(error);
        }
    }

    private void configureDatabase() {
        DatabaseConfiguration candidate = readDatabaseConfiguration();
        setBusy(true, "Validando y creando esquema...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                service.testConnection(candidate);
                service.persistInProgress(candidate);
                service.initializeSchema(candidate);
                return null;
            }

            @Override
            protected void done() {
                setBusy(false, null);
                try {
                    get();
                    configuration = candidate;
                    loadApplicationValues();
                    step++;
                    updateStep();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError(ex);
                } catch (ExecutionException ex) {
                    showError(ex.getCause());
                }
            }
        }.execute();
    }

    private void testConnection() {
        DatabaseConfiguration candidate;
        try {
            candidate = readDatabaseConfiguration();
        } catch (Throwable error) {
            showError(error);
            return;
        }
        setBusy(true, "Probando conexión...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                service.testConnection(candidate);
                return null;
            }

            @Override
            protected void done() {
                setBusy(false, null);
                try {
                    get();
                    JOptionPane.showMessageDialog(DialogInitialSetup.this,
                            "Conexión exitosa.", "Prueba de conexión", JOptionPane.INFORMATION_MESSAGE);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showError(ex);
                } catch (ExecutionException ex) {
                    showError(ex.getCause());
                }
            }
        }.execute();
    }

    private DatabaseConfiguration readDatabaseConfiguration() {
        if (localOption.isSelected()) {
            try {
                return service.localConfiguration(localDatabaseName.getText());
            } catch (java.io.IOException ex) {
                throw new RuntimeException("No se pudo preparar el directorio de datos.", ex);
            }
        }
        return service.remoteConfiguration(
                (DatabaseType) databaseType.getSelectedItem(),
                jdbcUrl.getText(), databaseUser.getText(), databasePassword.getPassword());
    }

    private void loadExistingValues() {
        String dbType = Resources.p("app.db.type");
        String url = Resources.p(Resources.getDsName() + ".jdbc.url");
        if (url == null || url.isBlank()) {
            updateSelectedDatabaseType();
            return;
        }

        DatabaseType selected = typeByResourceName(dbType);
        if (selected == DatabaseType.SQLITE) {
            localOption.setSelected(true);
            String fileName = new File(url.substring("jdbc:sqlite:".length())).getName();
            localDatabaseName.setText(fileName.replaceFirst("(?i)\\.db$", ""));
        } else if (selected != null) {
            remoteOption.setSelected(true);
            databaseType.setSelectedItem(selected);
            jdbcUrl.setText(url);
            databaseUser.setText(valueOrEmpty(Resources.p(Resources.getDsName() + ".jdbc.user")));
            String savedPassword = Resources.p(Resources.getDsName() + ".jdbc.password");
            if (savedPassword != null) {
                try {
                    savedPassword = CryptoUtils.decrypt(savedPassword);
                } catch (RuntimeException ignored) {
                }
                databasePassword.setText(savedPassword);
            }
        }
    }

    private void loadApplicationValues() {
        appName.setText(valueOrDefault(Resources.p("app.name"), "MiniStore"));
        appLogo.setText(valueOrDefault(Resources.p("app.logo"), "res/logo.png"));
        invoiceHeader.setText(valueOrDefault(Resources.p("app.invoice.header"), "MINI STORE\n"));
    }

    private void updateDatabaseMode() {
        setEnabledRecursively(localFields, localOption.isSelected());
        setEnabledRecursively(remoteFields, remoteOption.isSelected());
        updateSelectedDatabaseType();
    }

    private void updateSelectedDatabaseType() {
        DatabaseType selected = (DatabaseType) databaseType.getSelectedItem();
        if (selected == null) {
            return;
        }
        driverClass.setText(selected.driverClass());
        if (jdbcUrl.getText().isBlank() || isKnownSampleUrl(jdbcUrl.getText())) {
            jdbcUrl.setText(selected.sampleUrl());
        }
    }

    private boolean isKnownSampleUrl(String value) {
        for (DatabaseType type : DatabaseType.values()) {
            if (type.sampleUrl().equals(value)) {
                return true;
            }
        }
        return false;
    }

    private void updateStep() {
        cards.show(cardPanel, Integer.toString(step));
        stepLabel.setText("Paso " + (step + 1) + " de 4 — " + STEP_TITLES[step]);
        backButton.setVisible(step > 0 && step < 3);
        cancelButton.setVisible(step < 3);
        nextButton.setText(step == 3 ? "Abrir aplicación" : "Siguiente");
        getRootPane().setDefaultButton(nextButton);
    }

    private void setBusy(boolean busy, String text) {
        nextButton.setEnabled(!busy);
        backButton.setEnabled(!busy);
        cancelButton.setEnabled(!busy);
        testButton.setEnabled(!busy && remoteOption.isSelected());
        setCursor(busy
                ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR)
                : java.awt.Cursor.getDefaultCursor());
        if (text != null) {
            stepLabel.setText(text);
        } else {
            updateStep();
        }
    }

    private void chooseLogo() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            appLogo.setText(chooser.getSelectedFile().getPath());
        }
    }

    private void showError(Throwable error) {
        String message = error == null ? "Error desconocido" : error.getMessage();
        MinistoreDesktop.log.err("Error durante la configuración inicial", error);
        JOptionPane.showMessageDialog(this, message, "Configuración inicial", JOptionPane.ERROR_MESSAGE);
    }

    private static GridBagConstraints constraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

    private static void addRow(JPanel panel, int row, String label, Component component) {
        GridBagConstraints labelConstraints = constraints();
        labelConstraints.gridy = row;
        panel.add(new JLabel(label + ":"), labelConstraints);

        GridBagConstraints fieldConstraints = constraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, fieldConstraints);
    }

    private static void setEnabledRecursively(Component component, boolean enabled) {
        component.setEnabled(enabled);
        if (component instanceof java.awt.Container container) {
            for (Component child : container.getComponents()) {
                setEnabledRecursively(child, enabled);
            }
        }
    }

    private static DatabaseType typeByResourceName(String resourceName) {
        for (DatabaseType type : DatabaseType.values()) {
            if (type.resourceName().equalsIgnoreCase(valueOrEmpty(resourceName))) {
                return type;
            }
        }
        return null;
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
