package com.retailwave.fce.server.dao

/**
 * $Id: UserDAO.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/server/dao/UserDAO.java $
 */

import com.retailwave.fce.shared.domain.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import javax.annotation.PostConstruct
import javax.persistence.EntityManagerFactory

@Repository("userDAO")
class UserDAO : JpaDAO<Long?, User?>() {

    @Autowired
    lateinit var entityManagerFactory: EntityManagerFactory

    @PostConstruct
    fun init() {
        super.setEntityManagerFactory(entityManagerFactory)
    }
}
