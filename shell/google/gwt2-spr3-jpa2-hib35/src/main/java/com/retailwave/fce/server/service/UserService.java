package com.retailwave.fce.server.service;
/**
 * $Id: UserService.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/server/service/UserService.java $
 */

import com.retailwave.fce.server.dao.UserDAO;
import com.retailwave.fce.shared.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

@Service("userService")
public class UserService {

    @Autowired
    private UserDAO userDAO;

    public UserService() {
    }

    @PostConstruct
    public void init() throws Exception {
    }

    @PreDestroy
    public void destroy() {
    }

    public User findUser(long userId) {
		
        return userDAO.findById(userId);
		
    }
	
    @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
    public void saveUser(User user) {
        userDAO.persist(user);
    }

    @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
    public void updateUser(long userId, String name, String surname, String jobDescription) throws Exception {
		
    }
	
    @Transactional(propagation= Propagation.REQUIRED, rollbackFor=Exception.class)
    public void deleteUser(long userId) throws Exception {
		
        User userDTO = userDAO.findById(userId);
		
        if(userDTO != null)
            userDAO.remove(userDTO);

    }
	
    @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
    public void saveOrUpdateUser(User user) throws Exception {

        userDAO.merge(user);
		
    }

    public List<User> search(User user) {
        return userDAO.findAll();
    }

    public List<User> search(User user, int numRows, int firstRow) {
//        return userDAO.findAll(user, numRows, firstRow);
        return userDAO.findAll();
    }

    public User getUser(String id) {
        return userDAO.findById(Long.valueOf(id));
    }

    public User getUserByName(String name) {
//        return userDAO.getUserByName(name);
        return null;
    }

    public int countUsers() {
//        return userDao.countUsers();
        return 0;
    }

    public int countLexmarkUsers() {
//        return userDao.countLexmarkUsers();
        return 0;
    }

    public int countPartnerUsers() {
//        return userDao.countPartnerUsers();
        return 0;
    }

}
