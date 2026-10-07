package com.lasopro.ministore.desktop;

import com.lasopro.ministore.db.Categorias;
import com.lasopro.ministore.db.HistoricoInventario;
import com.lasopro.ministore.db.Productos;
import com.lasopro.ministore.db.Proveedores;
import com.lasopro.ministore.db.VentaDetalle;
import com.lasopro.ministore.util.Util;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author williams
 */
public class DialogProducto extends javax.swing.JDialog {
    
    private static DialogProducto me;
    
    private Productos db;
    private HistoricoInventario historicoDb;
    private VentaDetalle ventaDetalleDb;
    private DialogHistoricoPedido dialogHistoricoPedido;
    
    private Integer idProducto;
    private Double originalStock;
    private JTextArea tCambiosStock;
    private JTextArea tVentas;
    private JPanel panelDerecho;
    private javax.swing.JButton btToggleHistorial;
    private boolean panelDerechoVisible;

    public static final int UPDATED=1;
    public static final int INSERTED=2;
    public static final int DELETED=3;
    public static final int CANCELED=4;


    private int result;


    /**
     * Creates new form PanelEditAddProducto
     * @param parent
     * @param modal
     */
    public DialogProducto(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setupPanelesInfo();
        db = new Productos();
        historicoDb = new HistoricoInventario();
        ventaDetalleDb = new VentaDetalle();
        dialogHistoricoPedido = new DialogHistoricoPedido(parent);
    }
    
    
    public static DialogProducto getInst(){
        if(me == null){
            me = new DialogProducto(MinistoreDesktop.getMainFrame(), true);
            me.setTitle("Agregar / Modificar producto");
            me.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
            me.setLocationByPlatform(true);
            
        }
        return me;
    }
    

