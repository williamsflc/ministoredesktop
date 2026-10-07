package com.lasopro.ministore.desktop;

import com.lasopro.ministore.db.Usuario;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author williams
 */
public class DialogCambiarPassword extends JDialog {

    private final Usuario db = new Usuario();
    private JLabel lUsuarioActual;
    private JPasswordField tPassActual;
    private JPasswordField tPassNueva;
    private JPasswordField tPassConfirmar;

    public DialogCambiarPassword(java.awt.Frame parent) {
        super(parent, true);
        initComponents();
        refrescarUsuario();
    }

    private void initComponents() {
        setTitle("Cambiar contraseña");
        setBackground(java.awt.Color.WHITE);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(java.awt.Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        lUsuarioActual = new JLabel();
        tPassActual = new JPasswordField(20);
        tPassNueva = new JPasswordField(20);
        tPassConfirmar = new JPasswordField(20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("Usuario:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(lUsuarioActual, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Contraseña actual:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(tPassActual, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(new JLabel("Nueva contraseña:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(tPassNueva, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        panel.add(new JLabel("Confirmar contraseña:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(tPassConfirmar, gbc);

        JPanel buttons = new JPanel();
        buttons.setBackground(java.awt.Color.WHITE);
        JButton bAceptar = new JButton("Guardar");
        JButton bCancelar = new JButton("Cancelar");
        buttons.add(bAceptar);
        buttons.add(bCancelar);

        bAceptar.addActionListener(e -> guardar());
        bCancelar.addActionListener(e -> dispose());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        pack();
    }

    private void refrescarUsuario() {
        User user = MinistoreDesktop.getCurrentUser();
        if (user != null) {
            lUsuarioActual.setText(user.getUser());
        }
        tPassActual.setText("");
        tPassNueva.setText("");
        tPassConfirmar.setText("");
    }

    private void guardar() {
        User user = MinistoreDesktop.getCurrentUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "No hay un usuario activo en la sesión", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String passActual = new String(tPassActual.getPassword()).trim();
        String passNueva = new String(tPassNueva.getPassword()).trim();
        String passConfirmar = new String(tPassConfirmar.getPassword()).trim();

        if (passActual.isBlank()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar la contraseña actual", "Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (passNueva.isBlank()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar la nueva contraseña", "Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!passNueva.equals(passConfirmar)) {
            JOptionPane.showMessageDialog(this, "La confirmación de contraseña no coincide", "Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            db.updatePassword(user.getUser(), passActual, passNueva);
            JOptionPane.showMessageDialog(this, "Contraseña actualizada correctamente", "Cambio de contraseña", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Throwable err) {
            MinistoreDesktop.log.err("Error al cambiar la contraseña", err);
            JOptionPane.showMessageDialog(this, "Error al cambiar la contraseña: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
