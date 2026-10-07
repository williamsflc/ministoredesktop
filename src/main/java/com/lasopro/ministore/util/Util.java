/*
 * Copyright (C) 2018 Williams Lopez - JApps
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.lasopro.ministore.util;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

/**
 *
 * @author Williams Lopez - JApps
 */
public class Util {
    /**
     * Date format for show to users
     */
    public static SimpleDateFormat localeDateFormat;
    public static DateTimeFormatter localDateTimeFormatter;
    public static DateTimeFormatter localDateFormatter;
    /**
     * Date format for application internal
     */
    public static SimpleDateFormat internalDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    public static SimpleDateFormat internalDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    
    public static DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
    public static DecimalFormat decimalFormat = new DecimalFormat("###0.00",symbols);
    
    /**
     * Converts the date object to string in a format for user
     * @param date
     * @return 
     */
    public static String dateToLocaleString(Date date){
        if(localeDateFormat == null){
             localeDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        }
        return localeDateFormat.format(date);
    }
    
    /**
     * Converts the date object to string in a format for user
     * @param date
     * @return 
     */
    public static String dateToLocaleString(LocalDateTime date){
        if(localDateTimeFormatter == null){
            localDateTimeFormatter =  java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        }
        return localDateTimeFormatter.format(date);
    }
    
    /**
     * Converts the date object to string in a format for user
     * @param date
     * @return 
     */
    public static String dateToLocaleString(LocalDate date){
        if(localDateFormatter == null){
            localDateFormatter =  java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        }
        return localDateFormatter.format(date);
    }
    
    /**
     * Converts the date object to string in standar format
     * @param date
     * @return 
     */
    public static String dateToInternalFormat(Date date){
        return internalDateFormat.format(date);
    }
    
    public static String dateTimeToInternalFormat(Date date){
        return internalDateTimeFormat.format(date);
    }
    
