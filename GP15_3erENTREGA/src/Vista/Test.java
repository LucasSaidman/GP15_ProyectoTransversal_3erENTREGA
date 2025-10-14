package Vista;

import Modelo.Alumno;
import Persistencia.alumnoData;

import Modelo.Materia;
import Persistencia.materiaData;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Grupo 15
 * Luis Ezequiel Sosa
 * Lucas Saidman
 * Luca Rodrigaño
 * Ignacio Rodriguez
 */

public class Test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        try {
            //--- TEST ALUMNO ---
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
            
            
            //--- TEST MATERIA ---
            materiaData md = new materiaData();

            //1. Crear materias
            Materia m1 = new Materia("Programacion I", 1, true);
            md.guardar(m1);
            System.out.println("Materia creada: " + m1);

            Materia m2 = new Materia("Programacion II", 2, true);
            md.guardar(m2);
            System.out.println("Materia creada: " + m2);

            Materia m3 = new Materia("Estructuras de Datos", 3, true);
            md.guardar(m3);
            System.out.println("Materia creada: " + m3);

            Materia m4 = new Materia("Base de Datos", 4, true);
            md.guardar(m4);
            System.out.println("Materia creada: " + m4);

            Materia m5 = new Materia("Sistemas Operativos", 5, false);
            md.guardar(m5);
            System.out.println("Materia creada: " + m5);

            //2. Listar todas las materias
            List<Materia> materias = md.listarTodas();
            System.out.println("Listado de materias:");
            for (Materia m : materias) {
                System.out.println(m);
            }

            //3. Buscar una materia por nombre y cuatrimestre
            System.out.println("Buscando materia por Nombre + Cuatrimestre");
            Materia materiaBuscada = md.buscarPorNombreYCuat("Sistemas Operativos", 5);
            if (materiaBuscada != null) {
                System.out.println("Materia encontrada: " + materiaBuscada);
            } else {
                System.out.println("No se encontro la materia solicitada");
            }

            //4. Actualizar una materia
            if (materiaBuscada != null) {
                materiaBuscada.setEstado(true);
                materiaBuscada.setCuatrimestre(6);
                materiaBuscada.setNombre("Base de Datos Avanzada");
                md.actualizar(materiaBuscada);
                System.out.println("Materia actualizada:");
                System.out.println(materiaBuscada);
            }

            //5. Alta logica
            if (materiaBuscada != null) {
                md.altaLogica(materiaBuscada.getIdMateria());
                System.out.println("Materia dada de alta");
            }

            //6. Baja logica
            if (materiaBuscada != null) {
                md.bajaLogica(materiaBuscada.getIdMateria());
                System.out.println("Materia dada de baja");
            }

            //7. Borrar una materia
            if (materiaBuscada != null) {
                md.borrar(materiaBuscada.getIdMateria());
                System.out.println("Materia borrada");
            }

            //8. Ver listado final
            System.out.println("Listado final de materias:");
            for (Materia m : md.listarTodas()) {
                System.out.println(m);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
        }
        
    }
    
}