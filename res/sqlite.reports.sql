#REPORTE: Productos proximos a agotarse o agotados
select codigo, codigo_proveedor, nombre, precio_compra, stock, min_stock
from productos
where stock <= min_stock
  and activo = 'Activo'
order by 3


#REPORTE: Total de ventas diarias con ganancia [G]
select date(v.fecha / 1000.0, 'unixepoch', 'localtime') as dia,
       sum(v.total) - sum(v.descuento) as total_percibido,
       sum(v.descuento) as total_descuentos,
       sum(p.precio_compra) as total_precio_compra,
       sum(v.total) - sum(p.precio_compra) - sum(v.descuento) as total_ganancia
from ventas v
join (
    select d.venta_id,
           sum(p.precio_compra * d.cantidad) as precio_compra
    from venta_detalle d
    join productos p on d.producto_id = p.id
    group by d.venta_id
) p on v.id = p.venta_id
where v.fecha > unixepoch('now', '-45 days') * 1000
  and v.estado = 'Finalizada'
group by date(v.fecha / 1000.0, 'unixepoch', 'localtime')
order by 1


#REPORTE: Total de ventas por mes [G]
select strftime('%Y-%m', v.fecha / 1000.0, 'unixepoch', 'localtime') as dia,
       sum(v.total) - sum(v.descuento) as total_percibido,
       sum(v.descuento) as total_descuentos,
       sum(p.precio_compra) as total_precio_compra,
       sum(v.total) - sum(p.precio_compra) - sum(v.descuento) as total_ganancia
from ventas v
join (
    select d.venta_id,
           sum(p.precio_compra * d.cantidad) as precio_compra
    from venta_detalle d
    join productos p on d.producto_id = p.id
    group by d.venta_id
) p on v.id = p.venta_id
where v.fecha > unixepoch('now', '-365 days') * 1000
  and v.estado = 'Finalizada'
group by strftime('%Y-%m', v.fecha / 1000.0, 'unixepoch', 'localtime')
order by 1


#REPORTE: Promedio de ventas por día de la semana [G]
select case cast(strftime('%w', dia) as integer)
           when 0 then 'Domingo'
           when 1 then 'Lunes'
           when 2 then 'Martes'
           when 3 then 'Miércoles'
           when 4 then 'Jueves'
           when 5 then 'Viernes'
           when 6 then 'Sábado'
       end as dia,
       avg(total) as total
from (
    select date(v.fecha / 1000.0, 'unixepoch', 'localtime') as dia,
           sum(v.total) - sum(v.descuento) as total
    from ventas v
    where v.fecha > unixepoch('now', '-365 days') * 1000
      and v.estado = 'Finalizada'
    group by date(v.fecha / 1000.0, 'unixepoch', 'localtime')
) x
group by cast(strftime('%w', dia) as integer)
order by cast(strftime('%w', dia) as integer)


#REPORTE: Promedio de ventas por día del mes [G]
select strftime('%d', dia) as dia,
       avg(total) as total
from (
    select date(v.fecha / 1000.0, 'unixepoch', 'localtime') as dia,
           sum(v.total) - sum(v.descuento) as total
    from ventas v
    where v.fecha > unixepoch('now', '-365 days') * 1000
      and v.estado = 'Finalizada'
    group by date(v.fecha / 1000.0, 'unixepoch', 'localtime')
) x
group by strftime('%d', dia)
order by 1


#REPORTE: Promedio de ventas por hora del día [G]
select strftime('%H', hora) as dia,
       sum(x.total / max(
           (unixepoch('now') - (y.inicio / 1000.0)) / 86400.0,
           1
       )) as promedio,
       min(x.total) as minimo,
       max(x.total) as maximo,
       count(*) as dias_con_venta
from (
    select strftime('%Y-%m-%d %H:00:00', v.fecha / 1000.0, 'unixepoch', 'localtime') as hora,
           sum(v.total) - sum(v.descuento) as total
    from ventas v
    where v.fecha > unixepoch('now', '-365 days') * 1000
      and v.estado = 'Finalizada'
    group by strftime('%Y-%m-%d %H:00:00', v.fecha / 1000.0, 'unixepoch', 'localtime')
) x
cross join (
    select min(v2.fecha) as inicio
    from ventas v2
    where v2.fecha > unixepoch('now', '-365 days') * 1000
      and v2.estado = 'Finalizada'
) y
group by strftime('%H', hora)
order by 1


