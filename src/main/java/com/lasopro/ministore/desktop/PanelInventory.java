package com.lasopro.ministore.desktop;

import com.lasopro.ministore.db.Categorias;
import com.lasopro.ministore.db.Productos;
import com.lasopro.ministore.db.Proveedores;
import com.lasopro.ministore.util.Resources;
import com.lasopro.ministore.util.Util;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;

/**
 *
 * @author williams
 */
public class PanelInventory extends javax.swing.JPanel implements PanelInterface {

    private Productos db;
    private List<Map<String,Object>> filteredProducts;
    private List<Map<String,Object>> allProducts;
    private int paginaActual = 1;
    private MapListTextPane mapList;
    private List<Map<String,Object>> categoriasCache;
    private List<Map<String,Object>> proveedoresCache;
    
    
    
    /**
     * Creates new form PanelInventory
     */
    public PanelInventory() {
        initComponents();
        mapList = new MapListTextPane();
        mapList.addActionListener((e)->{ crearProducto(); });
        mapList.setFormatter(new MapListTextPane.Formatter() {
            @Override
            public String format(Map<String, Object> m) {
                StringBuilder sb = new StringBuilder();
                sb.append(Util.lpad((Integer)m.get("#")+"",5,' '));
                sb.append(" ");
                sb.append(Util.rpad((String)m.get("codigo"),20,' '));
                sb.append(Util.rpad((String)m.get("categoria"), 25, ' '));
                sb.append(Util.rpad((String)m.get("nombre"),50,' '));
                sb.append(Util.lpad(""+m.get("stock"),8,' '));
                sb.append(Util.lpad(""+m.get("min_stock"), 8, ' '));
                try{
                    sb.append(Util.lpad(Util.formatoMonto((Number)m.get("precio_compra")),10,' '));
                }catch(Throwable err){
                    sb.append(Util.lpad(""+m.get("precio_compra"),10,' '));
                }
                try{
                    sb.append(Util.lpad(Util.formatoMonto((Number)m.get("precio_venta")),10,' '));
                }catch(Throwable err){
                    sb.append(Util.lpad(""+m.get("precio_venta"),10,' '));
                }
                sb.append(" ");
                sb.append(Util.rpad((String)m.get("activo"),10,' '));
                return sb.toString();
            }

            @Override
            public String header() {
                return "No.   Codigo              Categoria                Nombre                                            "
                    + "   Stock Min stk    Compra     Venta Estado    ";
            }

            @Override
            public String footer() {
                return "--";
            }
        });
        
        this.panelProductItems.setLayout(new BoxLayout(this.panelProductItems,BoxLayout.Y_AXIS));
        
        this.panelProductItems.add(mapList);
        
        this.db = new Productos();
        txPageSize.setText(Resources.p("app.product.page.size"));
        jScrollPane1.getViewport().setBackground(Color.white);
    }
    
    
    @Override
    public void refresh(){
        MinistoreDesktop.runInThread("Actualizando inventario...",()->{
            _refresh();
        });
    }


    public void _refresh(){
        this.categoriasCache = new Categorias().list(1, 3000).getData();
        this.proveedoresCache = new Proveedores().list(1, 3000).getData();
        allProducts = db.list(1, 10000).getData();
        allProducts.forEach(m->{ m.put("normalized", normalizeProducto(m));  });
        DialogProducto.getInst().fillComboBox(categoriasCache, proveedoresCache);
        filter();
    }
    
    
    public void filter(){
        
        if(allProducts == null) return;
        
        String text = tFiltro.getText().trim();
        
        List data;
        String nt = Util.normalize(text);
        
        data = new ArrayList();
        for(Map<String,Object> p: allProducts){
            if(!Util.in((String)p.get("activo"), 
                    chbActivos.isSelected()?"Activo":"x", 
                    chbInactivos.isSelected()?"Inactivo":"x")) continue;
            
            String normalized = (String) p.get("normalized");
            if(!nt.isBlank() && (normalized == null || !normalized.contains(nt))) continue;
            data.add(p);
        }
        
        orderProducts(data);
        
        setProducts(data);
    }
    
    
    
