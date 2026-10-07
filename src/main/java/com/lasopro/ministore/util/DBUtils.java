package com.lasopro.ministore.util;



import java.sql.Connection;
import java.util.List;
import static com.lasopro.ministore.util.Resources.*;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 
 * Clase creada para administrar la conexion a BD
 *
 * @author admin
 */
public class DBUtils {
    

    
    public static final int ROW_AS_ARRAY = 1;
    public static final int ROW_AS_MAP = 2;
    public static String MAINDS= "mainds";
    private final Log log;

    
    public DBUtils(Log log){
        this.log = log;
    }
    
    

    /**
     * Execute an insert/update/delete script to database
     * cnname is the pefix in properties file to get connection information.
     * 
     * Example in properties file you should have the nex property to connecto to a sqlite database
     * app.jdbc.url=jdbc:sqlite:mydatabase.db
     * 
     * and you can include properties for login:
     * 
     * #app.jdbc.user=USERNAME
     * #app.jdbc.password=PASSWORD
     * 
     * @param cnname connection name, configured in the properties file
     * @param dbname database name to execute this script, null if no required
     * @param sql script SQL to execute, example: select * from User where name = ?
     * @param params parameters for this script SQL, parameters in the "sql" script should be mapped as ?
     * @return The information resultin is about the script execution, errors, affected rows, etc.
     */
    public Result executeWithResult(String cnname, String dbname , String sql, Object... params){
        
        log.info("Executing script over "+p(cnname+".jdbc.url")+": " + sql);
        log.info("Parameters: "+Arrays.toString(params));
        
        Result result = new Result();
        Statement st = null;
        String separator = p(cnname+".sql.separator");
        
        List<String> sentences = parseSQL(separator,sql);
        
        if(sentences==null || sentences.isEmpty()){
            throw new RuntimeException("No  se recibieron sentencias" );
        }
        
        if(sentences.size()>1 && (params != null && params.length>0)){
            throw new RuntimeException("Un script de sentencias multiples no puede recibir parametros");
        }
        
        try(Connection cnn = getConnection(cnname)){
            if(dbname != null) cnn.setCatalog(dbname);
            
            result.messages = "Executing "+sentences.size()+" sentences\n";
            result.success = true;
            result.affectedRows = 0;
            boolean sqlExecuteResult = false;
                    
            for(String s: sentences){
                if(s.trim().isEmpty()) continue;
                if(params == null || params.length == 0){
                    st = cnn.createStatement();
                    sqlExecuteResult = st.execute(s);
                }else{
                    st = cnn.prepareStatement(sql);
                    int index = 1;
                    for(Object p : params){
                        setParameter((PreparedStatement)st, index, p);
                        index++;
                    }
                    sqlExecuteResult = ((PreparedStatement)st).execute();
                }
                result.affectedRows += st.getUpdateCount();
                result.success = result.success & sqlExecuteResult;
                result.messages += readAllMessages(st) + "\n";
            }
        }catch(Exception err){
            log.err("Error executing script "+sql+": "+ err.getMessage(),err);
            result.affectedRows = 0;
            result.success = false;
            result.error = err;
            if(st !=null){
                try {
                    result.messages = readAllMessages(st);
                } catch (Exception e) {
                }
            }
        }
        return result;
    }
    
    
    
    
    /**
     * Execute an insert/update/delete script to database
     * cnname is the pefix in properties file to get connection information.
     * Example in properties file you should have the nex property to connecto to a sqlite database
     * app.jdbc.url=jdbc:sqlite:mydatabase.db
     * 
     * and you can include properties for login:
     * 
     * #app.jdbc.user=USERNAME
     * #app.jdbc.password=PASSWORD
     * 
     * @param cnname
     * @param sql
     * @param params
     * @return 
     */
    public boolean execute(String cnname, String sql, Object... params){
        Result res = executeWithResult(cnname, null, sql, params);
        if(res.getError() != null){
            throw new RuntimeException(res.getError().getClass().getSimpleName()+": "+res.getError().getMessage(),res.getError());
        }
        return res.success;
    }
    
