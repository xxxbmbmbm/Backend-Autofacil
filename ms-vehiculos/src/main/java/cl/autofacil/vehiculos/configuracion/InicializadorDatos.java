package cl.autofacil.vehiculos.configuracion;

import cl.autofacil.vehiculos.modelo.Categoria;
import cl.autofacil.vehiculos.modelo.Vehiculo;
import cl.autofacil.vehiculos.repositorio.CategoriaRepositorio;
import cl.autofacil.vehiculos.repositorio.VehiculoRepositorio;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Carga el catálogo que antes estaba fijo en el frontend, solo si la base de datos está vacía. */
@Component
public class InicializadorDatos implements CommandLineRunner {

    private static final String WIKI = "https://commons.wikimedia.org/wiki/Special:FilePath/";

    private final CategoriaRepositorio categoriaRepositorio;
    private final VehiculoRepositorio vehiculoRepositorio;

    public InicializadorDatos(CategoriaRepositorio categoriaRepositorio, VehiculoRepositorio vehiculoRepositorio) {
        this.categoriaRepositorio = categoriaRepositorio;
        this.vehiculoRepositorio = vehiculoRepositorio;
    }

    @Override
    public void run(String... argumentos) {
        if (categoriaRepositorio.count() > 0) {
            return;
        }
        Map<String, Categoria> categorias = new HashMap<>();
        for (String[] datos : List.of(
                new String[]{"Hatchback", "Autos compactos y urbanos"},
                new String[]{"Sedán", "Autos familiares con maletero separado"},
                new String[]{"SUV", "Vehículos utilitarios deportivos"},
                new String[]{"Pickup", "Camionetas con pick-up de carga"})) {
            categorias.put(datos[0], categoriaRepositorio.save(new Categoria(datos[0], datos[1])));
        }

        // marca, modelo, año, tipo, precio, precio anterior, km, transmisión, color, imagen
        Object[][] catalogo = {
                {"Toyota", "Yaris", 2021, "Hatchback", 8990000L, null, 32000, "Automática", "#141416", "2021_Toyota_Yaris_ATIV_Sport_Premium_%28Two-Tone%29.jpg"},
                {"Chevrolet", "Sail", 2020, "Sedán", 7490000L, 8190000L, 41500, "Mecánica", "#e6127d", "Chevrolet_Sail.jpg"},
                {"Nissan", "Qashqai", 2022, "SUV", 14990000L, null, 18200, "Automática", "#3a3a42", "Nissan_Qashqai_%28J12%29_IMG_4897.jpg"},
                {"Ford", "Ranger", 2019, "Pickup", 16500000L, null, 58000, "Mecánica", "#141416", "2019_Ford_Ranger_XLT_Super_Cab_FX4_front_6.1.19.jpg"},
                {"Hyundai", "Accent", 2021, "Sedán", 8290000L, null, 27800, "Automática", "#c40e69", "Hyundai-Accent-GLS-sedan.jpg"},
                {"Kia", "Sportage", 2020, "SUV", 13990000L, 15290000L, 39400, "Automática", "#3a3a42", "Kia_Sportage_%28NQ5%3B_2021%29.jpg"},
                {"Suzuki", "Swift", 2022, "Hatchback", 9490000L, null, 12900, "Mecánica", "#141416", "2018_Suzuki_Swift_%28AZ%29_GLX_Turbo_5-door_hatchback_%282018-02-20%29_01.jpg"},
                {"Mazda", "3", 2021, "Sedán", 11990000L, null, 24600, "Automática", "#e6127d", "Mazda_3.jpg"},
                {"Toyota", "Hilux", 2018, "Pickup", 15990000L, 17490000L, 72300, "Mecánica", "#3a3a42", "Toyota_Hilux_Pickup_4x4_2-door_1987.jpg"},
                {"Chevrolet", "Tracker", 2023, "SUV", 12490000L, null, 8100, "Automática", "#141416", "Chevrolet_Tracker_LT_1.8_2014_%2815732033549%29.jpg"},
                {"Peugeot", "208", 2020, "Hatchback", 7990000L, null, 35700, "Mecánica", "#c40e69", "Peugeot_208_B_IMG_2566.jpg"},
                {"Volkswagen", "Vento", 2019, "Sedán", 9990000L, null, 48900, "Automática", "#3a3a42", "Volkswagen_Vento_2014_Perfil.png"},
        };
        // Se guardan del último al primero para que el listado (más nuevos primero) quede en el mismo orden que antes
        for (int i = catalogo.length - 1; i >= 0; i--) {
            Object[] fila = catalogo[i];
            Vehiculo vehiculo = new Vehiculo();
            vehiculo.setMarca((String) fila[0]);
            vehiculo.setModelo((String) fila[1]);
            vehiculo.setAnio((Integer) fila[2]);
            vehiculo.setCategoria(categorias.get((String) fila[3]));
            vehiculo.setPrecio((Long) fila[4]);
            vehiculo.setPrecioAnterior((Long) fila[5]);
            vehiculo.setKilometraje((Integer) fila[6]);
            vehiculo.setTransmision((String) fila[7]);
            vehiculo.setColorFondo((String) fila[8]);
            vehiculo.setImagen(WIKI + fila[9]);
            vehiculoRepositorio.save(vehiculo);
        }
    }
}
