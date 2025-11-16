package co.edu.uco.parametersservice.catalog;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Catálogo en memoria que mantiene configuraciones para la gestión de
 * conjuntos residenciales. Los parámetros permiten ajustar reglas de negocio
 * como límites de paginación, validaciones de nombre y correo de contacto.
 */
public final class ParameterCatalog {

    private static final Map<String, Parameter> PARAMETERS = new ConcurrentHashMap<>();

    static {
        // Notificaciones y contacto
        register("notification.conjunto.administrator.email", "admin-conjuntos@uco.edu.co");
        register("notification.conjunto.creacion.template",
                "Hola %s, el conjunto residencial %s ha sido creado en la ciudad %s.");

        // Reglas de negocio para conjuntos
        register("gestion.conjunto.nombre.longitudMaxima", "80");
        register("gestion.conjunto.listado.limite", "50");
        register("conjunto.max.limit", "500");
    }

    private ParameterCatalog() {
        // Evitar instanciación
    }

    public static Parameter getParameterValue(String key) {
        return PARAMETERS.get(key);
    }

    public static void synchronizeParameterValue(Parameter parameter) {
        register(parameter.getKey(), parameter.getValue());
    }

    public static Parameter removeParameter(String key) {
        return PARAMETERS.remove(key);
    }

    public static Map<String, Parameter> getAllParameters() {
        return PARAMETERS;
    }

    private static void register(final String key, final String value) {
        PARAMETERS.put(key, new Parameter(key, value));
    }
}