    /**
     * Execute a select script to database.
     * 
     * cnname is the pefix in properties file to get connection information.
     * Example in properties file you should have the nex property to connecto to a sqlite database
     * app.jdbc.url=jdbc:sqlite:mydatabase.db
     * 
     * and you can include properties for login:
     * 
     * #app.jdbc.user=USERNAME
     * #app.jdbc.password=PASSWORD
     * 
     * @param cnname
     * @param sql
     * @param params
     * @return 
     */
    public List<Object[]> query(String cnname, String sql, Object... params){
        List result =  __query(ROW_AS_ARRAY,cnname,sql,params);
        return (List<Object[]>)result;
    }
    
    /**
     * Similar to query function but you will get a HashMap for every row, c
     * containing the column name an its value
     * @param cnname
     * @param sql
     * @param params
     * @return 
     */
    public List<Map<String,Object>> queryAsMap(String cnname, String sql, Object... params){
        List result =  __query(ROW_AS_MAP,cnname,sql,params);
        return (List<Map<String,Object>>)result;
    }
    
    
    
    /**
     * Similar to query function but you will get a HashMap for every row, c
     * containing the column name an its value
     * @param cnname
     * @param sql
     * @param params
     * @return 
     */
    public List<Map<String,Object>> queryAsMapWithPagination(String cnname, String sql, Object... params){
        List result =  __query(ROW_AS_MAP,cnname,sql,params);
        return (List<Map<String,Object>>)result;
    }
    
    
    /**
     * With this function you can get a Result with execution information
     * and the Result also contains all the resulting result sets data 
     * containing the column name an its value
     * @param cnname
     * @param dbname
     * @param sql
     * @param params
     * @return 
     */
    public Result  queryWithResult(String cnname, String dbname, String sql, Object... params){
        return __queryWithResult(ROW_AS_MAP, cnname,dbname, sql, params);
    }
    
    
    /**
     * Execute an script that returns only one value (one row, one column)
     * @param cnname the database connection name configured in properties
     * @param dbname the catalog/database name, null = default catalog
     * @param sql the script to be execute
     * @param params the parameters if script accept parameters
     * @return 
     */
    public Object  queryOneValueSelect(String cnname, String dbname, String sql, Object... params){
        Result result = queryWithResult(cnname,dbname, sql, params);
        if(result.isSuccess()){
            Iterator rs = result.getData();
            if(rs!=null && rs.hasNext()){
                List li = (List)rs.next();
                if(li!=null && !li.isEmpty()){
                    Map mp = (Map)li.get(0);
                    if(!mp.values().isEmpty()){
                        Object obj = mp.values().iterator().next();
                        return obj;
                    }
                }
            }
        }else{
            throw new RuntimeException(result.getError());
        }
        return null;
    }
    
    
    public boolean executeBatch(String cnname,String dbname, String sql, int batchMaxSize, List<Object[]> paramsList){
        log.info("Executing query over "+p(cnname+".jdbc.url")+": " + sql);
        if(cnname == null || sql == null ){
            log.info("There is not a connection or query configured ("+sql+"/"+cnname+")");
            return false;
        }
        
        try(Connection cnn = getConnection(cnname)) {
            if(cnn == null){
                throw new Exception("Cant create connection for "+cnname);
            }
            if(dbname != null) cnn.setCatalog(dbname);
            PreparedStatement st = cnn.prepareStatement(sql);
            int count=1;
            for(Object[] params: paramsList){
                int index = 1;
                for(Object p : params){
                    setParameter(st, index, p);
                    index++;
                }
                st.addBatch();
                
                if(count % batchMaxSize == 0){
                    st.executeBatch();
                }
                count++;
            }
            st.executeBatch();
            return true;
        } catch (Exception e) {
            log.err("Error executing query: "+sql+" ["+cnname+"]"+e.getMessage(),e);
            return false;
        }
    }
    
    
    private Result __queryWithResult(int rowResultType,String cnname, String dbname,String sql, Object... params){
        
        log.info("Executing query over "+p(cnname+".jdbc.url")+": " + sql);
        log.info("Parameters: "+Arrays.toString(params));
        
        Result result = new Result();
        Statement st = null;
        
        if(cnname == null || sql == null ){
            log.info("There is not a connection or query configured ("+sql+"/"+cnname+")");
            return null;
        }

        try(Connection cnn = getConnection(cnname)) {
            
            if(cnn == null){
                throw new Exception("Cant create connection for "+cnname);
            }
            
            if(dbname != null) cnn.setCatalog(dbname);
            
            ResultSet rs;
            if(params == null || params.length == 0){
                st = cnn.createStatement();
                st.setQueryTimeout(1800);
                st.execute(sql);
                rs = st.getResultSet();
            }else{
                st = cnn.prepareStatement(sql);
                st.setQueryTimeout(1800);
                int index = 1;
                for(Object p : params){
                    setParameter((PreparedStatement)st, index, p);
                    index++;
                }
                ((PreparedStatement)st).execute();
                rs = ((PreparedStatement)st).getResultSet();
            }
            List<List<Map<String,Object>>> resList = new ArrayList();
            do{
                ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();
                List res = new ArrayList<>();
                resList.add(res);
                //Get all resultset
                while(rs.next()){

                    if( rowResultType == ROW_AS_MAP){
                        Map<String,Object> hm = new LinkedHashMap<>();
                        for(int i=1;i<=cols;i++){
                            hm.put((String)md.getColumnLabel(i), getValue(rs, i, md.getColumnTypeName(i)));
                        }
                        res.add(hm);
                    }else{
                        Object[] obj = new Object[cols];
                        for(int i=1;i<=cols;i++){
                            obj[i-1] = getValue(rs, i, md.getColumnTypeName(i));
                        }
                        res.add(obj);
                    }
                }
            }while(st.getMoreResults());
            result.affectedRows = 0;
            result.data = resList.iterator();
            result.success = true;
            result.messages = readAllMessages(st);
        } catch (Exception e) {
            log.err("Error executing query: "+sql+" ["+cnname+"] : "+e.getMessage(),e);
            result.affectedRows = 0;
            result.success = false;
            result.error = e;
            if(st !=null){
                try {
                    result.messages = readAllMessages(st);
                    log.info(result.messages);
                } catch (Exception e2) {}
            }
        }
        return result;
    }
    
