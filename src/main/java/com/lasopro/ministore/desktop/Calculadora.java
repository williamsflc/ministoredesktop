package com.lasopro.ministore.desktop;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Calculadora POS en Swing puro
 * - Mouse y teclado (Key Bindings)
 * - Cinta de historial de operaciones
 * - Pensada para sistemas de punto de venta
 */
public class Calculadora extends JDialog {

    private JTextField display;
    private JTextArea cinta;

    private double valorActual = 0;
    private String operador = "";
    private boolean nuevoNumero = true;

    /* ================= CONSTRUCTOR ================= */
    public Calculadora(Window owner) {
        super(owner);
        setModal(false);
        setTitle("Calculadora POS");
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        construirUI();
        configurarTeclado();

        pack();
    }

    /* ================= UI ================= */
    private void construirUI() {
        setLayout(new BorderLayout(5, 5));

        display = new JTextField("0");
        display.setFont(new Font("Consolas", Font.BOLD, 24));
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setEditable(false);
        add(display, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new GridLayout(5, 4, 5, 5));
        String[] botones = {
                "7", "8", "9", "/",
                "4", "5", "6", "*",
                "1", "2", "3", "-",
                "0", ".", "%", "+",
                "C", "=", "", ""
        };

        for (String txt : botones) {
            if (txt.isEmpty()) {
                panelBotones.add(new JLabel());
                continue;
            }
            JButton b = new JButton(txt);
            b.setFont(new Font("Arial", Font.BOLD, 16));
            b.addActionListener(e -> procesarEntrada(txt));
            panelBotones.add(b);
        }

        add(panelBotones, BorderLayout.CENTER);

        cinta = new JTextArea(10, 18);
        cinta.setEditable(false);
        cinta.setFont(new Font("Consolas", Font.PLAIN, 13));
        add(new JScrollPane(cinta), BorderLayout.EAST);
    }

    /* ================= TECLADO ================= */
    private void configurarTeclado() {
        JComponent root = getRootPane();
        root.setFocusable(true);

        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();

        // Números
        for (int i = 0; i <= 9; i++) {
            String n = String.valueOf(i);
            im.put(KeyStroke.getKeyStroke(n.charAt(0)), n);
            am.put(n, accion(n));
        }

        // Operadores
        map(im, am, '+');
        map(im, am, '-');
        map(im, am, '*');
        map(im, am, '/');
        map(im, am, '%');
        map(im, am, '.');

        // Especiales
        map(im, am, KeyEvent.VK_ENTER, "=");
        map(im, am, '=', "=");
        map(im, am, KeyEvent.VK_BACK_SPACE, "C");
        map(im, am, KeyEvent.VK_ESCAPE, "C");
    }

    private AbstractAction accion(String cmd) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                procesarEntrada(cmd);
            }
        };
    }

    private void map(InputMap im, ActionMap am, char key) {
        im.put(KeyStroke.getKeyStroke(key), String.valueOf(key));
        am.put(String.valueOf(key), accion(String.valueOf(key)));
    }

    private void map(InputMap im, ActionMap am, int key, String cmd) {
        im.put(KeyStroke.getKeyStroke(key, 0), cmd);
        am.put(cmd, accion(cmd));
    }

    /* ================= LÓGICA ================= */
    private void procesarEntrada(String cmd) {
        if (cmd.matches("[0-9]")) {
            if (nuevoNumero) {
                display.setText(cmd);
                nuevoNumero = false;
            } else {
                display.setText(display.getText() + cmd);
            }
            return;
        }

        if (cmd.equals(".")) {
            if (!display.getText().contains(".")) {
                display.setText(display.getText() + ".");
            }
            return;
        }

        if (cmd.equals("C")) {
            valorActual = 0;
            operador = "";
            nuevoNumero = true;
            display.setText("0");
            cinta.append("--- LIMPIAR ---\n");
            return;
        }

        if (cmd.equals("%")) {
            double v = Double.parseDouble(display.getText());
            double r = valorActual * v / 100;
            cinta.append(valorActual + " % " + v + " = " + r + "\n");
            display.setText(String.valueOf(r));
            nuevoNumero = true;
            return;
        }

        if (cmd.equals("=")) {
            calcular();
            operador = "";
            nuevoNumero = true;
            return;
        }

        // Operadores
        valorActual = Double.parseDouble(display.getText());
        operador = cmd;
        nuevoNumero = true;
    }

    private void calcular() {
        if (operador.isEmpty()) return;

        double b = Double.parseDouble(display.getText());
        double r = 0;

        switch (operador) {
            case "+": r = valorActual + b; break;
            case "-": r = valorActual - b; break;
            case "*": r = valorActual * b; break;
            case "/":
                if (b == 0) {
                    JOptionPane.showMessageDialog(this, "División por cero");
                    return;
                }
                r = valorActual / b; break;
        }

        cinta.append(valorActual + " " + operador + " " + b + " = " + r + "\n");
        display.setText(String.valueOf(r));
        valorActual = r;
    }

    /* ================= MÉTODO POS ================= */
    public static void mostrarCalculadora(JComponent comp) {
        Window w = SwingUtilities.getWindowAncestor(comp);
        Calculadora calc = new Calculadora(w);

        Point p = comp.getLocationOnScreen();
        calc.setLocation(p.x, p.y - calc.getPreferredSize().height);

        calc.setVisible(true);
        calc.getRootPane().requestFocusInWindow();
    }

    /* ================= MAIN (PRUEBA) ================= */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Prueba POS");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(400, 200);

            JTextField campo = new JTextField();
            campo.setFont(new Font("Consolas", Font.BOLD, 18));

            campo.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    mostrarCalculadora(campo);
                }
            });

            f.add(campo);
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
