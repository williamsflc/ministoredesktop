
package com.lasopro.ministore.db;

import com.lasopro.ministore.desktop.MinistoreDesktop;
import com.lasopro.ministore.util.DBUtils;
import com.lasopro.ministore.util.PaginedResult;
import com.lasopro.ministore.util.Resources;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 *
 * @author williams
 */
public class Productos {
    
    
    private DBUtils db;

    public Productos() {
        this.db = new DBUtils(MinistoreDesktop.log);
    }
    
    
    
    public String getNextProductCode(){
        
        String prfx = Resources.p("app.product.code.prefix");
        String sql  = Resources.p("query.new-product-code");
        Long start  = Long.parseLong(Resources.p("app.product.code.starts"));

        String codigo = null;
        for(int i=0;i<1000;i++){
            String tmp = (String)db.queryOneValueSelect(Resources.getDsName(), null, sql, prfx, prfx);

            if(tmp == null){
                tmp = prfx + (start + (long)i);
            }

            if(findByCodigo(tmp)== null){
                codigo = tmp;
                break;
            }
        }
        if(codigo == null){
            throw new RuntimeException("No fue posible generar el codigo");
        }
        return codigo;
    }
    
    
    public Map<String, Object> findById(Integer id) {
        String sql = DBUtils.buildSimpleSqlSelect("productos", null, null, null, " id = ?");
        List<Map<String, Object>> l = db.queryAsMap(Resources.getDsName(), sql, id);
        if (!l.isEmpty()) {
            return l.get(0);
        }
        throw new RuntimeException("Producto con id= " + id + " no encontrado");
    }
    
    public Map<String, Object> findByCodigo(String codigo) {
        String sql = DBUtils.buildSimpleSqlSelect("productos", null, null, null, " codigo = ?");
        List l = db.queryAsMap(Resources.getDsName(), sql, codigo);
        if(!l.isEmpty()){
            return (Map<String, Object>)l.get(0);
        }
        return null;
    }

    
    public PaginedResult list(Integer page, Integer limit) {
        Integer offset = (page-1)*limit;;
        PaginedResult pr = new PaginedResult();
        pr.setPage(page);
        pr.setPageSize(limit);
        pr.setTotal(0);

        int total = db.count(Resources.getDsName(), "productos");
        pr.setTotal(total);
        pr.setTotalPages((int) Math.floor((double) total / (double) limit) + (total % limit == 0 ? 0 : 1));
        
        String sql = DBUtils.buildSimpleSqlSelect("productos", "id", limit.toString(), offset.toString(), null);
        pr.setData(db.queryAsMap(Resources.getDsName(), sql));
        return pr;
    }
    
    
    public PaginedResult listActiveOnly(Integer page, Integer limit) {
        Integer offset = (page-1)*limit;
        PaginedResult pr = new PaginedResult();
        pr.setPage(page);
        pr.setPageSize(limit);
        pr.setTotal(0);

        int total = db.count(Resources.getDsName(), "productos", "activo = 'Activo'");
        pr.setTotal(total);
        pr.setTotalPages((int) Math.floor((double) total / (double) limit) + (total % limit == 0 ? 0 : 1));
        
        String sql = DBUtils.buildSimpleSqlSelect("productos", "id", limit.toString(), offset.toString(), "activo = 'Activo'");
        pr.setData(db.queryAsMap(Resources.getDsName(), sql));
        return pr;
    }

    public List<Map<String, Object>> listActiveStockOnly() {
        return db.queryAsMap(Resources.getDsName(),
                "SELECT id, stock FROM productos WHERE activo = 'Activo'");
    }
    
    
    public PaginedResult search(Integer page, Integer limit, String textToSearch, String orderBy, String filtro) {
        Integer offset = (page-1)*limit;
        PaginedResult pr = new PaginedResult();
        pr.setPage(page);
        pr.setPageSize(limit);
        pr.setTotal(0);

        String searchParam = null;
        if (textToSearch != null && !textToSearch.isBlank()) {
            searchParam = '%' + textToSearch.toUpperCase() + '%';
            String searchFilters = "upper(nombre) like ? OR upper(descripcion) like ? OR upper(marca) like ? OR upper(codigo) like ?";
            if (filtro == null || filtro.isBlank()) {
                filtro = searchFilters;
            } else {
                filtro = filtro + " and (" + searchFilters + ")";
            }
        }

        int total;
        if (filtro != null && !filtro.isBlank()) {
            String countSql = "select count(1) from productos where " + filtro;
            if (searchParam != null) {
                total = ((Number) db.queryOneValueSelect(Resources.getDsName(), null, countSql,
                        searchParam, searchParam, searchParam, searchParam)).intValue();
            } else {
                total = ((Number) db.queryOneValueSelect(Resources.getDsName(), null, countSql)).intValue();
            }
        } else {
            total = db.count(Resources.getDsName(), "productos");
        }
        pr.setTotal(total);
        pr.setTotalPages((int) Math.floor((double) total / (double) limit) + (total % limit == 0 ? 0 : 1));

        String sql = DBUtils.buildSimpleSqlSelect("productos", orderBy, limit.toString(), offset.toString(), filtro);
        if (searchParam != null) {
            pr.setData(db.queryAsMap(Resources.getDsName(), sql, searchParam, searchParam, searchParam, searchParam));
        } else {
            pr.setData(db.queryAsMap(Resources.getDsName(), sql));
        }
        return pr;
    }
    
    
    
