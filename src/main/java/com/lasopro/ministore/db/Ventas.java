package com.lasopro.ministore.db;

import com.lasopro.ministore.desktop.MinistoreDesktop;
import com.lasopro.ministore.util.DBUtils;
import com.lasopro.ministore.util.PaginedResult;
import com.lasopro.ministore.util.Resources;
import com.lasopro.ministore.util.Util;
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
public class Ventas {
    
    private DBUtils db;

    public Ventas() {
        this.db = new DBUtils(MinistoreDesktop.log);
    }
    
    
    public Map<String, Object> findById(Long id) {
        String sql = DBUtils.buildSimpleSqlSelect("ventas", null, null, null, " id = ?");
        List<Map<String, Object>> l = db.queryAsMap(Resources.getDsName(), sql, id);
        if (!l.isEmpty()) {
            return l.get(0);
        }
        throw new RuntimeException("Venta con id= " + id + " no encontrada");
    }
    
    
    public Map<String, Object> findByHash(String hash) {
        String sql = DBUtils.buildSimpleSqlSelect("ventas", null, null, null, " hash = ?");
        List<Map<String, Object>> l = db.queryAsMap(Resources.getDsName(), sql, hash);
        if (!l.isEmpty()) {
            return l.get(0);
        }
        return null;
    }
    
