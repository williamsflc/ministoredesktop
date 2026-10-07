package com.lasopro.ministore.desktop;

import com.lasopro.ministore.db.Usuario;
import com.lasopro.ministore.util.PaginedResult;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author williams
 */
public class PanelUsuarios extends javax.swing.JPanel implements PanelInterface {

    private Usuario db;
    private Integer editingId;

    public PanelUsuarios() {
        initComponents();
        db = new Usuario();
        editingId = null;

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn("Id");
        model.addColumn("Usuario");
        model.addColumn("Nombre");
        model.addColumn("Tipo");
        tablaUsuarios.setModel(model);
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarUsuarioSeleccionado();
            }
        });

        cbTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Administrador", "Vendedor"}));
        limpiarFormulario();
        refresh();
    }

    private void cargarUsuarioSeleccionado() {
        int selected = tablaUsuarios.getSelectedRow();
        if (selected < 0) {
            return;
        }
        try {
            Integer id = (Integer) tablaUsuarios.getValueAt(selected, 0);
            Map<String, Object> info = db.findById(id);
            editingId = id;
            tUser.setText((String) info.get("user"));
            tPass.setText("");
            tPass.setToolTipText("Déjela vacía para conservar la contraseña actual");
            tNombre.setText((String) info.get("nombre"));
            cbTipo.setSelectedItem(info.get("tipo"));
            panelFormulario.setBorder(javax.swing.BorderFactory.createTitledBorder("[*] Modificar usuario"));
            btAgregar.setText("Guardar cambios");
        } catch (Throwable err) {
            JOptionPane.showMessageDialog(this, "Error al cargar el usuario: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            MinistoreDesktop.log.err("Error al cargar el usuario", err);
        }
    }

    private void limpiarFormulario() {
        editingId = null;
        tUser.setText("");
        tPass.setText("");
        tNombre.setText("");
        cbTipo.setSelectedIndex(0);
        tablaUsuarios.clearSelection();
        panelFormulario.setBorder(javax.swing.BorderFactory.createTitledBorder("[+] Agregar nuevo usuario"));
        btAgregar.setText("+ Agregar");
    }

    @Override
    public final void refresh() {
        MinistoreDesktop.runInThread("Actualizando usuarios...", () -> {
            _refresh();
        });
    }

    public void _refresh() {
        String text = tBuscar.getText();
        PaginedResult pr;
        if (text.isBlank()) {
            pr = db.list(1, 10000);
        } else {
            pr = db.search(1, 10000, text);
        }

        DefaultTableModel model = (DefaultTableModel) tablaUsuarios.getModel();
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            model.removeRow(i);
        }

        for (Map<String, Object> u : pr.getData()) {
            model.addRow(new Object[]{u.get("id"), u.get("user"), u.get("nombre"), u.get("tipo")});
        }
    }

    private void guardarUsuario() {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put("user", tUser.getText().trim());
            info.put("pass", new String(tPass.getPassword()).trim());
            info.put("nombre", tNombre.getText().trim());
            info.put("tipo", cbTipo.getSelectedItem());

            if (editingId == null) {
                db.insert(info);
            } else {
                info.put("id", editingId);
                db.update(info);
            }
            limpiarFormulario();
            refresh();
        } catch (Throwable err) {
            JOptionPane.showMessageDialog(this, "Error al guardar el usuario: " + err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            MinistoreDesktop.log.err("Error al guardar el usuario", err);
        }
    }

    private void eliminarUsuario() {
        int selected = tablaUsuarios.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un usuario para eliminarlo");
            return;
        }

        Integer id = (Integer) tablaUsuarios.getValueAt(selected, 0);
        String username = (String) tablaUsuarios.getValueAt(selected, 1);

        if (MinistoreDesktop.getCurrentUser() != null
                && username.equalsIgnoreCase(MinistoreDesktop.getCurrentUser().getUser())) {
            JOptionPane.showMessageDialog(this, "No puede eliminar el usuario con el que ha iniciado sesión");
            return;
        }

        int res = JOptionPane.showConfirmDialog(this,
                "¿Desea eliminar el usuario '" + username + "'?",
                "Eliminar usuario",
                JOptionPane.YES_NO_CANCEL_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            db.delete(id);
            limpiarFormulario();
            refresh();
        }
    }
    
    @Override
    public String name() {
        return "usuarios";
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        panelFormulario = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        tUser = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        tPass = new javax.swing.JPasswordField();
        jLabel3 = new javax.swing.JLabel();
        tNombre = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        cbTipo = new javax.swing.JComboBox<>();
        btLimpiar = new javax.swing.JButton();
        btAgregar = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        tBuscar = new javax.swing.JTextField();
        btEliminar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaUsuarios = new javax.swing.JTable();

        setBackground(new java.awt.Color(255, 255, 255));

        panelFormulario.setBackground(new java.awt.Color(255, 255, 255));
        panelFormulario.setBorder(javax.swing.BorderFactory.createTitledBorder("[+] Agregar nuevo usuario"));

        jLabel1.setText("Usuario:");

        tUser.setPreferredSize(new java.awt.Dimension(64, 30));

        jLabel2.setText("Contraseña:");

        tPass.setMinimumSize(new java.awt.Dimension(64, 30));

        jLabel3.setText("Nombre:");

        tNombre.setMinimumSize(new java.awt.Dimension(64, 30));

        jLabel4.setText("Tipo:");

        btLimpiar.setText("Limpiar");
        btLimpiar.addActionListener(evt -> limpiarFormulario());

        btAgregar.setText("+ Agregar");
        btAgregar.addActionListener(evt -> guardarUsuario());

        javax.swing.GroupLayout panelFormularioLayout = new javax.swing.GroupLayout(panelFormulario);
        panelFormulario.setLayout(panelFormularioLayout);
        panelFormularioLayout.setHorizontalGroup(
            panelFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFormularioLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelFormularioLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btLimpiar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btAgregar))
                    .addGroup(panelFormularioLayout.createSequentialGroup()
                        .addGroup(panelFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addGap(18, 18, 18)
                        .addGroup(panelFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tUser, javax.swing.GroupLayout.DEFAULT_SIZE, 538, Short.MAX_VALUE)
                            .addComponent(tPass)
                            .addComponent(tNombre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbTipo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        panelFormularioLayout.setVerticalGroup(
            panelFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelFormularioLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(panelFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(tUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(tPass, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(tNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(cbTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addGroup(panelFormularioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btAgregar)
                    .addComponent(btLimpiar))
                .addContainerGap())
        );

        jLabel5.setText("Buscar usuario:");

        tBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                refresh();
            }
        });

        btEliminar.setText("Eliminar seleccionado");
        btEliminar.addActionListener(evt -> eliminarUsuario());

        jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane1.setViewportView(tablaUsuarios);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btEliminar))
                    .addComponent(panelFormulario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelFormulario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(btEliminar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                .addContainerGap())
        );
    }

    private javax.swing.JButton btAgregar;
    private javax.swing.JButton btEliminar;
    private javax.swing.JButton btLimpiar;
    private javax.swing.JComboBox<String> cbTipo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel panelFormulario;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField tBuscar;
    private javax.swing.JTextField tNombre;
    private javax.swing.JPasswordField tPass;
    private javax.swing.JTextField tUser;
    private javax.swing.JTable tablaUsuarios;
}
