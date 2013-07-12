package com.dslplatform.client;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.dslplatform.patterns.AggregateRoot;
import com.dslplatform.patterns.PersistableRepository;
import com.dslplatform.patterns.ServiceLocator;

public class ClientPersistableRepository<T extends AggregateRoot>
        extends ClientRepository<T>
        implements PersistableRepository<T> {
    protected final StandardProxy standardProxy;
    private final ExecutorService  executorService;

    public ClientPersistableRepository(
            final Class<T> clazz,
            final ServiceLocator locator) {
        super(clazz, locator);
        this.standardProxy = locator.resolve(StandardProxy.class);
        this.executorService = locator.resolve(ExecutorService.class);
    }

    @Override
    public Future<List<String>> persist(
            final Iterable<T> inserts,
            final Iterable<Map.Entry<T, T>> updates,
            final Iterable<T> deletes) {
        return standardProxy.persist(inserts, updates, deletes);
    }

    @Override
    public Future<List<String>> insert(final Iterable<T> inserts) {
        return standardProxy.persist(inserts, null, null);
    }

    @Override
    public Future<String> insert(final T insert) {
        final Future<T> result = crudProxy.create(insert);
        return executorService.submit(
            new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return result.get().getURI();
                }
            });
    }

    @Override
    public Future<List<String>> update(final Iterable<T> updates) {
        final ArrayList<Map.Entry<T, T>> map = new ArrayList<Map.Entry<T, T>>();
        for(final T it: updates) {
            final Map.Entry<T, T> pair =
                new AbstractMap.SimpleEntry<T, T>(null, it);
            map.add(pair);
        }
        return standardProxy.persist(null, map, null);
    }

    @Override
    public Future<T> update(final T update) {
        return crudProxy.update(update);
    }

    @Override
    public Future<?> delete(final Iterable<T> deletes) {
        return standardProxy.persist(null, null, deletes);
    }

    @Override
    public Future<?> delete(final T delete) {
        return crudProxy.delete(manifest, delete.getURI());
    }
}