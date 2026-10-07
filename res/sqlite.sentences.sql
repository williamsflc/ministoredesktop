# Sentencias SQLite no portables.
# Clave = Resources.p("...").  ? = parametro JDBC.  {LIMIT}/{OFFSET} = plantilla de paginacion.

# Productos.getNextProductCode
# ? = prefijo (resultado), ? = prefijo (filtro)
query.new-product-code=\
    WITH params(output_prefix, filter_prefix) AS (VALUES (?, ?)) \
    SELECT output_prefix || ( \
        coalesce(max(CAST(substr(codigo, length(filter_prefix) + 1) AS INTEGER)), 0) + 1 \
    ) \
    FROM productos, params \
    WHERE substr(codigo, 1, length(filter_prefix)) = filter_prefix \
      AND length(codigo) > length(filter_prefix) \
      AND substr(codigo, length(filter_prefix) + 1) NOT GLOB '*[^0-9]*'

# DBUtils.buildSimpleSqlSelect
default.sql.pagination=LIMIT {LIMIT} OFFSET {OFFSET}

# Usuario.findByUsername
# ? = username en mayusculas
query.usuario.find-by-username=\
    SELECT * FROM usuario WHERE upper("user") = ?

# Usuario.search
# ? = %texto%, ? = %texto%, ? = %texto%
query.usuario.search-filter=\
    upper("user") like ? or upper(nombre) like ? or upper(tipo) like ?

# Usuario.insert
# ? = user, pass, nombre, tipo
query.usuario.insert=\
    INSERT INTO usuario ("user", pass, nombre, tipo) VALUES (?, ?, ?, ?)

# Usuario.update
# ? = user, pass, nombre, tipo, id
query.usuario.update=\
    UPDATE usuario SET "user" = ?, pass = ?, nombre = ?, tipo = ? WHERE id = ?

# HistoricoInventario.insert
# ? = fecha, proveedor_id, numero_pedido, accion, user, stock_actual, nuevo_stock, producto_id
query.historico-inventario.insert=\
    INSERT INTO historico_inventario \
      (fecha, proveedor_id, numero_pedido, accion, "user", stock_actual, nuevo_stock, producto_id) \
    VALUES (?, ?, ?, ?, ?, ?, ?, ?)

# VentaDetalle.listFinalizedSalesByProductoId
# ? = producto_id, ? = limit
query.venta-detalle.list-finalized-by-producto=\
    SELECT v.fecha, d.cantidad \
    FROM venta_detalle d \
    INNER JOIN ventas v ON d.venta_id = v.id \
    WHERE d.producto_id = ? AND v.estado = 'Finalizada' \
    ORDER BY v.fecha DESC \
    LIMIT ?