    public PaginedResult list(Integer page, Integer limit) {
        Integer offset = (page-1)*limit;;
        PaginedResult pr = new PaginedResult();
        pr.setPage(page);
        pr.setPageSize(limit);
        pr.setTotal(0);

        int total = db.count(Resources.getDsName(), "ventas");
        pr.setTotal(total);
        pr.setTotalPages((int) Math.floor((double) total / (double) limit) + (total % limit == 0 ? 0 : 1));
        
        String sql = DBUtils.buildSimpleSqlSelect("ventas", "id desc", limit.toString(), offset.toString(), null);
        pr.setData(db.queryAsMap(Resources.getDsName(), sql));
        return pr;
    }
    
    
    public List<Map<String,Object>> listByDate(Date date) {
        return db.queryAsMap(Resources.getDsName(), "select * from ventas where fecha >= ? order by fecha desc",date);
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
                     INSERT INTO ventas 
                       (hash,fecha, total, descuento, descuento_detalle, total_a_pagar, efectivo, tarjeta, cambio, 
                        cliente_id, factura_nit, factura_nombre, factura_email, factura_telefono, factura_direccion,
                        enviar_factura, estado,fecha_creacion, usuario_creo) 
                    VALUES(?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
        
        
        Object[] params = new Object[19];
        params[0]=info.get("hash");
        params[1]=info.get("fecha");
        params[2]=info.get("total");
        params[3]=info.get("descuento");
        params[4]=info.get("descuento_detalle");
        params[5]=info.get("total_a_pagar");
        params[6]=info.get("efectivo");
        params[7]=info.get("tarjeta");
        params[8]=info.get("cambio");
        params[9]=info.get("cliente_id");
        params[10]=info.get("factura_nit");
        params[11]=info.get("factura_nombre");
        params[12]=info.get("factura_email");
        params[13]=info.get("factura_telefono");
        params[14]=info.get("factura_direccion");
        params[15]=info.get("enviar_factura");
        params[16]=info.get("estado");
        params[17]=new Date(Calendar.getInstance().getTimeInMillis());
        params[18]=MinistoreDesktop.getCurrentUser().getId();
        
        db.execute(Resources.getDsName(), sql, params);
    }

    public void registrarVentaFinalizada(Map<String, Object> ventaInfo,
                                         List<Map<String, Object>> detalle,
                                         Map<String, Object> descuentoSobreTotal) {
        db.runInTransaction(Resources.getDsName(), conn -> {
            insertOnConnection(conn, ventaInfo);
            Object ventaIdObj = DBUtils.queryScalar(conn,
                    "SELECT id FROM ventas WHERE hash = ?", ventaInfo.get("hash"));
            if (ventaIdObj == null) {
                throw new RuntimeException("No fue posible almacenar la venta");
            }
            long ventaId = ((Number) ventaIdObj).longValue();

            Productos prod = new Productos();
            VentaDetalle vd = new VentaDetalle();

            for (Map<String, Object> m : detalle) {
                Integer productoId = ((Number) m.get("id")).intValue();
                m.put("producto_id", productoId);
                m.put("nombre_producto", m.get("nombre"));
                m.put("venta_id", ventaId);
                vd.insertOnConnection(conn, m);
                prod.decreaseStockOnConnection(conn, productoId,
                        ((Number) m.get("cantidad")).doubleValue());
            }

            if (descuentoSobreTotal != null) {
                descuentoSobreTotal.put("venta_id", ventaId);
                new DescXTotalVenta().insertOnConnection(conn, descuentoSobreTotal);
            }
        });
    }

    private void insertOnConnection(Connection conn, Map<String, Object> info) throws SQLException {
        String sql = """
                     INSERT INTO ventas
                       (hash,fecha, total, descuento, descuento_detalle, total_a_pagar, efectivo, tarjeta, cambio,
                        cliente_id, factura_nit, factura_nombre, factura_email, factura_telefono, factura_direccion,
                        enviar_factura, estado,fecha_creacion, usuario_creo)
                    VALUES(?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                     """;
        DBUtils.executeUpdate(conn, sql,
                info.get("hash"),
                info.get("fecha"),
                info.get("total"),
                info.get("descuento"),
                info.get("descuento_detalle"),
                info.get("total_a_pagar"),
                info.get("efectivo"),
                info.get("tarjeta"),
                info.get("cambio"),
                info.get("cliente_id"),
                info.get("factura_nit"),
                info.get("factura_nombre"),
                info.get("factura_email"),
                info.get("factura_telefono"),
                info.get("factura_direccion"),
                info.get("enviar_factura"),
                info.get("estado"),
                new Date(Calendar.getInstance().getTimeInMillis()),
                MinistoreDesktop.getCurrentUser().getId());
    }
    
    
    public void update(Map<String,Object> info){
        
        String sql = """
                     UPDATE ventas SET
                        fecha              = ?, 
                        total              = ?, 
                        descuento          = ?, 
                        descuento_detalle  = ?,
                        total_a_pagar      = ?, 
                        efectivo           = ?, 
                        tarjeta            = ?, 
                        cambio             = ?, 
                        cliente_id         = ?, 
                        enviar_factura     = ?, 
                        estado             = ?, 
                        fecha_modificacion = ?, 
                        usuario_modifico   = ? 
                    WHERE id = ?
                    """;
        
        Object[] params = new Object[14];
        params[0]=info.get("fecha");
        params[1]=info.get("total");
        params[2]=info.get("descuento");
        params[3]=info.get("descuento_detalle");
        params[4]=info.get("total_a_pagar");
        params[5]=info.get("efectivo");
        params[6]=info.get("tarjeta");
        params[7]=info.get("cambio");
        params[8]=info.get("cliente_id");
        params[9]=info.get("enviar_factura");
        params[10]=info.get("estado");
        params[11]=new Date(Calendar.getInstance().getTimeInMillis());
        params[12]=MinistoreDesktop.getCurrentUser().getId();
        params[13]=info.get("id");
        
        db.execute(Resources.getDsName(), sql, params);
        
    }
    
    
    public void anular(Long id){
        
        String sql = """
                    UPDATE ventas SET
                        estado             = ?,
                        fecha_modificacion = ?, 
                        usuario_modifico   = ? 
                    WHERE id = ?
                    """;
        
        
        Object[] params = new Object[4];
        params[0]="Anulada";
        params[1]=new Date(Calendar.getInstance().getTimeInMillis());
        params[2]=MinistoreDesktop.getCurrentUser().getId();
        params[3]=id;
        
        
        db.execute(Resources.getDsName(), sql, params);
        
        //regresar stock
        List<Map<String,Object>> detalle = (new VentaDetalle()).findByVentaId(id);
        Productos prod = new Productos();
        for(Map<String,Object> d: detalle){
            prod.updateStock((Integer)d.get("producto_id"), (Double)d.get("cantidad"), '+');
        }
        
        
        
    }
   
    
    public void delete(Integer id){
        String sql = "delete from ventas where id = ?";
        db.execute(Resources.getDsName(), sql, id);
    }
   
    
}
