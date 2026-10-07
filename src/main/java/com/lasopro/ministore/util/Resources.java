package com.lasopro.ministore.util;

import com.lasopro.ministore.desktop.MinistoreDesktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 *
 * @author admin
 */
public class Resources {
    
    private static List<Map<String,String>> dbReports;
    
    static Logger log = Logger.getLogger("Resources");
    private static Properties props = null;
    
    public static Properties getProperties(){
        if(props == null){
            p("");
        }
        return props;
    }
    
    public static String p(String key){
        if(props == null){
            loadProperties();
        }
        return props.getProperty(key);
    }

    public static void reload() {
        props = null;
    }

    /**
     * Detecta una instalación nueva sin intentar abrir una conexión.
     * app.setup.inProgress permite reanudar el asistente si se cerró antes
     * de completar todos sus pasos.
     */
    public static boolean needsInitialSetup() {
        Properties fileProps = loadFileProperties();
        String ds = trimToNull(fileProps.getProperty("app.datasource.name"));
        return ds == null
                || trimToNull(fileProps.getProperty(ds + ".jdbc.url")) == null
                || trimToNull(fileProps.getProperty(ds + ".jdbc.driverClass")) == null
                || Boolean.parseBoolean(fileProps.getProperty("app.setup.inProgress", "false"));
    }

    /**
     * Guarda únicamente la configuración local de conexión. Se usa un archivo
     * separado para no modificar los valores generales distribuidos con la app.
     */
    public static synchronized void saveDatabaseProperties(Properties databaseProperties) throws IOException {
        Path directory = Paths.get("res");
        Files.createDirectories(directory);
        Path target = directory.resolve("db.properties");
        Path temporary = directory.resolve("db.properties.tmp");
        try (var output = Files.newOutputStream(temporary)) {
            databaseProperties.store(output, "Configuracion local de base de datos");
        }
        try {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException ex) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
        reload();
    }

    private static Properties loadFileProperties() {
        Properties fileProps = new Properties();
        loadInto(fileProps, Paths.get("res", "app.properties"));
        loadInto(fileProps, Paths.get("res", "db.properties"));
        return fileProps;
    }

    private static void loadInto(Properties destination, Path path) {
        if (!Files.exists(path)) {
            return;
        }
        try (var input = Files.newInputStream(path)) {
            destination.load(input);
        } catch (IOException e) {
            log.warning("No se pudo cargar " + path + ": " + e.getMessage());
        }
    }

    private static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static void loadProperties() {
        props = new Properties();
        try {
            props.load(Files.newInputStream(Paths.get("res","app.properties")));
        } catch (IOException e) {}

        try {
            Path dbProps = Paths.get("res", "db.properties");
            if (Files.exists(dbProps)) {
                props.load(Files.newInputStream(dbProps));
            }
        } catch (IOException e) {}

        try {
            String dbType = props.getProperty("app.db.type");
            if (dbType != null && !dbType.isBlank()) {
                Path sentencesProps = Paths.get("res", dbType.trim() + ".sentences.properties");
                Path sentencesSql = Paths.get("res", dbType.trim() + ".sentences.sql");
                if (Files.exists(sentencesProps)) {
                    props.load(Files.newInputStream(sentencesProps));
                } else if (Files.exists(sentencesSql)) {
                    props.load(Files.newInputStream(sentencesSql));
                }
            }
        } catch (IOException e) {}

        try{
            List<Map<String,Object>> dbconf = (new DBUtils(MinistoreDesktop.log))
                    .queryAsMap(Resources.getDsName(), "select * from config");
            for(Map<String,Object> m : dbconf){
                props.put(m.get("nombre"), m.get("valor"));
            }
        }catch(Throwable e){
            System.out.println("No se pudo obtener la configuración desde base de datos");
        }
    }

    public static Path getLogPath() {
       return Paths.get(p("app.log.file"));
    }
    
    public static String getDsName(){
        return p("app.datasource.name");
    }
    
}
