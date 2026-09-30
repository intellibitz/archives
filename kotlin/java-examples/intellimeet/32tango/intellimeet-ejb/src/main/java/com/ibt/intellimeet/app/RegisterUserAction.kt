/*
 *  Copyright 2008 vijayan.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package com.ibt.intellimeet.app

/*
<!--
$Id::                                                                           $: Id of last commit
$Rev::                                                                          $: Revision of last commit
$Author::                                                                       $: Author of last commit
$Date::                                                                         $: Date of last commit
$HeadURL::                                                                      $: Head URL of last commit
-->
*/

import com.ibt.intellimeet.data.User
import org.hibernate.validator.NotNull
import org.jboss.seam.ScopeType.EVENT
import org.jboss.seam.annotations.Destroy
import org.jboss.seam.annotations.In
import org.jboss.seam.annotations.Logger
import org.jboss.seam.annotations.Name
import org.jboss.seam.annotations.Scope
import org.jboss.seam.faces.FacesMessages
import org.jboss.seam.log.Log
import java.io.Serializable
import javax.ejb.Remove
import javax.ejb.Stateful
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * @author vijayan
 */
@Stateful
@Scope(EVENT)
@Name("registerUserAction")
class RegisterUserAction : IRegisterUserActionLocal, Serializable {

    @Logger
    @Transient
    var log: Log? = null

    @In(required = true)
    private var user: User? = null

    @PersistenceContext
    private var entityManager: EntityManager? = null

    @NotNull
    private var verify: String? = null
    private var registered: Boolean = false

    fun findUser(id: Long): User? {
        return entityManager!!.find(User::class.java, id)
    }

    override fun register(): Long {
        if (user!!.password == verify) {
            val existing = entityManager!!.createQuery(
                "select u.email from User u where u.email=#{user.email}"
            ).resultList
            if (existing.isEmpty()) {
                //entityManager.persist(user);
                entityManager!!.merge(user)
                // facesMessages.add("Successfully registered as #{user.username}");
                log!!.info("Username #{user.email} successfully created")
                FacesMessages.instance().add("Username #{user.email} successfully created")
                registered = true
            } else {
                log!!.info("Username #{user.email} already exists")
                FacesMessages.instance().add("User #{user.email} already exists")
            }
        } else {
            log!!.info("Re-enter your password")
            FacesMessages.instance().add("Didn't match. Re-enter your password again")
            verify = null
        }
        return user!!.id
    }

    override fun getVerify(): String? {
        return verify
    }

    override fun setVerify(verify: String?) {
        this.verify = verify
    }

    fun getEntityManager(): EntityManager? {
        return entityManager
    }

    fun setEntityManager(entityManager: EntityManager?) {
        this.entityManager = entityManager
    }

    fun setRegistered(registered: Boolean) {
        this.registered = registered
    }

    override fun isRegistered(): Boolean {
        return registered
    }

    fun getUser(): User? {
        return user
    }

    fun setUser(user: User?) {
        this.user = user
    }

    @Destroy
    @Remove
    override fun destroy() {
        log!!.info("registerUserAction#destroy.. Done!")
    }

    companion object {
        const val serialVersionUID = 2973374377453022888L
    }
}
