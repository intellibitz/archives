package com.retailwave.fce.server.dao

/**
 * $Id: JpaDAO.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/server/dao/JpaDAO.java $
 */

import org.springframework.orm.jpa.JpaCallback
import org.springframework.orm.jpa.support.JpaDaoSupport
import java.lang.reflect.ParameterizedType
import javax.persistence.EntityManager
import javax.persistence.PersistenceException
import javax.persistence.Query

abstract class JpaDAO<K, E> : JpaDaoSupport() {

    protected var entityClass: Class<E>

    init {
        @Suppress("UNCHECKED_CAST")
        val genericSuperclass = javaClass.genericSuperclass as ParameterizedType
        this.entityClass = genericSuperclass.actualTypeArguments[1] as Class<E>
    }

    fun persist(entity: E) {
        jpaTemplate.persist(entity)
    }

    fun remove(entity: E) {
        jpaTemplate.remove(entity)
    }

    fun merge(entity: E): E {
        return jpaTemplate.merge(entity)
    }

    fun refresh(entity: E) {
        jpaTemplate.refresh(entity)
    }

    fun findById(id: K): E {
        return jpaTemplate.find(entityClass, id)
    }

    fun flush(entity: E): E {
        jpaTemplate.flush()
        return entity
    }

    @Suppress("UNCHECKED_CAST")
    fun findAll(): List<E> {
        val res = jpaTemplate.execute(object : JpaCallback {
            @Throws(PersistenceException::class)
            override fun doInJpa(em: EntityManager): Any {
                val q: Query = em.createQuery(
                    "SELECT h FROM " +
                            entityClass.name + " h"
                )
                return q.resultList
            }
        })

        return res as List<E>
    }

    @Suppress("UNCHECKED_CAST")
    fun removeAll(): Int {
        return jpaTemplate.execute(object : JpaCallback {
            @Throws(PersistenceException::class)
            override fun doInJpa(em: EntityManager): Any {
                val q: Query = em.createQuery(
                    "DELETE FROM " +
                            entityClass.name + " h"
                )
                return q.executeUpdate()
            }
        }) as Int
    }
}
