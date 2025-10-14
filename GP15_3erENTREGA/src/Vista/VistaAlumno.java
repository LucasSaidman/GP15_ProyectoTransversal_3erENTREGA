package Vista;

import Modelo.Alumno;
import Persistencia.alumnoData;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * @author Grupo 15 Luis Ezequiel Sosa Lucas Saidman Luca Rodrigaño Ignacio
 * Rodriguez
 *
 */
public class VistaAlumno extends javax.swing.JInternalFrame {

    private final alumnoData dao = new alumnoData();
    private DefaultTableModel modelo;
    private Alumno seleccionadoOriginal = null;

    public VistaAlumno() {
        initComponents();

        modelo = (DefaultTableModel) tabla_alumnos.getModel();
        tabla_alumnos.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        cb_regular.setModel(new DefaultComboBoxModel<>(new String[]{"Si", "No"}));
        cb_regular.setSelectedIndex(0);

        txt_fc.setToolTipText("Ej: 2000-01-30");

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
        
        txt_dni.addKeyListener(ka);
        txt_apellido.addKeyListener(ka);
        txt_nombre.addKeyListener(ka);
        txt_fc.addKeyListener(ka);

        cb_regular.addItemListener(e -> reglasHabilitacion());

        // click en tabla
        tabla_alumnos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                tablaClick();
            }
        });
    }
    
    private void reglasHabilitacion() {
        boolean dniIngresado  = !txt_dni.getText().trim().isEmpty();
        boolean haySeleccion  = tabla_alumnos.getSelectedRow() >= 0 && seleccionadoOriginal != null;

        btn_nuevo.setEnabled(true);

        btn_buscar.setEnabled(dniIngresado);

        boolean puedeGuardar = false;
        Integer dni = parseEntero(txt_dni.getText());
        LocalDate fn = parseFecha(txt_fc.getText());
        Boolean reg = getRegularCombo();
        
        if (dni != null && !txt_apellido.getText().trim().isEmpty() && !txt_nombre.getText().trim().isEmpty() && fn != null && reg != null && !haySeleccion) {
            try {
                puedeGuardar = (dao.buscarPorDni(dni) == null);
            } catch (Exception e) {
                puedeGuardar = false;
            }
        }
        
        btn_guardar.setEnabled(puedeGuardar);

        boolean puedeActualizar = false;
        if (haySeleccion && reg != null) {
            puedeActualizar = (reg != seleccionadoOriginal.isRegular());
        }
        
        btn_actualizar.setEnabled(puedeActualizar);
        btn_eliminar.setEnabled(haySeleccion);
    }
    
    private void cargarTablaBD() {
        try {
            List<Alumno> datos = dao.listarTodos();
            cargarTabla(datos);
        } catch (SQLException e) {
            error(e);
            System.out.println("ERROR: " + e);
        }
    }

    private void cargarTabla(List<Alumno> datos) {
        limpiarTabla();
        for (Alumno a : datos) {
            modelo.addRow(new String[]{
                String.valueOf(a.getDni()),
                nvl(a.getApellido()),
                nvl(a.getNombre()),
                a.getFechaNacimiento()==null ? "" : a.getFechaNacimiento().toString(),
                a.isRegular() ? "Si" : "No"
            });
        }
        
        tabla_alumnos.clearSelection();
        seleccionadoOriginal = null;
        reglasHabilitacion();
    }
    
    private void limpiarTabla() {
        modelo.setRowCount(0);
    }
    
    private void seleccionarFilaPorDni(int dni) {
        for (int i = 0; i < modelo.getRowCount(); i++) {
            Object valor = modelo.getValueAt(i, 0);
            
            if (valor != null && String.valueOf(dni).equals(valor.toString())) {
                tabla_alumnos.setRowSelectionInterval(i, i);
                tabla_alumnos.scrollRectToVisible(tabla_alumnos.getCellRect(i, 0, true));
                break;
            }
        }
    }
    
    private void tablaClick() {
        int fila = tabla_alumnos.getSelectedRow();
        if (fila < 0) {
            return;
        }

        Integer dni = parseEntero(String.valueOf(modelo.getValueAt(fila, 0)));
        if (dni == null) {
            return;
        }

        try {
            Alumno a = dao.buscarPorDni(dni);
            if (a != null) {
                setFormulario(a);
                seleccionadoOriginal = a;
                reglasHabilitacion();
            }
        } catch (SQLException e) {
            error(e);
            System.out.println("ERROR: " + e);
        }
    }
    
    private void setFormulario(Alumno a) {
        txt_dni.setText(String.valueOf(a.getDni()));
        txt_apellido.setText(nvl(a.getApellido()));
        txt_nombre.setText(nvl(a.getNombre()));
        txt_fc.setText(a.getFechaNacimiento() == null ? "" : a.getFechaNacimiento().toString());
        cb_regular.setSelectedItem(a.isRegular() ? "Si" : "No");
    }
    
    private void limpiarFormulario() {
        txt_dni.setText("");
        txt_apellido.setText("");
        txt_nombre.setText("");
        txt_fc.setText("");
        cb_regular.setSelectedIndex(0);
        tabla_alumnos.clearSelection();
        seleccionadoOriginal = null;
        reglasHabilitacion();
        txt_dni.requestFocus();
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

    private LocalDate parseFecha(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalDate.parse(s.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private Boolean getRegularCombo() {
        Object sel = cb_regular.getSelectedItem();
        
        if (sel == null) {
            return null;
        }
        
        String v = sel.toString().trim();
        if (v.equalsIgnoreCase("Si")) {
            return true;
        }
        
        if (v.equalsIgnoreCase("No")) {
            return false;
        }
        
        return null;
    }

    private void msg(String s){
        JOptionPane.showMessageDialog(this, s);
    }
    
    private void error(Exception e){
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Creates new form VistaAlumno
     */
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnl_gestion_alumnos = new javax.swing.JPanel();
        lb_titulo = new javax.swing.JLabel();
        sp_tabla_alumnos = new javax.swing.JScrollPane();
        tabla_alumnos = new javax.swing.JTable();
        lb_dni = new javax.swing.JLabel();
        lb_nombre = new javax.swing.JLabel();
        lb_regular = new javax.swing.JLabel();
        lb_fc = new javax.swing.JLabel();
        lb_apellido = new javax.swing.JLabel();
        txt_dni = new javax.swing.JTextField();
        txt_apellido = new javax.swing.JTextField();
        txt_fc = new javax.swing.JTextField();
        btn_nuevo = new javax.swing.JButton();
        btn_guardar = new javax.swing.JButton();
        btn_actualizar = new javax.swing.JButton();
        btn_eliminar = new javax.swing.JButton();
        btn_buscar = new javax.swing.JButton();
        cb_regular = new javax.swing.JComboBox<>();
        txt_nombre = new javax.swing.JTextField();
        btn_salir = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setPreferredSize(new java.awt.Dimension(1000, 700));

        lb_titulo.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lb_titulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lb_titulo.setText("Gestion de Alumnos");

        tabla_alumnos.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tabla_alumnos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "DNI", "Apellido", "Nombre", "Fecha de Nacimiento", "Regular"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabla_alumnos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabla_alumnosMouseClicked(evt);
            }
        });
        sp_tabla_alumnos.setViewportView(tabla_alumnos);

        lb_dni.setText("DNI:");

        lb_nombre.setText("Nombre:");

        lb_regular.setText("Regular:");

        lb_fc.setText("Fecha de Nacimiento:");

        lb_apellido.setText("Apellido:");

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

        javax.swing.GroupLayout pnl_gestion_alumnosLayout = new javax.swing.GroupLayout(pnl_gestion_alumnos);
        pnl_gestion_alumnos.setLayout(pnl_gestion_alumnosLayout);
        pnl_gestion_alumnosLayout.setHorizontalGroup(
            pnl_gestion_alumnosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lb_titulo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(pnl_gestion_alumnosLayout.createSequentialGroup()
                .addGap(196, 196, 196)
                .addGroup(pnl_gestion_alumnosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnl_gestion_alumnosLayout.createSequentialGroup()
                        .addGroup(pnl_gestion_alumnosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lb_regular, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lb_nombre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lb_fc, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(pnl_gestion_alumnosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txt_fc, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cb_regular, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txt_nombre, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnl_gestion_alumnosLayout.createSequentialGroup()
                        .addGroup(pnl_gestion_alumnosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(pnl_gestion_alumnosLayout.createSequentialGroup()
                                .addGroup(pnl_gestion_alumnosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(lb_apellido, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                                    .addComponent(lb_dni, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(pnl_gestion_alumnosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txt_apellido, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txt_dni, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btn_buscar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnl_gestion_alumnosLayout.createSequentialGroup()
                                .addComponent(btn_nuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btn_guardar, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btn_actualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btn_eliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addComponent(btn_salir, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(43, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_gestion_alumnosLayout.createSequentialGroup()
                .addContainerGap(21, Short.MAX_VALUE)
                .addComponent(sp_tabla_alumnos, javax.swing.GroupLayout.PREFERRED_SIZE, 946, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
        );
        pnl_gestion_alumnosLayout.setVerticalGroup(
            pnl_gestion_alumnosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_gestion_alumnosLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(lb_titulo)
                .addGap(30, 30, 30)
                .addComponent(sp_tabla_alumnos, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(70, 70, 70)
                .addGroup(pnl_gestion_alumnosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnl_gestion_alumnosLayout.createSequentialGroup()
                        .addGroup(pnl_gestion_alumnosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lb_dni, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txt_dni, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(pnl_gestion_alumnosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lb_apellido, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txt_apellido, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(btn_buscar, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnl_gestion_alumnosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lb_nombre, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_nombre, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnl_gestion_alumnosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lb_fc, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_fc, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnl_gestion_alumnosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lb_regular, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cb_regular, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(60, 60, 60)
                .addGroup(pnl_gestion_alumnosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_nuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_guardar, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_actualizar, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_eliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_salir, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(60, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnl_gestion_alumnos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnl_gestion_alumnos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_guardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_guardarActionPerformed
        Integer dni = parseEntero(txt_dni.getText());
        if(dni == null) {
            JOptionPane.showMessageDialog(this, "DNI invalido");
            txt_dni.requestFocus();
            return;
        }
        
        String apellido = txt_apellido.getText().trim();
        if(apellido.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete Apellido");
            txt_apellido.requestFocus();
            return;
        }
        
        String nombre = txt_nombre.getText().trim();
        if(nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete Nombre");
            txt_nombre.requestFocus();
            return;
        }
        
        java.time.LocalDate fn = parseFecha(txt_fc.getText());
        if(fn == null) {
            JOptionPane.showMessageDialog(this, "Fecha invalida, use anio-mes-dia");
            txt_fc.requestFocus();
            txt_fc.selectAll();
            return;
        }
        
        Boolean regular = getRegularCombo();
        if(regular == null) {
            JOptionPane.showMessageDialog(this, "Seleccione Regular: Si o No");
            cb_regular.requestFocus();
            return;
        }
        
        try {
            if(dao.buscarPorDni(dni) != null) {
                JOptionPane.showMessageDialog(this, "Ya existe un alumno con ese DNI");
                return;
            }
            
            Alumno a = new Alumno(dni, apellido, nombre, fn, regular);
            dao.guardar(a);
            
            JOptionPane.showMessageDialog(this, "Alumno guardado");
            cargarTablaBD();
            limpiarFormulario();
        } catch (Exception e) {
            error(e);
            System.out.println("ERROR: " + e);
        }
    }//GEN-LAST:event_btn_guardarActionPerformed

    private void btn_nuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_nuevoActionPerformed
        limpiarFormulario();
        txt_dni.requestFocus();
    }//GEN-LAST:event_btn_nuevoActionPerformed

    private void btn_buscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_buscarActionPerformed
        Integer dni = parseEntero(txt_dni.getText());
        if (dni == null) { 
            JOptionPane.showMessageDialog(this, "Ingrese el DNI"); 
            txt_dni.requestFocus(); 
            return; 
        }

        try {
            Alumno a = dao.buscarPorDni(dni);
            if (a == null) { 
                JOptionPane.showMessageDialog(this, "No existe un alumno con ese DNI"); 
                return; 
            }
            
            setFormulario(a);
            seleccionadoOriginal = a;
            seleccionarFilaPorDni(dni);
            reglasHabilitacion();
        } catch (Exception e) { 
            error(e);
            System.out.println("ERROR: " + e); 
        }
    }//GEN-LAST:event_btn_buscarActionPerformed

    private void btn_eliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_eliminarActionPerformed
        if(seleccionadoOriginal == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un alumno para eliminar");
            return;
        }
        
        int alumno = JOptionPane.showConfirmDialog(this, "Eliminar definiticamente al Alumno con DNI: " + seleccionadoOriginal.getDni() + "?",
                        "Confirmar", JOptionPane.YES_NO_OPTION);
        if(alumno != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            dao.borrar(seleccionadoOriginal.getIdAlumno());
            JOptionPane.showMessageDialog(this, "Alumno eliminado");
            cargarTablaBD();
            limpiarFormulario();
        } catch (Exception e) {
            error(e);
            System.out.println("ERROR: " + e);
        }
    }//GEN-LAST:event_btn_eliminarActionPerformed

    private void btn_actualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_actualizarActionPerformed
        if (seleccionadoOriginal == null) { 
            JOptionPane.showMessageDialog(this, "Seleccione un alumno para actualizar"); 
            return; 
        }

        Boolean regular = getRegularCombo();
        if (regular == null) { 
            JOptionPane.showMessageDialog(this, "Seleccione Regular: Si o No"); 
            cb_regular.requestFocus(); 
            return; 
        }

        try {
            int dniOriginal = seleccionadoOriginal.getDni();
            
            if (regular) {
                dao.altaLogica(seleccionadoOriginal.getIdAlumno());
            } else {
                dao.bajaLogica(seleccionadoOriginal.getIdAlumno());
            }

            cargarTablaBD();
            seleccionarFilaPorDni(dniOriginal);
            Alumno actualizado = dao.buscarPorDni(dniOriginal);
            if (actualizado != null) {
                setFormulario(actualizado);
                seleccionadoOriginal = actualizado;
            } else {
                seleccionadoOriginal = null;
            }
            
            JOptionPane.showMessageDialog(this, "Regularidad actualizada");
        } catch (Exception e) { 
            error(e);
            System.out.println("ERROR" + e); 
        }
    }//GEN-LAST:event_btn_actualizarActionPerformed

    private void tabla_alumnosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabla_alumnosMouseClicked
        int fila = tabla_alumnos.getSelectedRow();
        if(fila < 0) {
            return;
        }
        
        Integer dni = parseEntero(String.valueOf(modelo.getValueAt(fila, 0)));
        if(dni == null) {
            return;
        }
        
        try{
            Alumno a = dao.buscarPorDni(dni);
            if(a != null) {
                setFormulario(a);
                seleccionadoOriginal = a;
                reglasHabilitacion();
            }
        } catch (SQLException e) {
            error(e);
            System.out.println("ERROR: " + e);
        }
    }//GEN-LAST:event_tabla_alumnosMouseClicked

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
    private javax.swing.JComboBox<String> cb_regular;
    private javax.swing.JLabel lb_apellido;
    private javax.swing.JLabel lb_dni;
    private javax.swing.JLabel lb_fc;
    private javax.swing.JLabel lb_nombre;
    private javax.swing.JLabel lb_regular;
    private javax.swing.JLabel lb_titulo;
    private javax.swing.JPanel pnl_gestion_alumnos;
    private javax.swing.JScrollPane sp_tabla_alumnos;
    private javax.swing.JTable tabla_alumnos;
    private javax.swing.JTextField txt_apellido;
    private javax.swing.JTextField txt_dni;
    private javax.swing.JTextField txt_fc;
    private javax.swing.JTextField txt_nombre;
    // End of variables declaration//GEN-END:variables
}
