package com.lasopro.ministore.desktop;

import com.lasopro.ministore.db.Config;
import com.lasopro.ministore.util.Resources;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author williams
 */
public class PanelConfig extends javax.swing.JPanel implements PanelInterface {

    private static final String KEY_APP_NAME = "app.name";
    private static final String KEY_APP_LOGO = "app.logo";
    private static final String KEY_APP_CURRENCY = "app.currency";
    private static final String KEY_PAYMENT_METHOD = "app.payment.method";
    private static final String KEY_PRODUCT_PREFIX = "app.product.code.prefix";
    private static final String KEY_PRODUCT_STARTS = "app.product.code.starts";
    private static final String KEY_DESC_VENTA_TOTAL = "desc.x.venta.total";
    private static final String KEY_DESC_VENTA_PROD = "desc.x.venta.y.prod";
    private static final String KEY_INVOICE_HEADER = "app.invoice.header";
    private static final String KEY_INVOICE_FOOTER = "app.invoice.footer";
    private static final String KEY_UI_FONT_SIZE = "app.ui.font.size";
    private static final String KEY_PRINT_FONT_SIZE = "app.print.font.size";

    private Config db;
    private JFileChooser fileChooser;

    private JTextField tAppName;
    private JTextField tAppLogo;
    private JTextField tAppCurrency;
    private JTextField tPaymentMethod;
    private JTextField tProductPrefix;
    private JTextField tProductStarts;
    private JComboBox<String> cbDescVentaTotal;
    private JComboBox<String> cbDescVentaProd;
    private JTextArea tInvoiceHeader;
    private JTextArea tInvoiceFooter;
    private JTextField tUiFontSize;
    private JTextField tPrintFontSize;

    public PanelConfig() {
        initComponents();
        db = new Config();
        refresh();
    }

    @Override
    public final void refresh() {
        MinistoreDesktop.runInThread("Actualizando configuracion...", () -> {
            _refresh();
        });
    }

    public void _refresh() {
        Map<String, String> config = db.listAsMap();
        tAppName.setText(config.getOrDefault(KEY_APP_NAME, ""));
        tAppLogo.setText(config.getOrDefault(KEY_APP_LOGO, ""));
        tAppCurrency.setText(config.getOrDefault(KEY_APP_CURRENCY, ""));
        tPaymentMethod.setText(config.getOrDefault(KEY_PAYMENT_METHOD, "CASH"));
        tProductPrefix.setText(config.getOrDefault(KEY_PRODUCT_PREFIX, ""));
        tProductStarts.setText(config.getOrDefault(KEY_PRODUCT_STARTS, ""));
        cbDescVentaTotal.setSelectedItem(normalizarEstado(config.get(KEY_DESC_VENTA_TOTAL)));
        cbDescVentaProd.setSelectedItem(normalizarEstado(config.get(KEY_DESC_VENTA_PROD)));
        tInvoiceHeader.setText(config.getOrDefault(KEY_INVOICE_HEADER, ""));
        tInvoiceFooter.setText(config.getOrDefault(KEY_INVOICE_FOOTER, ""));
        tUiFontSize.setText(config.getOrDefault(KEY_UI_FONT_SIZE, "13"));
        tPrintFontSize.setText(config.getOrDefault(KEY_PRINT_FONT_SIZE, ""));
    }

    private String normalizarEstado(String value) {
        if (value != null && value.equalsIgnoreCase("Inhabilitado")) {
            return "Inhabilitado";
        }
        return "Habilitado";
    }

