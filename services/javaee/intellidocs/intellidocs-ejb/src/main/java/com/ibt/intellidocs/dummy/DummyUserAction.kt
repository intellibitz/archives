package com.ibt.intellidocs.dummy

import org.jboss.seam.ScopeType.EVENT
import org.jboss.seam.annotations.Destroy
import org.jboss.seam.annotations.In
import org.jboss.seam.annotations.Logger
import org.jboss.seam.annotations.Name
import org.jboss.seam.annotations.Scope
import org.jboss.seam.log.Log
import java.io.Serializable
import javax.ejb.Remove
import javax.ejb.Stateful
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * Created by IntelliJ IDEA. User: sara Date: Dec 15, 2007 Time: 12:42:47 PM To
 * change this template use File | Settings | File Templates.
 */
@Stateful
@Scope(EVENT)
@Name("dummyUserAction")
//@JndiName ("DummyUserAction/local")
class DummyUserAction : IDummyUserLocal, Serializable {

    @Logger
    var log: Log? = null

    @In(required = true)
    private var dummyUser: DummyUser? = null

    @PersistenceContext
    private var entityManager: EntityManager? = null

    private var verify: String? = null
    private var registered: Boolean = false

    override fun findDummyUser(id: Long): DummyUser? {
        return entityManager!!.find(DummyUser::class.java, id)
    }

    override fun register(): Long {
        if (dummyUser!!.password == verify) {
/*
             List existing = entityManager.createQuery
                     ("select u.username from DummyUser u where u.username=:username")
               .setParameter("username", dummyUser.getUsername())
*/
            val existing = entityManager!!.createQuery(
                "select u.username from DummyUser u where u.username=#{dummyUser.username}"
            ).resultList
            if (existing.size == 0) {
                entityManager!!.persist(dummyUser)
                // facesMessages.add("Successfully registered as #{dummyUser.username}");
                log!!.info("Username #{dummyUser.username} already exists")
                registered = true
            } else {
                log!!.info("Username #{dummyUser.username} already exists")
            }
        } else {
            log!!.info("Re-enter your password")
            verify = null
        }
        return dummyUser!!.id
    }

    override fun invalid() {
        log!!.info("Please try again")
    }

    override fun isRegistered(): Boolean {
        return registered
    }

    override fun getVerify(): String? {
        return verify
    }

    override fun setVerify(verify: String?) {
        this.verify = verify
    }

    override fun run() {
        log!!.info("dummySeamBean#run.. Done!")
    }

    override fun cancel() {
        log!!.info("dummySeamBean#cancel.. Done!")
    }

    @Destroy
    @Remove
    override fun destroy() {
        log!!.info("dummySeamBean#destroy.. Done!")
    }

    override fun getDummyUser(): DummyUser? {
        return dummyUser
    }

    override fun setDummyUser(dummyUser: DummyUser?) {
        this.dummyUser = dummyUser
    }
}
