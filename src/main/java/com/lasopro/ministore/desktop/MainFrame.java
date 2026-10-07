package com.lasopro.ministore.desktop;


import com.lasopro.ministore.util.Resources;
import com.lasopro.ministore.util.Util;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Cursor;
import java.awt.RenderingHints;
import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/**
 *
 * @author williams
 */
public class MainFrame extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MainFrame.class.getName());
    private static final String UI_FONT_SIZE_KEY = "app.ui.font.size";
    private static final int DEFAULT_UI_FONT_SIZE = 13;
    
    /*private PanelInventory inventario;
    private PanelVentas ventas;
    private PanelProveedores proveedores;
    private PanelCategoria categorias;
    private PanelClientes clientes;
    private PanelReportes reportes;
    private PanelUsuarios usuarios;
    private PanelConfig config;*/
    private Timer loadingTimer;
    private final SpinnerIcon loadingIcon = new SpinnerIcon();
    private Map<String,PanelInterface> panelMap;
    private PanelInterface currentPanel;
    
    
    
    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        setUIStyles();
        initComponents();
        configurarIconosNavegacion();
        this.panelMap = new HashMap<>();
        this.lbTopMessage.setText(Resources.p("app.name"));        
        panelvisible.setLayout(new BoxLayout(panelvisible,BoxLayout.Y_AXIS));
        iniciarHora();
        configurarLabelUsuario();
    }

    private void configurarIconosNavegacion() {
        setNavigationIcon(tbInventory, NavigationIcon.Type.INVENTORY);
        setNavigationIcon(tbVentas, NavigationIcon.Type.SALES);
        setNavigationIcon(tbProveedores, NavigationIcon.Type.SUPPLIERS);
        setNavigationIcon(tbCategorias, NavigationIcon.Type.CATEGORIES);
        setNavigationIcon(tbClientes, NavigationIcon.Type.CLIENTS);
        setNavigationIcon(tbReportes, NavigationIcon.Type.REPORTS);
        setNavigationIcon(tbUsuarios, NavigationIcon.Type.USERS);
        setNavigationIcon(tbConfig, NavigationIcon.Type.CONFIG);
        lUsuario.setIcon(new NavigationIcon(NavigationIcon.Type.PROFILE));
        lUsuario.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        lUsuario.setIconTextGap(5);
    }

    private void setNavigationIcon(AbstractButton button, NavigationIcon.Type type) {
        button.setIcon(new NavigationIcon(type));
        button.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        button.setIconTextGap(4);
    }
    
    
    public void displayPanel(String name){
        
        PanelInterface panel = panelMap.get(name);
        
        if(panel == null){
            switch(name){
                case "inventario" -> {
                    panel = new PanelInventory();
                }case "ventas" -> { 
                    panel = new PanelVentas();
                }case "proveedores" -> {
                    panel = new PanelProveedores();
                }case "categorias" -> {
                    panel = new PanelCategoria();
                }case "clientes" -> {
                    panel = new PanelClientes();
                }case "reportes" -> {
                    panel = new PanelReportes();
                }case "usuarios" -> {
                    panel = new PanelUsuarios();
                }case "config" -> {
                    panel = new PanelConfig();
                }default -> {
                    throw new RuntimeException("Panel no soportado");
                }
            }
        }
        currentPanel = panel;
        panelMap.put(name, panel);
        refreshPanel(name);
        this.panelvisible.removeAll();
        this.panelvisible.add((JComponent)panel);
        this.panelvisible.repaint();
        this.panelvisible.updateUI();
        
    }
    
    
    private void configurarLabelUsuario() {
        lUsuario.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lUsuario.setToolTipText("Clic para cambiar contraseña");
        lUsuario.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                abrirDialogoCambiarPassword();
            }
        });
    }

    private void abrirDialogoCambiarPassword() {
        if (MinistoreDesktop.getCurrentUser() == null) {
            return;
        }
        DialogCambiarPassword dialog = new DialogCambiarPassword(this);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    
    public static final void setUIStyles() {
        
        Font font = new FontUIResource(new Font("Monospaced", Font.PLAIN, getUIFontSize()));
        
        
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);

            if (value instanceof FontUIResource) {
                UIManager.put(key, font);
            }
            
        }
        
        
        
    }

    private static int getUIFontSize() {
        String configuredSize = Resources.p(UI_FONT_SIZE_KEY);
        if (configuredSize == null || configuredSize.isBlank()) {
            return DEFAULT_UI_FONT_SIZE;
        }
        try {
            int size = Integer.parseInt(configuredSize.trim());
            return size > 0 ? size : DEFAULT_UI_FONT_SIZE;
        } catch (NumberFormatException e) {
            logger.warning("Tamaño de fuente de interfaz inválido: " + configuredSize);
            return DEFAULT_UI_FONT_SIZE;
        }
    }
    
    
    public void setUsuario(User user){
        this.lUsuario.setText(user.getNombre());
        setMode(user.getTipo());
    }
    
    
    public void setMode(String mode){
        if(mode.equalsIgnoreCase("Administrador")){
            this.tbCategorias.setVisible(true);
            this.tbClientes.setVisible(true);
            this.tbInventory.setVisible(true);
            this.tbProveedores.setVisible(true);
            this.tbVentas.setVisible(true);
            this.tbReportes.setVisible(true);
            this.tbUsuarios.setVisible(true);
            this.tbConfig.setVisible(true);
        }else{
            this.tbCategorias.setVisible(false);
            this.tbClientes.setVisible(true);
            this.tbInventory.setVisible(false);
            this.tbProveedores.setVisible(false);
            this.tbVentas.setVisible(true);
            this.tbReportes.setVisible(false);
            this.tbUsuarios.setVisible(false);
            this.tbConfig.setVisible(false);
        }
    }
    
    
    public void refreshAll(){
        panelMap.forEach((k,v)->{ refreshPanel(k); });
    }
    
    public void refreshPanel(String panelName) {
        PanelInterface panel = this.panelMap.get(panelName);
        if (panel != null) {
            panel.refresh();
        }
    }
   
    
    
    public void setLogo(Image logo){
        if(logo == null) return;
        logo = logo.getScaledInstance(45, 45, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(logo);
        lbTopMessage.setIcon(icon);
    }

    public void refreshAppConfig() {
        setUIStyles();
        SwingUtilities.updateComponentTreeUI(this);
        revalidate();
        repaint();
        this.lbTopMessage.setText("<html><span style='font-size:16px'><b>" + Resources.p("app.name") + "</b></span> (MiniStore by Lasopro) </html>");
        try {
            setLogo(ImageIO.read(new File(Resources.p("app.logo"))));
        } catch (Exception e) {
            System.out.println("No se pudo obtener el logo: " + e.getMessage());
        }
        refreshPanel("ventas");
        refreshPanel("inventario");
    }
    
    private final void iniciarHora(){
        new Timer(1000,(e)->{
            StringBuilder sb = new StringBuilder("<html>");
            Calendar c = Calendar.getInstance();
            int dow = c.get(Calendar.DAY_OF_WEEK);
            switch(dow){
                case 1 -> sb.append("Domingo");
                case 2 -> sb.append("Lunes");
                case 3 -> sb.append("Martes");
                case 4 -> sb.append("Miercoles");
                case 5 -> sb.append("Jueves");
                case 6 -> sb.append("Viernes");
                case 7 -> sb.append("Sábado");
            }
            sb.append(" ");
            sb.append(c.get(Calendar.DAY_OF_MONTH));
            sb.append(" de ");
            
            switch(c.get(Calendar.MONTH)){
                case 0  -> sb.append("Enero");
                case 1  -> sb.append("Febrero");
                case 2  -> sb.append("Marzo");
                case 3  -> sb.append("Abril");
                case 4  -> sb.append("Mayo");
                case 5  -> sb.append("Junio");
                case 6  -> sb.append("Julio");
                case 7  -> sb.append("Agosto");
                case 8  -> sb.append("Septiembre");
                case 9 -> sb.append("Octubre");
                case 10 -> sb.append("Noviembre");
                case 11 -> sb.append("Diciembre");
            }
            sb.append(" de ");
            sb.append(c.get(Calendar.YEAR));
            sb.append("  <b>");
            sb.append(Util.dateTimeToInternalFormat(c.getTime()).substring(11));
            sb.append("</b></html>");
            lHora.setText(sb.toString());
            
        }).start();
    }

    /**
     * 
     * @param message
     * @param type ERROR, MESSAGE, WARNING, CLEAR
     */
    public void setMessageInLabel(String message, String type){
        switch (type) {
            case "MESSAGE" -> startLoadingMessage(message);
            case "CLEAR" -> stopLoadingMessage();
            case "WARNING" -> {
                stopLoadingMessage();
                labelMessage.setText(message);
            }
            default -> {
                stopLoadingMessage();
                labelMessage.setText(message);
            }
        }
    }

    private void startLoadingMessage(String message) {
        stopLoadingMessage();
        labelMessage.setIcon(loadingIcon);
        labelMessage.setText(" " + message);
        loadingTimer = new Timer(80, e -> {
            loadingIcon.advance();
            labelMessage.repaint();
        });
        loadingTimer.start();
    }

    private void stopLoadingMessage() {
        if (loadingTimer != null) {
            loadingTimer.stop();
            loadingTimer = null;
        }
        labelMessage.setIcon(null);
        labelMessage.setText("");
    }

    private static final class SpinnerIcon implements Icon {
        private static final int SIZE = 18;
        private static final Color COLOR = new Color(135, 170, 206);
        private int angle;

        void advance() {
            angle = (angle + 35) % 360;
        }

        @Override
        public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COLOR);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(x + 2, y + 2, SIZE - 4, SIZE - 4, angle, 270);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return SIZE;
        }

        @Override
        public int getIconHeight() {
            return SIZE;
        }
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgTop = new javax.swing.ButtonGroup();
        panelvisible = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        lbTopMessage = new javax.swing.JLabel();
        tbVentas = new javax.swing.JToggleButton();
        tbInventory = new javax.swing.JToggleButton();
        jButton1 = new javax.swing.JButton();
        tbProveedores = new javax.swing.JToggleButton();
        tbCategorias = new javax.swing.JToggleButton();
        tbClientes = new javax.swing.JToggleButton();
        lUsuario = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        tbReportes = new javax.swing.JToggleButton();
        tbUsuarios = new javax.swing.JToggleButton();
        tbConfig = new javax.swing.JToggleButton();
        lHora = new javax.swing.JLabel();
        labelMessage = new javax.swing.JLabel();
        btRefresh = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));

        panelvisible.setBackground(new java.awt.Color(255, 255, 255));
        panelvisible.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(135, 170, 206), 1, true));
        panelvisible.setAlignmentX(0.0F);
        panelvisible.setAlignmentY(0.0F);
        panelvisible.setMinimumSize(new java.awt.Dimension(0, 40));

        javax.swing.GroupLayout panelvisibleLayout = new javax.swing.GroupLayout(panelvisible);
        panelvisible.setLayout(panelvisibleLayout);
        panelvisibleLayout.setHorizontalGroup(
            panelvisibleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panelvisibleLayout.setVerticalGroup(
            panelvisibleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 794, Short.MAX_VALUE)
        );

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setAlignmentX(0.0F);
        jPanel1.setAlignmentY(0.0F);
        jPanel1.setMinimumSize(new java.awt.Dimension(0, 45));
        jPanel1.setPreferredSize(new java.awt.Dimension(1040, 45));

        lbTopMessage.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lbTopMessage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbTopMessage.setText("MINISTORE");

        tbVentas.setBackground(new java.awt.Color(135, 170, 206));
        bgTop.add(tbVentas);
        tbVentas.setForeground(new java.awt.Color(50, 50, 50));
        tbVentas.setSelected(true);
        tbVentas.setText("Ventas");
        tbVentas.setToolTipText("Ventas");
        tbVentas.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        tbVentas.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tbVentas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbVentasActionPerformed(evt);
            }
        });

        tbInventory.setBackground(new java.awt.Color(135, 170, 206));
        bgTop.add(tbInventory);
        tbInventory.setForeground(new java.awt.Color(50, 50, 50));
        tbInventory.setText("Invent.");
        tbInventory.setToolTipText("Inventario");
        tbInventory.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        tbInventory.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tbInventory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbInventoryActionPerformed(evt);
            }
        });

        jButton1.setBackground(new java.awt.Color(255, 0, 0));
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Salir");
        jButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        tbProveedores.setBackground(new java.awt.Color(135, 170, 206));
        bgTop.add(tbProveedores);
        tbProveedores.setForeground(new java.awt.Color(50, 50, 50));
        tbProveedores.setText("Provee.");
        tbProveedores.setToolTipText("Proveedores");
        tbProveedores.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        tbProveedores.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tbProveedores.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbProveedoresActionPerformed(evt);
            }
        });

        tbCategorias.setBackground(new java.awt.Color(135, 170, 206));
        bgTop.add(tbCategorias);
        tbCategorias.setForeground(new java.awt.Color(50, 50, 50));
        tbCategorias.setText("Categ.");
        tbCategorias.setToolTipText("Categorias");
        tbCategorias.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        tbCategorias.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tbCategorias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbCategoriasActionPerformed(evt);
            }
        });

        tbClientes.setBackground(new java.awt.Color(135, 170, 206));
        bgTop.add(tbClientes);
        tbClientes.setForeground(new java.awt.Color(50, 50, 50));
        tbClientes.setText("Clien.");
        tbClientes.setToolTipText("Clientes");
        tbClientes.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        tbClientes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tbClientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbClientesActionPerformed(evt);
            }
        });

        lUsuario.setBackground(new java.awt.Color(196, 216, 236));
        lUsuario.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lUsuario.setText("Usuario");
        lUsuario.setOpaque(true);

        jButton2.setBackground(new java.awt.Color(196, 216, 236));
        jButton2.setFont(new java.awt.Font("Noto Sans", 0, 18)); // NOI18N
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/keys.png"))); // NOI18N
        jButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        tbReportes.setBackground(new java.awt.Color(135, 170, 206));
        bgTop.add(tbReportes);
        tbReportes.setForeground(new java.awt.Color(50, 50, 50));
        tbReportes.setText("Reportes");
        tbReportes.setToolTipText("Reportes");
        tbReportes.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        tbReportes.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tbReportes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbReportesActionPerformed(evt);
            }
        });

        tbUsuarios.setBackground(new java.awt.Color(135, 170, 206));
        bgTop.add(tbUsuarios);
        tbUsuarios.setForeground(new java.awt.Color(50, 50, 50));
        tbUsuarios.setText("Usuarios");
        tbUsuarios.setToolTipText("Usuarios");
        tbUsuarios.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        tbUsuarios.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tbUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbUsuariosActionPerformed(evt);
            }
        });

        tbConfig.setBackground(new java.awt.Color(135, 170, 206));
        bgTop.add(tbConfig);
        tbConfig.setForeground(new java.awt.Color(50, 50, 50));
        tbConfig.setText("Config.");
        tbConfig.setToolTipText("Configuración");
        tbConfig.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        tbConfig.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tbConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbConfigActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(tbInventory, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tbVentas, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tbProveedores, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tbCategorias, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tbClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tbReportes, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tbUsuarios, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tbConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbTopMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tbProveedores, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tbCategorias, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tbClientes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tbReportes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tbUsuarios, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tbConfig, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addComponent(lbTopMessage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(lUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tbVentas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tbInventory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        lHora.setFont(new java.awt.Font("Noto Sans", 0, 14)); // NOI18N
        lHora.setText("Hora");

        labelMessage.setText("--");

        btRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/refresh.png"))); // NOI18N
        btRefresh.setToolTipText("Actualizar");
        btRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRefreshActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 510, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lHora, javax.swing.GroupLayout.PREFERRED_SIZE, 364, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btRefresh))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1160, Short.MAX_VALUE)
                    .addComponent(panelvisible, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(16, 16, 16))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelvisible, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btRefresh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labelMessage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lHora, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(3, 3, 3))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tbInventoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbInventoryActionPerformed
        displayPanel("inventario");
    }//GEN-LAST:event_tbInventoryActionPerformed

    private void tbVentasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbVentasActionPerformed
        displayPanel("ventas");
    }//GEN-LAST:event_tbVentasActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        
        PanelVentas ventas = (PanelVentas)panelMap.get("ventas");
        
        if(ventas.isVentaEnCurso()){
            JOptionPane.showMessageDialog(this,"Hay una venta en curso por favor finalice o aborte la venta antes de cerrar la aplicación.");
            return;
        }
        
        
        int res = JOptionPane.showConfirmDialog(this, "¿Desea cerrar la aplicación?", "Confirmación cerrar", JOptionPane.YES_NO_OPTION);
        
        if(res == JOptionPane.YES_OPTION){
            System.exit(0);
        }
        
        
    }//GEN-LAST:event_jButton1ActionPerformed

    private void tbProveedoresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbProveedoresActionPerformed
        displayPanel("proveedores");
    }//GEN-LAST:event_tbProveedoresActionPerformed

    private void tbCategoriasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbCategoriasActionPerformed
        displayPanel("categorias");
    }//GEN-LAST:event_tbCategoriasActionPerformed

    private void tbClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbClientesActionPerformed
        displayPanel("clientes");
    }//GEN-LAST:event_tbClientesActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        Calc.showCalc();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void tbReportesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbReportesActionPerformed
        displayPanel("reportes");
    }//GEN-LAST:event_tbReportesActionPerformed

    private void tbUsuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbUsuariosActionPerformed
        displayPanel("usuarios");
    }//GEN-LAST:event_tbUsuariosActionPerformed

    private void tbConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbConfigActionPerformed
        displayPanel("config");
    }//GEN-LAST:event_tbConfigActionPerformed

    private void btRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRefreshActionPerformed
        if(currentPanel!=null) refreshPanel(currentPanel.name());
    }//GEN-LAST:event_btRefreshActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgTop;
    private javax.swing.JButton btRefresh;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lHora;
    private javax.swing.JLabel lUsuario;
    private javax.swing.JLabel labelMessage;
    private javax.swing.JLabel lbTopMessage;
    private javax.swing.JPanel panelvisible;
    private javax.swing.JToggleButton tbCategorias;
    private javax.swing.JToggleButton tbClientes;
    private javax.swing.JToggleButton tbConfig;
    private javax.swing.JToggleButton tbInventory;
    private javax.swing.JToggleButton tbProveedores;
    private javax.swing.JToggleButton tbReportes;
    private javax.swing.JToggleButton tbUsuarios;
    private javax.swing.JToggleButton tbVentas;
    // End of variables declaration//GEN-END:variables





}
