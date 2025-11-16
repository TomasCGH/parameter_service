package co.edu.uco.parametersservice.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uco.parametersservice.catalog.CatalogEventType;
import co.edu.uco.parametersservice.catalog.Parameter;
import co.edu.uco.parametersservice.service.ReactiveParameterService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controlador que expone la configuración dinámica (parámetros) asociada a la gestión
 * de conjuntos residenciales. Las respuestas aplican políticas de no cacheo para garantizar
 * la lectura de valores actualizados.
 */
@RestController
@RequestMapping("/api/v1/parameters")
public class ParameterController {

    private static final CacheControl NO_CACHE = CacheControl.noStore().mustRevalidate();

    private final ReactiveParameterService service;

    public ParameterController(ReactiveParameterService service) {
        this.service = service;
    }

    /**
     * Retorna el listado completo de parámetros disponibles.
     */
    @GetMapping
    public Flux<Parameter> getAllParameters() {
        return service.findAll();
    }

    /**
     * Obtiene el valor de un parámetro por su clave.
     */
    @GetMapping("/{key}")
    public Mono<ResponseEntity<Parameter>> getParameter(@PathVariable String key) {
        return service.findByKey(key)
                .map(value -> ResponseEntity.ok()
                        .cacheControl(NO_CACHE)
                        .header("Pragma", "no-cache")
                        .header("Expires", "0")
                        .body(value))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .cacheControl(NO_CACHE)
                        .header("Pragma", "no-cache")
                        .header("Expires", "0")
                        .build()));
    }

    /**
     * Crea o actualiza un parámetro del catálogo.
     */
    @PostMapping
    public Mono<ResponseEntity<Parameter>> createParameter(@RequestBody Parameter body) {
        Parameter sanitizedParameter = new Parameter(body.getKey(), body.getValue());
        return service.upsert(sanitizedParameter)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED)
                        .cacheControl(NO_CACHE)
                        .header("Pragma", "no-cache")
                        .header("Expires", "0")
                        .body(saved));
    }

    /**
     * Actualiza el valor de un parámetro existente.
     */
    @PutMapping("/{key}")
    public Mono<ResponseEntity<Parameter>> updateParameter(@PathVariable String key, @RequestBody Parameter body) {
        Parameter sanitizedParameter = new Parameter(key, body.getValue());
        return service.upsert(sanitizedParameter)
                .map(saved -> ResponseEntity.ok()
                        .cacheControl(NO_CACHE)
                        .header("Pragma", "no-cache")
                        .header("Expires", "0")
                        .body(saved));
    }

    /**
     * Expone un flujo continuo con los cambios en los parámetros.
     */
    @GetMapping(value = "/stream", produces = "text/event-stream")
    public Flux<ServerSentEvent<Parameter>> streamUpdates() {
        return service.listenChanges()
                .filter(change -> change.type() != CatalogEventType.DELETED)
                .map(change -> ServerSentEvent.<Parameter>builder(change.payload())
                        .event(change.type().name())
                        .build());
    }
}
