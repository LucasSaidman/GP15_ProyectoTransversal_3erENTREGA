package Vista;

import Modelo.Alumno;
import Modelo.Materia;
import Persistencia.alumnoData;
import Persistencia.inscripcionData;

import java.time.LocalDate;
import java.sql.SQLException;
import java.util.ArrayList;
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

public class VistaInscripcion extends javax.swing.JInternalFrame {
    
    private final alumnoData alumnoDao = new alumnoData();
    private final inscripcionData inscDao = new inscripcionData();
    
    private DefaultTableModel modelo;
    private Alumno alumnoSeleccionado = null;
    private List<Materia> materiasEnCombo = new ArrayList<>();

    /**
     * Creates new form VistaInscripcion
     */
    public VistaInscripcion() {
        initComponents();

        modelo = (DefaultTableModel) tabla_gestion_inscripciones.getModel();
        tabla_gestion_inscripciones.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        rb_no_inscripto.setSelected(true);

        escucharCambios();
        cargarTablaAlumnosRegulares();
        limpiarComboMaterias();
        reglasHabilitacion();
    }
    
    private void escucharCambios() {
        tabla_gestion_inscripciones.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                tablaClick();
            }
        });
        
        java.awt.event.ActionListener rbListener = e -> {
            recargarMateriasDelCombo();
            reglasHabilitacion();
        };
        
        rb_inscripto.addActionListener(rbListener);
        rb_no_inscripto.addActionListener(rbListener);
        
        java.awt.event.KeyAdapter ka = new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) {
                reglasHabilitacion();
            }
        };
        
        txt_dni.addKeyListener(ka);
        cb_materias.addItemListener(e -> reglasHabilitacion());
    }

    private void reglasHabilitacion() {
        boolean dniIngresado = !txt_dni.getText().trim().isEmpty();
        boolean alumnoCargado = (alumnoSeleccionado != null);

        boolean hayMateriaSeleccionada = (cb_materias.getSelectedIndex() >= 0
                                          && cb_materias.getItemCount() > 0
                                          && cb_materias.getSelectedItem() != null
                                          && !"Materias".equalsIgnoreCase(cb_materias.getSelectedItem().toString()));

        btn_nuevo.setEnabled(true);
        btn_buscar.setEnabled(dniIngresado);

        boolean puedeInscribir = alumnoCargado && rb_no_inscripto.isSelected() && hayMateriaSeleccionada;
        boolean puedeAnular   = alumnoCargado && rb_inscripto.isSelected() && hayMateriaSeleccionada;

        btn_inscribir.setEnabled(puedeInscribir);
        btn_anular_inscripcion.setEnabled(puedeAnular);
    }

    private void limpiarFormulario() {
        txt_dni.setText("");
        txt_nombre.setText("");
        txt_apellido.setText("");

        alumnoSeleccionado = null;
        tabla_gestion_inscripciones.clearSelection();
        rb_no_inscripto.setSelected(true);

        limpiarComboMaterias();
        reglasHabilitacion();
        txt_dni.requestFocus();
    }

    private void limpiarComboMaterias() {
        materiasEnCombo = new ArrayList<>();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("Materias");
        cb_materias.setModel(model);
        cb_materias.setSelectedIndex(0);
    }

    private void setAlumnoEnFormulario(Alumno a) {
        alumnoSeleccionado = a;
        txt_dni.setText(String.valueOf(a.getDni()));
        txt_nombre.setText(nvl(a.getNombre()));
        txt_apellido.setText(nvl(a.getApellido()));
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private Integer parseEntero(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private void msg(String s) {
        JOptionPane.showMessageDialog(this, s);
    }

    private void error(Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private int anioActual() {
        return LocalDate.now().getYear();
    }

    private void seleccionarFilaPorDni(int dni) {
        for (int i = 0; i < modelo.getRowCount(); i++) {
            Object valor = modelo.getValueAt(i, 0);
            if (valor != null && String.valueOf(dni).equals(valor.toString())) {
                tabla_gestion_inscripciones.setRowSelectionInterval(i, i);
                tabla_gestion_inscripciones.scrollRectToVisible(tabla_gestion_inscripciones.getCellRect(i, 0, true));
                break;
            }
        }
    }
    
    private void cargarTablaAlumnosRegulares() {
        try {
            List<Alumno> lista = alumnoDao.listarTodos();
            limpiarTabla();
            for (Alumno a : lista) {
                if (a.isRegular()) {
                    modelo.addRow(new String[]{
                        String.valueOf(a.getDni()),
                        nvl(a.getNombre()),
                        nvl(a.getApellido())
                    });
                }
            }
            tabla_gestion_inscripciones.clearSelection();
        } catch (SQLException e) {
            error(e);
            System.out.println("ERROR: " + e);
        }
    }

    private void limpiarTabla() {
        modelo.setRowCount(0);
    }

    private void recargarMateriasDelCombo() {
        limpiarComboMaterias();
        if (alumnoSeleccionado == null) {
            return;
        }

        int anio = anioActual();
        
        try {
            List<Materia> lista;
            if (rb_inscripto.isSelected()) {
                lista = inscDao.listarMateriasInscriptas(alumnoSeleccionado.getIdAlumno(), anio);
            } else {
                lista = inscDao.listarMateriasNoInscriptas(alumnoSeleccionado.getIdAlumno(), anio);
            }

            materiasEnCombo = (lista == null) ? new ArrayList<>() : lista;

            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            if (materiasEnCombo.isEmpty()) {
                model.addElement("Materias");
            } else {
                for (Materia m : materiasEnCombo) {
                    model.addElement(m.getNombre() + "  (Cuat. " + m.getCuatrimestre() + ")");
                }
            }
            cb_materias.setModel(model);
            if (model.getSize() > 0) {
                cb_materias.setSelectedIndex(0);
            }

        } catch (SQLException e) {
            error(e);
            System.out.println("ERROR: " + e);
        }
    }

    private Materia getMateriaSeleccionadaEnCombo() {
        int idx = cb_materias.getSelectedIndex();
        if (idx < 0 || idx >= materiasEnCombo.size()) {
            return null;
        }
        if ("Materias".equalsIgnoreCase(String.valueOf(cb_materias.getSelectedItem()))) {
            return null;
        }
        return materiasEnCombo.get(idx);
    }

    private void tablaClick() {
        int fila = tabla_gestion_inscripciones.getSelectedRow();
        if (fila < 0) return;

        Integer dni = parseEntero(String.valueOf(modelo.getValueAt(fila, 0)));
        if (dni == null) {
            return;
        }

        try {
            Alumno a = alumnoDao.buscarPorDni(dni);
            if (a != null && a.isRegular()) {
                setAlumnoEnFormulario(a);
                recargarMateriasDelCombo();
                reglasHabilitacion();
            } else {
                msg("El alumno no es regular o no existe");
            }
        } catch (SQLException e) {
            error(e);
            System.out.println("ERROR: " + e);
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

        btnGroup_materias = new javax.swing.ButtonGroup();
        pnl_gestion_inscripciones = new javax.swing.JPanel();
        lb_titulo = new javax.swing.JLabel();
        sp_tabla_gestion_inscripciones = new javax.swing.JScrollPane();
        tabla_gestion_inscripciones = new javax.swing.JTable();
        lb_dni = new javax.swing.JLabel();
        lb_materias = new javax.swing.JLabel();
        lb_nombre = new javax.swing.JLabel();
        txt_dni = new javax.swing.JTextField();
        btn_nuevo = new javax.swing.JButton();
        btn_inscribir = new javax.swing.JButton();
        btn_anular_inscripcion = new javax.swing.JButton();
        btn_buscar = new javax.swing.JButton();
        cb_materias = new javax.swing.JComboBox<>();
        btn_salir = new javax.swing.JButton();
        txt_nombre = new javax.swing.JTextField();
        rb_inscripto = new javax.swing.JRadioButton();
        rb_no_inscripto = new javax.swing.JRadioButton();
        lb_apellido = new javax.swing.JLabel();
        txt_apellido = new javax.swing.JTextField();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setPreferredSize(new java.awt.Dimension(1000, 700));

        lb_titulo.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lb_titulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lb_titulo.setText("Gestion de Inscripciones");

        tabla_gestion_inscripciones.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tabla_gestion_inscripciones.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "DNI", "Nombre", "Apellido"
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
        sp_tabla_gestion_inscripciones.setViewportView(tabla_gestion_inscripciones);

        lb_dni.setText("DNI:");

        lb_materias.setText("Materias:");

        lb_nombre.setText("Nombre:");

        btn_nuevo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/escoba.png"))); // NOI18N
        btn_nuevo.setText("Nuevo");
        btn_nuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_nuevoActionPerformed(evt);
            }
        });

        btn_inscribir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/guardar.png"))); // NOI18N
        btn_inscribir.setText("Inscribir");
        btn_inscribir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_inscribirActionPerformed(evt);
            }
        });

        btn_anular_inscripcion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/eliminar.png"))); // NOI18N
        btn_anular_inscripcion.setText("Anular Inscripcion");
        btn_anular_inscripcion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_anular_inscripcionActionPerformed(evt);
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

        txt_nombre.setEditable(false);

        btnGroup_materias.add(rb_inscripto);
        rb_inscripto.setText("Inscripto");

        btnGroup_materias.add(rb_no_inscripto);
        rb_no_inscripto.setText("No Inscripto");

        lb_apellido.setText("Apellido:");

        txt_apellido.setEditable(false);

        javax.swing.GroupLayout pnl_gestion_inscripcionesLayout = new javax.swing.GroupLayout(pnl_gestion_inscripciones);
        pnl_gestion_inscripciones.setLayout(pnl_gestion_inscripcionesLayout);
        pnl_gestion_inscripcionesLayout.setHorizontalGroup(
            pnl_gestion_inscripcionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lb_titulo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_gestion_inscripcionesLayout.createSequentialGroup()
                .addContainerGap(21, Short.MAX_VALUE)
                .addComponent(sp_tabla_gestion_inscripciones, javax.swing.GroupLayout.PREFERRED_SIZE, 946, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
            .addGroup(pnl_gestion_inscripcionesLayout.createSequentialGroup()
                .addGap(196, 196, 196)
                .addGroup(pnl_gestion_inscripcionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnl_gestion_inscripcionesLayout.createSequentialGroup()
                        .addComponent(btn_nuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btn_inscribir, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btn_anular_inscripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_gestion_inscripcionesLayout.createSequentialGroup()
                        .addGroup(pnl_gestion_inscripcionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnl_gestion_inscripcionesLayout.createSequentialGroup()
                                .addComponent(lb_dni, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(txt_dni, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnl_gestion_inscripcionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(pnl_gestion_inscripcionesLayout.createSequentialGroup()
                                    .addComponent(lb_nombre, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txt_nombre, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(pnl_gestion_inscripcionesLayout.createSequentialGroup()
                                    .addComponent(lb_apellido, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGap(18, 18, 18)
                                    .addComponent(txt_apellido, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(cb_materias, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 398, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(pnl_gestion_inscripcionesLayout.createSequentialGroup()
                                    .addComponent(lb_materias, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(rb_inscripto, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(rb_no_inscripto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGap(78, 78, 78)
                        .addComponent(btn_buscar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(btn_salir, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        pnl_gestion_inscripcionesLayout.setVerticalGroup(
            pnl_gestion_inscripcionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_gestion_inscripcionesLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(lb_titulo)
                .addGap(30, 30, 30)
                .addComponent(sp_tabla_gestion_inscripciones, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(58, 58, 58)
                .addGroup(pnl_gestion_inscripcionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnl_gestion_inscripcionesLayout.createSequentialGroup()
                        .addGroup(pnl_gestion_inscripcionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lb_dni, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txt_dni, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(pnl_gestion_inscripcionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txt_nombre, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lb_nombre, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(btn_buscar, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnl_gestion_inscripcionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lb_apellido, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_apellido, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnl_gestion_inscripcionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lb_materias, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rb_inscripto, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rb_no_inscripto, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(cb_materias, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 58, Short.MAX_VALUE)
                .addGroup(pnl_gestion_inscripcionesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_nuevo, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_inscribir, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_anular_inscripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_salir, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(73, 73, 73))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnl_gestion_inscripciones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnl_gestion_inscripciones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_salirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_salirActionPerformed
        dispose();
    }//GEN-LAST:event_btn_salirActionPerformed

    private void btn_nuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_nuevoActionPerformed
        limpiarFormulario();
    }//GEN-LAST:event_btn_nuevoActionPerformed

    private void btn_inscribirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_inscribirActionPerformed
        if (alumnoSeleccionado == null) {
            msg("Busque o seleccione un alumno");
            return;
        }
        
        if (!rb_no_inscripto.isSelected()) {
            msg("Seleccione 'No Inscripto' para poder inscribir");
            return;
        }
        
        Materia m = getMateriaSeleccionadaEnCombo();
        if (m == null) {
            msg("Seleccione una materia");
            return;
        }
        
        int anio = anioActual();
        
        try {
            inscDao.inscribir(alumnoSeleccionado.getIdAlumno(), m.getIdMateria(), anio);
            msg("Inscripcion realizada para el anio " + anio);
            recargarMateriasDelCombo();
            reglasHabilitacion();
        } catch (SQLException e) {
            error(e);
            System.out.println("ERROR: " + e);
        }
    }//GEN-LAST:event_btn_inscribirActionPerformed

    private void btn_anular_inscripcionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_anular_inscripcionActionPerformed
        if (alumnoSeleccionado == null) {
            msg("Busque o seleccione un alumno");
            return;
        }
        
        if (!rb_inscripto.isSelected()) {
            msg("Seleccione 'Inscripto' para anular una inscripcion existente");
            return;
        }
        
        Materia m = getMateriaSeleccionadaEnCombo();
        if (m == null) {
            msg("Seleccione una materia");
            return;
        }
        
        int anio = anioActual();

        int conf = JOptionPane.showConfirmDialog(this,
                "¿Anular la inscripción a '" + m.getNombre() + "' del anio " + anio + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean ok = inscDao.anularInscripcion(alumnoSeleccionado.getIdAlumno(), m.getIdMateria(), anio);
            if (ok) {
                msg("Inscripcion anulada");
                recargarMateriasDelCombo();
                reglasHabilitacion();
            } else {
                msg("No se encontro la inscripcion para anular");
            }
        } catch (SQLException e) {
            error(e);
            System.out.println("ERROR: " + e);
        }
    }//GEN-LAST:event_btn_anular_inscripcionActionPerformed

    private void btn_buscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_buscarActionPerformed
        Integer dni = parseEntero(txt_dni.getText());
        if (dni == null) {
            msg("Ingrese un DNI numerico");
            txt_dni.requestFocus();
            return;
        }

        try {
            Alumno a = alumnoDao.buscarPorDni(dni);
            if (a == null) {
                msg("No existe un alumno con ese DNI");
                return;
            }
            
            if (!a.isRegular()) {
                msg("El alumno no es regular" + "\nNo puede inscribirse");
                return;
            }

            setAlumnoEnFormulario(a);
            seleccionarFilaPorDni(dni);
            recargarMateriasDelCombo();
            reglasHabilitacion();

        } catch (SQLException e) {
            error(e);
            System.out.println("ERROR: " + e);
        }
    }//GEN-LAST:event_btn_buscarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup btnGroup_materias;
    private javax.swing.JButton btn_anular_inscripcion;
    private javax.swing.JButton btn_buscar;
    private javax.swing.JButton btn_inscribir;
    private javax.swing.JButton btn_nuevo;
    private javax.swing.JButton btn_salir;
    private javax.swing.JComboBox<String> cb_materias;
    private javax.swing.JLabel lb_apellido;
    private javax.swing.JLabel lb_dni;
    private javax.swing.JLabel lb_materias;
    private javax.swing.JLabel lb_nombre;
    private javax.swing.JLabel lb_titulo;
    private javax.swing.JPanel pnl_gestion_inscripciones;
    private javax.swing.JRadioButton rb_inscripto;
    private javax.swing.JRadioButton rb_no_inscripto;
    private javax.swing.JScrollPane sp_tabla_gestion_inscripciones;
    private javax.swing.JTable tabla_gestion_inscripciones;
    private javax.swing.JTextField txt_apellido;
    private javax.swing.JTextField txt_dni;
    private javax.swing.JTextField txt_nombre;
    // End of variables declaration//GEN-END:variables
}