    public Integer getIdProducto(){
        return idProducto;
    }
    
    
    private void guardar(){
        
        Map<String,Object> info  = new HashMap<>();
        
        StringBuilder sb = new StringBuilder();
        
        if(tCodigo.getText().isBlank()){
            sb.append(" - El campo 'Codigo' no debe estar vacío\n");
        }
        
        if(tNombre.getText().isBlank()){
            sb.append(" - El campo 'Nombre' no debe estar vacío\n");
        }
        
        try {
            Double.parseDouble(tStock.getText());
        } catch (Exception e) {
            sb.append(" - El campo Stock debe ser un valor numerico\n");
        }
        
        try {
            Double.parseDouble(tStockMinimo.getText());
        } catch (Exception e) {
            sb.append(" - El campo Stock Minimo debe ser un valor numerico\n");
        }
        
        try {
            Double.parseDouble(tPrecioCompra.getText());
        } catch (Exception e) {
            sb.append(" - El campo 'Precio compra' debe ser un valor decimal\n");
        }
        
        try {
            Double.parseDouble(tPrecioVenta.getText());
        } catch (Exception e) {
            sb.append(" - El campo 'Precio venta' debe ser un valor decimal\n");
        }
        
        if(!sb.isEmpty()){
            JOptionPane.showMessageDialog(this, "Errores de validación: \n\n"+sb.toString(), "Errores de validación", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        
        info.put("codigo", tCodigo.getText());
        info.put("codigo_proveedor", tCodigoProveedor.getText().trim());
        info.put("descripcion", tDescripcion.getText());
        info.put("marca", tMarca.getText());
        info.put("nombre", tNombre.getText());
        info.put("precio_compra", Double.valueOf(tPrecioCompra.getText()));
        info.put("precio_venta", Double.valueOf(tPrecioVenta.getText()));
        info.put("stock", Double.valueOf(tStock.getText()));
        info.put("min_stock", Double.valueOf(tStockMinimo.getText()));
        info.put("categoria_id", ((ComboBoxItem)cbCategoria.getSelectedItem()).getId());
        info.put("proveedor_id", ((ComboBoxItem)cbProveedor.getSelectedItem()).getId());
        info.put("activo", cbEstado.getSelectedItem());
        
        Double nuevoStock = Double.valueOf(tStock.getText());
        Map<String, Object> historicoInfo = null;
        if (requiereHistorico(nuevoStock)) {
            Double stockActual = idProducto == null ? 0.0 : originalStock;
            historicoInfo = dialogHistoricoPedido.showDialog(stockActual, nuevoStock);
            if (historicoInfo == null) {
                return;
            }
            historicoInfo.put("accion", idProducto == null ? "NUEVO_PRODUCTO" : "CAMBIO_STOCK");
            historicoInfo.put("user", MinistoreDesktop.getCurrentUser().getUser());
        }
        
        if(idProducto == null){
            db.insert(info);
            result = INSERTED;
            Map<String, Object> producto = db.findByCodigo(tCodigo.getText());
            if (producto != null && producto.get("id") != null) {
                idProducto = ((Number) producto.get("id")).intValue();
            }
        }else{
            info.put("id", idProducto);
            db.update(info);
            result = UPDATED;
        }
        
        if (historicoInfo != null) {
            historicoInfo.put("producto_id", idProducto);
            historicoDb.insert(historicoInfo);
        }
        
        setVisible(false);
        
    }
    
    
    private boolean requiereHistorico(Double nuevoStock) {
        if (idProducto == null) {
            return true;
        }
        if (originalStock == null) {
            return false;
        }
        return !originalStock.equals(nuevoStock);
    }
    
    
    public int showEditDialog(Integer id){
        result = CANCELED;
        Map<String,Object> info = db.findById( id );
        this.idProducto = ((Number)info.get("id")).intValue();
        this.tCodigo.setText((String)info.get("codigo"));
        this.tCodigoProveedor.setText(info.get("codigo_proveedor") == null ? "" : String.valueOf(info.get("codigo_proveedor")));
        this.tDescripcion.setText((String)info.get("descripcion"));
        this.tMarca.setText((String)info.get("marca"));
        this.tNombre.setText((String)info.get("nombre"));
        this.tPrecioCompra.setText(info.get("precio_compra")+"");
        this.tPrecioVenta.setText(info.get("precio_venta")+"");
        this.tStock.setText(((Number)info.get("stock")).intValue()+"");
        this.tStockMinimo.setText((Number)info.get("min_stock")+"");
        this.originalStock = ((Number) info.get("stock")).doubleValue();
        this.lTitulo.setText("Modificar producto");
        this.bAceptar.setText("Modificar");
        this.bEliminar.setVisible(true);
        if(info.get("categoria_id") != null){
            this.cbCategoria.setSelectedItem(new ComboBoxItem(info.get("categoria_id")));
        }else{
            this.cbCategoria.setSelectedItem(null);
        }
        if(info.get("proveedor_id") != null){
            this.cbProveedor.setSelectedItem(new ComboBoxItem(info.get("proveedor_id")));
        }else{
            this.cbProveedor.setSelectedItem(null);
        }
        this.cbEstado.setSelectedItem(info.get("activo"));
        

        resetPanelDerecho();
        this.setVisible(true);
        return result;
    }
    
    public int showNewDialog(){
        result = CANCELED;
        this.idProducto = null;
        this.originalStock = null;
        this.tCodigo.setText(db.getNextProductCode());
        this.tCodigo.select(0, this.tCodigo.getText().length());
        this.tCodigoProveedor.setText("");
        this.tDescripcion.setText("");
        this.tMarca.setText("");
        this.tNombre.setText("");
        this.tPrecioCompra.setText("");
        this.tPrecioVenta.setText("");
        this.tStock.setText("");
        this.tStockMinimo.setText("0");
        this.lTitulo.setText("Agregar nuevo producto");
        this.bAceptar.setText("Agregar");
        this.bEliminar.setVisible(false);
        cargarPanelesInfo(null);
        resetPanelDerecho();
        this.setVisible(true);
        return result;
    }

    private void resetPanelDerecho() {
        panelDerechoVisible = false;
        if (panelDerecho != null) {
            panelDerecho.setVisible(false);
        }
        if (btToggleHistorial != null) {
            btToggleHistorial.setText("Ver historial »");
        }
        pack();
    }

    private void togglePanelDerecho() {
        panelDerechoVisible = !panelDerechoVisible;
        panelDerecho.setVisible(panelDerechoVisible);
        btToggleHistorial.setText(panelDerechoVisible ? "Ocultar historial «" : "Ver historial »");
        
        
        if(panelDerechoVisible) cargarPanelesInfo(idProducto);
        
        pack();
    }

    private void setupPanelesInfo() {
        tCambiosStock = new JTextArea();
        tCambiosStock.setEditable(false);
        tCambiosStock.setLineWrap(false);
        //tCambiosStock.setWrapStyleWord(true);

        tVentas = new JTextArea();
        tVentas.setEditable(false);
        tVentas.setLineWrap(false);
        //tVentas.setWrapStyleWord(true);

        JScrollPane scrollCambios = new JScrollPane(tCambiosStock);
        JScrollPane scrollVentas = new JScrollPane(tVentas);
        scrollCambios.setPreferredSize(new Dimension(400, 200));
        scrollVentas.setPreferredSize(new Dimension(400, 200));

        JPanel panelCambios = new JPanel(new BorderLayout(0, 4));
        panelCambios.setBackground(java.awt.Color.WHITE);
        panelCambios.add(new JLabel("Cambios stock"), BorderLayout.NORTH);
        panelCambios.add(scrollCambios, BorderLayout.CENTER);

        JPanel panelVentas = new JPanel(new BorderLayout(0, 4));
        panelVentas.setBackground(java.awt.Color.WHITE);
        panelVentas.add(new JLabel("Ventas"), BorderLayout.NORTH);
        panelVentas.add(scrollVentas, BorderLayout.CENTER);

        panelDerecho = new JPanel(new GridLayout(2, 1, 0, 8));
        panelDerecho.setBackground(java.awt.Color.WHITE);
        panelDerecho.setPreferredSize(new Dimension(350, 0));
        panelDerecho.add(panelCambios);
        panelDerecho.add(panelVentas);
        panelDerecho.setVisible(false);

        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout(12, 0));
        getContentPane().add(jPanel1, BorderLayout.CENTER);
        getContentPane().add(panelDerecho, BorderLayout.EAST);
        pack();
    }

    private void cargarPanelesInfo(Integer productoId) {
        if (productoId == null) {
            tCambiosStock.setText("");
            tVentas.setText("");
            return;
        }

        StringBuilder cambios = new StringBuilder();
        
        cambios.append("Fecha       ");
        cambios.append("Pedido      ");
        cambios.append("Actual ");
        cambios.append("Nuevo  ");
        cambios.append("\n");
        
        for (Map<String, Object> registro : historicoDb.listByProductoId(productoId)) {
            cambios.append(Util.rpad(Util.dateToLocaleString((Date)registro.get("fecha")), 10, ' ')).append("  ");
            cambios.append(registro.get("numero_pedido") == null ? "" : Util.rpad((String)registro.get("numero_pedido"),12,' '));
            cambios.append(Util.rpad(formatStock(registro.get("stock_actual")),7,' '));
            cambios.append(Util.rpad(formatStock(registro.get("nuevo_stock")),7,' '));
            cambios.append("\n");
        }
        if (cambios.isEmpty()) {
            cambios.append("Sin cambios de stock registrados.");
        }
        tCambiosStock.setText(cambios.toString());
        tCambiosStock.setCaretPosition(0);

        StringBuilder ventas = new StringBuilder();
        
        ventas.append("Fecha       ");
        ventas.append("Ventas ");
        ventas.append("\n");
        
        for (Map<String, Object> venta : ventaDetalleDb.listFinalizedSalesByProductoId(productoId, 100)) {
            ventas.append(Util.rpad(Util.dateToLocaleString((Date)venta.get("fecha")), 10, ' ')).append("  ");
            ventas.append(Util.rpad(formatStock(venta.get("cantidad")),7,' '));
            ventas.append("\n");
        }
        if (ventas.isEmpty()) {
            ventas.append("Sin ventas finalizadas registradas.");
        }
        tVentas.setText(ventas.toString());
        tVentas.setCaretPosition(0);
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
    
    public void fillComboBox(List<Map<String,Object>> categorias, List<Map<String,Object>> proveedores){
        cbCategoria.removeAllItems();
        cbProveedor.removeAllItems();
        categorias.forEach((m)->{cbCategoria.addItem(new ComboBoxItem(m.get("id"), m, (String)m.get("nombre")));});
        proveedores.forEach((m)->{cbProveedor.addItem(new ComboBoxItem(m.get("id"), m, (String)m.get("nombre")));});
        dialogHistoricoPedido.fillProveedores(proveedores);
        
    }
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField4 = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        lTitulo = new javax.swing.JLabel();
        bAceptar = new javax.swing.JButton();
        bCancelar = new javax.swing.JButton();
        bEliminar = new javax.swing.JButton();
        tCodigo = new javax.swing.JTextField();
        tCodigoProveedor = new javax.swing.JTextField();
        tNombre = new javax.swing.JTextField();
        tDescripcion = new javax.swing.JTextField();
        tMarca = new javax.swing.JTextField();
        tPrecioCompra = new javax.swing.JTextField();
        tPrecioVenta = new javax.swing.JTextField();
        tStock = new javax.swing.JTextField();
        cbCategoria = new javax.swing.JComboBox<>();
        cbProveedor = new javax.swing.JComboBox<>();
        cbEstado = new javax.swing.JComboBox<>();
        jLabel12 = new javax.swing.JLabel();
        tStockMinimo = new javax.swing.JTextField();

        jTextField4.setText("jTextField4");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setText("Código:");

        jLabel13.setText("Código proveedor:");

        jLabel3.setText("Nombre:");

        jLabel4.setText("Descripción:");

        jLabel5.setText("Marca:");

        jLabel6.setText("Precio compra:");

        jLabel7.setText("Precio venta:");

        jLabel8.setText("Stock:");

        jLabel10.setText("Categoria:");

        jLabel9.setText("Proveedor:");

        jLabel11.setText("Estado:");

        lTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lTitulo.setText("Agregar / Editar, producto");

        bAceptar.setText("Acpetar");
        bAceptar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bAceptarActionPerformed(evt);
            }
        });

        bCancelar.setText("Cancelar");
        bCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bCancelarActionPerformed(evt);
            }
        });

        bEliminar.setText("Eliminar");
        bEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bEliminarActionPerformed(evt);
            }
        });

        btToggleHistorial = new javax.swing.JButton();
        btToggleHistorial.setText("Ver historial »");
        btToggleHistorial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                togglePanelDerecho();
            }
        });

        cbEstado.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Activo", "Inactivo" }));

        jLabel12.setText("Stock minimo:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lTitulo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(158, 236, Short.MAX_VALUE)
                        .addComponent(btToggleHistorial)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bEliminar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bAceptar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bCancelar))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10)
                            .addComponent(jLabel8)
                            .addComponent(jLabel7)
                            .addComponent(jLabel6)
                            .addComponent(jLabel5)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2)
                            .addComponent(jLabel13)
                            .addComponent(jLabel12))
                        .addGap(47, 47, 47)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(tStockMinimo)
                            .addComponent(tStock, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tPrecioVenta, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tPrecioCompra, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tMarca, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tDescripcion, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tNombre, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tCodigoProveedor, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tCodigo, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbEstado, javax.swing.GroupLayout.Alignment.LEADING, 0, 341, Short.MAX_VALUE)
                            .addComponent(cbCategoria, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbProveedor, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(18, 18, 18))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lTitulo)
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(tCodigoProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(tNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(tDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(tMarca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(tPrecioCompra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(tPrecioVenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(tStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addComponent(tStockMinimo, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addGap(12, 12, 12)
                        .addComponent(jLabel9)
                        .addGap(12, 12, 12)
                        .addComponent(jLabel11))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(cbCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btToggleHistorial)
                    .addComponent(bAceptar)
                    .addComponent(bCancelar)
                    .addComponent(bEliminar))
                .addGap(11, 11, 11))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bAceptarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bAceptarActionPerformed
        try{
            guardar();
            
        }catch(Throwable err){
            MinistoreDesktop.log.err("Error al agregar/modificar el producto", err);
            JOptionPane.showMessageDialog(this, "Error al agregar/modificar el producto: "+err.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_bAceptarActionPerformed

    private void bCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bCancelarActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_bCancelarActionPerformed

    private void bEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bEliminarActionPerformed
        try{
            
            int res = JOptionPane.showConfirmDialog(this, "¿Confirme si desea eliminar el producto seleccionado", "Confirmar eliminar", JOptionPane.YES_NO_CANCEL_OPTION);
            
            if(res == JOptionPane.YES_OPTION){
                db.delete(idProducto);
                result = DELETED;
                setVisible(false);
            }
        }catch(Throwable err){
            MinistoreDesktop.log.err("Error al eliminar el producto", err);
            JOptionPane.showMessageDialog(this, "Error al eliminar el producto: "+err.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_bEliminarActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bAceptar;
    private javax.swing.JButton bCancelar;
    private javax.swing.JButton bEliminar;
    private javax.swing.JComboBox<ComboBoxItem> cbCategoria;
    private javax.swing.JComboBox<String> cbEstado;
    private javax.swing.JComboBox<ComboBoxItem> cbProveedor;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JLabel lTitulo;
    private javax.swing.JTextField tCodigo;
    private javax.swing.JTextField tCodigoProveedor;
    private javax.swing.JTextField tDescripcion;
    private javax.swing.JTextField tMarca;
    private javax.swing.JTextField tNombre;
    private javax.swing.JTextField tPrecioCompra;
    private javax.swing.JTextField tPrecioVenta;
    private javax.swing.JTextField tStock;
    private javax.swing.JTextField tStockMinimo;
    // End of variables declaration//GEN-END:variables
}
