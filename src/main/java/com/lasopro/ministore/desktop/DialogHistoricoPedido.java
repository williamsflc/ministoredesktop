package com.lasopro.ministore.desktop;

import com.lasopro.ministore.db.Proveedores;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author williams
 */
public class DialogHistoricoPedido extends JDialog {

    private JComboBox<ComboBoxItem> cbProveedor;
    private JTextField tNumeroPedido;
    private JLabel lStockActual;
    private JLabel lNuevoStock;
    private Map<String, Object> result;
    private Double stockActual;
    private Double nuevoStock;

    public DialogHistoricoPedido(java.awt.Frame parent) {
        super(parent, true);
        initComponents();
    }

    private void initComponents() {
        setTitle("Datos del pedido");
        setBackground(java.awt.Color.WHITE);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(java.awt.Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cbProveedor = new JComboBox<>();
        tNumeroPedido = new JTextField(20);
        lStockActual = new JLabel("-");
        lNuevoStock = new JLabel("-");

        int row = 0;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel("Stock actual:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(lStockActual, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel("Nuevo stock:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(lNuevoStock, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel("Proveedor:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(cbProveedor, gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel("Numero de pedido:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(tNumeroPedido, gbc);

        JPanel buttons = new JPanel();
        buttons.setBackground(java.awt.Color.WHITE);
        JButton bAceptar = new JButton("Aceptar");
        JButton bCancelar = new JButton("Cancelar");
        buttons.add(bAceptar);
        buttons.add(bCancelar);

        bAceptar.addActionListener(e -> aceptar());
        bCancelar.addActionListener(e -> cancelar());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationByPlatform(true);
    }
    

    public void fillProveedores(List<Map<String, Object>> proveedores) {
        proveedores = (new Proveedores()).list(1, 500).getData();
        cbProveedor.removeAllItems();
        proveedores.forEach(m -> cbProveedor.addItem(new ComboBoxItem(m.get("id"), m, (String) m.get("nombre"))));
    }

    private void aceptar() {
        if (cbProveedor.getSelectedItem() == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Debe seleccionar un proveedor", "Validación", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        result = new HashMap<>();
        result.put("proveedor_id", ((ComboBoxItem) cbProveedor.getSelectedItem()).getId());
        result.put("numero_pedido", tNumeroPedido.getText());
        result.put("stock_actual", stockActual);
        result.put("nuevo_stock", nuevoStock);
        setVisible(false);
    }

    private void cancelar() {
        result = null;
        setVisible(false);
    }

    public Map<String, Object> showDialog(Double stockActual, Double nuevoStock) {
        result = null;
        this.stockActual = stockActual;
        this.nuevoStock = nuevoStock;
        lStockActual.setText(formatStock(stockActual));
        lNuevoStock.setText(formatStock(nuevoStock));
        setVisible(true);
        return result;
    }

    static String formatStock(Double value) {
        if (value == null) {
            return "";
        }
        if (value == Math.floor(value)) {
            return String.valueOf(value.intValue());
        }
        return String.valueOf(value);
    }
}
