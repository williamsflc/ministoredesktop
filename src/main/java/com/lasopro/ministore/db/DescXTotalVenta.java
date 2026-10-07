package com.lasopro.ministore.db;

import com.lasopro.ministore.desktop.MinistoreDesktop;
import com.lasopro.ministore.util.DBUtils;
import com.lasopro.ministore.util.Resources;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author williams
 */
public class DescXTotalVenta {
    
    
    DBUtils db;
    
    public DescXTotalVenta(){
        db = new DBUtils(MinistoreDesktop.log);
    }
    
    
    public Map<String, Object> findById(Integer id) {
        String sql = DBUtils.buildSimpleSqlSelect("desc_x_total_venta", null, null, null, " id = ?");
        List<Map<String, Object>> l = db.queryAsMap(Resources.getDsName(), sql, id);
        if (!l.isEmpty()) {
            return l.get(0);
        }
        return null;
    }
    
    public List<Map<String,Object>> findByVentaId(Long venta_id){
        String sql = DBUtils.buildSimpleSqlSelect("desc_x_total_venta", null, null, null, "venta_id = ?");
        List<Map<String, Object>> l = db.queryAsMap(Resources.getDsName(), sql, venta_id);
        return l;
    }
    
    
    public void insert(Map<String,Object> info){
        
        String sql = """
                     INSERT INTO desc_x_total_venta
                       (venta_id, tipo, monto, detalle) 
                    VALUES(?, ?, ?, ?)
                    """;
        
        Object[] params = new Object[4];
        params[0]=info.get("venta_id");
        params[1]=info.get("tipo");
        params[2]=info.get("monto");
        params[3]=info.get("detalle");
        db.execute(Resources.getDsName(), sql, params);
    }

    void insertOnConnection(Connection conn, Map<String, Object> info) throws SQLException {
        String sql = """
                     INSERT INTO desc_x_total_venta
                       (venta_id, tipo, monto, detalle)
                    VALUES(?, ?, ?, ?)
                     """;
        DBUtils.executeUpdate(conn, sql,
                info.get("venta_id"),
                info.get("tipo"),
                info.get("monto"),
                info.get("detalle"));
    }
    
    
    public void update(Map<String,Object> info){
        
        String sql = """
                     UPDATE desc_x_total_venta SET
                        venta_id    = ?, 
                        tipo        = ?, 
                        monto       = ?, 
                        detalle     = ?
                    WHERE id = ?
                    """;
        
        Object[] params = new Object[5];
        params[0]=info.get("venta_id");
        params[1]=info.get("tipo");
        params[2]=info.get("monto");
        params[3]=info.get("detalle");
        params[4]=info.get("id");
        
        db.execute(Resources.getDsName(), sql, params);
        
    }
   
    
    public void delete(Integer id){
        String sql = "delete from desc_x_total_venta where id = ?";
        db.execute(Resources.getDsName(), sql, id);
    }
    
}
