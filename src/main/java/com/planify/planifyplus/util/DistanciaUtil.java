package com.planify.planifyplus.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilidad para trabajar con coordenadas y distancias entre ubicaciones.
 */
public class DistanciaUtil {

    /**
     * Coordenadas (lat, lon) del centro de algunas ciudades.
     */
    private static final Map<String, double[]> COORDENADAS_CIUDADES = new HashMap<>();

    static {
        COORDENADAS_CIUDADES.put("Sevilla", new double[]{37.3891, -5.9845});
        COORDENADAS_CIUDADES.put("Madrid", new double[]{40.4168, -3.7038});
        COORDENADAS_CIUDADES.put("Barcelona", new double[]{41.3851, 2.1734});
        COORDENADAS_CIUDADES.put("Valencia", new double[]{39.4699, -0.3763});
        COORDENADAS_CIUDADES.put("Bilbao", new double[]{43.2630, -2.9350});
        COORDENADAS_CIUDADES.put("Palma de Mallorca", new double[]{39.5696, 2.6502});
        COORDENADAS_CIUDADES.put("Málaga", new double[]{36.7213, -4.4214});
    }

    /**
     * Devuelve las coordenadas asociadas a una ciudad.
     *
     * @param ciudad nombre de la ciudad
     * @return array {latitud, longitud}
     */
    public static double[] getCoordenadasCiudad(String ciudad) {
        return COORDENADAS_CIUDADES.getOrDefault(ciudad, new double[]{0.0, 0.0});
    }

    /**
     * Calcula la distancia entre dos puntos usando Haversine.
     *
     * @return distancia aproximada en kilómetros
     */
    public static double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        final int RADIO_TIERRA_KM = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RADIO_TIERRA_KM * c;
    }
}
