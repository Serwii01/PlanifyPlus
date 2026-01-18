package com.planify.planifyplus.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.net.URL;

/**
 * Utilidad para la carga de vistas FXML.
 */
public class ViewUtil {

    private ViewUtil() {}

    /**
     * Carga un archivo FXML y devuelve su nodo raíz.
     * Intenta resolver diferencias comunes de mayúsculas/minúsculas en el nombre.
     */
    public static Parent loadFXML(Class<?> ctx, String fxmlPath) throws Exception {
        URL url = ctx.getResource(fxmlPath);
        if (url == null) {
            String alt = swapCaseLastSegment(fxmlPath);
            url = ctx.getResource(alt);
        }
        if (url == null) {
            throw new IllegalArgumentException("No se encontró el FXML: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(url);
        return loader.load();
    }

    /**
     * Devuelve un FXMLLoader preparado para un FXML concreto.
     */
    public static FXMLLoader loaderFXML(Class<?> ctx, String fxmlPath) throws Exception {
        URL url = ctx.getResource(fxmlPath);
        if (url == null) {
            String alt = swapCaseLastSegment(fxmlPath);
            url = ctx.getResource(alt);
        }
        if (url == null) {
            throw new IllegalArgumentException("No se encontró el FXML: " + fxmlPath);
        }

        return new FXMLLoader(url);
    }

    /**
     * Alterna la capitalización del primer carácter del nombre del archivo.
     */
    private static String swapCaseLastSegment(String path) {
        int idx = path.lastIndexOf('/');
        String dir = (idx >= 0) ? path.substring(0, idx + 1) : "";
        String file = (idx >= 0) ? path.substring(idx + 1) : path;

        if (file.isEmpty()) return path;

        char c0 = file.charAt(0);
        char swapped = Character.isUpperCase(c0)
                ? Character.toLowerCase(c0)
                : Character.toUpperCase(c0);

        return dir + swapped + file.substring(1);
    }
}
