package com.lasopro.ministore.desktop;

import com.lasopro.ministore.db.Clientes;
import com.lasopro.ministore.db.DescXTotalVenta;
import com.lasopro.ministore.db.Productos;
import com.lasopro.ministore.db.VentaDetalle;
import com.lasopro.ministore.db.Ventas;
import com.lasopro.ministore.util.Resources;
import com.lasopro.ministore.util.Util;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 *
 * @author williams
 */
public class PanelVenta extends javax.swing.JPanel {
    
    private static SimpleDateFormat formatForHash = new SimpleDateFormat("yyyyMMddHHmmss");
    
    Long idVenta;
    Map<String,Object> infoVenta;
    List<Map<String,Object>> products;
    
    boolean ventaEnCurso = false;
    List<Map<String,Object>> items;
    Productos dbProductos;
    Clientes dbclientes;
    Ventas dbVentas;
    VentaDetalle dbVentaDetalle;
    
    PanelVentas parent;
  

    /**
     * Creates new form PanelVenta
     */
    public PanelVenta() {
        initComponents();
        configurarAtajosTeclado();
        this.items = new ArrayList<>();
        this.dbProductos = new Productos();
        this.dbclientes = new Clientes();
        this.dbVentaDetalle = new VentaDetalle();
        this.dbVentas = new Ventas();
        
        this.mapListTextPane1.addActionListener((e)->{
            DialogVentaDetalle.mostrar(this.mapListTextPane1.getSelectedElement(),idVenta == null);
            this.suggestTextField1.requestFocus();
            refrescarValores();
            
        });
        
        this.mapListTextPane1.setFormatter(new MapListTextPane.Formatter() {
            @Override
            public String format(Map<String, Object> m) {
                
                Double precio       = ((Number) m.get("precio")).doubleValue();
                Double cantidad     = ((Number) m.get("cantidad")).doubleValue();
                Double total        = ((Number) m.get("total")).doubleValue();
                Double descuento    = m.get("descuento")==null ? 0d : ((Number) m.get("descuento")).doubleValue() ;
                
                StringBuilder sb = new StringBuilder();
                sb.append(Util.rpad((String) m.get("nombre"), 25, ' ')).append(' ');
                sb.append(Util.lpad("" + cantidad, 5, ' '));
                sb.append(Util.lpad(Util.formatDecimal(precio), 9, ' '));
                if(descuento > 0){
                    sb.append(" -").append(Util.rpad(Util.formatDecimal(descuento),8,' ')).append("");
                }else{
                    sb.append("          ");
                }
                sb.append(Util.lpad(Util.formatDecimal((((Number)m.get("precio")).doubleValue()-descuento)*cantidad), 9, ' '));

                return sb.toString();
            }

            @Override
            public String header() {
                String fecha = (infoVenta==null ? Util.dateTimeToInternalFormat(new Date()): 
                Util.dateTimeToInternalFormat((Date) infoVenta.get("fecha")));
                String asesor = (infoVenta==null ? MinistoreDesktop.getCurrentUser().getNombre(): (String)infoVenta.get("usuario_creo"));
                
                
                 return Resources.p("app.invoice.header").replace("${fecha_y_hora}", fecha)
                         .replace("${nombre_cliente}", tNombre.getText())
                         .replace("${nit_cliente}",tNit.getText())
                         .replace("${asesor}", asesor)
                         + "\n" + Util.rpad("", 59, '-')
                         +"\nProducto                   Cant   Precio              Total";
            }

            @Override
            public String footer() {
                StringBuilder sb = new StringBuilder();
                sb.append("").append(Util.rpad("", 59, '-')).append("\n");
                sb.append(" * Subtotal:     ").append(Util.lpad(tTotal.getText(),      15, ' ')).append("\n");
                sb.append(" * Descuento:    ").append(Util.lpad("- "+tDescuento.getText(),  15, ' ')).append("\n");
                sb.append(" * Total a pagar:").append(Util.lpad(tTotalAPagar.getText(),15, ' ')).append("\n"); 
                sb.append(Resources.p("app.invoice.footer"));
                return sb.toString();
            }
        });

        try{
            this.lEfectivo.setVisible(Resources.p("app.payment.method").contains("CASH"));
            this.tEfectivo.setVisible(Resources.p("app.payment.method").contains("CASH"));

            this.tTarjeta.setVisible(Resources.p("app.payment.method").contains("CARD"));
            this.lTarjeta.setVisible(Resources.p("app.payment.method").contains("CARD"));
        }catch(Exception err){}
        
        
        this.tNit.addKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent ke) {
                String nitNorm = Util.normalizarDocumento(tNit.getText());
                if("CF".equals(nitNorm) || Util.validarNIT(tNit.getText())){
                    tNit.setBorder(BorderFactory.createLineBorder(Color.GREEN));
                }else{
                    tNit.setBorder(BorderFactory.createLineBorder(Color.RED));
                }
                
                if(ke.getKeyCode() == KeyEvent.VK_ENTER){
                    buscarCliente();
                    refrescarValores();
                }
            }
        });
        
        this.tNit.addFocusListener(new FocusAdapter(){
            @Override
            public void focusLost(FocusEvent fe) {
                buscarCliente();
                refrescarValores();
            }
            
        });
        
        
        this.tNombre.addKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent e) {
                refrescarValores();
            }
            
        });
        
        this.suggestTextField1.addKeyListener(new BufferedKeyAction(KeyEvent.VK_CONTROL){
            @Override
            public void keyBufferedReleased(KeyEvent ke, String buff) {
                try {
                    tCantidad.setText(Double.valueOf(buff)+"");
                } catch (Exception e) {
                    tCantidad.setText("1.0");
                }
                
            }
        });
        
        this.mapListTextPane1.addKeyListener(new BufferedKeyAction(KeyEvent.VK_CONTROL){
            @Override
            public void keyBufferedReleased(KeyEvent ke, String bufferedKeys) {
                try {
                    Map<String,Object> info = mapListTextPane1.getSelectedElement();
                    if(info == null) return;
                    Double cantidad  =Double.valueOf(bufferedKeys);
                    info.put("cantidad", cantidad);
                    info.put("total", cantidad*(Double)info.get("precio"));
                    mapListTextPane1.setData(mapListTextPane1.getData());
                    refrescarValores();
                } catch (Exception e) {}
            }
        });
        this.mapListTextPane1.addKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent ke) {
                if(ke.getKeyCode() == KeyEvent.VK_DELETE){
                    eliminarProducto();
                }
            }
            
        });
        
        mostratDatosFacturacion(chbFactura.isSelected());
        
    }

    private void configurarAtajosTeclado() {
        btFinalizar.setText("Finalizar [F1]");
        btAbortarVenta.setText("Abortar [F2]");
        jButton1.setText("Imprimir [F3]");
        btAnularVenta.setText("Anular [F4]");

        registrarAtajo(KeyEvent.VK_F1, "finalizarVenta", btFinalizar);
        registrarAtajo(KeyEvent.VK_F2, "abortarVenta", btAbortarVenta);
        registrarAtajo(KeyEvent.VK_F3, "imprimirVenta", jButton1);
        registrarAtajo(KeyEvent.VK_F4, "anularVenta", btAnularVenta);
    }

    private void registrarAtajo(int keyCode, String actionName, javax.swing.JButton button) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(keyCode, 0), actionName);
        getActionMap().put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (button.isVisible() && button.isEnabled()) {
                    button.doClick();
                }
            }
        });
    }
    
    
    public void setParent(PanelVentas pv){
        this.parent = pv;
    }
    
    
    public void buscarCliente(){
        if (!ventaEnCurso) {
            return;
        }

        String nit = Util.normalizarDocumento(tNit.getText());
        if (!nit.isEmpty()) {
            tNit.setText(nit);
        }

        boolean esCf = "CF".equals(nit);
        Map<String,Object> info = nit.isBlank() ? null : dbclientes.findByDocumento(nit);
        boolean clienteExiste = info != null && !esCf;

        if (clienteExiste) {
            tNombre.setText(valorTexto(info.get("nombre")));
            tCorreo.setText(valorTexto(info.get("email")));
            tTelefono.setText(valorTexto(info.get("telefono")));
            tDireccion.setText(valorTexto(info.get("direccion")));
            habilitarCamposFacturacion(false);
        } else {
            habilitarCamposFacturacion(true);
            if (!esCf && info == null) {
                tNombre.setText("");
                tCorreo.setText("");
                tTelefono.setText("");
                tDireccion.setText("");
            }
        }
    }

    private void habilitarCamposFacturacion(boolean editable) {
        tNombre.setEnabled(editable);
        tCorreo.setEnabled(editable);
        tTelefono.setEnabled(editable);
        tDireccion.setEnabled(editable);
    }

    private static String valorTexto(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
    
    public void refresh(){
        if(products == null || products.isEmpty()){
            products = dbProductos.listActiveOnly(1, 10000000).getData();
        } else {
            actualizarStockProductos(dbProductos.listActiveStockOnly());
        }
        this.suggestTextField1.setValues(products);
    }

    public void fullRefresh(){
        products = null;
        refresh();
    }

    private void actualizarStockProductos(List<Map<String, Object>> stockRows) {
        if (products == null || stockRows == null) {
            return;
        }
        Map<Integer, Object> stockPorId = new HashMap<>();
        for (Map<String, Object> row : stockRows) {
            stockPorId.put(((Number) row.get("id")).intValue(), row.get("stock"));
        }
        for (Map<String, Object> product : products) {
            Object id = product.get("id");
            if (id == null) {
                continue;
            }
            Object stock = stockPorId.get(((Number) id).intValue());
            if (stock != null) {
                product.put("stock", stock);
            }
        }
    }

    
    
    public void iniciarNuevaVenta(){
        this.idVenta = null;
        this.infoVenta = null;
        this.suggestTextField1.setEnabled(true);
        this.ventaEnCurso = true;
        this.items = new ArrayList<>();
        this.suggestTextField1.setText("");
        this.btAbortarVenta.setVisible(true);
        this.btAnularVenta.setVisible(false);
        this.btFinalizar.setVisible(true);
        this.tCambio.setText("0.00");
        this.tCantidad.setText("1.0");
        this.tDescuento.setText("0.00");
        this.tDescuentoSobreTotal.setText("0.00");
        this.tDescuentoDetalle.setText("");
        this.tDireccion.setText("");
        this.tEfectivo.setText("0.00");
        this.tNit.setText("CF");
        this.tNombre.setText("Consumidor final");
        this.tTarjeta.setText("0.00");
        this.tTelefono.setText("");
        this.tTotal.setText("0.00");
        this.tTotalAPagar.setText("0.00");
        this.tCorreo.setText("");
        this.chbFactura.setSelected(false);
        this.mostratDatosFacturacion(false);
        this.mapListTextPane1.setData(new ArrayList<>());
        //insertarLogo();
        this.panelDescuentoSobreTotal.setVisible(false);
        this.cbDescuento.setSelectedItem("No");
        this.cbDescuento.setEnabled("Habilitado".equalsIgnoreCase(Resources.p("desc.x.venta.total")));
        
        
        this.tEfectivo.setEnabled(true);
        this.tTarjeta.setEnabled(true);
        this.tNit.setEnabled(true);
        this.tDescuentoDetalle.setEnabled(true);
        habilitarCamposFacturacion(true);
        
    }
    
    
    public void mostrarVenta(Map<String,Object> info){
        this.idVenta = ((Number)info.get("id")).longValue();
        this.infoVenta = info;
        this.suggestTextField1.setEnabled(false);
        this.ventaEnCurso = false;
        this.items = new ArrayList<>();
        this.suggestTextField1.setText("");
        this.btAbortarVenta.setVisible(false);
        this.btAnularVenta.setVisible("Finalizada".equalsIgnoreCase((String)info.get("estado")));
        this.btFinalizar.setVisible(false);
        
        this.tEfectivo.setEnabled(false);
        this.tTarjeta.setEnabled(false);
        this.tNit.setEnabled(false);
        this.tNombre.setEnabled(false);
        this.tCorreo.setEnabled(false);
        this.tTelefono.setEnabled(false);
        this.tDireccion.setEnabled(false);
        this.tDescuentoDetalle.setEnabled(false);
        
        this.tCambio.setText(Util.formatDecimal((Double)info.get("cambio")));
        this.tCantidad.setText("1.0");
        this.tDescuentoSobreTotal.setText("0.0");
        this.tDescuento.setText(Util.formatDecimal((Double)info.get("descuento")));
        this.tDescuentoDetalle.setText((String)info.get("descuento_detalle"));
        
        this.tEfectivo.setText(Util.formatDecimal((Double)info.get("efectivo")));
        this.tTarjeta.setText(Util.formatDecimal((Double)info.get("tarjeta")));
        this.tTotal.setText(Util.formatDecimal((Double)info.get("total")));
        this.tTotalAPagar.setText(Util.formatDecimal((Double)info.get("total_a_pagar")));
        this.chbEnviar.setSelected("Si".equalsIgnoreCase(info.get("enviar_factura")+""));
        
        
        if (info.get("factura_nit") != null) {
            this.tNit.setText(valorTexto(info.get("factura_nit")));
            this.tNombre.setText(valorTexto(info.get("factura_nombre")));
            this.tTelefono.setText(valorTexto(info.get("factura_telefono")));
            this.tCorreo.setText(valorTexto(info.get("factura_email")));
            this.tDireccion.setText(valorTexto(info.get("factura_direccion")));
        } else {
            Map<String,Object> cli = dbclientes.findById(info.get("cliente_id")+"");
            if(cli == null){
                this.tNit.setText("");
                this.tNombre.setText("Sin cliente asignado");
                this.tTelefono.setText("");
                this.tCorreo.setText("");
                this.tDireccion.setText("");
            }else{
                this.tNit.setText(Util.normalizarDocumento((String) cli.get("documento")));
                this.tNombre.setText(valorTexto(cli.get("nombre")));
                this.tTelefono.setText(valorTexto(cli.get("telefono")));
                this.tCorreo.setText(valorTexto(cli.get("email")));
                this.tDireccion.setText(valorTexto(cli.get("direccion")));
            }
        }
        
        this.chbFactura.setSelected(true);
        this.mostratDatosFacturacion(true);
        
        List<Map<String,Object>> list = dbVentaDetalle.findByVentaId(((Number)info.get("id")).longValue());
        mapListTextPane1.setData(list);
        items = list;
        
        //Colocamos el descuento sobre total
        try{
            this.cbDescuento.setEnabled(false);
            List<Map<String,Object>> desc1 = (new DescXTotalVenta()).findByVentaId(idVenta);
            if(!desc1.isEmpty()){
                Map<String,Object> dinfo1 = desc1.get(0);
                tDescuentoSobreTotal.setText(""+dinfo1.get("monto"));
                tDescuentoDetalle.setText(""+dinfo1.get("detalle"));
                cbDescuento.setSelectedItem(dinfo1.get("tipo"));
                this.panelDescuentoSobreTotal.setVisible(true);
            }else{
                cbDescuento.setSelectedItem("No");
                this.panelDescuentoSobreTotal.setVisible(false);
            }
        }catch(Throwable err){
            MinistoreDesktop.log.err("No se pudo recuperar la informaicón de descuento", err);
        }
    }
    
    
    private void agregarProducto(Map<String,Object> selection){
        try{

            String codigo = (String)selection.get("codigo");
            Map<String,Object> it = getItemPorCodigo(codigo);
            boolean nuevoItem = (it == null);
            if(nuevoItem){
                it = new HashMap<String,Object>();
                it.putAll(selection);
                it.put("cantidad", 0d);
                it.put("precio", it.get("precio_venta"));
                it.put("total", it.get("precio_venta"));
                /** AQUI DEBERIAN IR LOS OTROS DESCUENTOS */
                it.put("descuento", 0d);
            }
            Double agregarCantidad = Double.valueOf(tCantidad.getText());
            tCantidad.setText("1.0");

            Object stockObj = selection.get("stock");
            double stockDisponible = stockObj == null ? 0d : ((Number) stockObj).doubleValue();
            Double cantidad = ((Number) it.get("cantidad")).doubleValue() + agregarCantidad;
            if (cantidad > stockDisponible) {
                JOptionPane.showMessageDialog(this,
                        "Stock insuficiente para " + it.get("nombre")
                                + ". Disponible: " + Util.formatDecimal(stockDisponible)
                                + ", solicitado: " + Util.formatDecimal(cantidad),
                        "Stock insuficiente", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (nuevoItem) {
                this.items.add(it);
            }

            double descuento = it.get("descuento") == null ? 0d : ((Number) it.get("descuento")).doubleValue();
            double precio = ((Number) it.get("precio")).doubleValue();
            Double total = cantidad * (precio - descuento);
            
            it.put("cantidad", cantidad);
            it.put("total", total);
            
            suggestTextField1.setText("");
            refrescarValores();
            this.scrollToBottom();
        }catch(Throwable err){
            JOptionPane.showMessageDialog(this, "No se pudo agregar el producto: "+err.getMessage(),"Error al agregar producto",JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    private void insertarLogo(){
        //insertamos el logo
        int pos = mapListTextPane1.getText().indexOf("${logo}");
        if(MinistoreDesktop.log != null && pos >= 0){
            StyledDocument doc = mapListTextPane1.getStyledDocument();
            ImageIcon icon = new ImageIcon(MinistoreDesktop.logo.getScaledInstance(150, 150, Image.SCALE_SMOOTH));
            Style style = mapListTextPane1.addStyle("Imagen", null);
            StyleConstants.setIcon(style, icon);
            try {
                doc.remove(pos, 7);
                doc.insertString(pos, " ", style);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }
    
    
    private void refrescarValores(){        
        calcularTotal();
        calculaDescuento();
        this.calcularTotalAPagarYCambio();
        mapListTextPane1.setData(items);
    }
    
    
    private Double getCambio(){
        return Double.valueOf(tCambio.getText());
    }
    
    private Double getTotalAPagar(){
        return Double.valueOf(tTotalAPagar.getText());
    }
    
    
    private Double getTotal(){
        return Double.valueOf(tTotal.getText());
    }
    
    private void calcularTotal(){
        Double total = 0.0;
        for(Map<String,Object> m: items){
            total = total + (Double)m.get("precio")*(Double)m.get("cantidad");
        }
        tTotal.setText(Util.formatDecimal(total));
    }
    
    private void calcularTotalAPagarYCambio(){
        Double total = getTotal();
        Double descuento = getDescuento();
        Double totalAPagar = total - descuento;
        if(totalAPagar < 0 ){
            totalAPagar = 0.0;
        }
        tTotalAPagar.setText(Util.formatDecimal(totalAPagar));
        
        
        Double efectivo = 0.0;
        try {
            efectivo = Double.parseDouble(tEfectivo.getText().trim());
            tEfectivo.setBorder(BorderFactory.createLineBorder(Color.GREEN));
        } catch (Exception e) {
            tEfectivo.setBorder(BorderFactory.createLineBorder(Color.RED));
        }
        
        Double tarjeta = 0.0;
        try {
            tarjeta = Double.parseDouble(tTarjeta.getText().trim());
            tTarjeta.setBorder(BorderFactory.createLineBorder(Color.GREEN));
        } catch (Exception e) {
            tTarjeta.setBorder(BorderFactory.createLineBorder(Color.RED));
        }
        
        Double cambio =  efectivo - (totalAPagar-tarjeta);
        tCambio.setText(Util.formatDecimal(cambio));
        
        
        
    }
    
    
    private void calculaDescuento(){
        
        //Si id venta no es null eso significa que 
        //no es venta nueva, por lo que no se calcula
        if(idVenta != null){
            return;
        }
        
        //Calculamos el descuento total
        String text = (String)cbDescuento.getSelectedItem();
        Double monto = 0.0;
        
        tDescuentoSobreTotal.setEditable("Otro monto".equalsIgnoreCase(text));
        tDescuentoSobreTotal.setFocusable("Otro monto".equalsIgnoreCase(text));
        panelDescuentoSobreTotal.setVisible(!"NO".equalsIgnoreCase(text));
        
        if(text.equalsIgnoreCase("Otro monto")){
            try {
                monto = Double.parseDouble(tDescuentoSobreTotal.getText());
            } catch (Exception e) {}
        }else if(text.equalsIgnoreCase("No")){
            tDescuentoSobreTotal.setText(Util.formatDecimal(monto));
        }else if(text.contains("%")){
            panelDescuentoSobreTotal.setVisible(true);
            text = text.replace("%", "").trim();
            monto = ((Double.valueOf(text)/100d)*getTotal());
            tDescuentoSobreTotal.setText(Util.formatDecimal(monto));
        }
        
        //calculamos el descuento por producto
        for(Map<String,Object> p : mapListTextPane1.getData()){
            if(p.get("descuento")!=null){
                monto += ((Double)p.get("descuento"))*((Double)p.get("cantidad"));
            }
        }
        
        tDescuento.setText(Util.formatDecimal(monto));
        
    }
    
    private Double getDescuento(){
        String desc = tDescuento.getText();
        try {
            return Double.parseDouble(desc);
        } catch (Exception e) {
        }
        return 0.0;
    }
    
    
    private Map<String,Object> getItemPorCodigo(String codigo){
        for(Map<String,Object> it : items){
            if(codigo.equals(it.get("codigo"))){
                return it;
            }
        }
        return null;
    }
    
    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = jScrollPane1.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }
    
    private void eliminarProducto() {
        
        Map element = mapListTextPane1.getSelectedElement();
        
        if(element == null) return;
        
        int resp = JOptionPane.showConfirmDialog(this, "¿Eliminar producto "+element.get("nombre")+"?", "Confirmar eliminar producto", JOptionPane.YES_NO_OPTION);
        if(resp == JOptionPane.YES_OPTION){
            mapListTextPane1.getData().remove(element);
            mapListTextPane1.setData(mapListTextPane1.getData());
            refrescarValores();
        }
    }
    
    
    private boolean validarStockVentaContraBD(List<Map<String, Object>> detalle) {
        List<Integer> ids = new ArrayList<>();
        for (Map<String, Object> m : detalle) {
            ids.add(((Number) m.get("id")).intValue());
        }

        List<Map<String, Object>> stockRows = dbProductos.getStockByIds(ids);
        actualizarStockProductos(stockRows);
        this.suggestTextField1.setValues(products);

        Map<Integer, Double> stockPorId = new HashMap<>();
        for (Map<String, Object> row : stockRows) {
            Object stock = row.get("stock");
            stockPorId.put(((Number) row.get("id")).intValue(),
                    stock == null ? 0d : ((Number) stock).doubleValue());
        }

        StringBuilder errores = new StringBuilder();
        for (Map<String, Object> m : detalle) {
            Integer productoId = ((Number) m.get("id")).intValue();
            double cantidad = ((Number) m.get("cantidad")).doubleValue();
            double stock = stockPorId.getOrDefault(productoId, 0d);
            if (cantidad > stock) {
                if (errores.length() > 0) {
                    errores.append('\n');
                }
                errores.append(m.get("nombre"))
                        .append(". Disponible: ")
                        .append(Util.formatDecimal(stock))
                        .append(", en venta: ")
                        .append(Util.formatDecimal(cantidad));
            }
        }

        if (errores.length() > 0) {
            JOptionPane.showMessageDialog(this,
                    "Stock insuficiente para:\n" + errores,
                    "Stock insuficiente", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void finalizarVenta(){
        
        try{
            
            List<Map<String,Object>> detalle = mapListTextPane1.getData();
                    
            if(detalle.isEmpty()){
                JOptionPane.showMessageDialog(this, "Debe agregar al menos un producto antes de finalizar la compra");
                return;
            }
            
            if(getCambio() < 0){
                JOptionPane.showMessageDialog(this, "No se ha cubierto el monto total a pagar con el monto en 'Efectivo' o 'Tarjeta' ingresado");
                tEfectivo.requestFocus();
                return;
            }

            if (!validarStockVentaContraBD(detalle)) {
                return;
            }
        

            //Almacenamos la venta
            Map<String,Object> info = new HashMap<>();

            Date fecha = new Date();
            Double total  = getTotal();
            Map<String, Object> datosFactura = resolverDatosFacturacion();
            String doc = (String) datosFactura.get("factura_nit");
            String hash = generarHash(fecha, total, doc);


            info.put("hash", hash);
            info.put("fecha", fecha);
            info.put("total", total);
            info.put("descuento", getDescuento());
            info.put("descuento_detalle", tDescuentoDetalle.getText());
            info.put("total_a_pagar", getTotalAPagar());
            info.put("efectivo", Double.valueOf(tEfectivo.getText()));
            info.put("tarjeta", Double.valueOf(tTarjeta.getText()));
            info.put("cambio", getCambio());
            info.put("cliente_id", datosFactura.get("cliente_id"));
            info.put("factura_nit", datosFactura.get("factura_nit"));
            info.put("factura_nombre", datosFactura.get("factura_nombre"));
            info.put("factura_email", datosFactura.get("factura_email"));
            info.put("factura_telefono", datosFactura.get("factura_telefono"));
            info.put("factura_direccion", datosFactura.get("factura_direccion"));
            info.put("enviar_factura", chbEnviar.isSelected()? "Si" : "No");
            info.put("estado", "Finalizada");

            Map<String, Object> descuentoSobreTotal = null;
            try {
                Double descSobreTotal = Double.valueOf(tDescuentoSobreTotal.getText());
                if (descSobreTotal > 0) {
                    descuentoSobreTotal = new HashMap<>();
                    descuentoSobreTotal.put("tipo", cbDescuento.getSelectedItem());
                    descuentoSobreTotal.put("monto", descSobreTotal);
                    descuentoSobreTotal.put("detalle", tDescuentoDetalle.getText());
                }
            } catch (Exception e) {
                MinistoreDesktop.log.err("No se pudo preparar el descuento sobre monto total", e);
            }

            dbVentas.registrarVentaFinalizada(info, detalle, descuentoSobreTotal);
            
            this.ventaEnCurso = false;
            this.setVisible(false);

            JOptionPane.showMessageDialog(this, "Venta finalizada exitosamente");

            
        }catch(Throwable err){
            JOptionPane.showMessageDialog(this, "Error: "+err.getMessage());
            MinistoreDesktop.log.err("Error al finalizar la compra", err);
            err.printStackTrace();
        }
        
    }
    
    
    private void abortarVenta(){
        int resp = JOptionPane.showConfirmDialog(this, "¿Está seguro que desea cancelar, se perderán todos los datos de la venta?","Abortar venta",JOptionPane.YES_NO_OPTION);
        if(resp == JOptionPane.YES_OPTION){
            this.ventaEnCurso = false;
            this.setVisible(false);
            PanelVenta.this.parent.lightRefresh();
        }
        
    }
    
    
    private void anularVenta(){
        int resp = JOptionPane.showConfirmDialog(this, "¿Está seguro que desea ANULAR esta venta?, La anulación quedará registrada en el histórico de ventas?","Anular venta",JOptionPane.YES_NO_OPTION);
        if(resp == JOptionPane.YES_OPTION){
            this.ventaEnCurso = false;
            this.setVisible(false);
            try {
                dbVentas.anular(idVenta);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al anular la venta","Error anulacion", JOptionPane.ERROR_MESSAGE);
                MinistoreDesktop.log.err("Error anulando venta", e);
            }
            PanelVenta.this.parent.lightRefresh();
        }
    }
    
    
    private Map<String, Object> resolverDatosFacturacion() {
        Map<String, Object> datos = new HashMap<>();

        if (!chbFactura.isSelected()) {
            String nit = "CF";
            Map<String, Object> cliente = dbclientes.findByDocumento(nit);
            if (cliente == null) {
                Map<String, Object> infoCliente = new HashMap<>();
                infoCliente.put("nombre", "Consumidor final");
                infoCliente.put("documento", nit);
                infoCliente.put("direccion", "Ciudad");
                infoCliente.put("email", "");
                infoCliente.put("telefono", "");
                dbclientes.insert(infoCliente);
                cliente = dbclientes.findByDocumento(nit);
            }
            if (cliente == null) {
                throw new RuntimeException("No fue posible registrar el cliente CF");
            }
            datos.put("factura_nit", nit);
            datos.put("factura_nombre", valorTexto(cliente.get("nombre")));
            datos.put("factura_email", valorTexto(cliente.get("email")));
            datos.put("factura_telefono", valorTexto(cliente.get("telefono")));
            datos.put("factura_direccion", valorTexto(cliente.get("direccion")));
            datos.put("cliente_id", cliente.get("id"));
            return datos;
        }

        String nit = Util.normalizarDocumento(tNit.getText());

        if (!nit.equals("CF") && !Util.validarNIT(nit)) {
            throw new RuntimeException("El NIT ingresado no es válido");
        }

        Map<String, Object> cliente = dbclientes.findByDocumento(nit);
        boolean esCf = "CF".equals(nit);
        boolean clienteExiste = cliente != null && !esCf;

        if (clienteExiste) {
            datos.put("factura_nit", nit);
            datos.put("factura_nombre", valorTexto(cliente.get("nombre")));
            datos.put("factura_email", valorTexto(cliente.get("email")));
            datos.put("factura_telefono", valorTexto(cliente.get("telefono")));
            datos.put("factura_direccion", valorTexto(cliente.get("direccion")));
            datos.put("cliente_id", cliente.get("id"));
            return datos;
        }

        if (tNombre.getText().isBlank()) {
            throw new RuntimeException("Ingrese el nombre del cliente");
        }
        if (tDireccion.getText().isBlank()) {
            throw new RuntimeException("Ingrese la direccion del cliente");
        }

        datos.put("factura_nit", nit);
        datos.put("factura_nombre", tNombre.getText().trim());
        datos.put("factura_email", tCorreo.getText().trim());
        datos.put("factura_telefono", tTelefono.getText().trim());
        datos.put("factura_direccion", tDireccion.getText().trim());

        if (cliente == null) {
            Map<String, Object> infoCliente = new HashMap<>();
            infoCliente.put("nombre", datos.get("factura_nombre"));
            infoCliente.put("documento", nit);
            infoCliente.put("direccion", datos.get("factura_direccion"));
            infoCliente.put("email", datos.get("factura_email"));
            infoCliente.put("telefono", datos.get("factura_telefono"));
            dbclientes.insert(infoCliente);
            cliente = dbclientes.findByDocumento(nit);
        }

        if (cliente == null) {
            throw new RuntimeException("No fue posible registrar el cliente");
        }

        datos.put("cliente_id", cliente.get("id"));
        return datos;
    }
    
    
     public static String generarHash(Date fecha, Double monto, String documento) {
        try {
            String fechaStr = formatForHash.format(fecha);
            String montoStr = String.format(java.util.Locale.US, "%.2f", monto);
            String data = fechaStr + "|" + montoStr + "|" + Util.normalizarDocumento(documento);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generando hash", e);
        }
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        suggestTextField1 = new com.lasopro.ministore.desktop.SuggestTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        mapListTextPane1 = new com.lasopro.ministore.desktop.MapListTextPane();
        panelPagar = new javax.swing.JPanel();
        btFinalizar = new javax.swing.JButton();
        btAbortarVenta = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        lEfectivo = new javax.swing.JLabel();
        tEfectivo = new javax.swing.JTextField();
        lTarjeta = new javax.swing.JLabel();
        tTarjeta = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        tCambio = new javax.swing.JTextField();
        tTotal = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        tDescuento = new javax.swing.JTextField();
        tTotalAPagar = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        chbFactura = new javax.swing.JCheckBox();
        lNit = new javax.swing.JLabel();
        tNombre = new javax.swing.JTextField();
        lNombre = new javax.swing.JLabel();
        tNit = new javax.swing.JTextField();
        lTelefono = new javax.swing.JLabel();
        tTelefono = new javax.swing.JTextField();
        lCorreo = new javax.swing.JLabel();
        tCorreo = new javax.swing.JTextField();
        chbEnviar = new javax.swing.JCheckBox();
        tDireccion = new javax.swing.JTextField();
        lDirecicon = new javax.swing.JLabel();
        btAnularVenta = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        panelDescuentoSobreTotal = new javax.swing.JPanel();
        lDescuentoSobreTotal = new javax.swing.JLabel();
        lPorqueDescuentoSobreTotal = new javax.swing.JLabel();
        tDescuentoDetalle = new javax.swing.JTextField();
        tDescuentoSobreTotal = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        cbDescuento = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        tCantidad = new javax.swing.JTextField();

        setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setText("Cod / Nom Producto:");

        suggestTextField1.setToolTipText("Escribir código o nombre del producto");
        suggestTextField1.setFont(new java.awt.Font("Noto Sans", 0, 18)); // NOI18N
        suggestTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                suggestTextField1ActionPerformed(evt);
            }
        });

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        mapListTextPane1.setBackground(new java.awt.Color(255, 255, 255));
        mapListTextPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        mapListTextPane1.setFont(new java.awt.Font("Monospaced", 0, 16)); // NOI18N
        jScrollPane1.setViewportView(mapListTextPane1);

        panelPagar.setBackground(new java.awt.Color(255, 255, 255));
        panelPagar.setBorder(javax.swing.BorderFactory.createTitledBorder("Pagar"));

        btFinalizar.setBackground(new java.awt.Color(0, 255, 0));
        btFinalizar.setText("Finalizar");
        btFinalizar.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 5, 3, 5));
        btFinalizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btFinalizarActionPerformed(evt);
            }
        });

        btAbortarVenta.setBackground(new java.awt.Color(255, 0, 51));
        btAbortarVenta.setForeground(new java.awt.Color(255, 255, 255));
        btAbortarVenta.setText("Abortar");
        btAbortarVenta.setToolTipText("Abortar venta");
        btAbortarVenta.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 5, 3, 5));
        btAbortarVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAbortarVentaActionPerformed(evt);
            }
        });

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel2.setText("Total: ");

        lEfectivo.setText("Efectivo:");

        tEfectivo.setBackground(new java.awt.Color(255, 255, 153));
        tEfectivo.setFont(new java.awt.Font("Noto Sans", 1, 18)); // NOI18N
        tEfectivo.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tEfectivo.setText("0");
        tEfectivo.setToolTipText("Ingrese el monto pagado en Efectivo");
        tEfectivo.setMinimumSize(new java.awt.Dimension(64, 35));
        tEfectivo.setPreferredSize(new java.awt.Dimension(64, 35));
        tEfectivo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tEfectivoFocusGained(evt);
            }
        });
        tEfectivo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tEfectivoKeyReleased(evt);
            }
        });

        lTarjeta.setText("Tarjeta:");

        tTarjeta.setBackground(new java.awt.Color(255, 255, 204));
        tTarjeta.setFont(new java.awt.Font("Noto Sans", 1, 18)); // NOI18N
        tTarjeta.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tTarjeta.setText("0.00");
        tTarjeta.setToolTipText("Ingrese el monto pagado con Tarjeta");
        tTarjeta.setMinimumSize(new java.awt.Dimension(64, 35));
        tTarjeta.setPreferredSize(new java.awt.Dimension(64, 35));

        jLabel10.setText("Cambio:");

        tCambio.setEditable(false);
        tCambio.setBackground(new java.awt.Color(255, 255, 255));
        tCambio.setFont(new java.awt.Font("Noto Sans", 1, 18)); // NOI18N
        tCambio.setForeground(new java.awt.Color(153, 0, 0));
        tCambio.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tCambio.setText("0.00");
        tCambio.setToolTipText("Cambio o Vuelto");
        tCambio.setFocusable(false);
        tCambio.setMinimumSize(new java.awt.Dimension(64, 35));
        tCambio.setPreferredSize(new java.awt.Dimension(64, 35));

        tTotal.setEditable(false);
        tTotal.setBackground(new java.awt.Color(255, 255, 255));
        tTotal.setFont(new java.awt.Font("Noto Sans", 0, 18)); // NOI18N
        tTotal.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tTotal.setText("0");
        tTotal.setToolTipText("Monto total general");
        tTotal.setFocusable(false);
        tTotal.setMinimumSize(new java.awt.Dimension(64, 35));
        tTotal.setPreferredSize(new java.awt.Dimension(35, 35));

        jLabel3.setText("Descuentos:");

        tDescuento.setEditable(false);
        tDescuento.setBackground(new java.awt.Color(255, 255, 255));
        tDescuento.setFont(new java.awt.Font("Noto Sans", 0, 18)); // NOI18N
        tDescuento.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tDescuento.setText("0");
        tDescuento.setToolTipText("Descuento aplicado a la compra");
        tDescuento.setFocusable(false);
        tDescuento.setMinimumSize(new java.awt.Dimension(64, 35));
        tDescuento.setPreferredSize(new java.awt.Dimension(35, 35));
        tDescuento.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tDescuentoKeyReleased(evt);
            }
        });

        tTotalAPagar.setEditable(false);
        tTotalAPagar.setBackground(new java.awt.Color(255, 255, 255));
        tTotalAPagar.setFont(new java.awt.Font("Noto Sans", 1, 18)); // NOI18N
        tTotalAPagar.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tTotalAPagar.setToolTipText("Cantidad total a pagar con el descuento");
        tTotalAPagar.setFocusable(false);
        tTotalAPagar.setMinimumSize(new java.awt.Dimension(64, 35));
        tTotalAPagar.setPreferredSize(new java.awt.Dimension(35, 35));

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel5.setText("A Pagar: ");

        chbFactura.setText("Factura");
        chbFactura.setToolTipText("Marque si el cliente desea factura");
        chbFactura.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chbFacturaActionPerformed(evt);
            }
        });

        lNit.setText("NIT:");

        tNombre.setText("Consumidor Final");
        tNombre.setToolTipText("Nombre del cliente");

        lNombre.setText("Nombre:");

        tNit.setText("CF");
        tNit.setToolTipText("Número de Identificación Tributaria");
        tNit.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tNitFocusGained(evt);
            }
        });

        lTelefono.setText("Teléfono:");

        tTelefono.setToolTipText("Teléfono del cliente");

        lCorreo.setText("Correo:");

        tCorreo.setToolTipText("Correo del cliente");

        chbEnviar.setText("Enviar");
        chbEnviar.setToolTipText("Marcar si el cliente desea que se le envíe la factura por mail o por teléfono");

        tDireccion.setText("Ciudad");
        tDireccion.setToolTipText("Correo del cliente");

        lDirecicon.setText("Direccion:");

        btAnularVenta.setBackground(new java.awt.Color(255, 0, 51));
        btAnularVenta.setForeground(new java.awt.Color(255, 255, 255));
        btAnularVenta.setText("Anular");
        btAnularVenta.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 5, 3, 5));
        btAnularVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAnularVentaActionPerformed(evt);
            }
        });

        jButton1.setBackground(new java.awt.Color(0, 51, 204));
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Imprimir");
        jButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setMaximumSize(new java.awt.Dimension(59, 24));
        jButton1.setMinimumSize(new java.awt.Dimension(59, 24));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        panelDescuentoSobreTotal.setBackground(new java.awt.Color(252, 252, 255));

        lDescuentoSobreTotal.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lDescuentoSobreTotal.setText("Monto del descuento:");

        lPorqueDescuentoSobreTotal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lPorqueDescuentoSobreTotal.setText("¿Porqué se aplica el descuento?");

        tDescuentoSobreTotal.setFont(new java.awt.Font("Noto Sans", 1, 18)); // NOI18N
        tDescuentoSobreTotal.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        tDescuentoSobreTotal.setText("0");
        tDescuentoSobreTotal.setToolTipText("Descuento aplicado a la compra");
        tDescuentoSobreTotal.setFocusable(false);
        tDescuentoSobreTotal.setMinimumSize(new java.awt.Dimension(64, 35));
        tDescuentoSobreTotal.setPreferredSize(new java.awt.Dimension(64, 35));
        tDescuentoSobreTotal.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tDescuentoSobreTotalFocusGained(evt);
            }
        });
        tDescuentoSobreTotal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tDescuentoSobreTotalKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout panelDescuentoSobreTotalLayout = new javax.swing.GroupLayout(panelDescuentoSobreTotal);
        panelDescuentoSobreTotal.setLayout(panelDescuentoSobreTotalLayout);
        panelDescuentoSobreTotalLayout.setHorizontalGroup(
            panelDescuentoSobreTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lPorqueDescuentoSobreTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelDescuentoSobreTotalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelDescuentoSobreTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDescuentoSobreTotalLayout.createSequentialGroup()
                        .addComponent(lDescuentoSobreTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tDescuentoSobreTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(tDescuentoDetalle, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        panelDescuentoSobreTotalLayout.setVerticalGroup(
            panelDescuentoSobreTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDescuentoSobreTotalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelDescuentoSobreTotalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tDescuentoSobreTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lDescuentoSobreTotal))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lPorqueDescuentoSobreTotal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tDescuentoDetalle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel4.setText("Aplicar descuento:");

        cbDescuento.setFont(new java.awt.Font("Noto Sans", 0, 18)); // NOI18N
        cbDescuento.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "No", "5%", "10%", "15%", "20%", "25%", "30%", "Otro monto" }));
        cbDescuento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbDescuentoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelPagarLayout = new javax.swing.GroupLayout(panelPagar);
        panelPagar.setLayout(panelPagarLayout);
        panelPagarLayout.setHorizontalGroup(
            panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelDescuentoSobreTotal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(panelPagarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator2)
                    .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelPagarLayout.createSequentialGroup()
                        .addGroup(panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lEfectivo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lTarjeta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tDescuento, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tTotalAPagar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tEfectivo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tTarjeta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tCambio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(panelPagarLayout.createSequentialGroup()
                        .addGroup(panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lNit, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lDirecicon, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(chbFactura))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelPagarLayout.createSequentialGroup()
                                .addComponent(chbEnviar)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(tNit)
                            .addComponent(tNombre)
                            .addComponent(tTelefono)
                            .addComponent(tCorreo)
                            .addComponent(tDireccion)))
                    .addGroup(panelPagarLayout.createSequentialGroup()
                        .addGroup(panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelPagarLayout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                                .addComponent(cbDescuento, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelPagarLayout.createSequentialGroup()
                                .addGroup(panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btAbortarVenta, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btFinalizar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btAnularVenta, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addContainerGap())))
        );
        panelPagarLayout.setVerticalGroup(
            panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPagarLayout.createSequentialGroup()
                .addGroup(panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(cbDescuento, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelDescuentoSobreTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel2)
                    .addComponent(tTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel3)
                    .addComponent(tDescuento, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(tTotalAPagar, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lEfectivo)
                    .addComponent(tEfectivo, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lTarjeta)
                    .addComponent(tTarjeta, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel10)
                    .addComponent(tCambio, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chbFactura)
                    .addComponent(chbEnviar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lNit)
                    .addComponent(tNit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lNombre)
                    .addComponent(tNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lCorreo)
                    .addComponent(tCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lDirecicon)
                    .addComponent(tDireccion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btAbortarVenta, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                    .addComponent(btFinalizar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPagarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btAnularVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel6.setText("#");

        tCantidad.setEditable(false);
        tCantidad.setFont(new java.awt.Font("Noto Sans", 0, 18)); // NOI18N
        tCantidad.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        tCantidad.setText("1");
        tCantidad.setToolTipText("Cantidad a agregar (Ctrl + 1, Ctrl + 2, Ctrl + 3, etc)");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addGap(18, 18, 18))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(suggestTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addComponent(panelPagar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tCantidad, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addComponent(suggestTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(10, 10, 10)
                .addComponent(jScrollPane1)
                .addContainerGap())
            .addComponent(panelPagar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void suggestTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_suggestTextField1ActionPerformed
        agregarProducto(((SuggestTextField)evt.getSource()).getSelected());
        this.refrescarValores();
    }//GEN-LAST:event_suggestTextField1ActionPerformed

    private void cbDescuentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbDescuentoActionPerformed
       this.refrescarValores();
    }//GEN-LAST:event_cbDescuentoActionPerformed

    private void tDescuentoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tDescuentoKeyReleased
        this.refrescarValores();
    }//GEN-LAST:event_tDescuentoKeyReleased

    private void tEfectivoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tEfectivoKeyReleased
        this.refrescarValores();
    }//GEN-LAST:event_tEfectivoKeyReleased

    private void btAbortarVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAbortarVentaActionPerformed
        abortarVenta();
    }//GEN-LAST:event_btAbortarVentaActionPerformed

    private void chbFacturaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chbFacturaActionPerformed
        mostratDatosFacturacion(chbFactura.isSelected());
        if (chbFactura.isSelected()) {
            buscarCliente();
            refrescarValores();
        }
    }//GEN-LAST:event_chbFacturaActionPerformed

    private void btFinalizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btFinalizarActionPerformed
        btFinalizar.setEnabled(false);
        btAbortarVenta.setEnabled(false);
        MinistoreDesktop.runInThread("Procesando venta...",()->{
            try{
                finalizarVenta();
                PanelVenta.this.parent.lightRefresh();
            }catch(Throwable err){}
            btFinalizar.setEnabled(true);
            btAbortarVenta.setEnabled(true);
        });
        
        
    }//GEN-LAST:event_btFinalizarActionPerformed

    private void btAnularVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAnularVentaActionPerformed
        anularVenta();
    }//GEN-LAST:event_btAnularVentaActionPerformed

    private void tEfectivoFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tEfectivoFocusGained
        this.tEfectivo.select(0, this.tEfectivo.getText().length());
    }//GEN-LAST:event_tEfectivoFocusGained

    private void tNitFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tNitFocusGained
        this.tNit.select(0, this.tNit.getText().length());
    }//GEN-LAST:event_tNitFocusGained

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            imprimir(mapListTextPane1);
        } catch (Exception e) {
            MinistoreDesktop.log.err("Error al imprimir", e);
            JOptionPane.showMessageDialog(this, "Error al enviar la impresion: "+e.getMessage(),"Error de impresion",JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void tDescuentoSobreTotalKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tDescuentoSobreTotalKeyReleased
        this.refrescarValores();
    }//GEN-LAST:event_tDescuentoSobreTotalKeyReleased

    private void tDescuentoSobreTotalFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tDescuentoSobreTotalFocusGained
        try {
            ((JTextField)evt.getSource()).select(0, ((JTextField)evt.getSource()).getText().length());
        } catch (Exception e) {}
    }//GEN-LAST:event_tDescuentoSobreTotalFocusGained


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAbortarVenta;
    private javax.swing.JButton btAnularVenta;
    private javax.swing.JButton btFinalizar;
    private javax.swing.JComboBox<String> cbDescuento;
    private javax.swing.JCheckBox chbEnviar;
    private javax.swing.JCheckBox chbFactura;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel lCorreo;
    private javax.swing.JLabel lDescuentoSobreTotal;
    private javax.swing.JLabel lDirecicon;
    private javax.swing.JLabel lEfectivo;
    private javax.swing.JLabel lNit;
    private javax.swing.JLabel lNombre;
    private javax.swing.JLabel lPorqueDescuentoSobreTotal;
    private javax.swing.JLabel lTarjeta;
    private javax.swing.JLabel lTelefono;
    private com.lasopro.ministore.desktop.MapListTextPane mapListTextPane1;
    private javax.swing.JPanel panelDescuentoSobreTotal;
    private javax.swing.JPanel panelPagar;
    private com.lasopro.ministore.desktop.SuggestTextField suggestTextField1;
    private javax.swing.JTextField tCambio;
    private javax.swing.JTextField tCantidad;
    private javax.swing.JTextField tCorreo;
    private javax.swing.JTextField tDescuento;
    private javax.swing.JTextField tDescuentoDetalle;
    private javax.swing.JTextField tDescuentoSobreTotal;
    private javax.swing.JTextField tDireccion;
    private javax.swing.JTextField tEfectivo;
    private javax.swing.JTextField tNit;
    private javax.swing.JTextField tNombre;
    private javax.swing.JTextField tTarjeta;
    private javax.swing.JTextField tTelefono;
    private javax.swing.JTextField tTotal;
    private javax.swing.JTextField tTotalAPagar;
    // End of variables declaration//GEN-END:variables

    void focusOnInputText() {
        this.suggestTextField1.requestFocus();
    }

    private void mostratDatosFacturacion(boolean selected) {
        lNit.setVisible(selected);
        lNombre.setVisible(selected);
        lCorreo.setVisible(selected);
        lTelefono.setVisible(selected);
        tNit.setVisible(selected);
        tNombre.setVisible(selected);
        tCorreo.setVisible(selected);
        tTelefono.setVisible(selected);
        chbEnviar.setVisible(selected);
        tDireccion.setVisible(selected);
        lDirecicon.setVisible(selected);
    }
    
    public boolean isVentaEnCurso(){
        return this.ventaEnCurso;
    }
    
    

    
    public static void imprimir(JTextPane original)
        throws PrinterException {

        // 1. Clonar documento (mantiene imágenes y estilos)
        JTextPane printPane = new JTextPane();
        StyledDocument printDoc =
                clonarDocumentoConImagenes(original.getStyledDocument());
        printPane.setDocument(printDoc);

        // 2. Fuente más pequeña solo para impresión
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontSize(attrs, Integer.parseInt(Resources.p("app.print.font.size")));
        printDoc.setCharacterAttributes(0, printDoc.getLength(), attrs, false);
        printPane.print();
       
////        // 3. Configurar impresión
////        PrinterJob job = PrinterJob.getPrinterJob();
////        PageFormat pf = job.defaultPage();
////
////        // Tamaño CARTA: 8.5 x 11 pulgadas
////        Paper paper = new Paper();
////        paper.setSize(612, 792); // puntos (72 * pulgadas)
////
////        // Márgenes (0.5 pulgadas)
////        paper.setImageableArea(36, 36, 540, 720);
////
////        pf.setPaper(paper);
////        pf.setOrientation(PageFormat.LANDSCAPE);
////
////        job.setPrintable(printPane.getPrintable(null, null), pf);
////
////        if (job.printDialog()) {
////            job.print();
////        }
    }

    
    public static StyledDocument clonarDocumentoConImagenes(StyledDocument src) {

        StyledDocument dst = new DefaultStyledDocument();

        try {
            int length = src.getLength();
            int offset = 0;

            while (offset < length) {
                Element elem = src.getCharacterElement(offset);
                int start = elem.getStartOffset();
                int end   = elem.getEndOffset();

                AttributeSet attrs = elem.getAttributes();

                String text = src.getText(start, end - start);
                dst.insertString(dst.getLength(), text, attrs);

                offset = end;
            }
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }

        return dst;
    } 


    
    
}