    public void setProducts(List<Map<String,Object>> products){
        this.filteredProducts = products;
       
        
        if(this.filteredProducts != null && !this.filteredProducts.isEmpty()){
            
            int i=1;
            for(Map<String,Object> m: filteredProducts){
                m.put("#", i);
                
                if(m.get("categoria_id") != null){
                    Map info = findInCacheById(categoriasCache, ((Number)m.get("categoria_id")).intValue());
                    if(info != null)  m.put("categoria", info.get("nombre"));
                }
                if(m.get("proveedor_id") != null){
                    Map info = findInCacheById(proveedoresCache, ((Number)m.get("proveedor_id")).intValue());
                    if(info != null) m.put("proveedor", info.get("nombre"));
                }
                i++;
            }
        }
        setPage(paginaActual);
    }
    
    
    private Map<String,Object> findInCacheById(List<Map<String,Object>> cache, Object id){
        
        if(id == null || cache == null) return null;
        
        int iid = ((Number) id).intValue();
        for(Map<String,Object> m : cache){
            Object mid = m.get("id");
            if(mid != null && iid == ((Number) mid).intValue()){
                return m;
            }
        }
        return null;
    }

    private void replaceInCache(Map<String,Object> cached, Map<String,Object> fresh){
        cached.clear();
        cached.putAll(fresh);
        cached.put("normalized", normalizeProducto(cached));
    }

    private static String normalizeProducto(Map<String,Object> m){
        return Util.normalize(
                valorTexto(m.get("nombre")) + " "
                + valorTexto(m.get("descripcion")) + " "
                + valorTexto(m.get("codigo")));
    }

    private static String valorTexto(Object value){
        return value == null ? "" : String.valueOf(value);
    }
    
    
    private void setPage(int page){   
        
        int pageSize = getPageSize();
        
        
        //this.mapList.setText("");
        
        if(this.filteredProducts == null || this.filteredProducts.isEmpty()){
            this.mapList.setText("");
            return;
        }
        
        
        int totalPages = getTotalPages();
        
        if(page > totalPages){
            page = totalPages;
        }
        
        
        int start = (page-1)*pageSize;
        int end = page*pageSize;
        
        
        this.lPagina.setText(page+"/"+totalPages +" ("+this.filteredProducts.size()+" productos)");
        this.btPaginaAnterior.setEnabled(page > 1);
        this.btPaginaSiguiente.setEnabled(page < totalPages);
        
        paginaActual = page;
        
        if(start >= filteredProducts.size()){
            return;
        }
        
        if(end > filteredProducts.size()){
            end = filteredProducts.size();
        }
        
        mapList.setData(this.filteredProducts.subList(start, end));
    }
    
    
    public void crearProducto(){
        if(mapList.getSelectedElement() ==null) return;
        Map<String,Object> prd = mapList.getSelectedElement();
        int res = DialogProducto.getInst().showEditDialog(((Number)prd.get("id")).intValue());
        if(res == DialogProducto.UPDATED){
            Map<String,Object> cached = findInCacheById(allProducts, prd.get("id"));
            if(cached != null){
                replaceInCache(cached, db.findById(((Number) cached.get("id")).intValue()));
            }
            filter();
        }
        if(res == DialogProducto.DELETED){
            prd = findInCacheById(allProducts, prd.get("id"));
            if(prd!=null){
                allProducts.remove(prd);
            }
            filter();
        }
    }
    
    
    public int getTotalPages(){
        int pageSize = getPageSize();
        return (int)((double)filteredProducts.size()/(double)pageSize) + (filteredProducts.size()%pageSize > 0? 1: 0);
    }
    
