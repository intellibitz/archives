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

package com.ibt.intellimeet.app;

/**
 *
 * @author jailani
 */
import com.ibt.intellimeet.data.User;
import java.io.Serializable;
import java.util.List;
import javax.ejb.Remove;
import static org.jboss.seam.ScopeType.EVENT;
import org.jboss.seam.annotations.Name;

import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;


@Stateful
@Name("loginUserAction")
public class LoginUserAction implements ILoginUserActionLocal,Serializable{
    
    
    @Logger private transient Log log;

    
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Out(scope=ScopeType.SESSION)
    private String username;
    
    @In(required = true)
    private User user;
    
    
    private String password;
    
    private boolean existing;
    
     public User findUser(long id)
    {
        return entityManager.find(User.class, id);
    }

    
    public String validateUserLogin(){
        log.info("Validate user");
         List existing = getEntityManager().createQuery
                    ("select u.email from User u where u.email=#{user.email} and u.password=#{user.password}")
                    .getResultList();
//         User usr=(User)existing;
//         String userEmailId=usr.getEmail();
         
         if(existing.isEmpty()){
             log.info("Email does not exists!");
             return "/error.xhtml";
         }
         else{
            setUsername(existing.toString());
              return "/Welcome.xhtml";
         }
     // 
     // if(usr.getEmail() usr.getPassword()
        
       
    }
    
    
    @Create
    public void init(){
        username="ashok";
    }
    
    
    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public


    String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public

    boolean isExisting() {
        return existing;
    }

    public void setExisting(boolean existing) {
        this.existing = existing;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
   
    
    @Destroy
    @Remove
    public void destroy()
    {
        getLog().info("loginUserAction#destroy.. Done!");
    }


}