    private List __query(int rowResultType,String cnname,String sql, Object... params){
        Result result = __queryWithResult(rowResultType, cnname,null, sql, params);
        
        if(result.getError() != null){
            throw new RuntimeException(result.getError());
        }
        
        if(result.data.hasNext()){
            return result.data.next();
        }
        return new ArrayList<HashMap<String,Object>>();
    }

    
    
    public List<String> getAvailableDatabases(String cnname) throws SQLException{
        List<String> dbs = new ArrayList<>();
        try(Connection cnn = getConnection(cnname)){
            DatabaseMetaData metaData = cnn.getMetaData();
            ResultSet rs = metaData.getCatalogs();
            while (rs.next()) { dbs.add(rs.getString("TABLE_CAT")); }
        }
        return dbs;
    }
    
    
    public static Connection getConnection(String cnname) throws SQLException{
        
        String jdbcurl=p(cnname+".jdbc.url");
        String jdbcuser = p(cnname+".jdbc.user");
        String jdbcpass = p(cnname+".jdbc.password");
        try {
            jdbcpass = CryptoUtils.decrypt(jdbcpass);
        } catch (Exception e) {
        }
        boolean readOnly = false;
        try {
            readOnly = "true".equalsIgnoreCase(p(cnname+".jdbc.readOnly"));
        } catch (Exception e) {}
        
        if(jdbcurl == null){
            throw new RuntimeException("jdbc connection with name "+cnname+" not found in properties");
        }
        
        jdbcurl = jdbcurl.trim();
        
        try {
           Class.forName(p(cnname+".jdbc.driverClass"));    
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }
        
        Connection cnn;
        if(jdbcurl.contains("integratedSecurity=true")){
            cnn= DriverManager.getConnection(jdbcurl);
        }else if(jdbcuser != null && jdbcpass != null){
            jdbcpass = jdbcpass.trim();
            jdbcuser = jdbcuser.trim();
            cnn= DriverManager.getConnection(jdbcurl, jdbcuser, jdbcpass);
        }else{
            cnn= DriverManager.getConnection(jdbcurl);
        }
        cnn.setReadOnly(readOnly);
        
        return cnn;
    }
    
