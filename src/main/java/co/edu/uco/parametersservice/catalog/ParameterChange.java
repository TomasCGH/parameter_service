package co.edu.uco.parametersservice.catalog;

/**
 * Evento emitido cuando un parámetro cambia.
 */
public record ParameterChange(CatalogEventType type, Parameter payload) {
}
