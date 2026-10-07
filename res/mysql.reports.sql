#REPORTE: Productos proximos a agotarse o agotados
select codigo, codigo_proveedor, nombre, precio_compra, stock, min_stock
from productos
where stock <= min_stock
  and activo = 'Activo'
order by 3
