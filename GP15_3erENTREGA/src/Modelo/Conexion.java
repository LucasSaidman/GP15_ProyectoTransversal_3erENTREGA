package Modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** 
    @author Grupo 15
    Luis Ezequiel Sosa
    Lucas Saidman
    Luca Rodrigaño
    Ignacio Rodriguez
**/

public class Conexion {
    private static final String URL = "jdbc:mariadb://localhost:3306/universidadulp";
    private static final String USER = "root";
    private static final String PASS = "";

    private static Connection connection;

    private Conexion() {}

    public static Connection getConexion() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.mariadb.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("No se encontró el driver de MariaDB", e);
            }
            connection = DriverManager.getConnection(URL, USER, PASS);
        }
        return connection;
    }
}
