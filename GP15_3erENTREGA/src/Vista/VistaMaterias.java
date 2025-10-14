package Vista;

import Modelo.Materia;
import Persistencia.materiaData;

import java.sql.SQLException;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/** 
    @author Grupo 15
    Luis Ezequiel Sosa
    Lucas Saidman
    Luca Rodrigaño
    Ignacio Rodriguez
**/

public class VistaMaterias extends javax.swing.JInternalFrame {
    
    private final materiaData dao = new materiaData();
    private DefaultTableModel modelo;
    private Materia seleccionadoOriginal = null;

    public VistaMaterias() {
        initComponents();
        
        modelo = (DefaultTableModel) tabla_materias.getModel();
        tabla_materias.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        cb_cuatrimestre.setModel(new DefaultComboBoxModel<>(new String[]{"1","2","3","4","5","6"}));
        cb_cuatrimestre.setSelectedIndex(-1);
        cb_estado.setModel(new DefaultComboBoxModel<>(new String[]{"Activa","Inactiva"}));
        cb_estado.setSelectedIndex(0);

        escucharCambios();
        cargarTablaBD();
        reglasHabilitacion();
    }
    
    private void escucharCambios() {
        java.awt.event.KeyAdapter ka = new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) {
                reglasHabilitacion();
            }
        };
        txt_nombre.addKeyListener(ka);

        cb_cuatrimestre.addItemListener(e -> reglasHabilitacion());
        cb_estado.addItemListener(e -> reglasHabilitacion());

        tabla_materias.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                tablaClick();
            }
        });
    }
    
    private void reglasHabilitacion() {
        boolean nombreIngresado = !txt_nombre.getText().trim().isEmpty();
        boolean haySeleccion    = tabla_materias.getSelectedRow() >= 0 && seleccionadoOriginal != null;

        btn_nuevo.setEnabled(true);
        btn_buscar.setEnabled(nombreIngresado);

        boolean puedeGuardar = false;
        String  nombre = nombreActual();
        Integer cuatrimestre   = getCuatrimestreFromCombo();
        Boolean estado = getEstadoFromCombo();

        if (nombre != null && !nombre.isEmpty() && cuatrimestre != null && estado != null && !haySeleccion) {
            try {
                puedeGuardar = !existeNombre(nombre);
            } catch (Exception e) {
                puedeGuardar = false;
            }
        }
        btn_guardar.setEnabled(puedeGuardar);

        boolean puedeActualizar = false;
        if (haySeleccion && estado != null) {
            puedeActualizar = (estado != seleccionadoOriginal.isEstado());
        }
        
        btn_actualizar.setEnabled(puedeActualizar);
        btn_eliminar.setEnabled(haySeleccion);
    }
    
    private void cargarTablaBD() {
        try {
            List<Materia> datos = dao.listarTodas();
            cargarTabla(datos);
        } catch (SQLException e) {
            error(e);
            System.out.println("ERROR: " + e);
        }
    }

    private void cargarTabla(List<Materia> datos) {
        limpiarTabla();
        for (Materia m : datos) {
            modelo.addRow(new String[]{
                nvl(m.getNombre()),
                String.valueOf(m.getCuatrimestre()),
                m.isEstado() ? "Activa" : "Inactiva"
            });
        }
        tabla_materias.clearSelection();
        seleccionadoOriginal = null;
        reglasHabilitacion();
    }

    private void limpiarTabla() {
        modelo.setRowCount(0);
    }
    
    private void seleccionarFilaPorNombre(String nombre) {
        if (nombre == null) {
            return;
        }
        
        for (int i = 0; i < modelo.getRowCount(); i++) {
            Object valor = modelo.getValueAt(i, 0);
            if (valor != null && nombre.equalsIgnoreCase(valor.toString())) {
                tabla_materias.setRowSelectionInterval(i, i);
                tabla_materias.scrollRectToVisible(tabla_materias.getCellRect(i, 0, true));
                break;
            }
        }
    }
    
    private void tablaClick() {
        int fila = tabla_materias.getSelectedRow();
        if (fila < 0) {
            return;
        }

        String  nombre = String.valueOf(modelo.getValueAt(fila, 0));
        Integer cuatrimestre = parseEntero(String.valueOf(modelo.getValueAt(fila, 1)));

        try {
            Materia m = null;
            if (cuatrimestre != null) {
                m = dao.buscarPorNombreYCuat(nombre, cuatrimestre);
            }
            
            if (m == null) {
                for (Materia x : dao.listarTodas()) {
                    if (x.getNombre() != null && x.getNombre().equalsIgnoreCase(nombre)) {
                        m = x;
                        break;
                    }
                }
            }
            
            if (m != null) {
                setFormulario(m);
                seleccionadoOriginal = m;
                reglasHabilitacion();
            }
        } catch (SQLException e) {
            error(e);
            System.out.println("ERROR: " + e);
        }
    }
    
    private String nombreActual() {
        return txt_nombre.getText().trim();
    }

    private Integer getCuatrimestreFromCombo() {
        Object sel = cb_cuatrimestre.getSelectedItem();
        
        if (sel == null) {
            return null;
        }
        
        try {
            return Integer.parseInt(sel.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean getEstadoFromCombo() {
        Object sel = cb_estado.getSelectedItem();
        
        if (sel == null) {
            return null;
        }
        
        String v = sel.toString().trim();
        if (v.equalsIgnoreCase("Activa")) {
            return true;
        }
        if (v.equalsIgnoreCase("Inactiva")) {
            return false;
        }
        return null;
    }

    private void setFormulario(Materia m) {
        txt_nombre.setText(nvl(m.getNombre()));
        cb_cuatrimestre.setSelectedItem(String.valueOf(m.getCuatrimestre()));
        cb_estado.setSelectedItem(m.isEstado() ? "Activa" : "Inactiva");
    }

    private void limpiarFormulario() {
        txt_nombre.setText("");
        cb_cuatrimestre.setSelectedIndex(-1);
        cb_estado.setSelectedIndex(0);
        tabla_materias.clearSelection();
        seleccionadoOriginal = null;
        reglasHabilitacion();
        txt_nombre.requestFocus();
    }

    private boolean existeNombre(String nombre) throws SQLException {
        for (Materia m : dao.listarTodas()) {
            if (m.getNombre() != null && m.getNombre().equalsIgnoreCase(nombre)) {
                return true;
            }
        }
        return false;
    }
    
    private String nvl(String s){
        return s == null ? "" : s;
    }

    private Integer parseEntero(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private void msg(String s){
        JOptionPane.showMessageDialog(this, s);
    }
    
    private void error(Exception e){
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnl_gestion_materias = new javax.swing.JPanel();
        lb_titulo = new javax.swing.JLabel();
        sp_tabla_materias = new javax.swing.JScrollPane();
        tabla_materias = new javax.swing.JTable();
        lb_nombre = new javax.swing.JLabel();
        lb_estado = new javax.swing.JLabel();
        lb_cuatrimestre = new javax.swing.JLabel();
        txt_nombre = new javax.swing.JTextField();
        cb_cuatrimestre = new javax.swing.JComboBox<>();
        btn_nuevo = new javax.swing.JButton();
        btn_guardar = new javax.swing.JButton();
        btn_actualizar = new javax.swing.JButton();
        btn_eliminar = new javax.swing.JButton();
        btn_buscar = new javax.swing.JButton();
        cb_estado = new javax.swing.JComboBox<>();
        btn_salir = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setPreferredSize(new java.awt.Dimension(1000, 700));

        lb_titulo.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lb_titulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lb_titulo.setText("Gestion de Materias");

        tabla_materias.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tabla_materias.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Nombre", "Cuatrimestre", "Estado"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        sp_tabla_materias.setViewportView(tabla_materias);

        lb_nombre.setText("Nombre:");

        lb_estado.setText("Estado:");

        lb_cuatrimestre.setText("Cuatrimestre:");

        btn_nuevo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/escoba.png"))); // NOI18N
        btn_nuevo.setText("Nuevo");
        btn_nuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_nuevoActionPerformed(evt);
            }
        });

        btn_guardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/guardar.png"))); // NOI18N
        btn_guardar.setText("Guardar");
        btn_guardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_guardarActionPerformed(evt);
            }
        });

        btn_actualizar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/icons8-aprobar-y-actualizar-48.png"))); // NOI18N
        btn_actualizar.setText("Actualizar");
        btn_actualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_actualizarActionPerformed(evt);
            }
        });

        btn_eliminar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/eliminar.png"))); // NOI18N
        btn_eliminar.setText("Eliminar");
        btn_eliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_eliminarActionPerformed(evt);
            }
        });

        btn_buscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/icons8-magnifying-glass-tilted-right-48.png"))); // NOI18N
        btn_buscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_buscarActionPerformed(evt);
            }
        });

        btn_salir.setText("Salir");
        btn_salir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_salirActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnl_gestion_materiasLayout = new javax.swing.GroupLayout(pnl_gestion_materias);
        pnl_gestion_materias.setLayout(pnl_gestion_materiasLayout);
        pnl_gestion_materiasLayout.setHorizontalGroup(
            pnl_gestion_materiasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lb_titulo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_gestion_materiasLayout.createSequentialGroup()
                .addContainerGap(21, Short.MAX_VALUE)
                .addComponent(sp_tabla_materias, javax.swing.GroupLayout.PREFERRED_SIZE, 946, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
            .addGroup(pnl_gestion_materiasLayout.createSequentialGroup()
                .addGap(196, 196, 196)
                .addGroup(pnl_gestion_materiasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnl_gestion_materiasLayout.createSequentialGroup()
                        .addComponent(btn_nuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btn_guardar, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btn_actualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btn_eliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btn_salir, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnl_gestion_materiasLayout.createSequentialGroup()
                        .addGroup(pnl_gestion_materiasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnl_gestion_materiasLayout.createSequentialGroup()
                                .addComponent(lb_nombre, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(txt_nombre, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnl_gestion_materiasLayout.createSequentialGroup()
                                .addComponent(lb_cuatrimestre, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(cb_cuatrimestre, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(108, 108, 108)
                        .addComponent(btn_buscar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnl_gestion_materiasLayout.createSequentialGroup()
                        .addComponent(lb_estado, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cb_estado, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(43, Short.MAX_VALUE))
        );
        pnl_gestion_materiasLayout.setVerticalGroup(
            pnl_gestion_materiasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_gestion_materiasLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(lb_titulo)
                .addGap(30, 30, 30)
                .addComponent(sp_tabla_materias, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(70, 70, 70)
                .addGroup(pnl_gestion_materiasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btn_buscar, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(pnl_gestion_materiasLayout.createSequentialGroup()
                        .addGroup(pnl_gestion_materiasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lb_nombre, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txt_nombre, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(pnl_gestion_materiasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lb_cuatrimestre, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cb_cuatrimestre, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(18, 18, 18)
                .addGroup(pnl_gestion_materiasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lb_estado, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cb_estado, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(50, 50, 50)
                .addGroup(pnl_gestion_materiasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_nuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_guardar, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_actualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_eliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_salir, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(166, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnl_gestion_materias, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnl_gestion_materias, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_buscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_buscarActionPerformed
        String nombre = nombreActual();
        
        if (nombre.isEmpty()) {
            msg("Ingrese el nombre de la materia");
            txt_nombre.requestFocus();
            return;
        }

        try {
            Materia encontrada = null;
            Integer cuat = getCuatrimestreFromCombo();
            
            if (cuat != null) {
                encontrada = dao.buscarPorNombreYCuat(nombre, cuat);
            }
            
            if (encontrada == null) {
                for (Materia m : dao.listarTodas()) {
                    if (m.getNombre() != null && m.getNombre().equalsIgnoreCase(nombre)) {
                        encontrada = m;
                        break;
                    }
                }
            }
            
            if (encontrada == null) {
                msg("No existe una materia con ese nombre");
                return;
            }

            setFormulario(encontrada);
            seleccionadoOriginal = encontrada;
            seleccionarFilaPorNombre(encontrada.getNombre());
            reglasHabilitacion();
        } catch (Exception e) {
            error(e);
            System.out.println("ERROR: " + e);
        }
    }//GEN-LAST:event_btn_buscarActionPerformed

    private void btn_nuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_nuevoActionPerformed
        limpiarFormulario();
    }//GEN-LAST:event_btn_nuevoActionPerformed

    private void btn_guardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_guardarActionPerformed
        String  nombre = nombreActual();
        if (nombre.isEmpty()) {
            msg("Complete Nombre");
            txt_nombre.requestFocus();
            return;
        }

        Integer cuat = getCuatrimestreFromCombo();
        if (cuat == null) {
            msg("Seleccione Cuatrimestre");
            cb_cuatrimestre.requestFocus();
            return;
        }

        Boolean estado = getEstadoFromCombo();
        if (estado == null) {
            msg("Seleccione Estado");
            cb_estado.requestFocus();
            return;
        }

        try {
            if (existeNombre(nombre)) {
                msg("Ya existe una materia con ese nombre");
                return;
            }

            Materia m = new Materia(nombre, cuat, estado);
            dao.guardar(m);

            msg("Materia guardada");
            cargarTablaBD();
            limpiarFormulario();
        } catch (Exception e) {
            error(e);
            System.out.println("ERROR: " + e);
        }
    }//GEN-LAST:event_btn_guardarActionPerformed

    private void btn_actualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_actualizarActionPerformed
        if (seleccionadoOriginal == null) {
            msg("Seleccione una materia para actualizar");
            return;
        }

        Boolean estado = getEstadoFromCombo();
        if (estado == null) {
            msg("Seleccione Estado");
            cb_estado.requestFocus();
            return;
        }

        try {
            String nombreBase = seleccionadoOriginal.getNombre();
            int cuatBase = seleccionadoOriginal.getCuatrimestre();

            if (estado) {
                dao.altaLogica(seleccionadoOriginal.getIdMateria());
            } else {
                dao.bajaLogica(seleccionadoOriginal.getIdMateria());
            }

            cargarTablaBD();

            seleccionarFilaPorNombre(nombreBase);
            Materia actualizada = dao.buscarPorNombreYCuat(nombreBase, cuatBase);
            if (actualizada != null) {
                setFormulario(actualizada);
                seleccionadoOriginal = actualizada;
            } else {
                seleccionadoOriginal = null;
            }

            msg("Estado actualizado");
        } catch (Exception e) {
            error(e);
            System.out.println("ERROR: " + e);
        }
    }//GEN-LAST:event_btn_actualizarActionPerformed

    private void btn_eliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_eliminarActionPerformed
        if (seleccionadoOriginal == null) {
            msg("Seleccione una materia para eliminar");
            return;
        }
        
        int r = JOptionPane.showConfirmDialog(this, "Eliminar definitivamente la materia \"" + seleccionadoOriginal.getNombre() + "\"?", "Confirmar", JOptionPane.YES_NO_OPTION);
        
        if (r != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            dao.borrar(seleccionadoOriginal.getIdMateria());
            msg("Materia eliminada");
            cargarTablaBD();
            limpiarFormulario();
        } catch (Exception e) {
            error(e);
            System.out.println("ERROR: " + e);
        }
    }//GEN-LAST:event_btn_eliminarActionPerformed

    private void btn_salirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_salirActionPerformed
        dispose();
    }//GEN-LAST:event_btn_salirActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_actualizar;
    private javax.swing.JButton btn_buscar;
    private javax.swing.JButton btn_eliminar;
    private javax.swing.JButton btn_guardar;
    private javax.swing.JButton btn_nuevo;
    private javax.swing.JButton btn_salir;
    private javax.swing.JComboBox<String> cb_cuatrimestre;
    private javax.swing.JComboBox<String> cb_estado;
    private javax.swing.JLabel lb_cuatrimestre;
    private javax.swing.JLabel lb_estado;
    private javax.swing.JLabel lb_nombre;
    private javax.swing.JLabel lb_titulo;
    private javax.swing.JPanel pnl_gestion_materias;
    private javax.swing.JScrollPane sp_tabla_materias;
    private javax.swing.JTable tabla_materias;
    private javax.swing.JTextField txt_nombre;
    // End of variables declaration//GEN-END:variables
}
