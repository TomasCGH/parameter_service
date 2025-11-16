package co.edu.uco.parametersservice.catalog;

/**
 * Representa un parámetro configurable asociado a las reglas del dominio de
 * viviendas. Los parámetros permiten ajustar dinámicamente comportamientos del
 * proceso sin requerir despliegues adicionales.
 */
public class Parameter {

    private String key;
    private String value;

    public Parameter() {
        // Constructor por defecto requerido por los componentes de WebFlux.
    }

    public Parameter(String key, String value) {
        setKey(key);
        setValue(value);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
