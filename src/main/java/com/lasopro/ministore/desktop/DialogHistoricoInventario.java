package com.lasopro.ministore.desktop;

import com.lasopro.ministore.db.HistoricoInventario;
import com.lasopro.ministore.db.Productos;
import com.lasopro.ministore.db.Proveedores;
import com.lasopro.ministore.util.Util;
import java.awt.BorderLayout;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author williams
 */
public class DialogHistoricoInventario extends JDialog {

    private static DialogHistoricoInventario me;
    private final JTextArea textArea;

    public DialogHistoricoInventario(java.awt.Frame parent) {
        super(parent, true);
        setTitle("Historial de cambios de inventario");
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        setBackground(java.awt.Color.WHITE);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        textArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(textArea);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scroll, BorderLayout.CENTER);
        setSize(1100, 420);
        setLocationByPlatform(true);
    }

    public static DialogHistoricoInventario getInst() {
        if (me == null) {
            me = new DialogHistoricoInventario(MinistoreDesktop.getMainFrame());
        }
        return me;
    }

    public void refresh() {
        List<Map<String, Object>> registros = (new HistoricoInventario()).list(1, 500).getData();
        List<Map<String, Object>> proveedores = (new Proveedores()).list(1, 500).getData();
        List<Map<String, Object>> productos = (new Productos()).list(1, 50000).getData();

        StringBuilder sb = new StringBuilder();
        sb.append(Util.rpad("Fecha", 20, ' '));
        sb.append(Util.rpad("Producto", 30, ' '));
        sb.append(Util.rpad("Proveedor", 20, ' '));
        sb.append(Util.rpad("Num. pedido", 14, ' '));
        sb.append(Util.rpad("Stock act.", 12, ' '));
        sb.append(Util.rpad("Nuevo stock", 12, ' '));
        sb.append(Util.rpad("Accion", 16, ' '));
        sb.append("Usuario\n");
        sb.append("-".repeat(150)).append("\n");

        for (Map<String, Object> registro : registros) {
            String proveedor = "";
            Object proveedorId = registro.get("proveedor_id");
            if (proveedorId != null) {
                for (Map<String, Object> p : proveedores) {
                    if (((Number) p.get("id")).intValue() == ((Number) proveedorId).intValue()) {
                        proveedor = (String) p.get("nombre");
                        break;
                    }
                }
            }
            String producto = findNombreProducto(productos, registro.get("producto_id"));
            sb.append(Util.rpad(Util.dateToLocaleString((Date)registro.get("fecha")), 20, ' '));
            sb.append(Util.rpad(producto, 30, ' '));
            sb.append(Util.rpad(proveedor, 20, ' '));
            sb.append(Util.rpad(registro.get("numero_pedido") == null ? "" : "" + registro.get("numero_pedido"), 14, ' '));
            sb.append(Util.rpad(formatStock(registro.get("stock_actual")), 12, ' '));
            sb.append(Util.rpad(formatStock(registro.get("nuevo_stock")), 12, ' '));
            sb.append(Util.rpad(registro.get("accion") == null ? "" : "" + registro.get("accion"), 16, ' '));
            sb.append(registro.get("user") == null ? "" : registro.get("user"));
            sb.append("\n");
        }

        if (registros.isEmpty()) {
            sb.append("\nNo hay registros en el historial.\n");
        }

        textArea.setText(sb.toString());
        textArea.setCaretPosition(0);
    }

    public void showDialog() {
        refresh();
        setVisible(true);
    }

    private static String formatStock(Object value) {
        if (value == null) {
            return "";
        }
        double d = ((Number) value).doubleValue();
        if (d == Math.floor(d)) {
            return String.valueOf((long) d);
        }
        return String.valueOf(d);
    }

    private static String findNombreProducto(List<Map<String, Object>> productos, Object productoId) {
        if (productoId == null) {
            return "";
        }
        int id = ((Number) productoId).intValue();
        for (Map<String, Object> p : productos) {
            if (((Number) p.get("id")).intValue() == id) {
                return (String) p.get("nombre");
            }
        }
        return "";
    }
}
