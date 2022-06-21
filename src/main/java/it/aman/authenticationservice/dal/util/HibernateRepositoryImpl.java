package it.aman.authenticationservice.dal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.hibernate.Session;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.AbstractSharedSessionContract;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * An improvement to the default {@linkplain JpaRepository} <b><em>save, merge
 * and update</em></b> methods <br/>
 * https://vladmihalcea.com/best-spring-data-jparepository </br>
 * 
 * <ul>
 * It is advisable that:
 * <li>All operations be done on entities on the same layer and use dto to
 * trasfer data from layer to another layer. And use update (of hibernate) when
 * necessary(avoid calling jpa merge).</li>
 * <li>Use respective methods to avoid unnecessary cost related to queries
 * generated (like in to persist/update).</li>
 * <ul>
 * 
 * </br>
 * Note: Calling merge on a managed entity burns CPU cycles by triggering a
 * MergeEvent. Though it depends on the entity association depth, data sets,
 * number of users, and Hibernate version, it is better to avoid it all
 * together.
 * 
 * Moreover, this is just one problem, but there are many others. Calling merge
 * when you should not have to can expose you to merge-related bugs, such as the
 * one that was duplicating list entries for from 3.6 up to 5.0.</br>
 * 
 * https://www.reddit.com/r/java/comments/v73kpr/the_best_spring_data_jparepository
 * </br>
 * 
 * 
 * @author Aman
 *
 * @param <T>
 */
public class HibernateRepositoryImpl<T> implements HibernateRepository<T> {
    /**
     * The persistence context type for the entity manager is defaulted or defined
     * as PersistenceContextType.TRANSACTION. So, everytime there a commit/rollback,
     * entities will be detached.
     * 
     * @PersistenceContext takes care to create a unique EntityManager for every
     *                     thread. Hence, create a new persistence
     *                     context(first-level cache).
     */
    @PersistenceContext
    private EntityManager em;

    @Override
    public <S extends T> S save(S entity) {
        return unsupported();
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        return unsupported();
    }

    @Override
    public <S extends T> S saveAndFlush(S entity) {
        return unsupported();
    }

    @Override
    public <S extends T> List<S> saveAllAndFlush(Iterable<S> entities) {
        return unsupported();
    }

    @Transactional
    public <S extends T> S persist(S entity) {
        em.persist(entity);
        return entity;
    }

    @Transactional
    public <S extends T> S persistAndFlush(S entity) {
        persist(entity);
        em.flush();
        return entity;
    }

    @Transactional
    public <S extends T> List<S> persistAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add(persist(entity));
        }
        return result;
    }

    @Transactional
    public <S extends T> List<S> peristAllAndFlush(Iterable<S> entities) {
        return executeBatch(() -> {
            List<S> result = new ArrayList<>();
            for (S entity : entities) {
                result.add(persist(entity));
            }
            em.flush();
            return result;
        });
    }

    @Transactional
    public <S extends T> S merge(S entity) {
        return em.merge(entity);
    }

    @Transactional
    @Override
    public <S extends T> S mergeAndFlush(S entity) {
        S result = merge(entity);
        em.flush();
        return result;
    }

    @Transactional
    @Override
    public <S extends T> List<S> mergeAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add(merge(entity));
        }
        return result;
    }

    @Transactional
    @Override
    public <S extends T> List<S> mergeAllAndFlush(Iterable<S> entities) {
        return executeBatch(() -> {
            List<S> result = new ArrayList<>();
            for (S entity : entities) {
                result.add(merge(entity));
            }
            em.flush();
            return result;
        });
    }

    @Transactional
    public <S extends T> S update(S entity) {
        session().update(entity);
        return entity;
    }

    @Transactional
    @Override
    public <S extends T> S updateAndFlush(S entity) {
        update(entity);
        em.flush();
        return entity;
    }

    @Transactional
    @Override
    public <S extends T> List<S> updateAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add(update(entity));
        }
        return result;
    }

    @Transactional
    @Override
    public <S extends T> List<S> updateAllAndFlush(Iterable<S> entities) {
        return executeBatch(() -> {
            List<S> result = new ArrayList<>();
            for (S entity : entities) {
                result.add(update(entity));
            }
            em.flush();
            return result;
        });
    }

    protected Integer getBatchSize(Session session) {
        SessionFactoryImplementor sessionFactory = session.getSessionFactory().unwrap(SessionFactoryImplementor.class);

        final JdbcServices jdbcServices = sessionFactory.getServiceRegistry().getService(JdbcServices.class);

        if (!jdbcServices.getExtractedMetaDataSupport().supportsBatchUpdates()) {
            return Integer.MIN_VALUE;
        }
        return session.unwrap(AbstractSharedSessionContract.class).getConfiguredJdbcBatchSize();
    }

    protected <R> R executeBatch(Supplier<R> callback) {
        Session session = session();
        Integer jdbcBatchSize = getBatchSize(session);
        Integer originalSessionBatchSize = session.getJdbcBatchSize();
        try {
            if (jdbcBatchSize == null) {
                session.setJdbcBatchSize(10);
            }
            return callback.get();
        } finally {
            session.setJdbcBatchSize(originalSessionBatchSize);
        }
    }

    protected Session session() {
        return em.unwrap(Session.class);
    }

    protected <S extends T> S unsupported() {
        throw new UnsupportedOperationException(
                "There's no such thing as a save method in JPA, so don't use this hack!");
    }

}
