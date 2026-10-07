package com.lasopro.ministore.db;

import com.lasopro.ministore.desktop.MinistoreDesktop;
import com.lasopro.ministore.util.DBUtils;
import com.lasopro.ministore.util.PaginedResult;
import com.lasopro.ministore.util.Resources;
import java.sql.Date;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 *
 * @author williams
 */
public class HistoricoInventario {

    private DBUtils db;

    public HistoricoInventario() {
        this.db = new DBUtils(MinistoreDesktop.log);
    }

    public Map<String, Object> findById(Integer id) {
        String sql = DBUtils.buildSimpleSqlSelect("historico_inventario", null, null, null, " id = ?");
        List<Map<String, Object>> l = db.queryAsMap(Resources.getDsName(), sql, id);
        if (!l.isEmpty()) {
            return l.get(0);
        }
        throw new RuntimeException("Registro historico con id= " + id + " no encontrado");
    }

    public PaginedResult list(Integer page, Integer limit) {
        Integer offset = (page - 1) * limit;
        PaginedResult pr = new PaginedResult();
        pr.setPage(page);
        pr.setPageSize(limit);
        pr.setTotal(0);

        int total = db.count(Resources.getDsName(), "historico_inventario");
        pr.setTotal(total);
        pr.setTotalPages((int) Math.floor((double) total / (double) limit) + (total % limit == 0 ? 0 : 1));

        String sql = DBUtils.buildSimpleSqlSelect("historico_inventario", "id desc", limit.toString(), offset.toString(), null);
        pr.setData(db.queryAsMap(Resources.getDsName(), sql));
        return pr;
    }

    public List<Map<String, Object>> listByProductoId(Integer productoId) {
        String sql = DBUtils.buildSimpleSqlSelect("historico_inventario", "id desc", null, null, " producto_id = ?");
        return db.queryAsMap(Resources.getDsName(), sql, productoId);
    }

    public void insert(Map<String, Object> info) {
        String sql = Resources.p("query.historico-inventario.insert");

        Object[] params = new Object[8];
        params[0] = info.get("fecha") != null ? info.get("fecha") : new Date(Calendar.getInstance().getTimeInMillis());
        params[1] = info.get("proveedor_id");
        params[2] = info.get("numero_pedido");
        params[3] = info.get("accion");
        params[4] = info.get("user");
        params[5] = info.get("stock_actual");
        params[6] = info.get("nuevo_stock");
        params[7] = info.get("producto_id");

        db.execute(Resources.getDsName(), sql, params);
    }
}
