package com.lasopro.ministore.db;

import com.lasopro.ministore.desktop.MinistoreDesktop;
import com.lasopro.ministore.util.DBUtils;
import com.lasopro.ministore.util.PaginedResult;
import com.lasopro.ministore.util.Resources;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author williams
 */
public class VentaDetalle {
    
    private DBUtils db;

    public VentaDetalle() {
        this.db = new DBUtils(MinistoreDesktop.log);
    }
    
    
    public Map<String, Object> findById(Integer id) {
        String sql = DBUtils.buildSimpleSqlSelect("venta_detalle", null, null, null, " id = ?");
        List<Map<String, Object>> l = db.queryAsMap(Resources.getDsName(), sql, id);
        if (!l.isEmpty()) {
            return l.get(0);
        }
        return null;
    }
    
    
    
    public List<Map<String,Object>> findByVentaId(Long venta_id){
        String sql = DBUtils.buildSimpleSqlSelect("venta_detalle d, productos p", null, null, null, " d.producto_id = p.id and venta_id = ?");
        List<Map<String, Object>> l = db.queryAsMap(Resources.getDsName(), sql, venta_id);
        return l;
    }

    public List<Map<String, Object>> listFinalizedSalesByProductoId(Integer productoId, int limit) {
        String sql = Resources.p("query.venta-detalle.list-finalized-by-producto");
        return db.queryAsMap(Resources.getDsName(), sql, productoId, limit);
    }
    
    public PaginedResult list(Integer page, Integer limit) {
        Integer offset = (page-1)*limit;;
        PaginedResult pr = new PaginedResult();
        pr.setPage(page);
        pr.setPageSize(limit);
        pr.setTotal(0);

        int total = db.count(Resources.getDsName(), "venta_detalle");
        pr.setTotal(total);
        pr.setTotalPages((int) Math.floor((double) total / (double) limit) + (total % limit == 0 ? 0 : 1));
        
        String sql = DBUtils.buildSimpleSqlSelect("venta_detalle", "id", limit.toString(), offset.toString(), null);
        pr.setData(db.queryAsMap(Resources.getDsName(), sql));
        return pr;
    }
    
    
//    public PaginedResult search(Integer page, Integer limit, String textToSearch) {
//        Integer offset = (page-1)*limit;
//        PaginedResult pr = new PaginedResult();
//        pr.setPage(page);
//        pr.setPageSize(limit);
//        pr.setTotal(0);
//
//        int total = db.count(Resources.getDsName(), "categorias");
//        pr.setTotal(total);
//        pr.setTotalPages((int) Math.floor((double) total / (double) limit) + (total % limit == 0 ? 0 : 1));
//        textToSearch = '%'+textToSearch.toUpperCase()+"%";
//        String sql = DBUtils.buildSimpleSqlSelect("productos", "id", limit.toString(), offset.toString(), 
//                " upper(nombre) like ? OR upper(descripcion) like ? OR upper(marca) like ? OR upper(codigo) like ? ");
//        pr.setData(db.queryAsMap(Resources.getDsName(), sql,textToSearch,textToSearch,textToSearch,textToSearch));
//        return pr;
//    }
    
    
    
    public void insert(Map<String,Object> info){
        
        String sql = """
                     INSERT INTO venta_detalle
                       (venta_id, producto_id, nombre_producto, precio, cantidad, total, descuento) 
                    VALUES(?, ?, ?, ?, ?, ?, ?)
                    """;
        
        
        Object[] params = new Object[7];
        params[0]=info.get("venta_id");
        params[1]=info.get("producto_id");
        params[2]=info.get("nombre_producto");
        params[3]=info.get("precio");
        params[4]=info.get("cantidad");
        params[5]=info.get("total");
        params[6]=info.get("descuento");
        
        db.execute(Resources.getDsName(), sql, params);
    }

    void insertOnConnection(Connection conn, Map<String, Object> info) throws SQLException {
        String sql = """
                     INSERT INTO venta_detalle
                       (venta_id, producto_id, nombre_producto, precio, cantidad, total, descuento)
                    VALUES(?, ?, ?, ?, ?, ?, ?)
                     """;
        DBUtils.executeUpdate(conn, sql,
                info.get("venta_id"),
                info.get("producto_id"),
                info.get("nombre_producto"),
                info.get("precio"),
                info.get("cantidad"),
                info.get("total"),
                info.get("descuento"));
    }
    
    
    public void update(Map<String,Object> info){
        
        String sql = """
                     UPDATE venta_detalle SET
                        nombre_producto    = ?, 
                        precio             = ?, 
                        cantidad           = ?, 
                        total              = ?
                        descuento          = ?
                    WHERE id = ?
                    """;
        
        Object[] params = new Object[6];
        params[0]=info.get("nombre_producto");
        params[1]=info.get("precio");
        params[2]=info.get("cantidad");
        params[3]=info.get("total");
        params[4]=info.get("descuento");
        params[5]=info.get("id");
        db.execute(Resources.getDsName(), sql, params);
        
    }
   
    
    public void delete(Integer id){
        String sql = "delete from venta_detalle where id = ?";
        db.execute(Resources.getDsName(), sql, id);
    }
   
    
}
