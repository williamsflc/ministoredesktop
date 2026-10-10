package com.lasopro.ministore.desktop;

import com.formdev.flatlaf.FlatLightLaf;
import com.lasopro.ministore.util.DBUtils;
import com.lasopro.ministore.util.Log;
import com.lasopro.ministore.util.Resources;
import com.lasopro.ministore.util.Util;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author williams
 */
public class MinistoreDesktop {
    
    public static Log log;
    public static Image logo;
    private static MainFrame frame;
    private static User user;
    

    public static void main(String[] args) throws Exception {
        log = new Log();
        //FlatLightLaf.setup();

        if (Resources.needsInitialSetup()) {
            boolean[] setupCompleted = {false};
            SwingUtilities.invokeAndWait(() -> {
                DialogInitialSetup setup = new DialogInitialSetup(null);
                setupCompleted[0] = setup.showWizard();
            });
            if (!setupCompleted[0]) {
                return;
            }
        }

        startStorage();
        
        try {
            UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
        } catch( Exception ex ) {
            System.err.println( "Failed to initialize LaF" );
        }

        
        
        SwingUtilities.invokeLater(() -> {
            frame = new MainFrame();
            //set logo
            try {
                logo = ImageIO.read(new File(Resources.p("app.logo")));
                frame.setIconImage(logo);
                frame.setLogo(logo);
            } catch (Exception e) {
                System.out.println("No se pudo obtener el logo: "+e.getMessage());
            }
            
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            GraphicsDevice gd = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice();

            
            SwingUtilities.invokeLater(() -> {
                DialogLogin login = new DialogLogin(frame, true);
                login.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                login.setLocationRelativeTo(frame);
                login.setLocationByPlatform(true);
                MinistoreDesktop.user = login.login();
                frame.setUsuario(MinistoreDesktop.user);
                frame.displayPanel("ventas");
                frame.refreshAll();
            });
            
            boolean fullscreen = Resources.p("app.fullscreen") == null || Resources.p("app.fullscreen").equalsIgnoreCase("true");
            if(fullscreen){ 
                gd.setFullScreenWindow(frame);
            }else{
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.setVisible(true);
            }
            
        });
        
        
        
        
        
    }
    
     public static void startStorage() throws IOException{
        boolean schemaExists = false;

        Connection cnn = null;

        try{
            cnn = DBUtils.getConnection(Resources.getDsName());
        }catch(Exception err){
            JOptionPane.showMessageDialog(
                frame, 
                "No se pudo establecer la conexión a la base de datos:\n"
                    +"  - "+Resources.getDsName()+": "+ Resources.p(Resources.getDsName()+".jdbc.url")+"\n"
                    +"  - Error: " +err.getMessage(),
                "Error al conectar a la base de datos",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }


        try {
            cnn.createStatement().execute("SELECT * FROM APP");
            schemaExists  = true;
        } catch (SQLException e) {
            //do nothing
        } finally{
            //Force close
            try{ cnn.close(); }catch(Throwable err){}
        }
        
        
        if(!schemaExists){
            log.info("El almacenamiento no existe, se crearan las tablas");
            DBUtils db = new DBUtils(log);
            String sql = Util.readIS(Files.newInputStream(Paths.get("res",Resources.p("app.db.type")+".schema.sql")));
            db.execute(Resources.getDsName(), sql);
        }else{
            log.info("Las tablas ya existen en el almacenamiento");
        }
        
    }

    public static User getCurrentUser() {
        return user;
    }

    static MainFrame getMainFrame() {
        return frame;
    }
    
    public static void runInThread(Runnable r){
        runInThread("Actualizando...",r);
         
    }
    

    public static void runInThread(String message, Runnable r){
        new Thread(() -> {
            try {
                SwingUtilities.invokeLater(() -> getMainFrame().setMessageInLabel(message, "MESSAGE"));
                r.run();
                SwingUtilities.invokeLater(() -> getMainFrame().setMessageInLabel("", "CLEAR"));
            } catch (Throwable e) {
                SwingUtilities.invokeLater(() -> getMainFrame().setMessageInLabel(e.getMessage(), "WARNING"));
            }
        }).start();
    }

    public static void runInThreadLock(Runnable r) {
        runInThreadLock("Procesando...", r);
    }

    public static void runInThreadLock(String message, Runnable r) {
        MainFrame mainFrame = getMainFrame();
        new Thread(() -> {
            DialogLoading dialog = new DialogLoading(mainFrame, message);
            try {
                SwingUtilities.invokeLater(() -> dialog.setVisible(true));
                r.run();
            } catch (Throwable e) {
                SwingUtilities.invokeLater(() -> mainFrame.setMessageInLabel(e.getMessage(), "WARNING"));
            } finally {
                SwingUtilities.invokeLater(dialog::dispose);
            }
        }).start();
    }
    
}
