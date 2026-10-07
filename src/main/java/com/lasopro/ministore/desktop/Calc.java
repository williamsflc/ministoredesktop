
package com.lasopro.ministore.desktop;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;



/**
 *
 * @author williams
 */
public class Calc extends javax.swing.JDialog {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Calc.class.getName());
    
    private double numero1 = 0;
    private String operador = "";
    private boolean nuevaEntrada = true;
    private static Calc me;
    
    DecimalFormat format = new DecimalFormat("#.##");

    /**
     * Creates new form Calc
     */
    public Calc(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        agregarEventos();
        agregarTeclado();
        
    }
    
     // =========================
    // LÓGICA
    // =========================
    private void agregarNumero(String num) {
        if (nuevaEntrada) {
            inputText_setText(num);
            nuevaEntrada = false;
        } else {
            if (num.equals(".") && inputText.getText().contains(".")) return;
            inputText_setText(inputText.getText() + num);
        }
    }

    private void setOperacion(String op) {

        // Si ya hay una operación pendiente, calcular primero
        if (!operador.isEmpty() && !nuevaEntrada) {
            calcular();
        }

        numero1 = Double.parseDouble(inputText.getText());
        operador = op;
        loper.setText(numero1+" "+operador);
        nuevaEntrada = true;
    }

    private void calcular() {

        if (operador.isEmpty()) return;

        double numero2 = Double.parseDouble(inputText.getText());
        double resultado = 0;

        switch (operador) {
            case "+": resultado = numero1 + numero2; break;
            case "-": resultado = numero1 - numero2; break;
            case "*": resultado = numero1 * numero2; break;
            case "/": resultado = numero2 != 0 ? numero1 / numero2 : 0; break;
            case "%": resultado = numero1 * numero2 / 100; break;
        }

        jTextArea1.append(numero1 + " " + operador + " " + numero2 + " = " + resultado + "\n");

        inputText_setText(resultado);

        numero1 = resultado;   // 🔥 CLAVE
        nuevaEntrada = true;
    }

    private void limpiar() {
        inputText_setText(0d);
        numero1 = 0;
        operador = "";
        nuevaEntrada = true;
        loper.setText("");
        jTextArea1.append(" - limpiar - \n");
    }
    
    
    private void borrar() {
        String t = inputText.getText();
        if(t.length() == 0){
            return;
        }
        if(t.length() == 1){
            inputText_setText("");
            return;
        }
        if(t.length() > 1){
            inputText_setText(t.substring(0, t.length()-1));
        }
    }

    // =========================
    // EVENTOS BOTONES
    // =========================
    private void agregarEventos() {

        b1.addActionListener(e -> agregarNumero("1"));
        b2.addActionListener(e -> agregarNumero("2"));
        b3.addActionListener(e -> agregarNumero("3"));
        b4.addActionListener(e -> agregarNumero("4"));
        b5.addActionListener(e -> agregarNumero("5"));
        b6.addActionListener(e -> agregarNumero("6"));
        b7.addActionListener(e -> agregarNumero("7"));
        b8.addActionListener(e -> agregarNumero("8"));
        b9.addActionListener(e -> agregarNumero("9"));
        bZ.addActionListener(e -> agregarNumero("0"));
        bPunto.addActionListener(e -> agregarNumero("."));

        bSuma.addActionListener(e -> setOperacion("+"));
        bResta.addActionListener(e -> setOperacion("-"));
        bMultiplicacion.addActionListener(e -> setOperacion("*"));
        bDivision.addActionListener(e -> setOperacion("/"));
        bPorcentaje.addActionListener(e -> setOperacion("%"));

        bIgual.addActionListener(e -> calcular());

        bLimpiar.addActionListener(e -> limpiar());

        bBorrar.addActionListener(e -> borrar());
    }

    // =========================
    // SOPORTE TECLADO
    // =========================
    private void agregarTeclado() {

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                char c = e.getKeyChar();

                if (Character.isDigit(c)) {
                    agregarNumero(String.valueOf(c));
                }

                switch (c) {
                    case '+': setOperacion("+"); break;
                    case '-': setOperacion("-"); break;
                    case '*': setOperacion("*"); break;
                    case '/': setOperacion("/"); break;
                    case '%': setOperacion("%"); break;
                    case '.': agregarNumero("."); break;
                }

                if (e.getKeyCode() == KeyEvent.VK_ENTER) calcular();
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) limpiar();
                if (e.getKeyCode() == KeyEvent.VK_C) limpiar();
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) borrar();
            }
        });

        this.setFocusable(true);
        this.requestFocusInWindow();
    }
    
    
    public static void showCalc(){
        SwingUtilities.invokeLater(()->{
        
        if(me == null){
            me = new Calc(null, false);
            me.setLocationRelativeTo(null);
            me.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            me.pack();
        }
        if(!me.isVisible()){
            me.setVisible(true);
        }
        
        me.setAlwaysOnTop(true);
        me.toFront();    
        me.requestFocus();
        me.setAlwaysOnTop(false);
                
        });
        
    }
    
    
    private void inputText_setText(Double num){
        inputText.setText(format.format(num));
    }
    
    private void inputText_setText(String num){
        inputText.setText(num);
    }
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panel = new javax.swing.JPanel();
        inputText = new javax.swing.JTextField();
        bMultiplicacion = new javax.swing.JButton();
        b7 = new javax.swing.JButton();
        b8 = new javax.swing.JButton();
        b9 = new javax.swing.JButton();
        b4 = new javax.swing.JButton();
        b5 = new javax.swing.JButton();
        b6 = new javax.swing.JButton();
        bResta = new javax.swing.JButton();
        b3 = new javax.swing.JButton();
        b1 = new javax.swing.JButton();
        b2 = new javax.swing.JButton();
        bSuma = new javax.swing.JButton();
        bPunto = new javax.swing.JButton();
        bZ = new javax.swing.JButton();
        bIgual = new javax.swing.JButton();
        bLimpiar = new javax.swing.JButton();
        bBorrar = new javax.swing.JButton();
        bPorcentaje = new javax.swing.JButton();
        bDivision = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        loper = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        panel.setBackground(new java.awt.Color(255, 255, 255));

        inputText.setEditable(false);
        inputText.setBackground(new java.awt.Color(247, 248, 249));
        inputText.setFont(new java.awt.Font("Courier 10 Pitch", 1, 48)); // NOI18N
        inputText.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        inputText.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(175, 182, 188), 1, true));
        inputText.setCaretColor(new java.awt.Color(204, 204, 204));
        inputText.setFocusable(false);

        bMultiplicacion.setBackground(new java.awt.Color(222, 231, 235));
        bMultiplicacion.setFont(new java.awt.Font("Courier 10 Pitch", 1, 48)); // NOI18N
        bMultiplicacion.setText("*");
        bMultiplicacion.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(231, 243, 242), 1, true));
        bMultiplicacion.setFocusable(false);

        b7.setBackground(new java.awt.Color(222, 231, 235));
        b7.setFont(new java.awt.Font("Courier 10 Pitch", 1, 48)); // NOI18N
        b7.setText("7");
        b7.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(231, 243, 242), 1, true));
        b7.setFocusable(false);

        b8.setBackground(new java.awt.Color(222, 231, 235));
        b8.setFont(new java.awt.Font("Courier 10 Pitch", 1, 48)); // NOI18N
        b8.setText("8");
        b8.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(231, 243, 242), 1, true));
        b8.setFocusable(false);

        b9.setBackground(new java.awt.Color(222, 231, 235));
        b9.setFont(new java.awt.Font("Courier 10 Pitch", 1, 48)); // NOI18N
        b9.setText("9");
        b9.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(231, 243, 242), 1, true));
        b9.setFocusable(false);

        b4.setBackground(new java.awt.Color(222, 231, 235));
        b4.setFont(new java.awt.Font("Courier 10 Pitch", 1, 48)); // NOI18N
        b4.setText("4");
        b4.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(231, 243, 242), 1, true));
        b4.setFocusable(false);

        b5.setBackground(new java.awt.Color(222, 231, 235));
        b5.setFont(new java.awt.Font("Courier 10 Pitch", 1, 48)); // NOI18N
        b5.setText("5");
        b5.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(231, 243, 242), 1, true));
        b5.setFocusable(false);

        b6.setBackground(new java.awt.Color(222, 231, 235));
        b6.setFont(new java.awt.Font("Courier 10 Pitch", 1, 48)); // NOI18N
        b6.setText("6");
        b6.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(231, 243, 242), 1, true));
        b6.setFocusable(false);

        bResta.setBackground(new java.awt.Color(222, 231, 235));
        bResta.setFont(new java.awt.Font("Courier 10 Pitch", 1, 48)); // NOI18N
        bResta.setText("-");
        bResta.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(231, 243, 242), 1, true));
        bResta.setFocusable(false);

        b3.setBackground(new java.awt.Color(222, 231, 235));
        b3.setFont(new java.awt.Font("Courier 10 Pitch", 1, 48)); // NOI18N
        b3.setText("3");
        b3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(231, 243, 242), 1, true));
        b3.setFocusable(false);

        b1.setBackground(new java.awt.Color(222, 231, 235));
        b1.setFont(new java.awt.Font("Courier 10 Pitch", 1, 48)); // NOI18N
        b1.setText("1");
        b1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(231, 243, 242), 1, true));
        b1.setFocusable(false);

        b2.setBackground(new java.awt.Color(222, 231, 235));
        b2.setFont(new java.awt.Font("Courier 10 Pitch", 1, 48)); // NOI18N
        b2.setText("2");
        b2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(231, 243, 242), 1, true));
        b2.setFocusable(false);

        bSuma.setBackground(new java.awt.Color(222, 231, 235));
        bSuma.setFont(new java.awt.Font("Courier 10 Pitch", 1, 48)); // NOI18N
        bSuma.setText("+");
        bSuma.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(231, 243, 242), 1, true));
        bSuma.setFocusable(false);

        bPunto.setBackground(new java.awt.Color(222, 231, 235));
        bPunto.setFont(new java.awt.Font("Courier 10 Pitch", 1, 48)); // NOI18N
        bPunto.setText(".");
        bPunto.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(231, 243, 242), 1, true));
        bPunto.setFocusable(false);

        bZ.setBackground(new java.awt.Color(222, 231, 235));
        bZ.setFont(new java.awt.Font("Courier 10 Pitch", 1, 48)); // NOI18N
        bZ.setText("0");
        bZ.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(231, 243, 242), 1, true));
        bZ.setFocusable(false);

        bIgual.setBackground(new java.awt.Color(222, 231, 235));
        bIgual.setFont(new java.awt.Font("Courier 10 Pitch", 1, 48)); // NOI18N
        bIgual.setText("=");
        bIgual.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(231, 243, 242), 1, true));
        bIgual.setFocusable(false);

        bLimpiar.setBackground(new java.awt.Color(222, 231, 235));
        bLimpiar.setFont(new java.awt.Font("Courier 10 Pitch", 1, 48)); // NOI18N
        bLimpiar.setText("C");
        bLimpiar.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(231, 243, 242), 1, true));
        bLimpiar.setFocusable(false);

        bBorrar.setBackground(new java.awt.Color(222, 231, 235));
        bBorrar.setFont(new java.awt.Font("Courier 10 Pitch", 1, 48)); // NOI18N
        bBorrar.setText("<");
        bBorrar.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(231, 243, 242), 1, true));
        bBorrar.setFocusable(false);

        bPorcentaje.setBackground(new java.awt.Color(222, 231, 235));
        bPorcentaje.setFont(new java.awt.Font("Courier 10 Pitch", 1, 48)); // NOI18N
        bPorcentaje.setText("%");
        bPorcentaje.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(231, 243, 242), 1, true));
        bPorcentaje.setFocusable(false);

        bDivision.setBackground(new java.awt.Color(222, 231, 235));
        bDivision.setFont(new java.awt.Font("Courier 10 Pitch", 1, 48)); // NOI18N
        bDivision.setText("/");
        bDivision.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(231, 243, 242), 1, true));
        bDivision.setFocusable(false);

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setFocusable(false);

        jTextArea1.setEditable(false);
        jTextArea1.setBackground(new java.awt.Color(255, 255, 255));
        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Courier 10 Pitch", 0, 18)); // NOI18N
        jTextArea1.setRows(5);
        jTextArea1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jTextArea1.setFocusable(false);
        jScrollPane1.setViewportView(jTextArea1);

        loper.setFont(new java.awt.Font("Courier 10 Pitch", 1, 20)); // NOI18N
        loper.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);

        javax.swing.GroupLayout panelLayout = new javax.swing.GroupLayout(panel);
        panel.setLayout(panelLayout);
        panelLayout.setHorizontalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLayout.createSequentialGroup()
                .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(panelLayout.createSequentialGroup()
                                .addComponent(bLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bBorrar, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bPorcentaje, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bDivision, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(panelLayout.createSequentialGroup()
                                        .addComponent(b4, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(b5, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(b6, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(bResta, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(panelLayout.createSequentialGroup()
                                        .addComponent(b7, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(b8, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(b9, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(bMultiplicacion, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(panelLayout.createSequentialGroup()
                                    .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(panelLayout.createSequentialGroup()
                                            .addComponent(bZ, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(bPunto, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(bIgual, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(panelLayout.createSequentialGroup()
                                            .addComponent(b1, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(b2, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(b3, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(bSuma, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(inputText, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 318, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(loper, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelLayout.setVerticalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 473, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelLayout.createSequentialGroup()
                        .addComponent(loper, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(inputText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(bLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(bBorrar, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(bPorcentaje, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(bDivision, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(b7, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(b8, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(b9, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(bMultiplicacion, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(b4, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(b5, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(b6, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(bResta, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(panelLayout.createSequentialGroup()
                                .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(b1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(b2, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(b3, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(bZ, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(bPunto, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(bIgual, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(bSuma, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(7, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Create and display the dialog */
        showCalc();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton b1;
    private javax.swing.JButton b2;
    private javax.swing.JButton b3;
    private javax.swing.JButton b4;
    private javax.swing.JButton b5;
    private javax.swing.JButton b6;
    private javax.swing.JButton b7;
    private javax.swing.JButton b8;
    private javax.swing.JButton b9;
    private javax.swing.JButton bBorrar;
    private javax.swing.JButton bDivision;
    private javax.swing.JButton bIgual;
    private javax.swing.JButton bLimpiar;
    private javax.swing.JButton bMultiplicacion;
    private javax.swing.JButton bPorcentaje;
    private javax.swing.JButton bPunto;
    private javax.swing.JButton bResta;
    private javax.swing.JButton bSuma;
    private javax.swing.JButton bZ;
    private javax.swing.JTextField inputText;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel loper;
    private javax.swing.JPanel panel;
    // End of variables declaration//GEN-END:variables
}
