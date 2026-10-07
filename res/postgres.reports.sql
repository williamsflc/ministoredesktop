
#REPORTE: Productos proximos a agotarse o agotados
select codigo, codigo_proveedor, nombre, precio_compra, stock, min_stock
from productos
where stock <= min_stock
  and activo = 'Activo'
order by 3


#REPORTE: Total de ventas diarias con ganancia [G]
select cast(v.fecha as date) as dia,
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
where v.fecha > current_timestamp - interval '45 days'
  and v.estado = 'Finalizada'
group by cast(v.fecha as date)
order by 1


#REPORTE: Total de ventas por mes [G]
select to_char(v.fecha, 'YYYY-MM') as dia,
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
where v.fecha > current_timestamp - interval '365 days'
  and v.estado = 'Finalizada'
group by to_char(v.fecha, 'YYYY-MM')
order by 1


#REPORTE: Promedio de ventas por día de la semana [G]
select case extract(dow from dia)::integer
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
    select cast(v.fecha as date) as dia,
           sum(v.total) - sum(v.descuento) as total
    from ventas v
    where v.fecha > current_timestamp - interval '365 days'
      and v.estado = 'Finalizada'
    group by cast(v.fecha as date)
) x
group by extract(dow from dia)::integer
order by extract(dow from dia)::integer


#REPORTE: Promedio de ventas por día del mes [G]
select to_char(dia, 'DD') as dia,
       avg(total) as total
from (
    select cast(v.fecha as date) as dia,
           sum(v.total) - sum(v.descuento) as total
    from ventas v
    where v.fecha > current_timestamp - interval '365 days'
      and v.estado = 'Finalizada'
    group by cast(v.fecha as date)
) x
group by to_char(dia, 'DD')
order by 1


#REPORTE: Promedio de ventas por hora del día [G]
select to_char(hora, 'HH24') as dia,
       sum(x.total / greatest(
           extract(epoch from (localtimestamp - y.inicio)) / 86400.0,
           1
       )) as promedio,
       min(x.total) as minimo,
       max(x.total) as maximo,
       count(*) as dias_con_venta
from (
    select date_trunc('hour', v.fecha) as hora,
           sum(v.total) - sum(v.descuento) as total
    from ventas v
    where v.fecha > current_timestamp - interval '365 days'
      and v.estado = 'Finalizada'
    group by date_trunc('hour', v.fecha)
) x
cross join (
    select min(v2.fecha) as inicio
    from ventas v2
    where v2.fecha > current_timestamp - interval '365 days'
      and v2.estado = 'Finalizada'
) y
group by to_char(hora, 'HH24')
order by 1


#REPORTE: Productos más vendidos (últimos 6 meses)-
select p.nombre || ' (' || p.codigo || ')' as producto,
       sum(d.cantidad) as cantidad_vendida,
       sum(d.precio * d.cantidad) as monto_total_vendido,
       sum(d.precio * d.cantidad) - sum(p.precio_compra * d.cantidad) as ganancia
from venta_detalle d
join productos p on d.producto_id = p.id
join ventas v on v.id = d.venta_id
where v.fecha > current_timestamp - interval '185 days'
  and v.estado = 'Finalizada'
group by p.id, p.nombre, p.codigo
order by 2 desc

#REPORTE: Últimas ventas (15 días)-
select '#'||v.id as Venta,
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
where v.fecha > current_timestamp - interval '15 days'
  and v.estado = 'Finalizada'
order by 1 desc


#REPORTE: Inversion productos y recuperación
select round(sum(precio_compra * stock)::numeric, 2) as inversion_producto,
       round(sum(precio_venta * stock)::numeric, 2) as recuperable,
       round((
           100 * (sum(precio_venta * stock) - sum(precio_compra * stock))
           / nullif(sum(precio_compra * stock), 0)
       )::numeric, 1)::text || '%' as porcentaje_recuperable
from productos p
where p.activo = 'Activo'


#REPORTE: Total de ventas por semana [G]
select TO_CHAR(v.fecha, 'YY-MM IW') as semana,
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
where v.fecha > current_timestamp - interval '365 days'
  and v.estado = 'Finalizada'
group by TO_CHAR(v.fecha, 'YY-MM IW')
order by 1


#REPORTE: Total ventas en mismo periodo de tiempo [G]
select TO_CHAR(v.fecha, 'YY-MM')||' (Del 1 a '|| EXTRACT(DAY FROM CURRENT_DATE) ||')' as MES,
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
where v.fecha > current_timestamp - interval '365 days'
  and v.estado = 'Finalizada'
  and EXTRACT(DAY FROM v.fecha) <= EXTRACT(DAY FROM CURRENT_DATE)
group by TO_CHAR(v.fecha, 'YY-MM')
order by 1