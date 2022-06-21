package it.aman.authenticationservice.dal.util;

import java.util.List;

/**
 * Custom {@code Repository}.</br>
 * 
 * Repository {@literal interface}'s should extend both {@code JpaRepository}
 * and this one. </br>
 * </br>
 * 
 * Note:</br>
 * - It is better to use {@code persist} from jpa and {@code update} from
 * hibernate avoiding {@code merge} as much as possible. Use {@code merge} if
 * you have to update a big dependency graph only. </br>
 * 
 * - Find methods are not included. Hence, we will have 2 EntityManagers. This
 * allows {@code update} to work without any problem.
 * 
 * 
 * https://thorben-janssen.com/persist-save-merge-saveorupdate-whats-difference-one-use/
 * 
 * @author Aman
 *
 * @param <T>
 */
public interface HibernateRepository<T> {

    // Save methods will trigger an UnsupportedOperationException
    @Deprecated
    <S extends T> S save(S entity);

    @Deprecated
    <S extends T> List<S> saveAll(Iterable<S> entities);

    @Deprecated
    <S extends T> S saveAndFlush(S entity);

    @Deprecated
    <S extends T> List<S> saveAllAndFlush(Iterable<S> entities);

    // Persist methods are meant to save newly created entities
    <S extends T> S persist(S entity);

    <S extends T> S persistAndFlush(S entity);

    <S extends T> List<S> persistAll(Iterable<S> entities);

    <S extends T> List<S> peristAllAndFlush(Iterable<S> entities);

    /**
     * Merge methods are meant to propagate detached entity state changes if they
     * are really needed </br>
     * 
     * A detached entity results from transaction commit if a transaction-scoped
     * persistence context is used (see section 3.3); from transaction rollback (see
     * section 3.3.3); from detaching the entity from the persistence context; from
     * clearing the persistence context; from closing an entity manager; or from
     * serializing an entity or otherwise passing an entity by value—e.g., to a
     * separate application tier, through a remote interface, etc.</br>
     * </br>
     * 
     * http://download.oracle.com/otn-pub/jcp/persistence-2_1-fr-eval-spec/JavaPersistence.pdf</br>
     * </br>
     * 
     * 
     * JpaRepositories(CrudRepository): When using with default impl, {@code save}
     * method checks if the entity has an id. If so, merge is used instead of
     * persist. Hence, it is wise to avoid the above mentioned scenarios in which an
     * entity ends up detached. </br>
     * </br>
     * 
     * NOTE: Merge is costly</br>
     * 
     * Hibernate generates a SELECT statement first to fetch the latest state of the
     * underlying database record, and afterwards, it copies the detached entity
     * state onto the newly fetched managed entity.
     * 
     * @param <S>
     * @param entity
     * @return
     */
    <S extends T> S merge(S entity);

    <S extends T> S mergeAndFlush(S entity);

    <S extends T> List<S> mergeAll(Iterable<S> entities);

    <S extends T> List<S> mergeAllAndFlush(Iterable<S> entities);

    /**
     * Update the persistent instance with the identifier of the given detached
     * instance. If there is a persistent instance with the same identifier, an
     * exception is thrown. This operation cascades to associated instances if the
     * association is mapped with cascade="save-update"
     * 
     * 
     * In our implementation we make sure that we have different transaction scopes , so we will
     * not get any exception.
     * 
     * @param entity - entity to issue an update for. No dirty checking is done
     *               since hibernate doesn't do a select before update(default
     *               behaviour)
     * @return
     */
    <S extends T> S update(S entity);

    <S extends T> S updateAndFlush(S entity);

    <S extends T> List<S> updateAll(Iterable<S> entities);

    <S extends T> List<S> updateAllAndFlush(Iterable<S> entities);
}
