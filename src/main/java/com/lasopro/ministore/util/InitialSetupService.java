package com.lasopro.ministore.util;

import com.lasopro.ministore.db.Config;
import com.lasopro.ministore.db.Usuario;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Operaciones del setup que no dependen de componentes Swing.
 */
public final class InitialSetupService {

    public static final String DATASOURCE_NAME = "default";
    private static final Pattern LOCAL_DATABASE_NAME = Pattern.compile("[A-Za-z0-9_-]+");
    private static final String[] REQUIRED_TABLES = {
        "APP", "categorias", "clientes", "config", "desc_x_total_venta",
        "historico_inventario", "productos", "proveedores", "tmp", "usuario",
        "venta_detalle", "ventas"
    };

    public enum DatabaseType {
        SQLITE("SQLite", "sqlite", "org.sqlite.JDBC", ""),
        POSTGRESQL("PostgreSQL", "postgres", "org.postgresql.Driver",
                "jdbc:postgresql://localhost:5432/ministore"),
        SQL_SERVER("SQL Server", "sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver",
                "jdbc:sqlserver://localhost:1433;databaseName=ministore;encrypt=true;trustServerCertificate=true"),
        ORACLE("Oracle", "oracle", "oracle.jdbc.OracleDriver",
                "jdbc:oracle:thin:@localhost:1521/FREEPDB1"),
        MYSQL("MySQL", "mysql", "com.mysql.cj.jdbc.Driver",
                "jdbc:mysql://localhost:3306/ministore?useSSL=false&serverTimezone=UTC");

        private final String displayName;
        private final String resourceName;
        private final String driverClass;
        private final String sampleUrl;

        DatabaseType(String displayName, String resourceName, String driverClass, String sampleUrl) {
            this.displayName = displayName;
            this.resourceName = resourceName;
            this.driverClass = driverClass;
            this.sampleUrl = sampleUrl;
        }

        public String resourceName() {
            return resourceName;
        }

        public String driverClass() {
            return driverClass;
        }

        public String sampleUrl() {
            return sampleUrl;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public record DatabaseConfiguration(
            DatabaseType type, String jdbcUrl, String username, String password) {
    }

    public DatabaseConfiguration localConfiguration(String databaseName) throws IOException {
        String name = databaseName == null ? "" : databaseName.trim();
        if (!LOCAL_DATABASE_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "El nombre de la base de datos solo puede contener letras, números, guion y guion bajo.");
        }
        Path dataDirectory = Paths.get("data");
        Files.createDirectories(dataDirectory);
        String absolutePath = dataDirectory.resolve(name + ".db").toAbsolutePath().normalize().toString();
        return new DatabaseConfiguration(DatabaseType.SQLITE, "jdbc:sqlite:" + absolutePath, "", "");
    }

    public DatabaseConfiguration remoteConfiguration(
            DatabaseType type, String jdbcUrl, String username, char[] password) {
        if (type == null || type == DatabaseType.SQLITE) {
            throw new IllegalArgumentException("Seleccione un tipo de base de datos remota.");
        }
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            throw new IllegalArgumentException("La JDBC URL es obligatoria.");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }
        return new DatabaseConfiguration(type, jdbcUrl.trim(), username.trim(), new String(password));
    }

    public void testConnection(DatabaseConfiguration configuration) throws SQLException {
        try {
            Class.forName(configuration.type().driverClass());
        } catch (ClassNotFoundException ex) {
            throw new SQLException("No se encontró el driver " + configuration.type().driverClass(), ex);
        }
        try (Connection ignored = openConnection(configuration)) {
            // Abrir y cerrar la conexión es el test.
        }
    }

    public void persistInProgress(DatabaseConfiguration configuration) throws IOException {
        Resources.saveDatabaseProperties(toProperties(configuration, true));
    }