#REPORTE: Productos más vendidos (últimos 6 meses)-
select p.nombre || ' (' || p.codigo || ')' as producto,
       sum(d.cantidad) as cantidad_vendida,
       sum(d.precio * d.cantidad) as monto_total_vendido,
       sum(d.precio * d.cantidad) - sum(p.precio_compra * d.cantidad) as ganancia
from venta_detalle d
join productos p on d.producto_id = p.id
join ventas v on v.id = d.venta_id
where v.fecha > unixepoch('now', '-185 days') * 1000
  and v.estado = 'Finalizada'
group by p.id, p.nombre, p.codigo
order by 2 desc


#REPORTE: Últimas ventas (15 días)-
select '#' || v.id as Venta,
       v.fecha as Fecha,
       v.usuario_creo as Vendedor,
       p.nombre || ' (' || p.codigo || ')' as Producto,
       d.cantidad as Cantidad,
       p.precio_compra as Precio_Compra,
       d.precio as Precio_Venta,
       (d.precio * d.cantidad) as Monto,
       ((d.precio * d.cantidad) - (p.precio_compra * d.cantidad)) as Ganancia
from venta_detalle d
join productos p on d.producto_id = p.id
join ventas v on v.id = d.venta_id
where v.fecha > unixepoch('now', '-15 days') * 1000
  and v.estado = 'Finalizada'
order by v.id desc


#REPORTE: Inversion productos y recuperación
select round(sum(precio_compra * stock), 2) as inversion_producto,
       round(sum(precio_venta * stock), 2) as recuperable,
       round(
           100.0 * (sum(precio_venta * stock) - sum(precio_compra * stock))
           / nullif(sum(precio_compra * stock), 0),
           1
       ) || '%' as porcentaje_recuperable
from productos p
where p.activo = 'Activo'


#REPORTE: Total de ventas por semana [G]
select substr(strftime('%Y-%m', v.fecha / 1000.0, 'unixepoch', 'localtime'), 3)
       || ' ' ||
       printf(
           '%02d',
           (
               cast(strftime(
                   '%j',
                   date(v.fecha / 1000.0, 'unixepoch', 'localtime', '-3 days', 'weekday 4')
               ) as integer) - 1
           ) / 7 + 1
       ) as semana,
       sum(v.total) - sum(v.descuento) as total_percibido,
       sum(v.descuento) as total_descuentos,
       sum(p.precio_compra) as total_precio_compra,
       sum(v.total) - sum(p.precio_compra) - sum(v.descuento) as total_ganancia
from ventas v
join (
    select d.venta_id,
           sum(p.precio_compra * d.cantidad) as precio_compra
    from venta_detalle d
    join productos p on d.producto_id = p.id
    group by d.venta_id
) p on v.id = p.venta_id
where v.fecha > unixepoch('now', '-365 days') * 1000
  and v.estado = 'Finalizada'
group by semana
order by 1


#REPORTE: Total ventas en mismo periodo de tiempo [G]
select substr(strftime('%Y-%m', v.fecha / 1000.0, 'unixepoch', 'localtime'), 3)
       || ' (Del 1 a '
       || cast(strftime('%d', 'now', 'localtime') as integer)
       || ')' as MES,
       sum(v.total) - sum(v.descuento) as total_percibido,
       sum(v.descuento) as total_descuentos,
       sum(p.precio_compra) as total_precio_compra,
       sum(v.total) - sum(p.precio_compra) - sum(v.descuento) as total_ganancia
from ventas v
join (
    select d.venta_id,
           sum(p.precio_compra * d.cantidad) as precio_compra
    from venta_detalle d
    join productos p on d.producto_id = p.id
    group by d.venta_id
) p on v.id = p.venta_id
where v.fecha > unixepoch('now', '-365 days') * 1000
  and v.estado = 'Finalizada'
  and cast(strftime('%d', v.fecha / 1000.0, 'unixepoch', 'localtime') as integer)
      <= cast(strftime('%d', 'now', 'localtime') as integer)
group by strftime('%Y-%m', v.fecha / 1000.0, 'unixepoch', 'localtime')
order by 1
