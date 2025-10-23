package Persistencia;

import Modelo.Inscripcion;
import Modelo.Materia;
import Modelo.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** 
    @author Grupo 15
    Luis Ezequiel Sosa
    Lucas Saidman
    Luca Rodrigaño
    Ignacio Rodriguez
**/

public class inscripcionData {
    public Inscripcion inscribir(int idAlumno, int idMateria, int anio) throws SQLException {
        if (!alumnoEsRegular(idAlumno)) {
            throw new SQLException("El alumno no esta regular \nNo se puede inscribir");
        }
        if (!materiaEstaActiva(idMateria)) {
            throw new SQLException("La materia no esta activa \nNo se puede inscribir");
        }
        if (existeInscripcion(idAlumno, idMateria, anio)) {
            throw new SQLException("Ya existe una inscripcion del alumno a esa materia en ese anio.");
        }

        String sql = "INSERT INTO inscripcion (idAlumno, idMateria, anioInscripcion, nota) VALUES (?, ?, ?, NULL)";
        try (PreparedStatement ps = Conexion.getConexion()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, idAlumno);
            ps.setInt(2, idMateria);
            ps.setInt(3, anio);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Inscripcion insc = new Inscripcion();
                    insc.setIdInscripto(rs.getInt(1));
                    insc.setIdAlumno(idAlumno);
                    insc.setIdMateria(idMateria);
                    insc.setAnioInscripcion(anio);
                    insc.setNota(0.0);
                    return insc;
                }
            }
        }
        return null;
    }

    public boolean anularInscripcion(int idInscripto) throws SQLException {
        String sql = "DELETE FROM inscripcion WHERE idInscripto = ?";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idInscripto);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean anularInscripcion(int idAlumno, int idMateria, int anio) throws SQLException {
        String sql = "DELETE FROM inscripcion WHERE idAlumno=? AND idMateria=? AND anioInscripcion=?";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idAlumno);
            ps.setInt(2, idMateria);
            ps.setInt(3, anio);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Materia> listarMateriasInscriptas(int idAlumno, int anio) throws SQLException {
        String sql =
            "SELECT m.idMateria, m.nombre, m.cuatrimestre, m.estado" +
            "\nFROM inscripcion i" +
            "\nJOIN materia m ON m.idMateria = i.idMateria" +
            "\nWHERE i.idAlumno = ? AND i.anioInscripcion = ?" +
            "\nORDER BY m.nombre";
        List<Materia> out = new ArrayList<>();
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idAlumno);
            ps.setInt(2, anio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapMateria(rs));
            }
        }
        return out;
    }

    public List<Materia> listarMateriasNoInscriptas(int idAlumno, int anio) throws SQLException {
        String sql =
            "SELECT m.idMateria, m.nombre, m.cuatrimestre, m.estado" +
            "\nFROM materia m" +
            "\nWHERE m.estado = 1" +
            "\nAND m.idMateria NOT IN (" +
            "\nSELECT i.idMateria" +
            "\nFROM inscripcion i" +
            "\nWHERE i.idAlumno = ? AND i.anioInscripcion = ?" +
            "\n)" +
            "\nORDER BY m.nombre";
        List<Materia> out = new ArrayList<>();
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idAlumno);
            ps.setInt(2, anio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapMateria(rs));
            }
        }
        return out;
    }

    public void actualizarNota(int idInscripto, double nota) throws SQLException {
        double n = limitesNota(redondear(nota), 0.0, 10.0);

        String sql = "UPDATE inscripcion SET nota = ? WHERE idInscripto = ?";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setDouble(1, n);
            ps.setInt(2, idInscripto);
            ps.executeUpdate();
        }
    }

    private Materia mapMateria(ResultSet rs) throws SQLException {
        Materia m = new Materia();
        m.setIdMateria(rs.getInt("idMateria"));
        m.setNombre(rs.getString("nombre"));
        m.setCuatrimestre(rs.getInt("cuatrimestre"));
        m.setEstado(rs.getBoolean("estado"));
        return m;
    }

    private boolean existeInscripcion(int idAlumno, int idMateria, int anio) throws SQLException {
        String sql = "SELECT 1 FROM inscripcion WHERE idAlumno=? AND idMateria=? AND anioInscripcion=? LIMIT 1";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idAlumno);
            ps.setInt(2, idMateria);
            ps.setInt(3, anio);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean alumnoEsRegular(int idAlumno) throws SQLException {
        String sql = "SELECT regular FROM alumno WHERE idAlumno=? LIMIT 1";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idAlumno);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBoolean(1);
                throw new SQLException("Alumno inexistente: \nid = " + idAlumno);
            }
        }
    }

    private boolean materiaEstaActiva(int idMateria) throws SQLException {
        String sql = "SELECT estado FROM materia WHERE idMateria=? LIMIT 1";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idMateria);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBoolean(1);
                throw new SQLException("Materia inexistente: \nid = " + idMateria);
            }
        }
    }
    
    public List<Object[]> listarInscripcionesDetalladasPorMateria(int idMateria, int anio) throws SQLException {
        String sql =
            "SELECT i.idInscripto, a.dni, a.nombre, a.apellido, m.nombre AS materia, m.cuatrimestre, i.anioInscripcion, i.nota " +
            "\nFROM inscripcion i " +
            "\nJOIN alumno a   ON a.idAlumno = i.idAlumno " +
            "\nJOIN materia m  ON m.idMateria = i.idMateria " +
            "\nWHERE i.idMateria = ? AND i.anioInscripcion = ? " +
            "\nORDER BY a.apellido, a.nombre";

        List<Object[]> out = new ArrayList<>();
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idMateria);
            ps.setInt(2, anio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = new Object[]{
                        rs.getInt("idInscripto"),
                        String.valueOf(rs.getInt("dni")),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("materia"),
                        String.valueOf(rs.getInt("cuatrimestre")),
                        String.valueOf(rs.getInt("anioInscripcion")),
                        rs.getObject("nota") == null ? "" : String.valueOf(rs.getDouble("nota"))
                    };
                    out.add(fila);
                }
            }
        }
        return out;
    }
    
    public List<Object[]> listarInscripcionesPorAlumno(int idAlumno) throws SQLException {
        String sql =
            "SELECT a.dni, a.nombre, a.apellido, m.nombre AS materia, m.cuatrimestre, i.anioInscripcion " +
            "\nFROM inscripcion i " +
            "\nJOIN alumno a ON a.idAlumno = i.idAlumno " +
            "\nJOIN materia m ON m.idMateria = i.idMateria " +
            "\nWHERE i.idAlumno = ? " +
            "\nORDER BY i.anioInscripcion DESC, m.nombre";

        List<Object[]> out = new ArrayList<>();
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, idAlumno);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = new Object[6];
                    fila[0] = rs.getInt("dni");
                    fila[1] = rs.getString("nombre");
                    fila[2] = rs.getString("apellido");
                    fila[3] = rs.getString("materia");
                    fila[4] = rs.getInt("cuatrimestre");
                    fila[5] = rs.getInt("anioInscripcion");
                    out.add(fila);
                }
            }
        }
        return out;
    }

    private double redondear(double value) {
        return Math.round(value * 2.0) / 2.0;
    }

    private double limitesNota(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
