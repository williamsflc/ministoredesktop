CREATE TABLE APP (
    nombre VARCHAR(100),
    fecha_creacion DATETIME
)
[GO]

CREATE TABLE categorias (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) UNIQUE NOT NULL,
    fecha_creacion DATETIME,
    fecha_modificacion DATETIME,
    usuario_creo VARCHAR(255),
    usuario_modifico VARCHAR(255)
)
[GO]

CREATE TABLE clientes (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    documento VARCHAR(255) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    telefono VARCHAR(100),
    direccion TEXT,
    fecha_creacion DATETIME,
    fecha_modificacion DATETIME,
    usuario_creo VARCHAR(255),
    usuario_modifico VARCHAR(255)
)
[GO]

CREATE TABLE config (
    nombre VARCHAR(255) PRIMARY KEY,
    valor TEXT
)
[GO]

CREATE TABLE desc_x_total_venta (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    venta_id INTEGER,
    tipo VARCHAR(100),
    monto DOUBLE,
    detalle TEXT
)
[GO]

CREATE TABLE historico_inventario (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME NOT NULL,
    proveedor_id INTEGER,
    numero_pedido VARCHAR(255),
    accion VARCHAR(255),
    `user` VARCHAR(255),
    stock_actual DOUBLE,
    nuevo_stock DOUBLE,
    producto_id INTEGER
)
[GO]

CREATE TABLE productos (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(255) UNIQUE NOT NULL,
    codigo_proveedor VARCHAR(255),
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    marca VARCHAR(255),
    precio_compra DOUBLE NOT NULL,
    precio_venta DOUBLE NOT NULL,
    stock DOUBLE NOT NULL DEFAULT 0,
    proveedor_id INTEGER,
    categoria_id INTEGER,
    fecha_creacion DATETIME,
    fecha_modificacion DATETIME,
    usuario_creo VARCHAR(255),
    usuario_modifico VARCHAR(255),
    activo VARCHAR(50),
    min_stock DOUBLE DEFAULT 0
)
[GO]

CREATE TABLE proveedores (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    telefono VARCHAR(100),
    direccion TEXT,
    email VARCHAR(255),
    fecha_creacion DATETIME,
    fecha_modificacion DATETIME,
    usuario_creo VARCHAR(255),
    usuario_modifico VARCHAR(255)
)
[GO]

CREATE TABLE tmp (
    producto_id INTEGER,
    cantidad_vendida DOUBLE
)
[GO]

CREATE TABLE usuario (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    `user` VARCHAR(255) UNIQUE NOT NULL,
    pass TEXT NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    tipo VARCHAR(50) NOT NULL
)
[GO]

CREATE TABLE venta_detalle (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    venta_id INTEGER NOT NULL,
    producto_id INTEGER NOT NULL,
    nombre_producto VARCHAR(255) NOT NULL,
    precio DOUBLE NOT NULL,
    cantidad DOUBLE NOT NULL,
    total DOUBLE NOT NULL,
    descuento DOUBLE
)
[GO]

CREATE TABLE ventas (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    hash VARCHAR(255) NOT NULL,
    fecha DATETIME NOT NULL,
    total DOUBLE NOT NULL,
    descuento DOUBLE NOT NULL,
    descuento_detalle TEXT,
    total_a_pagar DOUBLE NOT NULL,
    efectivo DOUBLE NOT NULL,
    tarjeta DOUBLE NOT NULL,
    cambio DOUBLE NOT NULL,
    cliente_id INTEGER NOT NULL,
    factura_nit VARCHAR(255),
    factura_nombre VARCHAR(255),
    factura_email VARCHAR(255),
    factura_telefono VARCHAR(100),
    factura_direccion TEXT,
    enviar_factura VARCHAR(50),
    estado VARCHAR(50),
    fecha_creacion DATETIME,
    fecha_modificacion DATETIME,
    usuario_creo VARCHAR(255),
    usuario_modifico VARCHAR(255)
)
[GO]

INSERT INTO APP(nombre, fecha_creacion)
VALUES ('MINISTORE', CURRENT_TIMESTAMP)
[GO]

INSERT INTO config(nombre, valor) VALUES
('desc.x.venta.y.prod', 'Habilitado'),
('desc.x.venta.total', 'Habilitado'),
('desc.x.cliente', 'Habilitado'),
('desc.x.producto', 'Habilitado'),
('app.name', 'MiniStore'),
('app.logo', 'res/logo.png'),
('app.currency', 'Q'),
('app.product.page.size', '40'),
('app.log.file', 'app.log'),
('app.payment.method', 'CASH'),
('app.product.code.prefix', 'M'),
('app.product.code.starts', '7001'),
('app.ui.font.size', '13'),
('app.print.font.size', '10'),
('app.invoice.header', 'MINI STORE\n<ubicacion>\n* Comprobante *\nCliente: ${nit_cliente} - ${nombre_cliente}\nAsesor: ${asesor}\nFecha y hora: ${fecha_y_hora}\n'),
('app.invoice.footer', ' ¡Ha sido un placer atenderle! ')
[GO]