    public void initializeSchema(DatabaseConfiguration configuration) throws IOException, SQLException {
        Path schemaPath = Paths.get("res", configuration.type().resourceName() + ".schema.sql");
        if (!Files.isRegularFile(schemaPath)) {
            throw new IOException("No existe el esquema requerido: " + schemaPath);
        }

        try (Connection connection = openConnection(configuration)) {
            if (schemaExists(connection)) {
                return;
            }
            rejectPartialSchema(connection);

            String script = Files.readString(schemaPath);
            String[] statements = script.split("\\[GO\\]");
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                for (String sql : statements) {
                    String normalized = removeLeadingComments(sql).trim();
                    if (!normalized.isEmpty()) {
                        statement.execute(normalized);
                    }
                }
                connection.commit();
                validateCompleteSchema(connection);
            } catch (SQLException ex) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                throw new SQLException("No se pudo crear el esquema desde " + schemaPath
                        + ": " + ex.getMessage(), ex);
            } finally {
                try {
                    connection.setAutoCommit(previousAutoCommit);
                } catch (SQLException ignored) {
                }
            }
        }
        Resources.reload();
    }

    public void saveApplicationConfiguration(String appName, String logo, String invoiceHeader) {
        if (appName == null || appName.isBlank()) {
            throw new IllegalArgumentException("El nombre de la aplicación es obligatorio.");
        }
        if (logo == null || logo.isBlank()) {
            throw new IllegalArgumentException("La ruta del logo es obligatoria.");
        }
        if (invoiceHeader == null || invoiceHeader.isBlank()) {
            throw new IllegalArgumentException("El encabezado de factura es obligatorio.");
        }
        Map<String, String> values = new HashMap<>();
        values.put("app.name", appName.trim());
        values.put("app.logo", logo.trim());
        values.put("app.invoice.header", invoiceHeader.trim());
        new Config().updateAll(values);
        Resources.reload();
    }

    public void createAdministrator(String username, char[] password, char[] confirmation) {
        String cleanUsername = username == null ? "" : username.trim();
        String cleanPassword = new String(password);
        if (cleanUsername.isBlank()) {
            throw new IllegalArgumentException("El usuario es obligatorio.");
        }
        if (cleanPassword.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
        if (!cleanPassword.equals(new String(confirmation))) {
            throw new IllegalArgumentException("La confirmación de contraseña no coincide.");
        }

        Usuario users = new Usuario();
        Map<String, Object> existing = users.findByUsername(cleanUsername);
        if (existing != null) {
            if ("Administrador".equalsIgnoreCase(String.valueOf(existing.get("tipo")))
                    && PasswordUtils.matches(cleanPassword, existing.get("pass"))) {
                return;
            }
            throw new IllegalArgumentException("El usuario '" + cleanUsername + "' ya existe.");
        }

        Map<String, Object> admin = new HashMap<>();
        admin.put("user", cleanUsername);
        admin.put("pass", cleanPassword);
        admin.put("nombre", cleanUsername);
        admin.put("tipo", "Administrador");
        users.insert(admin);
    }

    public void complete(DatabaseConfiguration configuration) throws IOException {
        Resources.saveDatabaseProperties(toProperties(configuration, false));
    }

    private Properties toProperties(DatabaseConfiguration configuration, boolean inProgress) {
        Properties properties = new Properties();
        properties.setProperty("app.datasource.name", DATASOURCE_NAME);
        properties.setProperty("app.db.type", configuration.type().resourceName());
        properties.setProperty("app.setup.inProgress", Boolean.toString(inProgress));
        properties.setProperty(DATASOURCE_NAME + ".jdbc.url", configuration.jdbcUrl());
        properties.setProperty(DATASOURCE_NAME + ".jdbc.driverClass", configuration.type().driverClass());
        properties.setProperty(DATASOURCE_NAME + ".jdbc.readOnly", "false");
        if (!configuration.username().isBlank()) {
            properties.setProperty(DATASOURCE_NAME + ".jdbc.user", configuration.username());
            properties.setProperty(DATASOURCE_NAME + ".jdbc.password",
                    CryptoUtils.encrypt(configuration.password()));
        }
        properties.setProperty(DATASOURCE_NAME + ".sql.separator", "\\[GO\\]");
        return properties;
    }

    private Connection openConnection(DatabaseConfiguration configuration) throws SQLException {
        if (configuration.username().isBlank()) {
            return DriverManager.getConnection(configuration.jdbcUrl());
        }
        return DriverManager.getConnection(
                configuration.jdbcUrl(), configuration.username(), configuration.password());
    }

    private boolean schemaExists(Connection connection) {
        return tableExists(connection, "APP");
    }

    private boolean tableExists(Connection connection, String table) {
        try (Statement statement = connection.createStatement()) {
            statement.executeQuery("SELECT * FROM " + table + " WHERE 1 = 0");
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    private void rejectPartialSchema(Connection connection) throws SQLException {
        for (String table : REQUIRED_TABLES) {
            if (tableExists(connection, table)) {
                throw new SQLException("La base de datos contiene un esquema parcial (tabla "
                        + table + "). Use una base de datos vacía o elimine el esquema incompleto.");
            }
        }
    }

    private void validateCompleteSchema(Connection connection) throws SQLException {
        StringBuilder missing = new StringBuilder();
        for (String table : REQUIRED_TABLES) {
            if (!tableExists(connection, table)) {
                if (!missing.isEmpty()) {
                    missing.append(", ");
                }
                missing.append(table);
            }
        }
        if (!missing.isEmpty()) {
            throw new SQLException("El esquema está incompleto. Faltan las tablas: " + missing);
        }
    }

    private String removeLeadingComments(String sql) {
        return sql.replaceFirst("(?s)^\\s*(?:--[^\\r\\n]*(?:\\r?\\n|$)\\s*)*", "");
    }
}
