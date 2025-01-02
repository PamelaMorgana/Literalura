package com.aluracursos.literalura.repository;

import com.aluracursos.literalura.model.Libros;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibroRepositorio extends JpaRepository<Libros, Long> {

    boolean existsByTitulo(String titulo);

    @Query("SELECT l FROM Libros l WHERE l.idiomas LIKE %:idiomas%")
    List<Libros> findByIdiomas(@Param("idiomas") String idiomas);

    @Query("SELECT COUNT(l) FROM Libros l WHERE l.idiomas LIKE %:idioma%")
    long obtenerCantidadLibrosPorIdioma(@Param("idioma") String idioma);
}
