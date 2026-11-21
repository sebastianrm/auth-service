package com.yanki.auth.infrastructure.persistence;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.reactivestreams.Publisher;

/**
 * Adaptador entre los publishers de Project Reactor (repositorios y Redis) y
 * los tipos de RxJava 3 usados en la capa de servicio.
 *
 * <p>Las conversiones son perezosas: el publisher de origen solo se
 * suscribe cuando se suscribe el tipo de RxJava resultante, de modo que no
 * se ejecutan consultas ni llamadas a Redis fuera del flujo reactivo.</p>
 */
public final class ReactiveAdapter {

    /**
     * Constructor privado: la clase es un contenedor de funciones estaticas.
     */
    private ReactiveAdapter() {
    }

    /**
     * Convierte un publisher en un {@link Single}.
     *
     * @param publisher publisher de origen
     * @param <T> tipo del elemento publicado
     * @return single con el primer elemento publicado
     */
    public static <T> Single<T> toSingle(Publisher<T> publisher) {
        return Flowable.fromPublisher(publisher).singleOrError();
    }

    /**
     * Convierte un publisher en un {@link Maybe}.
     *
     * @param publisher publisher de origen
     * @param <T> tipo del elemento publicado
     * @return maybe con el elemento publicado o vacio
     */
    public static <T> Maybe<T> toMaybe(Publisher<T> publisher) {
        return Flowable.fromPublisher(publisher).singleElement();
    }

    /**
     * Convierte un publisher en un {@link Flowable}.
     *
     * @param publisher publisher de origen
     * @param <T> tipo de los elementos publicados
     * @return flowable con todos los elementos publicados
     */
    public static <T> Flowable<T> toFlowable(Publisher<T> publisher) {
        return Flowable.fromPublisher(publisher);
    }

    /**
     * Convierte un publisher en un {@link Completable}.
     *
     * @param publisher publisher de origen
     * @return completable que termina cuando el publisher termina
     */
    public static Completable toCompletable(Publisher<?> publisher) {
        return Flowable.fromPublisher(publisher).ignoreElements();
    }
}