    /**
     * Execute process
     * @param workingDirectory
     * @param command
     * @return
     * @throws Exception 
     */
    public static String execute(Path workingDirectory, String... command) throws Exception{
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workingDirectory.toFile());
        Process process= pb.start();
        process.waitFor();
        InputStream is = process.getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int c;
        while ((c = is.read()) > 0) {
            os.write(c);
        }
        os.write('\n');
        is = process.getErrorStream();
        while ((c = is.read()) > 0) {
            os.write(c);
        }
        return os.toString();
    
    }
    
    /**
     * Get the parent JFrame of a component
     * @param comp
     * @return 
     */
    public static JFrame getParentWindow(java.awt.Component comp){
        java.awt.Component parent = comp.getParent();
        do{
            if(parent instanceof JFrame){
                return (JFrame)parent;
            }
        }while((parent=parent.getParent())!=null);
        return null;
    }
    
    /**
     * Read an image from a string path
     * @param path
     * @return 
     */
    public static Image readImage(String path){
        return readImage(Paths.get(path));
    }
    
    /**
     * Reads an image from a path
     * @param path
     * @return 
     */
    public static Image readImage(Path path){
        try {
            return ImageIO.read(path.toUri().toURL());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Creates a unique hash id based on data and time
     * @param data
     * @return 
     */
    public static String createUniqueHashId(String data){
        String input = Calendar.getInstance().getTimeInMillis()+data; 

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(input.getBytes());
            byte[] hashBytes = md.digest();
            byte[] truncatedHashBytes = new byte[16];
            System.arraycopy(hashBytes, 0, truncatedHashBytes, 0, truncatedHashBytes.length);
            StringBuilder hashString = new StringBuilder();
            for (byte b : truncatedHashBytes) {
                hashString.append(String.format("%02x", b));
            }
            return hashString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Da formato en hh:mm:dd.zzz a un dato de milisegundos
     * @param millis
     * @return 
     */
    public static String formatMillis(long millis) {
        long hours = millis / (1000 * 60 * 60);
        long minutes = (millis / (1000 * 60)) % 60;
        long seconds = (millis / 1000) % 60;
        long milliseconds = millis % 1000;
        String millisFormatted = String.format("%04d", milliseconds * 10);
        return String.format("%02d:%02d:%02d.%s", hours, minutes, seconds, millisFormatted);
    }
    
    
    public static String extractStackTrace(Throwable err){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        err.printStackTrace(new PrintStream(baos));
        return baos.toString();
    }
    
    
    public static String readIS(InputStream is) throws IOException{
        StringBuilder sb = new StringBuilder();
        byte[] buff = new byte[1024*10];
        int readed = -1;
        while((readed=is.read(buff))>0){
            sb.append(new String(buff,0,readed,"UTF-8"));
        }
        return sb.toString();
    }
    
    /**
     * valida si el primer valor "value", existe en la lista de valores siguiente
     * @param value
     * @param inList
     * @return 
     */
    public static boolean in(int value, int... inList){
        for(int v2: inList){
            if(value == v2){
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * valida si el primer valor "value", existe en la lista de valores siguiente
     * @param value
     * @param inList
     * @return 
     */
    public static boolean in(double value, double... inList){
        for(double v2: inList){
            if(value == v2){
                return true;
            }
        }
        return false;
    }
    
    /**
     * valida si el primer valor "value", existe en la lista de valores siguiente
     * @param value
     * @param inList
     * @return 
     */
    public static boolean in(String value, String... inList){    
        for(String v2: inList){
            if(value.equalsIgnoreCase(v2)){
                return true;
            }
        }
        return false;
    }

    public static List<String> readLinesFormFile(Path path) throws IOException{
        CharsetDecoder decoder = StandardCharsets.UTF_8
                    .newDecoder()
                    .onMalformedInput(CodingErrorAction.IGNORE)
                    .onUnmappableCharacter(CodingErrorAction.IGNORE);

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Files.newInputStream(path), decoder));

        final List<String> lines = new ArrayList<>();
        reader.lines().forEach(lines::add);
        return lines;
    }
    
    
    public static String rpad(String value, int len, char c){
        
        if(value == null ) value = "";
        
        if(value.length() > len){
            return value.substring(0, len);
        }
        
        for(int i=value.length(); i< len; i++){
            value += c;
        }
        
        return value;
    }
    
    
    public static String lpad(String value, int len, char c){
        
        if(value == null ) value = "";
        
        if(value.length() > len){
            return value.substring(0, len);
        }
        
        for(int i=value.length(); i< len; i++){
            value = c + value;
        }
        
        return value;
    }
    
    
    public static String formatDecimal(Number number){
        
        if(number == null){
            return "";
        }
        
        return decimalFormat.format(number.doubleValue());
    }
    
    
    public static String formatoMonto(Number number){
        return Resources.p("app.currency")+" "+formatDecimal(number);
    }
    
    
    public static boolean validarNIT(String nit) {
        if (nit == null) {
            return false;
        }

        nit = nit.trim().toUpperCase();

        // Eliminar guiones
        nit = nit.replace("-", "");

        // Mínimo 2 caracteres (numero + verificador)
        if (nit.length() < 2) {
            return false;
        }

        String numero = nit.substring(0, nit.length() - 1);
        char verificadorChar = nit.charAt(nit.length() - 1);

        // El número debe ser solo dígitos
        if (!numero.matches("\\d+")) {
            return false;
        }

        int factor = 2;
        int suma = 0;

        // Se recorre de derecha a izquierda
        for (int i = numero.length() - 1; i >= 0; i--) {
            int digito = Character.getNumericValue(numero.charAt(i));
            suma += digito * factor;
            factor++;
        }

        int modulo = suma % 11;
        int verificadorCalculado = 11 - modulo;

        char verificadorEsperado;
        if (verificadorCalculado == 10) {
            verificadorEsperado = 'K';
        } else if (verificadorCalculado == 11) {
            verificadorEsperado = '0';
        } else {
            verificadorEsperado = Character.forDigit(verificadorCalculado, 10);
        }

        return verificadorChar == verificadorEsperado;
    }

    public static String normalizarDocumento(String doc) {
        if (doc == null) {
            return "";
        }
        return doc.trim().toUpperCase().replace("-", "").replace(" ", "");
    }
    
    
    /**
     * Normaliza texto: minúsculas + sin tildes
     * @param s
     * @return 
     */
    public static String normalize(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        n = n.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return n.toLowerCase(Locale.ROOT);
    }
    
    /**
     * Ordena un map respecto a una llave dentro del map
     * @param listMap
     * @param nombreLlave
     * @param asc
     * @return 
     */
    public static void ordenarListMap(
            List<Map<String, Object>> listMap,
            String nombreLlave,
            boolean asc) {

        if (listMap == null || listMap.size() <= 1) {
            return;
        }

        Comparator<Map<String, Object>> comparator = (m1, m2) -> {
            Object v1 = m1 != null ? m1.get(nombreLlave) : null;
            Object v2 = m2 != null ? m2.get(nombreLlave) : null;

            // Manejo de nulos
            if (v1 == v2) {
                return 0;
            }
            if (v1 == null) {
                return -1;
            }
            if (v2 == null) {
                return 1;
            }

            int resultado;

            // Si ambos implementan Comparable y son compatibles
            if (v1 instanceof Comparable<?> && v2 instanceof Comparable<?>
                    && v1.getClass().isAssignableFrom(v2.getClass())) {
                Comparable<Object> c1 = (Comparable<Object>) v1;
                resultado = c1.compareTo(v2);
            } else {
                resultado = String.valueOf(v1)
                        .compareTo(String.valueOf(v2));
            }

            return asc ? resultado : -resultado;
        };
        listMap.sort(comparator);
    }
    
    
    
    
}