    public void insert(Map<String,Object> productInfo){
        String sql = """
                     INSERT INTO productos 
                       (codigo, codigo_proveedor, nombre, descripcion, marca, precio_compra, precio_venta, stock, proveedor_id, 
                        categoria_id, activo, fecha_creacion, usuario_creo,min_stock) 
                    VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
        
        //Validaciones
        StringBuilder validationErrors = new StringBuilder();
        
        Map<String,Object> exists = findByCodigo((String)productInfo.get("codigo"));
        if(exists != null){
           validationErrors.append("El codigo indicado ya existe: ").append(productInfo.get("codigo")).append("\r\n");
        }
        
        if(!validationErrors.isEmpty()){
            throw new RuntimeException(validationErrors.toString().trim());
        }
        
        Object[] params = new Object[14];
        params[0] = productInfo.get("codigo");
        params[1] = emptyToNull(productInfo.get("codigo_proveedor"));
        params[2] = productInfo.get("nombre");
        params[3] = productInfo.get("descripcion");
        params[4] = productInfo.get("marca");
        params[5] = productInfo.get("precio_compra");
        params[6] = productInfo.get("precio_venta");
        params[7] = productInfo.get("stock");
        params[8] = productInfo.get("proveedor_id");
        params[9] = productInfo.get("categoria_id");
        params[10] = productInfo.get("activo");
        params[11]= new Date(Calendar.getInstance().getTimeInMillis());
        params[12]= MinistoreDesktop.getCurrentUser().getId();
        params[13]= productInfo.get("min_stock");
        
        db.execute(Resources.getDsName(), sql, params);
    }
    
    
    public void updateStock(Integer idProducto, Double cantidad, char operator){
         String sql = "UPDATE productos SET stock = (stock "+operator+" ?) WHERE id = ? ";
        db.execute(Resources.getDsName(), sql, cantidad, idProducto);
    }

    public double getStock(Integer idProducto) {
        Object stock = db.queryOneValueSelect(Resources.getDsName(), null,
                "SELECT stock FROM productos WHERE id = ?", idProducto);
        return stock == null ? 0d : ((Number) stock).doubleValue();
    }

    public List<Map<String, Object>> getStockByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        StringBuilder placeholders = new StringBuilder();
        Object[] params = new Object[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                placeholders.append(',');
            }
            placeholders.append('?');
            params[i] = ids.get(i);
        }
        return db.queryAsMap(Resources.getDsName(),
                "SELECT id, stock FROM productos WHERE id IN (" + placeholders + ")",
                params);
    }

    public boolean decreaseStockIfAvailable(Integer idProducto, Double cantidad) {
        String sql = "UPDATE productos SET stock = (stock - ?) WHERE id = ? AND stock >= ?";
        DBUtils.Result res = db.executeWithResult(Resources.getDsName(), null, sql, cantidad, idProducto, cantidad);
        return res.getAffectedRows() > 0;
    }

    void decreaseStockOnConnection(Connection conn, Integer idProducto, Double cantidad) throws SQLException {
        int rows = DBUtils.executeUpdate(conn,
                "UPDATE productos SET stock = (stock - ?) WHERE id = ? AND stock >= ?",
                cantidad, idProducto, cantidad);
        if (rows == 0) {
            throw new SQLException("Stock insuficiente para producto id=" + idProducto);
        }
    }
    
    
    
    public void update(Map<String,Object> productInfo){
        
        String sql = """
                     UPDATE productos SET
                        codigo             = ?, 
                        codigo_proveedor   = ?,
                        nombre             = ?, 
                        descripcion        = ?, 
                        marca              = ?, 
                        precio_compra      = ?, 
                        precio_venta       = ?, 
                        stock              = ?, 
                        proveedor_id       = ?, 
                        categoria_id       = ?, 
                        activo             = ?, 
                        fecha_modificacion = ?, 
                        usuario_modifico   = ?,
                        min_stock          = ?
                    WHERE id = ?
                    """;
        
        Object[] params = new Object[15];
        params[0]  = productInfo.get("codigo");
        params[1]  = emptyToNull(productInfo.get("codigo_proveedor"));
        params[2]  = productInfo.get("nombre");
        params[3]  = productInfo.get("descripcion");
        params[4]  = productInfo.get("marca");
        params[5]  = productInfo.get("precio_compra");
        params[6]  = productInfo.get("precio_venta");
        params[7]  = productInfo.get("stock");
        params[8]  = productInfo.get("proveedor_id");
        params[9]  = productInfo.get("categoria_id");
        params[10] = productInfo.get("activo");
        params[11] = new Date(Calendar.getInstance().getTimeInMillis());
        params[12] = MinistoreDesktop.getCurrentUser().getId();
        params[13] = productInfo.get("min_stock");
        params[14] = productInfo.get("id");
        
        db.execute(Resources.getDsName(), sql, params);
        
    }
   
    
    public void delete(Integer id){
        String sql = "delete from productos where id = ?";
        db.execute(Resources.getDsName(), sql, id);
    }

    private static Object emptyToNull(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }
    
    
    
}