    public String dbTypeToString(Object obj){
        
        if(obj == null) return "";
        
        if(obj instanceof LocalDateTime){
            return Util.dateToLocaleString((LocalDateTime)obj);
        }
        
        if(obj instanceof LocalDate){
            return Util.dateToLocaleString((LocalDate)obj);
        }
        
        if(obj instanceof Date){
            return Util.dateToLocaleString((Date)obj);
        }
        if(obj instanceof Double || obj instanceof Float || obj instanceof BigDecimal){
            return NumberFormat.getInstance().format(((Number)obj).doubleValue());
        }
        
        return obj.toString();
        
    }
    
    private String readAllMessages(Statement st) throws SQLException{
        StringBuilder sb = new StringBuilder();
        SQLWarning w = st.getWarnings();
        while(w!=null){
            sb.append("[").append(w.getErrorCode())
                    .append("/").append(w.getSQLState())
                    .append("] ").append(w.getMessage()).append("\n");
            w = w.getNextWarning();
        }
        return sb.toString();
    }
    
    public static List<String> parseSQL(String separator,String sql) {
        ArrayList<String> arr = new ArrayList<>();
        if(separator == null){
            arr.add(sql);
            return arr;
        }
        String[] list = sql.split(separator);
        arr.addAll(Arrays.asList(list));
        return arr;
    }

    
    
    public class Result{
        private boolean success;
        private String messages;
        private Throwable error;
        private long affectedRows;
        private Iterator<List<Map<String,Object>>> data;
        
        public boolean isSuccess() {
            return success;
        }

        public String getMessages() {
            return messages;
        }

        public Throwable getError() {
            return error;
        }

        public long getAffectedRows() {
            return affectedRows;
        }

        public Iterator<List<Map<String, Object>>> getData() {
            return data;
        }

        
        
    }
    
    
    public static String getStartQuotedIdentifier(String ds) {
        
        if(Util.in(ds, "PARQUET","CSV")){
            return "";
        }
        

        if (Resources.p(ds + ".jdbc.startQuotedIdentifier") != null) {
            return Resources.p(ds + ".jdbc.startQuotedIdentifier");
        }

        String url = Resources.p(ds + ".jdbc.url");

        if (url.contains("jdbc:oracle")) {
            return "\"";
        }

        if (url.contains("jdbc:sqlserver")) {
            return "[";
        }

        return "";
    }

    public static String getEndQuotedIdentifier(String ds) {

        if(Util.in(ds, "PARQUET","CSV")){
            return "";
        }
        
        if (Resources.p(ds + ".jdbc.endQuotedIdentifier") != null) {
            return Resources.p(ds + ".jdbc.endQuotedIdentifier");
        }

        String url = Resources.p(ds + ".jdbc.url");

        if (url.contains("jdbc:oracle")) {
            return "\"";
        }

        if (url.contains("jdbc:sqlserver")) {
            return "]";
        }

        return "";
    }
    
    
    public static String getDateSqlFormatted(Date date,String ds) {

        String url = Resources.p(ds + ".jdbc.url");
        
        String sdate = Util.dateToInternalFormat(date);

        if (url.contains("jdbc:oracle")) {
            return "DATE'"+sdate+"'";
        }

        if (url.contains("jdbc:sqlserver")) {
            return "'"+sdate+"'";
        }

        return "'"+sdate+"'";
    }
    
    
    public static String getStringDataTypeName(String ds) {

        String url = Resources.p(ds + ".jdbc.url");

        if (url.contains("jdbc:oracle")) {
            return "VARCHAR2";
        }

        if (url.contains("jdbc:sqlserver")) {
            return "VARCHAR";
        }

        return "";
    }
    
    public static String getDatabaseType(String ds){
        
        if(ds == null){
            return null;
        }
        
        String url = Resources.p(ds + ".jdbc.url");
        
        if(url == null){
            return "nosupported";
        }
        
        if (url.contains("jdbc:oracle")) {
            return "oracle";
        }
        if (url.contains("jdbc:sqlserver")) {
            return "sqlserver";
        }
        if(url.contains("jdbc:postgresql")){
            return "postgresql";
        }
            
        return "notsupported";
    }
    
    
    public static Object getValue(ResultSet rs, int num, String columnType) throws SQLException, IOException {
        Object value;
        if(columnType.contains("XML")) {
            value = rs.getSQLXML(num).getString();
        } else if (columnType.contains("CLOB")) {
            value = readerToBytes(rs.getClob(num).getCharacterStream());
        } else if (columnType.contains("BLOB")) {
            value = inputStreamToBytes(rs.getBlob(num).getBinaryStream());
        } else if (columnType.contains("BINARY")) {
            value = rs.getBytes(num);
        } else if (columnType.contains("DATETIME")){
            value = rs.getObject(num);
            if(value instanceof Long sl){
                value = new Date(sl);
            } 
        }else {
            value = rs.getObject(num);
        }
        return value;
    }

