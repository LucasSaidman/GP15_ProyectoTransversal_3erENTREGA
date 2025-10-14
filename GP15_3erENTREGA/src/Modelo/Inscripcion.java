package Modelo;

/** 
    @author Grupo 15
    Luis Ezequiel Sosa
    Lucas Saidman
    Luca Rodrigaño
    Ignacio Rodriguez
**/

public class Inscripcion {
    private int idInscripto;
    private int idAlumno;
    private int idMateria;
    private int anioInscripcion;
    private double nota;

    public Inscripcion() {
    }

    public Inscripcion(int idAlumno, int idMateria, int anioInscripcion, double nota) {
        this.idAlumno = idAlumno;
        this.idMateria = idMateria;
        this.anioInscripcion = anioInscripcion;
        this.nota = nota;
    }

    public int getIdInscripto() {
        return idInscripto;
    }

    public void setIdInscripto(int idInscripto) {
        this.idInscripto = idInscripto;
    }

    public int getIdAlumno() {
        return idAlumno;
    }

    public void setIdAlumno(int idAlumno) {
        this.idAlumno = idAlumno;
    }

    public int getIdMateria() {
        return idMateria;
    }

    public void setIdMateria(int idMateria) {
        this.idMateria = idMateria;
    }

    public int getAnioInscripcion() {
        return anioInscripcion;
    }

    public void setAnioInscripcion(int anioInscripcion) {
        this.anioInscripcion = anioInscripcion;
    }

    public double getNota() {
        return nota;
    }

    public void setNota(double nota) {
        this.nota = nota;
    }

    @Override
    public String toString() {
        return "Inscripcion{" + "idInscripto=" + idInscripto + ", idAlumno=" + idAlumno + ", idMateria=" + idMateria + ", anioInscripcion=" + anioInscripcion + ", nota=" + nota + '}';
    }
}