    private void seleccionarLogo() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "png", "jpg", "jpeg", "gif", "bmp"));
        }
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            if (selected != null) {
                tAppLogo.setText(selected.getPath());
            }
        }
    }

    private void guardar() {
        StringBuilder errors = new StringBuilder();

        if (tAppName.getText().isBlank()) {
            errors.append(" - El nombre de la aplicación es obligatorio\n");
        }
        if (tAppLogo.getText().isBlank()) {
            errors.append(" - Debe indicar la ruta del logo\n");
        }
        if (tAppCurrency.getText().isBlank()) {
            errors.append(" - El prefijo de moneda es obligatorio\n");
        }
        if (tProductPrefix.getText().isBlank()) {
            errors.append(" - El prefijo de código de producto es obligatorio\n");
        }
        try {
            Integer.parseInt(tProductStarts.getText().trim());
        } catch (Exception e) {
            errors.append(" - El valor inicial de códigos debe ser numérico\n");
        }
        try {
            int size = Integer.parseInt(tUiFontSize.getText().trim());
            if (size <= 0) {
                errors.append(" - El tamaño de fuente de la interfaz debe ser mayor a cero\n");
            }
        } catch (Exception e) {
            errors.append(" - El tamaño de fuente de la interfaz debe ser un valor numérico\n");
        }
        try {
            int size = Integer.parseInt(tPrintFontSize.getText().trim());
            if (size <= 0) {
                errors.append(" - El tamaño de fuente para impresión debe ser mayor a cero\n");
            }
        } catch (Exception e) {
            errors.append(" - El tamaño de fuente para impresión debe ser un valor numérico\n");
        }

        if (!errors.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Errores de validación:\n\n" + errors, "Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Map<String, String> values = new HashMap<>();
            values.put(KEY_APP_NAME, tAppName.getText().trim());
            values.put(KEY_APP_LOGO, tAppLogo.getText().trim());
            values.put(KEY_APP_CURRENCY, tAppCurrency.getText().trim());
            values.put(KEY_PAYMENT_METHOD, tPaymentMethod.getText().trim());
            values.put(KEY_PRODUCT_PREFIX, tProductPrefix.getText().trim());
            values.put(KEY_PRODUCT_STARTS, tProductStarts.getText().trim());
            values.put(KEY_DESC_VENTA_TOTAL, (String) cbDescVentaTotal.getSelectedItem());
            values.put(KEY_DESC_VENTA_PROD, (String) cbDescVentaProd.getSelectedItem());
            values.put(KEY_INVOICE_HEADER, tInvoiceHeader.getText());
            values.put(KEY_INVOICE_FOOTER, tInvoiceFooter.getText());
            values.put(KEY_UI_FONT_SIZE, tUiFontSize.getText().trim());
            values.put(KEY_PRINT_FONT_SIZE, tPrintFontSize.getText().trim());

            db.updateAll(values);
            Resources.reload();
            MinistoreDesktop.getMainFrame().refreshAppConfig();
            JOptionPane.showMessageDialog(this, "Configuración guardada correctamente", "Configuración", JOptionPane.INFORMATION_MESSAGE);
        } catch (Throwable err) {
            MinistoreDesktop.log.err("Error al guardar la configuración", err);
            JOptionPane.showMessageDialog(this, "Error al guardar la configuración: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initComponents() {
        setBackground(java.awt.Color.WHITE);
        setLayout(new java.awt.BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(java.awt.Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;

        tAppName = new JTextField(40);
        tAppLogo = new JTextField(40);
        tAppCurrency = new JTextField(5);
        tPaymentMethod = new JTextField(10);
        tPaymentMethod.setText("CASH");
        tPaymentMethod.setEditable(false);
        tProductPrefix = new JTextField(20);
        tProductStarts = new JTextField(20);
        cbDescVentaTotal = new JComboBox<>(new String[]{"Habilitado", "Inhabilitado"});
        cbDescVentaProd = new JComboBox<>(new String[]{"Habilitado", "Inhabilitado"});
        tInvoiceHeader = new JTextArea(5, 40);
        tInvoiceHeader.setLineWrap(true);
        tInvoiceHeader.setWrapStyleWord(true);
        tInvoiceFooter = new JTextArea(2, 40);
        tInvoiceFooter.setLineWrap(true);
        tInvoiceFooter.setWrapStyleWord(true);
        tUiFontSize = new JTextField(10);
        tPrintFontSize = new JTextField(10);

        JButton btLogo = new JButton("...");
        btLogo.setToolTipText("Seleccionar archivo");
        btLogo.addActionListener(e -> seleccionarLogo());

        JPanel logoPanel = new JPanel(new java.awt.BorderLayout(4, 0));
        logoPanel.setBackground(java.awt.Color.WHITE);
        logoPanel.add(tAppLogo, java.awt.BorderLayout.CENTER);
        logoPanel.add(btLogo, java.awt.BorderLayout.EAST);

        int row = 0;
        addField(form, gbc, row++, "Nombre de la aplicación:", tAppName);
        addField(form, gbc, row++, "Logo (Seleccione un archivo):", logoPanel);
        addField(form, gbc, row++, "Prefijo de moneda:", tAppCurrency);
        addField(form, gbc, row++, "Método de autenticación:", tPaymentMethod);
        addField(form, gbc, row++, "Prefijo para códigos de nuevos productos:", tProductPrefix);
        addField(form, gbc, row++, "Valor inicial para códigos de nuevos productos:", tProductStarts);
        addField(form, gbc, row++, "Descuento sobre venta total:", cbDescVentaTotal);
        addField(form, gbc, row++, "Descuento sobre producto por venta:", cbDescVentaProd);
        addField(form, gbc, row++, "Encabezado de comprobante:", new JScrollPane(tInvoiceHeader));
        addField(form, gbc, row++, "Pie de comprobante:", new JScrollPane(tInvoiceFooter));
        addField(form, gbc, row++, "Tamaño de fuente de la interfaz:", tUiFontSize);
        addField(form, gbc, row++, "Tamaño de fuente para impresión:", tPrintFontSize);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        JButton btGuardar = new JButton("Guardar configuración");
        btGuardar.addActionListener(e -> guardar());
        form.add(btGuardar, gbc);

        
        JPanel container = new JPanel();
        container.setLayout(new GridBagLayout());
        form.setMaximumSize(new Dimension(600,5000));
        container.add(form);
        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        scroll.getViewport().setBackground(java.awt.Color.WHITE);
        add(scroll, java.awt.BorderLayout.CENTER);
    }

    private void addField(JPanel form, GridBagConstraints gbc, int row, String label, java.awt.Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 100;
        form.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(field, gbc);
    }
    
    @Override
    public String name() {
        return "config";
    }
}
