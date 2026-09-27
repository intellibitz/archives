package com.retailwave.fce.server.dao;

/**
 * $Id: UserDAO.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/server/dao/UserDAO.java $
 */

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;

import com.retailwave.fce.shared.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository("userDAO")
public class UserDAO extends JpaDAO <Long, User>{

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @PostConstruct
    public void init() {
        super.setEntityManagerFactory(entityManagerFactory);
    }

}
