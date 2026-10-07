package com.lasopro.ministore.db;


import com.lasopro.ministore.desktop.MinistoreDesktop;
import com.lasopro.ministore.util.DBUtils;
import com.lasopro.ministore.util.PaginedResult;
import com.lasopro.ministore.util.Resources;
import com.lasopro.ministore.util.Util;
import java.sql.Date;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 *
 * @author williams
 */
public class Clientes {
    
       
    private DBUtils db;

    public Clientes() {
        this.db = new DBUtils(MinistoreDesktop.log);
    }
    
    
    public Map<String, Object> findById(String id) {
        String sql = DBUtils.buildSimpleSqlSelect("clientes", null, null, null, " id = ?");
        List<Map<String, Object>> l = db.queryAsMap(Resources.getDsName(), sql, id);
        if (!l.isEmpty()) {
            return l.get(0);
        }
        return null;
    }
    
    
    public Map<String, Object> findByDocumento(String doc) {
        String normalized = Util.normalizarDocumento(doc);
        String sql = DBUtils.buildSimpleSqlSelect("clientes", null, null, null,
                " REPLACE(upper(documento), '-', '') = ?");
        List<Map<String, Object>> l = db.queryAsMap(Resources.getDsName(), sql, normalized);
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

        int total = db.count(Resources.getDsName(), "clientes");
        pr.setTotal(total);
        pr.setTotalPages((int) Math.floor((double) total / (double) limit) + (total % limit == 0 ? 0 : 1));
        
        String sql = DBUtils.buildSimpleSqlSelect("clientes", "id", limit.toString(), offset.toString(), null);
        pr.setData(db.queryAsMap(Resources.getDsName(), sql));
        return pr;
    }
    
    
    public PaginedResult search(Integer page, Integer limit, String textToSearch) {
        Integer offset = (page-1)*limit;
        PaginedResult pr = new PaginedResult();
        pr.setPage(page);
        pr.setPageSize(limit);
        pr.setTotal(0);

        int total = db.count(Resources.getDsName(), "clientes");
        pr.setTotal(total);
        pr.setTotalPages((int) Math.floor((double) total / (double) limit) + (total % limit == 0 ? 0 : 1));
        textToSearch = '%'+textToSearch.toUpperCase()+"%";
        String sql = DBUtils.buildSimpleSqlSelect("clientes", "id", limit.toString(), offset.toString(), " upper(nombre) like ? or upper(email) like ?");
        pr.setData(db.queryAsMap(Resources.getDsName(), sql,textToSearch,textToSearch));
        return pr;
    }
    
    
    public void insert(Map<String,Object> info){
        
        String sql = """
                     INSERT INTO clientes 
                       (nombre, documento, telefono, direccion, email, fecha_creacion,usuario_creo) 
                       VALUES(?, ?, ?, ?, ?, ?, ?)
                    """;
        
        //Validaciones
        StringBuilder validationErrors = new StringBuilder();
        
        
        if(!validationErrors.isEmpty()){
            throw new RuntimeException(validationErrors.toString().trim());
        }
        
        Object[] params = new Object[7];
        params[0]=info.get("nombre");
        params[1]=Util.normalizarDocumento((String) info.get("documento"));
        params[2]=info.get("telefono");
        params[3]=info.get("direccion");
        params[4]=info.get("email");
        params[5]=new Date(Calendar.getInstance().getTimeInMillis());
        params[6]=MinistoreDesktop.getCurrentUser().getId();
        
        db.execute(Resources.getDsName(), sql, params);
    }
    
    
    public void update(Map<String,Object> info){
        
        String sql = """
                     UPDATE clientes SET
                        nombre             = ?, 
                        documento          = ?,
                        telefono           = ?, 
                        direccion          = ?, 
                        email              = ?, 
                        fecha_modificacion = ?, 
                        usuario_modifico   = ? 
                    WHERE id = ?
                    """;
        
        Object[] params = new Object[8];
        params[0]=info.get("nombre");
        params[1]=Util.normalizarDocumento((String) info.get("documento"));
        params[2]=info.get("telefono");
        params[3]=info.get("direccion");
        params[4]=info.get("email");
        params[5]=new Date(Calendar.getInstance().getTimeInMillis());
        params[6]=MinistoreDesktop.getCurrentUser().getId();
        params[7]=info.get("id");
        
        db.execute(Resources.getDsName(), sql, params);
        
    }
    
    public void delete(Integer id){
        String sql = "delete from clientes where id = ?";
        db.execute(Resources.getDsName(), sql, id);
    }
    
}
