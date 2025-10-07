package Vista;

import Modelo.Alumno;
import Persistencia.alumnoData;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Grupo 15
 * Luis Ezequiel Sosa
 * Lucas Saidman
 * Luca Rodrigaño
 * Ignacio Rodriguez
 */

public class App {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        try {
            alumnoData ad = new alumnoData();

            //1. Crear alumnos del Grupo
            Alumno a1 = new Alumno(43765294, "Saidman", "Lucas", LocalDate.of(2001, 10, 9), true);
            ad.guardar(a1);
            System.out.println("Alumno Creado correctamente:");
            System.out.println(a1);
            
            Alumno a2 = new Alumno(41054010, "Scarso", "Ignacio", LocalDate.of(1998, 4, 22), true);
            ad.guardar(a2);
            System.out.println("Alumno Creado correctamente:");
            System.out.println(a2);
            
            Alumno a3 = new Alumno(43620897, "Sosa", "Luis", LocalDate.of(2001, 7, 6), true);
            ad.guardar(a3);
            System.out.println("Alumno Creado correctamente:");
            System.out.println(a3);
            
            Alumno a4 = new Alumno(39395910, "Rodrigano", "Luca", LocalDate.of(1996, 4, 26), true);
            ad.guardar(a4);
            System.out.println("Alumno Creado correctamente:");
            System.out.println(a4);
            
            Alumno a5 = new Alumno(45123456, "Simpson", "Homero", LocalDate.of(1956, 5, 12), false);
            ad.guardar(a5);
            System.out.println("Alumno Creado correctamente:");
            System.out.println(a5);

            //2. Listar todos los alumnos
            List<Alumno> alumnos = ad.listarTodos();
            System.out.println("Listado de alumnos:");
            for (Alumno a : alumnos) {
                System.out.println(a);
            }

            //3. Buscar un alumno por DNI
            System.out.println("Buscando alumno por DNI");
            Alumno alumnoBuscado = ad.buscarPorDni(45123456);
            if (alumnoBuscado != null) {
                System.out.println("Alumno encontrado: " + alumnoBuscado);
            } else {
                System.out.println("No se encontro el alumno con ese DNI");
            }

            //4. Actualizar un alumno
            if (alumnoBuscado != null) {
                alumnoBuscado.setNombre("Bart");
                alumnoBuscado.setFechaNacimiento(LocalDate.of(1980, 4, 1));
                ad.actualizar(alumnoBuscado);
                System.out.println(" Alumno actualizado:");
                System.out.println(alumnoBuscado);
            }
            
            //5. Dar alta lógica
            if (alumnoBuscado != null) {
                ad.altaLogica(alumnoBuscado.getIdAlumno());
                System.out.println("Alumno dado de alta");
            }

            //6. Dar baja lógica
            if (alumnoBuscado != null) {
                ad.bajaLogica(alumnoBuscado.getIdAlumno());
                System.out.println("Alumno dado de baja");
            }
            
            //7. Borrar un alumno
            ad.borrar(alumnoBuscado.getIdAlumno());
            System.out.println("Alumno borrado");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
        }
        
    }
    
}
