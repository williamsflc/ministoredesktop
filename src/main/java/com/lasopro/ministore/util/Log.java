package com.lasopro.ministore.util;



import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author admin
 */
public class Log {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private FileWriter writer;
    
    public Log(){
    }
    
    public void err(String msg, Throwable err){
        try{
            StringBuilder sb = new StringBuilder();
            sb.append(sdf.format(new Date()))
                    .append(" - ERROR: ")
                    .append(msg)
                    .append("\n");
            
            if(err != null){
               ByteArrayOutputStream baos = new ByteArrayOutputStream();
               PrintStream ps = new PrintStream(baos);
               err.printStackTrace(ps);               
               sb.append(baos.toString());
               sb.append("\n");
            }
            write(sb.toString());
        }catch(IOException e){}
    }
    
    public void info(String msg){
        try{
            StringBuilder sb = new StringBuilder();
            sb.append(sdf.format(new Date()))
                    .append(" - INFO: ")
                    .append(msg)
                    .append("\n");
            write(sb.toString());
        }catch(IOException e){}
    }
    
    
    private void write(String msg) throws IOException{
        if(writer == null){
            Path p = Resources.getLogPath();
            writer = new FileWriter(p.toFile(), true);
        }
        writer.write(msg);
        writer.flush();
    }
    
}
