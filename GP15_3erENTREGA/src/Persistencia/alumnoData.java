package Persistencia;

import Modelo.Alumno;
import Modelo.Conexion;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** 
    @author Grupo 15
    Luis Ezequiel Sosa
    Lucas Saidman
    Luca Rodrigaño
    Ignacio Rodriguez
**/

public class alumnoData {
    
    public Alumno guardar(Alumno a) throws SQLException {
        
        String sql = "INSERT INTO alumno (dni, apellido, nombre, fechaNacimiento, regular) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getDni());
            ps.setString(2, a.getApellido());
            ps.setString(3, a.getNombre());
            ps.setDate(4, Date.valueOf(a.getFechaNacimiento()));
            ps.setBoolean(5, a.isRegular());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()){
                    a.setIdAlumno(rs.getInt(1));
                }
            }
        }
        return a;
    }
    
    public void actualizar(Alumno a) throws SQLException {
        String sql = "UPDATE alumno SET dni=?, apellido=?, nombre=?, fechaNacimiento=?, regular=? WHERE idAlumno=?";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, a.getDni());
            ps.setString(2, a.getApellido());
            ps.setString(3, a.getNombre());
            ps.setDate(4, Date.valueOf(a.getFechaNacimiento()));
            ps.setBoolean(5, a.isRegular());
            ps.setInt(6, a.getIdAlumno());
            ps.executeUpdate();
        }
    }

    public Alumno buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM alumno WHERE idAlumno=?";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return map(rs);
            }
        }
    }

    public Alumno buscarPorDni(int dni) throws SQLException {
        String sql = "SELECT * FROM alumno WHERE dni=?";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                return map(rs);
            }
        }
    }

    public List<Alumno> listarTodos() throws SQLException {
        String sql = "SELECT * FROM alumno ORDER BY apellido, nombre";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Alumno> out = new ArrayList<>();
            while (rs.next()) out.add(mapRow(rs));
            return out;
        }
    }

    public void altaLogica(int id) throws SQLException {
        try (PreparedStatement ps = Conexion.getConexion()
                .prepareStatement("UPDATE alumno SET regular=1 WHERE idAlumno=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void bajaLogica(int id) throws SQLException {
        try (PreparedStatement ps = Conexion.getConexion()
                .prepareStatement("UPDATE alumno SET regular=0 WHERE idAlumno=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void borrar(int id) throws SQLException {
        try (PreparedStatement ps = Conexion.getConexion()
                .prepareStatement("DELETE FROM alumno WHERE idAlumno=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Alumno map(ResultSet rs) throws SQLException {
        if (rs.next()) {
            return mapRow(rs);
        }
        
        return null;
    }

    private Alumno mapRow(ResultSet rs) throws SQLException {
        Alumno a = new Alumno();
        a.setIdAlumno(rs.getInt("idAlumno"));
        a.setDni(rs.getInt("dni"));
        a.setApellido(rs.getString("apellido"));
        a.setNombre(rs.getString("nombre"));
        Date fn = rs.getDate("fechaNacimiento");
        a.setFechaNacimiento(fn != null ? fn.toLocalDate() : null);
        a.setRegular(rs.getBoolean("regular"));
        return a;
    }
}
