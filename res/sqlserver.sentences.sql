query.new-product-code=\
    SELECT CONCAT(?, COALESCE(MAX(TRY_CAST(SUBSTRING(codigo, LEN(p.prefix) + 1, 255) AS INTEGER)), 0) + 1) \
    FROM productos \
    CROSS JOIN (SELECT CAST(? AS NVARCHAR(255)) AS prefix) p \
    WHERE codigo LIKE CONCAT(p.prefix, '%')

default.sql.pagination=OFFSET {OFFSET} ROWS FETCH NEXT {LIMIT} ROWS ONLY

query.usuario.find-by-username=\
    SELECT * FROM usuario WHERE upper([user]) = ?

query.usuario.search-filter=\
    upper([user]) like ? or upper(nombre) like ? or upper(tipo) like ?

query.usuario.insert=\
    INSERT INTO usuario ([user], pass, nombre, tipo) VALUES (?, ?, ?, ?)

query.usuario.update=\
    UPDATE usuario SET [user] = ?, pass = ?, nombre = ?, tipo = ? WHERE id = ?

query.historico-inventario.insert=\
    INSERT INTO historico_inventario \
      (fecha, proveedor_id, numero_pedido, accion, [user], stock_actual, nuevo_stock, producto_id) \
    VALUES (?, ?, ?, ?, ?, ?, ?, ?)

query.venta-detalle.list-finalized-by-producto=\
    SELECT v.fecha, d.cantidad \
    FROM venta_detalle d \
    INNER JOIN ventas v ON d.venta_id = v.id \
    WHERE d.producto_id = ? AND v.estado = 'Finalizada' \
    ORDER BY v.fecha DESC \
    OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY
