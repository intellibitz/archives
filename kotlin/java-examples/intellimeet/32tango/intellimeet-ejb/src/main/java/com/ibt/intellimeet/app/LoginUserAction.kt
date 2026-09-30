/*
 *  Copyright 2008 jailani.
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

/**
 *
 * @author jailani
 */
import com.ibt.intellimeet.data.User
import org.jboss.seam.ScopeType
import org.jboss.seam.annotations.Create
import org.jboss.seam.annotations.Destroy
import org.jboss.seam.annotations.In
import org.jboss.seam.annotations.Logger
import org.jboss.seam.annotations.Name
import org.jboss.seam.annotations.Out
import org.jboss.seam.log.Log
import java.io.Serializable
import javax.ejb.Remove
import javax.ejb.Stateful
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Stateful
@Name("loginUserAction")
class LoginUserAction : ILoginUserActionLocal, Serializable {

    @Logger
    @Transient
    private var log: Log? = null

    @PersistenceContext
    private var entityManager: EntityManager? = null

    @Out(scope = ScopeType.SESSION)
    private var username: String? = null

    @In(required = true)
    private var user: User? = null

    private var password: String? = null

    private var existing: Boolean = false

    fun findUser(id: Long): User? {
        return entityManager!!.find(User::class.java, id)
    }

    override fun validateUserLogin(): String? {
        log!!.info("Validate user")
        val existing = getEntityManager()!!.createQuery(
            "select u.email from User u where u.email=#{user.email} and u.password=#{user.password}"
        ).resultList
//         User usr=(User)existing;
//         String userEmailId=usr.getEmail();

        if (existing.isEmpty()) {
            log!!.info("Email does not exists!")
            return "/error.xhtml"
        } else {
            setUsername(existing.toString())
            return "/Welcome.xhtml"
        }
        //
        // if(usr.getEmail() usr.getPassword()
    }

    @Create
    override fun init() {
        username = "ashok"
    }

    fun getLog(): Log? {
        return log
    }

    fun setLog(log: Log?) {
        this.log = log
    }

    fun getEntityManager(): EntityManager? {
        return entityManager
    }

    fun setEntityManager(entityManager: EntityManager?) {
        this.entityManager = entityManager
    }

    fun getUsername(): String? {
        return username
    }

    fun setUsername(username: String?) {
        this.username = username
    }

    fun getPassword(): String? {
        return password
    }

    fun setPassword(password: String?) {
        this.password = password
    }

    fun isExisting(): Boolean {
        return existing
    }

    fun setExisting(existing: Boolean) {
        this.existing = existing
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
        getLog()!!.info("loginUserAction#destroy.. Done!")
    }
}