    public int getPageSize(){
        try {
            return Integer.parseInt(txPageSize.getText());
        } catch (Exception e) {
            return 25;
        }
    }
    
    
    private void orderProducts(List<Map<String,Object>> productList){

        String orden  = (String)this.cbOrden.getSelectedItem();
        String orderBy;
        
        if(orden.equalsIgnoreCase("Ordenar por nombre")){
            orderBy = "nombre";
        }else if(orden.equalsIgnoreCase("Ordenar por stock")){
            orderBy = "stock";
        }else if(orden.equalsIgnoreCase("Ordenar por categoria")){
            orderBy = "categoria_id";
        }else if(orden.equalsIgnoreCase("Ordenar por precio de compra")){
            orderBy = "precio_compra";
        }else if(orden.equalsIgnoreCase("Ordenar por precio de venta")){
            orderBy = "precio_venta";
        }else if(orden.equalsIgnoreCase("Ordenar por proveedor")){
            orderBy = "proveedor_id";
        }else if(orden.equalsIgnoreCase("Ordenar por codigo")){
            orderBy = "codigo";
        }else{
            orderBy = "id";
        }        
        Util.ordenarListMap(productList, orderBy, rbAscendente.isSelected());
    }
    
    
    @Override
    public String name() {
        return "inventario";
    }
    
    


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgAsceDesc = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        panelProductItems = new javax.swing.JPanel();
        toolbar = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        btPaginaAnterior = new javax.swing.JButton();
        btPaginaSiguiente = new javax.swing.JButton();
        txPageSize = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        lPagina = new javax.swing.JLabel();
        tFiltro = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        chbActivos = new javax.swing.JCheckBox();
        chbInactivos = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        cbOrden = new javax.swing.JComboBox<>();
        rbDescendente = new javax.swing.JRadioButton();
        rbAscendente = new javax.swing.JRadioButton();
        jButton1 = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));

        jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        panelProductItems.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout panelProductItemsLayout = new javax.swing.GroupLayout(panelProductItems);
        panelProductItems.setLayout(panelProductItemsLayout);
        panelProductItemsLayout.setHorizontalGroup(
            panelProductItemsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1138, Short.MAX_VALUE)
        );
        panelProductItemsLayout.setVerticalGroup(
            panelProductItemsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 834, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(panelProductItems);

        toolbar.setBackground(new java.awt.Color(255, 255, 255));

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add.png"))); // NOI18N
        jButton2.setText("Agregar producto");
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        btPaginaAnterior.setBackground(new java.awt.Color(242, 242, 242));
        btPaginaAnterior.setIcon(new javax.swing.ImageIcon(getClass().getResource("/arrow_left.png"))); // NOI18N
        btPaginaAnterior.setToolTipText("Página anterior");
        btPaginaAnterior.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btPaginaAnterior.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btPaginaAnteriorActionPerformed(evt);
            }
        });

        btPaginaSiguiente.setBackground(new java.awt.Color(242, 242, 242));
        btPaginaSiguiente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/arrow_right.png"))); // NOI18N
        btPaginaSiguiente.setToolTipText("Página siguiente");
        btPaginaSiguiente.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btPaginaSiguiente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btPaginaSiguienteActionPerformed(evt);
            }
        });

        txPageSize.setText("20");
        txPageSize.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txPageSizeKeyReleased(evt);
            }
        });

        jLabel1.setText("Pag.");

        lPagina.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lPagina.setText("##");

        tFiltro.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tFiltroKeyReleased(evt);
            }
        });

        jLabel2.setText("Buscar texto:");

        chbActivos.setSelected(true);
        chbActivos.setText("Activos");
        chbActivos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chbActivosActionPerformed(evt);
            }
        });

        chbInactivos.setSelected(true);
        chbInactivos.setText("Inactivos");
        chbInactivos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chbInactivosActionPerformed(evt);
            }
        });

        jLabel3.setText("Ordenar:");

        cbOrden.setBackground(new java.awt.Color(242, 242, 242));
        cbOrden.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Ordenar por nombre", "Ordenar por stock", "Ordenar por categoria", "Ordenar por proveedor", "Ordenar por codigo", "Ordenar por precio de compra", "Ordenar por precio de venta", " " }));
        cbOrden.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbOrdenActionPerformed(evt);
            }
        });

        bgAsceDesc.add(rbDescendente);
        rbDescendente.setText("Desc.");
        rbDescendente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbDescendenteActionPerformed(evt);
            }
        });

        bgAsceDesc.add(rbAscendente);
        rbAscendente.setSelected(true);
        rbAscendente.setText("Asce.");
        rbAscendente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbAscendenteActionPerformed(evt);
            }
        });

        jButton1.setBackground(new java.awt.Color(242, 242, 242));
        jButton1.setText("<html>Historial<br/>de cambios</html>");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout toolbarLayout = new javax.swing.GroupLayout(toolbar);
        toolbar.setLayout(toolbarLayout);
        toolbarLayout.setHorizontalGroup(
            toolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toolbarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(toolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txPageSize, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(toolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(tFiltro, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(toolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chbActivos)
                    .addComponent(chbInactivos))
                .addGroup(toolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(toolbarLayout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(rbAscendente)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rbDescendente))
                    .addGroup(toolbarLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbOrden, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(toolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(toolbarLayout.createSequentialGroup()
                        .addComponent(btPaginaAnterior, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btPaginaSiguiente, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lPagina, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        toolbarLayout.setVerticalGroup(
            toolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toolbarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(toolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, toolbarLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(toolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, toolbarLayout.createSequentialGroup()
                                .addGroup(toolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(toolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(tFiltro, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                                    .addComponent(txPageSize)))
                            .addComponent(lPagina, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addGroup(toolbarLayout.createSequentialGroup()
                        .addGroup(toolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(toolbarLayout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addGroup(toolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(chbActivos)
                                    .addGroup(toolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(rbAscendente)
                                        .addComponent(rbDescendente)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(toolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cbOrden, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(chbInactivos)))
                            .addGroup(toolbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(btPaginaAnterior, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btPaginaSiguiente, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1150, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 794, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btPaginaAnteriorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btPaginaAnteriorActionPerformed
        int pagina = paginaActual-1 < 1? 1 : paginaActual-1;
        setPage(pagina);
    }//GEN-LAST:event_btPaginaAnteriorActionPerformed

    private void btPaginaSiguienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btPaginaSiguienteActionPerformed
        int pagina = paginaActual + 1 > getTotalPages() ? getTotalPages() : paginaActual+1;
        setPage(pagina);
    }//GEN-LAST:event_btPaginaSiguienteActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        int res = DialogProducto.getInst().showNewDialog();

        if(res == DialogProducto.INSERTED){
            Integer id = DialogProducto.getInst().getIdProducto();
            
            if(id == null || allProducts == null) return;
            
            Map<String, Object> prd = findInCacheById(allProducts, id);
            if(prd != null){
                allProducts.remove(prd);
            }
            
            prd = db.findById(id);
            prd.put("normalized", normalizeProducto(prd));
            allProducts.add(prd);
            filter();
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void txPageSizeKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txPageSizeKeyReleased
        filter();
    }//GEN-LAST:event_txPageSizeKeyReleased

    private void tFiltroKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tFiltroKeyReleased
        filter();
    }//GEN-LAST:event_tFiltroKeyReleased

    private void chbActivosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chbActivosActionPerformed
        filter();
    }//GEN-LAST:event_chbActivosActionPerformed

    private void chbInactivosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chbInactivosActionPerformed
        filter();
    }//GEN-LAST:event_chbInactivosActionPerformed

    private void cbOrdenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbOrdenActionPerformed
        filter();
    }//GEN-LAST:event_cbOrdenActionPerformed

    private void rbAscendenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbAscendenteActionPerformed
        filter();
    }//GEN-LAST:event_rbAscendenteActionPerformed

    private void rbDescendenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbDescendenteActionPerformed
        filter();
    }//GEN-LAST:event_rbDescendenteActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        DialogHistoricoInventario.getInst().showDialog();
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgAsceDesc;
    private javax.swing.JButton btPaginaAnterior;
    private javax.swing.JButton btPaginaSiguiente;
    private javax.swing.JComboBox<String> cbOrden;
    private javax.swing.JCheckBox chbActivos;
    private javax.swing.JCheckBox chbInactivos;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lPagina;
    private javax.swing.JPanel panelProductItems;
    private javax.swing.JRadioButton rbAscendente;
    private javax.swing.JRadioButton rbDescendente;
    private javax.swing.JTextField tFiltro;
    private javax.swing.JPanel toolbar;
    private javax.swing.JTextField txPageSize;
    // End of variables declaration//GEN-END:variables
}



