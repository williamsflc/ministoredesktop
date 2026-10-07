package com.lasopro.ministore.db;

import com.lasopro.ministore.desktop.MinistoreDesktop;
import com.lasopro.ministore.util.DBUtils;
import com.lasopro.ministore.util.Resources;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author williams
 */
public class Config {

    private DBUtils db;

    public Config() {
        this.db = new DBUtils(MinistoreDesktop.log);
    }

    public List<Map<String, Object>> list() {
        String sql = "SELECT nombre, valor FROM config ORDER BY nombre";
        return db.queryAsMap(Resources.getDsName(), sql);
    }

    public Map<String, Object> findByName(String nombre) {
        String sql = "SELECT nombre, valor FROM config WHERE nombre = ?";
        List<Map<String, Object>> l = db.queryAsMap(Resources.getDsName(), sql, nombre);
        if (!l.isEmpty()) {
            return l.get(0);
        }
        return null;
    }

    public Map<String, String> listAsMap() {
        Map<String, String> result = new HashMap<>();
        for (Map<String, Object> row : list()) {
            result.put((String) row.get("nombre"), row.get("valor") == null ? "" : (String) row.get("valor"));
        }
        return result;
    }

    public void update(String nombre, String valor) {
        if (findByName(nombre) != null) {
            db.execute(Resources.getDsName(), "UPDATE config SET valor = ? WHERE nombre = ?", valor, nombre);
        } else {
            db.execute(Resources.getDsName(), "INSERT INTO config (nombre, valor) VALUES (?, ?)", nombre, valor);
        }
    }

    public void updateAll(Map<String, String> values) {
        values.forEach(this::update);
    }
}