    private static void setParameter(PreparedStatement ps, int index, Object value) throws SQLException {
        if (value instanceof Date date) {
            ps.setTimestamp(index, new Timestamp(date.getTime()));
        } else {
            ps.setObject(index, value);
        }
    }
    
    
    public static void setValue(PreparedStatement ps, int num, Object value, int type, Connection cn) throws SQLException, IOException {

        if (value instanceof String) {
            String charset = Resources.p("app.charset");
            if (!charset.equals("UTF-8")) {
                byte[] bytesISO = ((String) value).getBytes(Charset.forName("ISO-8859-15"));
                value = new String(bytesISO, Charset.forName("ISO-8859-15"));
            }

            switch (type) {
                case Types.SQLXML:
                    SQLXML xml = cn.createSQLXML();
                    xml.setString((String) value);
                    ps.setSQLXML(num, xml);
                    break;
                case Types.CLOB:
                    Clob clob = cn.createClob();
                    clob.setString(1, (String) value);
                    ps.setClob(num, clob);
                    break;
                case Types.BLOB:
                    Blob blob = cn.createBlob();
                    blob.setBytes(1, ((String) value).getBytes());
                    ps.setBlob(num, blob);
                    break;
                default:
                    setParameter(ps, num, value);
                    break;
            }

        } else {
            setParameter(ps, num, value);
        }
    }


    private static byte[] inputStreamToBytes(InputStream is) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(is.available());) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            is.close();
            return baos.toByteArray();
        }
    }

    private static byte[] readerToBytes(Reader reader) throws IOException {
        try (CharArrayWriter caw = new CharArrayWriter();) {
            char[] buffer = new char[4096];
            int len;
            while ((len = reader.read(buffer)) != -1) {
                caw.write(buffer, 0, len);
            }
            reader.close();
            return new String(caw.toCharArray()).getBytes("UTF-8");
        }
    }
    
    public static String buildSimpleSqlSelect(String tableName, String order,String limit, String offset, String where){
        
        
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ").append(tableName);
        
        if(where  != null && !where.isBlank()){
            sb.append(" WHERE ").append(where);
        }
        
        if(order != null && !order.isBlank()){
            sb.append(" ORDER BY ").append(order).append(" ");
            
            if(limit != null && !limit.isBlank() && 
                    offset != null && !offset.isBlank()){
                String pag = Resources.p("default.sql.pagination");
                pag = pag.replace("{LIMIT}", limit+"");
                pag = pag.replace("{OFFSET}", offset+"");
                sb.append(" ").append(pag).append(" ");
            }
            
        }
        return sb.toString();
    }

    
    public int count(String ds,String tableName){
        
        return count(ds, tableName, null);
        
    }
    
    
    public int count(String ds,String tableName, String filters){
        
        String sql = "select count(1) from "+tableName;
        
        if(filters != null && !filters.isBlank()){
            sql += " where "+filters;
        }
            
        
        Object obj = queryOneValueSelect(ds, null, sql);
        
        return ((Number)obj).intValue();
        
    }

    @FunctionalInterface
    public interface TransactionCallback {
        void run(Connection conn) throws SQLException;
    }

    public void runInTransaction(String cnname, TransactionCallback callback) {
        try (Connection conn = getConnection(cnname)) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                callback.run(conn);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } catch (RuntimeException e) {
                conn.rollback();
                throw e;
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException(e);
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int executeUpdate(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                setParameter(ps, i + 1, params[i]);
            }
            return ps.executeUpdate();
        }
    }

    public static Object queryScalar(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                setParameter(ps, i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject(1);
                }
                return null;
            }
        }
    }

    
}
