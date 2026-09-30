package com.retailwave.fce.server.service
/**
 * $Id: UserService.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/server/service/UserService.java $
 */

import com.retailwave.fce.server.dao.UserDAO
import com.retailwave.fce.shared.domain.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Service("userService")
class UserService {

    @Autowired
    private lateinit var userDAO: UserDAO

    @PostConstruct
    @Throws(Exception::class)
    fun init() {
    }

    @PreDestroy
    fun destroy() {
    }

    fun findUser(userId: Long): User? {
        return userDAO.findById(userId)
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = [Exception::class])
    fun saveUser(user: User?) {
        userDAO.persist(user)
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = [Exception::class])
    @Throws(Exception::class)
    fun updateUser(userId: Long, name: String?, surname: String?, jobDescription: String?) {
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = [Exception::class])
    @Throws(Exception::class)
    fun deleteUser(userId: Long) {
        val userDTO = userDAO.findById(userId)

        if (userDTO != null)
            userDAO.remove(userDTO)
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = [Exception::class])
    @Throws(Exception::class)
    fun saveOrUpdateUser(user: User?) {
        userDAO.merge(user)
    }

    fun search(user: User?): List<User> {
        return userDAO.findAll()
    }

    fun search(user: User?, numRows: Int, firstRow: Int): List<User> {
//        return userDAO.findAll(user, numRows, firstRow);
        return userDAO.findAll()
    }

    fun getUser(id: String): User? {
        return userDAO.findById(java.lang.Long.valueOf(id))
    }

    fun getUserByName(name: String?): User? {
//        return userDAO.getUserByName(name);
        return null
    }

    fun countUsers(): Int {
//        return userDao.countUsers();
        return 0
    }

    fun countLexmarkUsers(): Int {
//        return userDao.countLexmarkUsers();
        return 0
    }

    fun countPartnerUsers(): Int {
//        return userDao.countPartnerUsers();
        return 0
    }
}
