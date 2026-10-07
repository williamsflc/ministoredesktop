package com.lasopro.ministore.db;


import com.lasopro.ministore.desktop.MinistoreDesktop;
import com.lasopro.ministore.util.DBUtils;
import com.lasopro.ministore.util.PaginedResult;
import com.lasopro.ministore.util.PasswordUtils;
import com.lasopro.ministore.util.Resources;
import java.util.List;
import java.util.Map;

/**
 *
 * @author williams
 */
public class Usuario {
    
       
    private DBUtils db;

    public Usuario() {
        this.db = new DBUtils(MinistoreDesktop.log);
    }
    
    
    public Map<String, Object> findById(Integer id) {
        String sql = DBUtils.buildSimpleSqlSelect("usuario", null, null, null, " id = ?");
        List<Map<String, Object>> l = db.queryAsMap(Resources.getDsName(), sql, id);
        if (!l.isEmpty()) {
            return l.get(0);
        }
        throw new RuntimeException("Usuario con id= " + id + " no encontrado");
    }
    
    
    public Map<String, Object> findByUsername(String user) {
        String sql = Resources.p("query.usuario.find-by-username");
        List<Map<String, Object>> l = db.queryAsMap(Resources.getDsName(), sql, user.toUpperCase());
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

        int total = db.count(Resources.getDsName(), "usuario");
        pr.setTotal(total);
        pr.setTotalPages((int) Math.floor((double) total / (double) limit) + (total % limit == 0 ? 0 : 1));
        
        String sql = DBUtils.buildSimpleSqlSelect("usuario", "id", limit.toString(), offset.toString(), null);
        pr.setData(db.queryAsMap(Resources.getDsName(), sql));
        return pr;
    }
    
    
    public PaginedResult search(Integer page, Integer limit, String textToSearch) {
        Integer offset = (page-1)*limit;
        PaginedResult pr = new PaginedResult();
        pr.setPage(page);
        pr.setPageSize(limit);
        pr.setTotal(0);

        int total = db.count(Resources.getDsName(), "usuario");
        pr.setTotal(total);
        pr.setTotalPages((int) Math.floor((double) total / (double) limit) + (total % limit == 0 ? 0 : 1));
        textToSearch = '%'+textToSearch.toUpperCase()+"%";
        String sql = DBUtils.buildSimpleSqlSelect("usuario", "id", limit.toString(), offset.toString(),
                Resources.p("query.usuario.search-filter"));
        pr.setData(db.queryAsMap(Resources.getDsName(), sql, textToSearch, textToSearch, textToSearch));
        return pr;
    }
    
    
    public void insert(Map<String,Object> info){
        
        String sql = Resources.p("query.usuario.insert");
        
        StringBuilder validationErrors = new StringBuilder();
        
        if(info.get("user") == null || ((String)info.get("user")).isBlank()){
            validationErrors.append("El nombre de usuario es obligatorio\n");
        }
        if(info.get("pass") == null || ((String)info.get("pass")).isBlank()){
            validationErrors.append("La contraseña es obligatoria\n");
        }
        if(info.get("nombre") == null || ((String)info.get("nombre")).isBlank()){
            validationErrors.append("El nombre es obligatorio\n");
        }
        if(findByUsername((String)info.get("user")) != null){
            validationErrors.append("El usuario '").append(info.get("user")).append("' ya existe\n");
        }
        
        if(!validationErrors.isEmpty()){
            throw new RuntimeException(validationErrors.toString().trim());
        }
        
        Object[] params = new Object[4];
        params[0]=info.get("user");
        params[1]=PasswordUtils.hash((String) info.get("pass"));
        params[2]=info.get("nombre");
        params[3]=info.get("tipo");
        
        db.execute(Resources.getDsName(), sql, params);
    }
    
    
    public void update(Map<String,Object> info){
        
        String sql = Resources.p("query.usuario.update");
        
        StringBuilder validationErrors = new StringBuilder();
        
        if(info.get("user") == null || ((String)info.get("user")).isBlank()){
            validationErrors.append("El nombre de usuario es obligatorio\n");
        }
        if(info.get("nombre") == null || ((String)info.get("nombre")).isBlank()){
            validationErrors.append("El nombre es obligatorio\n");
        }
        
        Map<String, Object> exists = findByUsername((String)info.get("user"));
        if(exists != null && !((Number)exists.get("id")).equals(info.get("id"))){
            validationErrors.append("El usuario '").append(info.get("user")).append("' ya existe\n");
        }
        
        if(!validationErrors.isEmpty()){
            throw new RuntimeException(validationErrors.toString().trim());
        }
        
        Object[] params = new Object[5];
        params[0]=info.get("user");
        String newPassword = info.get("pass") == null ? "" : (String) info.get("pass");
        params[1]=newPassword.isBlank()
                ? exists.get("pass")
                : PasswordUtils.hash(newPassword);
        params[2]=info.get("nombre");
        params[3]=info.get("tipo");
        params[4]=info.get("id");
        
        db.execute(Resources.getDsName(), sql, params);
        
    }
    
    public void delete(Integer id){
        String sql = "delete from usuario where id = ?";
        db.execute(Resources.getDsName(), sql, id);
    }

    public void updatePassword(String username, String currentPass, String newPass) {
        Map<String, Object> info = findByUsername(username);
        if (info == null) {
            throw new RuntimeException("Usuario no encontrado");
        }
        if (!PasswordUtils.matches(currentPass, info.get("pass"))) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }
        if (newPass == null || newPass.isBlank()) {
            throw new RuntimeException("La nueva contraseña es obligatoria");
        }

        String sql = "UPDATE usuario SET pass = ? WHERE id = ?";
        db.execute(Resources.getDsName(), sql, PasswordUtils.hash(newPass), info.get("id"));
    }
    
}
