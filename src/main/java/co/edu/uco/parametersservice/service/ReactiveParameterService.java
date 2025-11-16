package co.edu.uco.parametersservice.service;

import org.springframework.stereotype.Service;

import co.edu.uco.parametersservice.catalog.Parameter;
import co.edu.uco.parametersservice.catalog.ParameterChange;
import co.edu.uco.parametersservice.catalog.ReactiveParameterCatalog;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio reactivo que encapsula las operaciones sobre el catálogo de
 * parámetros de vivienda.
 */
@Service
public class ReactiveParameterService {

    private final ReactiveParameterCatalog catalog;

    public ReactiveParameterService(ReactiveParameterCatalog catalog) {
        this.catalog = catalog;
    }

    public Flux<Parameter> findAll() {
        return catalog.findAll();
    }

    public Mono<Parameter> findByKey(String key) {
        return catalog.findByKey(key);
    }

    public Mono<Parameter> upsert(Parameter parameter) {
        return catalog.save(parameter);
    }

    public Mono<Parameter> delete(String key) {
        return catalog.remove(key);
    }

    public Flux<ParameterChange> listenChanges() {
        return catalog.changes();
    }
}
