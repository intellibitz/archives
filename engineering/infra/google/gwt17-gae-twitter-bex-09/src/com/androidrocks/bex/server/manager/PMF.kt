package com.androidrocks.bex.server.manager

import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.datastore.Entity
import com.google.appengine.api.datastore.EntityNotFoundException
import com.google.appengine.api.datastore.Key
import com.google.appengine.api.datastore.Query
import com.google.appengine.api.datastore.FetchOptions
import javax.jdo.JDOHelper
import javax.jdo.PersistenceManager
import javax.jdo.PersistenceManagerFactory
import javax.jdo.Transaction
import java.util.HashSet
import java.util.Iterator
import java.util.List
import java.util.Map
import java.util.Set
import java.util.logging.Logger

class PMF private constructor() {
    companion object {
        private val log: Logger = Logger.getLogger(PMF::class.java
            .getName())
        private val pmfInstance: PersistenceManagerFactory = JDOHelper.getPersistenceManagerFactory("transactions-optional")
        const val PAGING_LIMIT = 50
        @JvmStatic
        fun get(): PersistenceManagerFactory? {

                    return pmfInstance
        }

        @JvmStatic
        fun chopSetToPaging(src: Set<Key>?): Set<Key>? {

                    val dest = HashSet<Key>(PMF.PAGING_LIMIT)
                    if (null != src && src.size <= PMF.PAGING_LIMIT){
                        return src
                    } else  if (null != src && src.size > PMF.PAGING_LIMIT){
                        val iter = src.iterator()
                        for (i in 0..PMF.PAGING_LIMIT){
                            dest.add(iter.next())
                        }
                        return dest
                    }
                    return src
        }

        @JvmStatic
        fun delete(object: Any?) {

                    val pm = get().persistenceManager
                    val tx = pm.currentTransaction()
                    try {
                        tx.begin()
                        pm.deletePersistent(object)
                        log.info("#delete: " + object)
                        tx.commit()
                    } finally {
                        if (tx.isActive) {
                            tx.rollback()
                        }
                        pm.close()
                    }
        }

        @JvmStatic
        fun makePersistent(object: Any?) {

                    val pm = get().persistenceManager
                    val tx = pm.currentTransaction()
                    try {
                        tx.begin()
                        pm.makePersistent(object)
                        log.info("#makePersistent: " + object)
                        tx.commit()
                    } finally {
                        if (tx.isActive) {
                            tx.rollback()
                        }
                        pm.close()
                    }
        }

        @JvmStatic
        fun loadObjectById(klass: Class?, key: Key?): Any? {

                    val pm = get().persistenceManager
                        val result = pm.getObjectById(klass, key)
                        pm.close()
                    return result
        }

        @Throws(EntityNotFoundException::class)
        @JvmStatic
        fun get(key: Key?): Entity? {

                    val datastoreService = DatastoreServiceFactory.getDatastoreService()
                    return datastoreService.get(key)
        }

        @JvmStatic
        fun get(keys: Set<Key>?): Map<Key,Entity>? {

                    val datastoreService = DatastoreServiceFactory.getDatastoreService()
                    return datastoreService.get(keys)
        }

        @JvmStatic
        fun put(entities: Iterable<Entity>?): List<Key>? {

                    val datastoreService = DatastoreServiceFactory.getDatastoreService()
                    return datastoreService.put(entities)
        }

        @JvmStatic
        fun query(query: Query?): List<Entity>? {

                    val datastoreService = DatastoreServiceFactory.getDatastoreService()
                    return datastoreService.prepare(query).asList(FetchOptions.Builder.withLimit(PAGING_LIMIT))
        }

    }
}
