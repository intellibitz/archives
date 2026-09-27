package com.androidrocks.bex.server.manager;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.FetchOptions;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public final class PMF {

    private static final Logger log = Logger.getLogger(PMF.class
            .getName());

    private static final PersistenceManagerFactory pmfInstance =
            JDOHelper.getPersistenceManagerFactory("transactions-optional");

    public static final int PAGING_LIMIT = 50;

    private PMF() {
    }

    public static PersistenceManagerFactory get() {
        return pmfInstance;
    }

    public static Set<Key> chopSetToPaging(Set<Key> src){
        Set<Key> dest = new HashSet<Key>(PMF.PAGING_LIMIT);
        if (null != src && src.size() <= PMF.PAGING_LIMIT){
            return src;
        } else  if (null != src && src.size() > PMF.PAGING_LIMIT){
            Iterator<Key> iter = src.iterator();
            for (int i=0; i<=PMF.PAGING_LIMIT; i++){
                dest.add(iter.next());
            }
            return dest;
        }
        return src;
    }

    public static void delete(Object object) {
        PersistenceManager pm = get().getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            pm.deletePersistent(object);
            log.info("#delete: " + object);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    public static void makePersistent(Object object) {
        PersistenceManager pm = get().getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            pm.makePersistent(object);
            log.info("#makePersistent: " + object);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    public static Object loadObjectById(Class klass, Key key) {
        PersistenceManager pm = get().getPersistenceManager();
            Object result = pm.getObjectById(klass, key);
            pm.close();
        return result;
    }

    public static Entity get(Key key) throws EntityNotFoundException {
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        return datastoreService.get(key);
    }

    public static Map<Key,Entity> get(Set<Key> keys) {
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        return datastoreService.get(keys);
    }

    public static List<Key> put(Iterable<Entity> entities) {
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        return datastoreService.put(entities);
    }

    public static List<Entity> query (Query query){
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        return datastoreService.prepare(query).asList(FetchOptions.Builder.withLimit(PAGING_LIMIT));
    }
}
