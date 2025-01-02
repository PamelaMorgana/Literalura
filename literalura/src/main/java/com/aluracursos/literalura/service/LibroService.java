package com.aluracursos.literalura.service;

import com.aluracursos.literalura.model.Autor;
import com.aluracursos.literalura.model.DatosLibros;
import com.aluracursos.literalura.model.Libros;
import com.aluracursos.literalura.repository.AutorRepositorio;
import com.aluracursos.literalura.repository.LibroRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LibroService {
    @Autowired
    private AutorRepositorio autorRepositorio;
    @Autowired
    private LibroRepositorio libroRepositorio;

    @Autowired
    public LibroService(AutorRepositorio autorRepositorio, LibroRepositorio libroRepositorio){
        this.autorRepositorio = autorRepositorio;
        this.libroRepositorio = libroRepositorio;
    }

    public void guardarLibro(DatosLibros datos) {
        // Busca el autor en la base de datos. Si no existe, lo crea.
        Autor autor = autorRepositorio.findByNombre(datos.autor().get(0).nombre());
        if (autor == null) {
            autor = new Autor(datos.autor().get(0));
            autor = autorRepositorio.save(autor);
        }

        Libros libro = new Libros(datos, autor); // Pasa el autor al constructor de Libros
        libroRepositorio.save(libro);
    }

    public long obtenerCantidadLibrosPorIdioma(String idiomaSeleccionado) {
        return libroRepositorio.obtenerCantidadLibrosPorIdioma(idiomaSeleccionado);
    }

    public List<Libros> findByIdiomas(String idiomaSeleccionado) {
        return libroRepositorio.findByIdiomas(idiomaSeleccionado);
    }
}
