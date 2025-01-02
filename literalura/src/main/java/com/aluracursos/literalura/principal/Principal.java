package com.aluracursos.literalura.principal;

import com.aluracursos.literalura.model.Autor;
import com.aluracursos.literalura.model.Datos;
import com.aluracursos.literalura.model.DatosLibros;
import com.aluracursos.literalura.model.Libros;
import com.aluracursos.literalura.repository.AutorRepositorio;
import com.aluracursos.literalura.repository.LibroRepositorio;
import com.aluracursos.literalura.service.ConsumoAPI;
import com.aluracursos.literalura.service.ConvierteDatos;
import com.aluracursos.literalura.service.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Principal {
    private final ConsumoAPI consumoAPI = new ConsumoAPI();
    private static final String URL_BASE = "https://gutendex.com/books/";
    private final ConvierteDatos conversor = new ConvierteDatos();
    private final Scanner teclado = new Scanner(System.in);
    @Autowired
    private LibroRepositorio libroRepositorio;
    @Autowired
    private AutorRepositorio autorRepositorio;
    @Autowired
    private LibroService libroService;

    public void muestraElMenu() {
        var json = consumoAPI.obtenerDatos(URL_BASE);

        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    MENU PRINCIPAL
                    1 - Buscar libro por título
                    2 - Listar libros registrados
                    3 - Listar autores registrados
                    4 - Listar autores vivos en un determinado año
                    5 - Listar libros por idioma
                    
                    0 - Salir
                    
                    Escoge una de las opciones:
                    """;
            System.out.println(menu);
            try {
                opcion = teclado.nextInt();
                teclado.nextLine();

                switch (opcion) {
                    case 1:
                        buscarLibro();
                        break;
                    case 2:
                        mostrarLibrosBuscados();
                        break;
                    case 3:
                        mostrarAutoresBuscados();
                        break;
                    case 4:
                        mostrarAutorVivoPorAno();
                        break;
                    case 5:
                        mostrarLibrosPorIdioma();
                        break;
                    case 0:
                        System.out.println("Cerrando la aplicación...");
                        break;
                    default:
                        System.out.println("Opción inválida");
                }
            } catch (InputMismatchException e) {
                System.err.println("Entrada inválida. Por favor, introduce un número.");
                teclado.nextLine();
            }
        }
    }

    private DatosLibros getDatosLibro() {
        System.out.println("Escribe el nombre del libro que deseas buscar");
        var tituloLibro = teclado.nextLine();
        var json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + tituloLibro.replace(" ", "+"));
        var datosBusqueda = conversor.obtenerDatos(json, Datos.class);
        Optional<DatosLibros> libroBuscado = datosBusqueda.resultados().stream()
                .filter(l -> l.titulo().toUpperCase().contains(tituloLibro.toUpperCase()))
                .findFirst();
        if (libroBuscado.isPresent()){
            return libroBuscado.get();
        } else {
            return null;
        }
    }
    private void buscarLibro() {
        DatosLibros datos = getDatosLibro();
        if (datos != null) {
            // Revisa si el libro ya está en la base de datos
            if (!libroRepositorio.existsByTitulo(datos.titulo())){
                try {
                    Autor autor = new Autor(datos.autor().get(0));
                    //Revisa si el autor ya esta en la base de datos
                    Autor autorExistente = autorRepositorio.findByNombre(autor.getNombre());

                    if (autorExistente == null) {
                        autor = autorRepositorio.save(autor);
                    } else {
                        autor = autorExistente;
                    }

                    Libros libro = new Libros(datos, autor);
                    libroRepositorio.save(libro);
                    System.out.println("Libro guardado correctamente: " + datos);
                } catch (Exception e) {
                    System.err.println("Error al guardar el libro:" + datos);
                }
            } else {
                System.out.println("El libro '" + datos.titulo() + "' ya existe en la base de datos.");
            }
        } else {
            System.out.println("Libro no encontrado");
        }
    }
    private void mostrarLibrosBuscados() {
        List<Libros> libros = libroRepositorio.findAll();

        libros.stream()
                .sorted(Comparator.comparing(Libros::getTitulo))
                .forEach(l -> System.out.println("Título: " + l.getTitulo()
                        + ", Autor: " + l.getAutor().getNombre()
                        + ", Idiomas: " + l.getIdiomas()
                        + ", Número de descargas: " + l.getNumeroDeDescargas()));
    }
    private void mostrarAutoresBuscados(){
        List<Autor> autores = autorRepositorio.findAll();

        autores.stream()
                .sorted(Comparator.comparing(Autor::getNombre))
                .forEach(a -> System.out.println("Nombre: " + a.getNombre()
                        + ", Fecha de nacimiento: " + a.getFechaDeNacimiento()
                        + ", Fecha de fallecimiento: " + a.getFechaDeFallecimiento()));
    }

    private void mostrarLibrosPorIdioma(){
        System.out.println("""
                    Ingrese el idioma para buscar los libros:
                    es- Español
                    en- Ingles
                    """);

        var idiomaSeleccionado = teclado.nextLine().toLowerCase();
        if (idiomaSeleccionado.equals("es") || idiomaSeleccionado.equals("en")) {
            long cantidad = libroService.obtenerCantidadLibrosPorIdioma(idiomaSeleccionado);
            System.out.println("Cantidad de libros en ese idioma: " + cantidad);
        } else {
            System.out.println("Escribe una opción correcta: en o es.");
            return;
        }

        List<Libros> librosPorIdioma = libroService.findByIdiomas(idiomaSeleccionado);
        librosPorIdioma.forEach(l ->
                System.out.println("Título: " + l.getTitulo()
                        + ", Autor: " + l.getAutor().getNombre()
                        + ", Idiomas: " + l.getIdiomas()
                        + ", Número de descargas: " + l.getNumeroDeDescargas()));
    }

    private void mostrarAutorVivoPorAno() {
        int anoSeleccionado;
        do {
            System.out.println("Escribe el año (AAAA) que estás buscando:");
            while (!teclado.hasNextInt()) {
                System.out.println("Entrada inválida. Por favor, ingresa un número entero.");
                teclado.next();
            }
            anoSeleccionado = teclado.nextInt();
            teclado.nextLine();

            if (String.valueOf(anoSeleccionado).length() != 4) {
                System.out.println("El año debe tener cuatro dígitos. Intenta de nuevo.");
            }
        } while (String.valueOf(anoSeleccionado).length() != 4);

        System.out.println("Año seleccionado: " + anoSeleccionado);
        List<Autor> filtroAutores = autorRepositorio.buscarAutorVivoPorAno(anoSeleccionado);
        filtroAutores.forEach(a ->
                System.out.println("Nombre: " + a.getNombre()
                        + ", Año de Nacimiento: " + a.getFechaDeNacimiento()
                        + ", Año de Fallecimiento: " + a.getFechaDeFallecimiento()));
    }
}