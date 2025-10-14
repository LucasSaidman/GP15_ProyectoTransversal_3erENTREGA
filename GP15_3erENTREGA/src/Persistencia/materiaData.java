package Persistencia;

import Modelo.Conexion;
import Modelo.Materia;

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

public class materiaData {

    public Materia guardar(Materia m) throws SQLException {
        String sql = "INSERT INTO materia (nombre, cuatrimestre, estado) VALUES (?, ?, ?)";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getNombre());
            ps.setInt(2, m.getCuatrimestre());
            ps.setBoolean(3, m.isEstado());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) m.setIdMateria(rs.getInt(1));
            }
        }
        return m;
    }

    public void actualizar(Materia m) throws SQLException {
        String sql = "UPDATE materia SET nombre=?, cuatrimestre=?, estado=? WHERE idMateria=?";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, m.getNombre());
            ps.setInt(2, m.getCuatrimestre());
            ps.setBoolean(3, m.isEstado());
            ps.setInt(4, m.getIdMateria());
            ps.executeUpdate();
        }
    }

    public Materia buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM materia WHERE idMateria=?";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public Materia buscarPorNombreYCuat(String nombre, int cuatrimestre) throws SQLException {
        String sql = "SELECT * FROM materia WHERE nombre=? AND cuatrimestre=?";
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, cuatrimestre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Materia> listarTodas() throws SQLException {
        String sql = "SELECT * FROM materia ORDER BY nombre";
        List<Materia> out = new ArrayList<>();
        try (PreparedStatement ps = Conexion.getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(mapRow(rs));
        }
        return out;
    }

    public void bajaLogica(int id) throws SQLException {
        try (PreparedStatement ps = Conexion.getConexion()
                .prepareStatement("UPDATE materia SET estado=0 WHERE idMateria=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void altaLogica(int id) throws SQLException {
        try (PreparedStatement ps = Conexion.getConexion()
                .prepareStatement("UPDATE materia SET estado=1 WHERE idMateria=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void borrar(int id) throws SQLException {
        try (PreparedStatement ps = Conexion.getConexion()
                .prepareStatement("DELETE FROM materia WHERE idMateria=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Materia mapRow(ResultSet rs) throws SQLException {
        Materia m = new Materia();
        m.setIdMateria(rs.getInt("idMateria"));
        m.setNombre(rs.getString("nombre"));
        m.setCuatrimestre(rs.getInt("cuatrimestre"));
        m.setEstado(rs.getBoolean("estado"));
        return m;
    }
}