package co.edu.uco.parametersservice.catalog;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * Catálogo reactivo que delega todas las operaciones al catálogo estático
 * {@link ParameterCatalog} asegurando que los datos expuestos sean los
 * actualmente definidos y que los cambios emitan eventos.
 */
@Component
public class ReactiveParameterCatalog {

    private final Sinks.Many<ParameterChange> sink = Sinks.many().replay().latest();
    private final Flux<ParameterChange> changeStream = sink.asFlux();

    public ReactiveParameterCatalog() {
        // Sin carga de defaults: la fuente es ParameterCatalog
    }

    public Flux<Parameter> findAll() {
        return Flux.defer(() -> Flux.fromIterable(ParameterCatalog.getAllParameters().values())
                .map(this::copyOf));
    }

    public Mono<Parameter> findByKey(String key) {
        return Mono.defer(() -> Mono.justOrEmpty(ParameterCatalog.getParameterValue(key))
                .map(this::copyOf));
    }

    public Mono<Parameter> save(Parameter parameter) {
        return Mono.fromSupplier(() -> {
            Parameter sanitized = copyOf(parameter);
            CatalogEventType type = ParameterCatalog.getParameterValue(sanitized.getKey()) != null
                    ? CatalogEventType.UPDATED
                    : CatalogEventType.CREATED;
            ParameterCatalog.synchronizeParameterValue(sanitized);
            emit(type, sanitized);
            return copyOf(sanitized);
        });
    }

    public Mono<Parameter> remove(String key) {
        return Mono.defer(() -> {
            Parameter removed = ParameterCatalog.removeParameter(key);
            if (removed == null) {
                return Mono.empty();
            }
            emit(CatalogEventType.DELETED, removed);
            return Mono.just(copyOf(removed));
        });
    }

    public Flux<ParameterChange> changes() {
        return changeStream;
    }

    private void emit(CatalogEventType type, Parameter parameter) {
        sink.emitNext(new ParameterChange(type, copyOf(parameter)), Sinks.EmitFailureHandler.FAIL_FAST);
    }

    private Parameter copyOf(Parameter parameter) {
        return new Parameter(parameter.getKey(), parameter.getValue());
    }
}
