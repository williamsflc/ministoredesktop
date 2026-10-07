CREATE TABLE APP (
    nombre NVARCHAR(100),
    fecha_creacion DATETIME2
)
[GO]

CREATE TABLE categorias (
    id INTEGER IDENTITY(1,1) PRIMARY KEY,
    nombre NVARCHAR(255) UNIQUE NOT NULL,
    fecha_creacion DATETIME2,
    fecha_modificacion DATETIME2,
    usuario_creo NVARCHAR(255),
    usuario_modifico NVARCHAR(255)
)
[GO]

CREATE TABLE clientes (
    id INTEGER IDENTITY(1,1) PRIMARY KEY,
    documento NVARCHAR(255) NOT NULL,
    nombre NVARCHAR(255) NOT NULL,
    email NVARCHAR(255),
    telefono NVARCHAR(100),
    direccion NVARCHAR(MAX),
    fecha_creacion DATETIME2,
    fecha_modificacion DATETIME2,
    usuario_creo NVARCHAR(255),
    usuario_modifico NVARCHAR(255)
)
[GO]

CREATE TABLE config (
    nombre NVARCHAR(255) PRIMARY KEY,
    valor NVARCHAR(MAX)
)
[GO]

CREATE TABLE desc_x_total_venta (
    id INTEGER IDENTITY(1,1) PRIMARY KEY,
    venta_id INTEGER,
    tipo NVARCHAR(100),
    monto FLOAT,
    detalle NVARCHAR(MAX)
)
[GO]

CREATE TABLE historico_inventario (
    id INTEGER IDENTITY(1,1) PRIMARY KEY,
    fecha DATETIME2 NOT NULL,
    proveedor_id INTEGER,
    numero_pedido NVARCHAR(255),
    accion NVARCHAR(255),
    [user] NVARCHAR(255),
    stock_actual FLOAT,
    nuevo_stock FLOAT,
    producto_id INTEGER
)
[GO]

CREATE TABLE productos (
    id INTEGER IDENTITY(1,1) PRIMARY KEY,
    codigo NVARCHAR(255) UNIQUE NOT NULL,
    codigo_proveedor NVARCHAR(255),
    nombre NVARCHAR(255) NOT NULL,
    descripcion NVARCHAR(MAX),
    marca NVARCHAR(255),
    precio_compra FLOAT NOT NULL,
    precio_venta FLOAT NOT NULL,
    stock FLOAT NOT NULL DEFAULT 0,
    proveedor_id INTEGER,
    categoria_id INTEGER,
    fecha_creacion DATETIME2,
    fecha_modificacion DATETIME2,
    usuario_creo NVARCHAR(255),
    usuario_modifico NVARCHAR(255),
    activo NVARCHAR(50),
    min_stock FLOAT DEFAULT 0
)
[GO]

CREATE TABLE proveedores (
    id INTEGER IDENTITY(1,1) PRIMARY KEY,
    nombre NVARCHAR(255) NOT NULL,
    telefono NVARCHAR(100),
    direccion NVARCHAR(MAX),
    email NVARCHAR(255),
    fecha_creacion DATETIME2,
    fecha_modificacion DATETIME2,
    usuario_creo NVARCHAR(255),
    usuario_modifico NVARCHAR(255)
)
[GO]

CREATE TABLE tmp (
    producto_id INTEGER,
    cantidad_vendida FLOAT
)
[GO]

CREATE TABLE usuario (
    id INTEGER IDENTITY(1,1) PRIMARY KEY,
    [user] NVARCHAR(255) UNIQUE NOT NULL,
    pass NVARCHAR(MAX) NOT NULL,
    nombre NVARCHAR(255) NOT NULL,
    tipo NVARCHAR(50) NOT NULL
)
[GO]

CREATE TABLE venta_detalle (
    id INTEGER IDENTITY(1,1) PRIMARY KEY,
    venta_id INTEGER NOT NULL,
    producto_id INTEGER NOT NULL,
    nombre_producto NVARCHAR(255) NOT NULL,
    precio FLOAT NOT NULL,
    cantidad FLOAT NOT NULL,
    total FLOAT NOT NULL,
    descuento FLOAT
)
[GO]

CREATE TABLE ventas (
    id INTEGER IDENTITY(1,1) PRIMARY KEY,
    hash NVARCHAR(255) NOT NULL,
    fecha DATETIME2 NOT NULL,
    total FLOAT NOT NULL,
    descuento FLOAT NOT NULL,
    descuento_detalle NVARCHAR(MAX),
    total_a_pagar FLOAT NOT NULL,
    efectivo FLOAT NOT NULL,
    tarjeta FLOAT NOT NULL,
    cambio FLOAT NOT NULL,
    cliente_id INTEGER NOT NULL,
    factura_nit NVARCHAR(255),
    factura_nombre NVARCHAR(255),
    factura_email NVARCHAR(255),
    factura_telefono NVARCHAR(100),
    factura_direccion NVARCHAR(MAX),
    enviar_factura NVARCHAR(50),
    estado NVARCHAR(50),
    fecha_creacion DATETIME2,
    fecha_modificacion DATETIME2,
    usuario_creo NVARCHAR(255),
    usuario_modifico NVARCHAR(255)
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
('app.invoice.header', 'MINI STORE
<ubicacion>
* Comprobante *
Cliente: ${nit_cliente} - ${nombre_cliente}
Asesor: ${asesor}
Fecha y hora: ${fecha_y_hora}
'),
('app.invoice.footer', ' ¡Ha sido un placer atenderle! ')
[GO]
