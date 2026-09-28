package com.retailwave.fce.server.rpc
/**
 * $Id: UserServiceRemoteImpl.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/server/rpc/UserServiceRemoteImpl.java $
 */

import com.google.gwt.gen2.table.client.TableModelHelper
import com.google.gwt.user.server.rpc.RemoteServiceServlet
import com.google.gwt.user.server.rpc.UnexpectedException
import com.retailwave.fce.server.service.UserService
import com.retailwave.fce.shared.domain.User
import com.retailwave.fce.shared.dto.UserDTO
import com.retailwave.fce.shared.rpc.UserServiceRemote
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.ServletContextAware
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.Controller

import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.ArrayList
import java.util.HashSet
import java.util.List
import java.util.Set

open class UserServiceRemoteImpl : RemoteServiceServlet(), UserServiceRemote, Controller, ServletContextAware {

    private var serialVersionUID: long  = 1L

    // Instance fields
    private var servletContext: ServletContext? = null

    @Autowired
    private var userService: UserService? = null

    constructor() {
    }

        override fun getServletContext(): ServletContext {
        return servletContext
    }

    fun setServletContext(servletContext: ServletContext) {
        this.servletContext = servletContext
    }

    fun setUserService(userService: UserService) {
        this.userService = userService
    }

    /**
     * Call GWT's RemoteService doPost() method and return null.
     *
     * @param request  The current HTTP request
     * @param response The current HTTP response
     * @return A ModelAndView to render, or null if handled directly
     * @throws Exception In case of errors
     */
    @Throws(Exception::class)
    fun handleRequest(request: HttpServletRequest, response: HttpServletResponse): ModelAndView {
        doPost(request, response)
        return null; // response handled by GWT RPC over XmlHttpRequest
    }

        override fun getUser(): UserDTO {
        val userDTO = UserDTO()
//        User serverUser = vscService.getUser(getThreadLocalRequest())
        val serverUser = null
        if (null != serverUser) {
            userDTO.setUserId(serverUser.getExternalId())
            userDTO.setName(serverUser.getName())
            userDTO.setFullName(serverUser.getFullName())
            userDTO.setEmailAddress(serverUser.getEmailAddress())
        }
        return userDTO
    }

        override fun getUser(id: String): UserDTO {
        val clientUserDTO = UserDTO()
        val domainUser = userService.getUser(id)
        if (null != domainUser) {
            clientUserDTO.setUserId(domainUser.getExternalId())
            clientUserDTO.setName(domainUser.getName())
            clientUserDTO.setFullName(domainUser.getFullName())
            clientUserDTO.setEmailAddress(domainUser.getEmailAddress())
            clientUserDTO.setActive(domainUser.isActive())
        }
        return clientUserDTO
    }

        override fun saveUser(userDTO: UserDTO) {
        // validate data
        val serverUser = userService.getUserByName(userDTO.getName())
        if (null == serverUser) {
            // convert to domain data
            val partnerUser = User()
            partnerUser.setName(userDTO.getName())
            partnerUser.setActive(userDTO.isActive())
            partnerUser.setEmailAddress(userDTO.getEmailAddress())
            partnerUser.setFullName(userDTO.getFullName())
            partnerUser.setExternalId(userDTO.getExternalId())
            userService.saveUser(partnerUser)
        } else {
            throw IllegalArgumentException("UserDTO already exists with name: " + userDTO.getName())
        }
    }

        override fun updateUser(userDTO: UserDTO) {
    }

        override fun searchUsers(userDTO: UserDTO, request: TableModelHelper.Request): List<UserDTO> {
        val serverUsers = null
        return toClientUsers(ArrayList(), serverUsers)
    }

        override fun countUsers(): Int {
        return userService.countUsers()
    }

        override fun countLexmarkUsers(): Int {
        return userService.countLexmarkUsers()
    }

        override fun countPartnerUsers(): Int {
        return userService.countPartnerUsers()
    }

    private fun toClientUsers(userDTOs: List<UserDTO>, serverUsers: List<out User>): List<UserDTO> {
        for (serverUser in serverUsers) {
            val clientUserDTO = UserDTO()
            clientUserDTO.setUserId(serverUser.getExternalId())
            clientUserDTO.setName(serverUser.getName())
            clientUserDTO.setFullName(serverUser.getFullName())
            clientUserDTO.setEmailAddress(serverUser.getEmailAddress())
            clientUserDTO.setActive(serverUser.isActive())
            userDTOs.add(clientUserDTO)
        }
        return userDTOs
    }

}