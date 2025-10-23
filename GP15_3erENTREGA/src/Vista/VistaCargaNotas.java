package Vista;

import Modelo.Materia;
import Persistencia.materiaData;
import Persistencia.inscripcionData;

import java.time.LocalDate;
import java.util.*;
import java.sql.SQLException;
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

public class VistaCargaNotas extends javax.swing.JInternalFrame {
    
    private final materiaData mDao = new materiaData();
    private final inscripcionData iDao = new inscripcionData();
    
    private DefaultTableModel modelo;
    private final java.util.List<Integer> idsInscriptoPorFila = new ArrayList<>();
    private final Map<String, Materia> mapComboMaterias = new LinkedHashMap<>();

    /**
     * Creates new form VistaCargaNotas
     */
    public VistaCargaNotas() {
        initComponents();
        configurarTabla();
        cargarComboMaterias();
        escucharCambios();
        reglasHabilitacion();
    }

    private int anioActual() {
        return LocalDate.now().getYear();
    }

    private void msg(String s) {
        JOptionPane.showMessageDialog(this, s);
    }

    private void error(Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void configurarTabla() {
        modelo = (DefaultTableModel) tabla_gestion_notas.getModel();
        tabla_gestion_notas.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    }

    private void cargarComboMaterias() {
        try {
            java.util.List<Materia> materias = mDao.listarTodas();
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            model.addElement("Seleccionar");

            mapComboMaterias.clear();
            for (Materia m : materias) {
                String texto = m.getNombre() + " (Cuat. " + m.getCuatrimestre() + ")";
                model.addElement(texto);
                mapComboMaterias.put(texto, m);
            }
            cb_materia.setModel(model);
            cb_materia.setSelectedIndex(0);
        } catch (SQLException e) {
            error(e);
            System.out.println("ERROR: " + e);
        }
    }

    private Materia getMateriaSeleccionada() {
        Object sel = cb_materia.getSelectedItem();
        if (sel == null) {
            return null;
        }
        return mapComboMaterias.get(sel.toString());
    }

    private void cargarTablaParaMateria(Materia m) {
        limpiarTabla();
        if (m == null) {
            return;
        }

        try {
            java.util.List<Object[]> filas = iDao.listarInscripcionesDetalladasPorMateria(m.getIdMateria(), anioActual());
            for (Object[] f : filas) {
                idsInscriptoPorFila.add((Integer) f[0]);
                modelo.addRow(new Object[]{
                    f[1], f[2], f[3], f[4], f[5], f[6], f[7]
                });
            }
        } catch (SQLException e) {
            error(e);
            System.out.println("ERROR: " + e);
        }
        reglasHabilitacion();
    }

    private void limpiarTabla() {
        modelo.setRowCount(0);
        idsInscriptoPorFila.clear();
    }

    private void reglasHabilitacion() {
        boolean materiaElegida = (getMateriaSeleccionada() != null);
        btn_guardar.setEnabled(materiaElegida && modelo.getRowCount() > 0);
        btn_salir.setEnabled(true);
    }

    private void guardarNotas() {
        if (tabla_gestion_notas.isEditing()) {
            tabla_gestion_notas.getCellEditor().stopCellEditing();
        }

        int colNota = 6;
        for (int row = 0; row < modelo.getRowCount(); row++) {
            Integer idIns = idsInscriptoPorFila.get(row);
            String sNota = String.valueOf(modelo.getValueAt(row, colNota)).trim();

            if (sNota.isEmpty()) {
                continue;
            }

            Double nota = parseNota(sNota);
            if (nota == null) {
                msg("Nota invalida en fila " + (row + 1) + "\nUsar solo numeros entre 0 y 10");
                return;
            } else if(nota > 10) {
                msg("Nota mayor a 10" + "\nVERIFICAR!!");
            } else if(nota < 0) {
                msg("Nota menor a 0" + "\nVERIFICAR!!");
            }
            
            
            try {                    
                iDao.actualizarNota(idIns, nota);
            } catch (SQLException e) {
                error(e);
                System.out.println("ERROR: " + e);
                return;
            }
        }
        msg("Nota(s) actualizada(s)");
        Materia m = getMateriaSeleccionada();
        cargarTablaParaMateria(m);
    }

    private Double parseNota(String s) {
        try {
            return Double.parseDouble(s.replace(',', '.').trim());
        } catch (Exception e) {
            return null;
        }
    }

    private void escucharCambios() {
        cb_materia.addItemListener(e -> {
            Materia m = getMateriaSeleccionada();
            cargarTablaParaMateria(m);
        });
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnl_gestion_notas = new javax.swing.JPanel();
        lb_titulo = new javax.swing.JLabel();
        sp_tabla_gestion_notas = new javax.swing.JScrollPane();
        tabla_gestion_notas = new javax.swing.JTable();
        btn_salir = new javax.swing.JButton();
        lb_materia = new javax.swing.JLabel();
        cb_materia = new javax.swing.JComboBox<>();
        btn_guardar = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setPreferredSize(new java.awt.Dimension(1000, 700));

        lb_titulo.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lb_titulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lb_titulo.setText("Gestion Notas");

        tabla_gestion_notas.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tabla_gestion_notas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "DNI", "Nombre", "Apellido", "Materia", "Cuatrimestre", "Año Inscripcion", "Nota"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabla_gestion_notas.setPreferredSize(new java.awt.Dimension(450, 350));
        sp_tabla_gestion_notas.setViewportView(tabla_gestion_notas);

        btn_salir.setText("Salir");
        btn_salir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_salirActionPerformed(evt);
            }
        });

        lb_materia.setText("Materia:");

        btn_guardar.setText("Guardar");
        btn_guardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_guardarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnl_gestion_notasLayout = new javax.swing.GroupLayout(pnl_gestion_notas);
        pnl_gestion_notas.setLayout(pnl_gestion_notasLayout);
        pnl_gestion_notasLayout.setHorizontalGroup(
            pnl_gestion_notasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lb_titulo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_gestion_notasLayout.createSequentialGroup()
                .addContainerGap(21, Short.MAX_VALUE)
                .addGroup(pnl_gestion_notasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnl_gestion_notasLayout.createSequentialGroup()
                        .addComponent(btn_guardar, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btn_salir, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(sp_tabla_gestion_notas, javax.swing.GroupLayout.PREFERRED_SIZE, 946, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21))
            .addGroup(pnl_gestion_notasLayout.createSequentialGroup()
                .addGap(299, 299, 299)
                .addComponent(lb_materia, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(cb_materia, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnl_gestion_notasLayout.setVerticalGroup(
            pnl_gestion_notasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_gestion_notasLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(lb_titulo)
                .addGap(40, 40, 40)
                .addGroup(pnl_gestion_notasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lb_materia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cb_materia, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(sp_tabla_gestion_notas, javax.swing.GroupLayout.PREFERRED_SIZE, 385, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(pnl_gestion_notasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btn_salir, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                    .addComponent(btn_guardar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(51, 51, 51))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnl_gestion_notas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnl_gestion_notas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_salirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_salirActionPerformed
        dispose();
    }//GEN-LAST:event_btn_salirActionPerformed

    private void btn_guardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_guardarActionPerformed
        guardarNotas();
    }//GEN-LAST:event_btn_guardarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_guardar;
    private javax.swing.JButton btn_salir;
    private javax.swing.JComboBox<String> cb_materia;
    private javax.swing.JLabel lb_materia;
    private javax.swing.JLabel lb_titulo;
    private javax.swing.JPanel pnl_gestion_notas;
    private javax.swing.JScrollPane sp_tabla_gestion_notas;
    private javax.swing.JTable tabla_gestion_notas;
    // End of variables declaration//GEN-END:variables
}
