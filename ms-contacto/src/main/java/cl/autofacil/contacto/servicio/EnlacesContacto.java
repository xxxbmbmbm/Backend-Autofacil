package cl.autofacil.contacto.servicio;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Arma los enlaces wa.me, mailto: y tel: para responder al cliente con un clic. */
public final class EnlacesContacto {

    private EnlacesContacto() {
    }

    /**
     * Normaliza un celular chileno al formato internacional sin "+": 56912345678.
     * Acepta "+56 9 1234 5678", "912345678" o "12345678". Devuelve null si no se puede interpretar.
     */
    public static String normalizarTelefono(String telefono) {
        if (telefono == null) return null;
        String digitos = telefono.replaceAll("\\D", "");
        if (digitos.length() == 11 && digitos.startsWith("56")) return digitos;
        if (digitos.length() == 9) return "56" + digitos;
        if (digitos.length() == 8) return "569" + digitos;
        return null;
    }

    public static String whatsapp(String telefono, String nombre, String asunto) {
        String numero = normalizarTelefono(telefono);
        if (numero == null) return null;
        String texto = "Hola " + nombre + ", te escribimos de AutoFacil por tu mensaje: \"" + asunto + "\".";
        return "https://wa.me/" + numero + "?text=" + codificar(texto);
    }

    public static String correo(String correo, String asunto) {
        return "mailto:" + correo + "?subject=" + codificar("AutoFacil - " + asunto);
    }

    public static String llamada(String telefono) {
        String numero = normalizarTelefono(telefono);
        return numero == null ? null : "tel:+" + numero;
    }

    private static String codificar(String texto) {
        return URLEncoder.encode(texto, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
