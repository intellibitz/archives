package com.retailwave.fce.server.rpc;
/**
 * $Id: UserServiceRemoteImpl.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/server/rpc/UserServiceRemoteImpl.java $
 */


import com.google.gwt.gen2.table.client.TableModelHelper;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.UnexpectedException;
import com.retailwave.fce.server.service.UserService;
import com.retailwave.fce.shared.domain.User;
import com.retailwave.fce.shared.dto.UserDTO;
import com.retailwave.fce.shared.rpc.UserServiceRemote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserServiceRemoteImpl
        extends RemoteServiceServlet
        implements UserServiceRemote, Controller, ServletContextAware {


    private static final long serialVersionUID = 1L;


    // Instance fields
    private ServletContext servletContext;

    @Autowired
    private UserService userService;

    public UserServiceRemoteImpl() {
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Call GWT's RemoteService doPost() method and return null.
     *
     * @param request  The current HTTP request
     * @param response The current HTTP response
     * @return A ModelAndView to render, or null if handled directly
     * @throws Exception In case of errors
     */
    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        doPost(request, response);
        return null; // response handled by GWT RPC over XmlHttpRequest
    }


    @Override
    public UserDTO getUser() {
        UserDTO userDTO = new UserDTO();
//        User serverUser = vscService.getUser(getThreadLocalRequest());
        User serverUser = null;
        if (null != serverUser) {
            userDTO.setUserId(serverUser.getExternalId());
            userDTO.setName(serverUser.getName());
            userDTO.setFullName(serverUser.getFullName());
            userDTO.setEmailAddress(serverUser.getEmailAddress());
        }
        return userDTO;
    }

    @Override
    public UserDTO getUser(String id) {
        UserDTO clientUserDTO = new UserDTO();
        User domainUser = userService.getUser(id);
        if (null != domainUser) {
            clientUserDTO.setUserId(domainUser.getExternalId());
            clientUserDTO.setName(domainUser.getName());
            clientUserDTO.setFullName(domainUser.getFullName());
            clientUserDTO.setEmailAddress(domainUser.getEmailAddress());
            clientUserDTO.setActive(domainUser.isActive());
        }
        return clientUserDTO;
    }

    @Override
    public void saveUser(UserDTO userDTO) {
        // validate data
        User serverUser = userService.getUserByName(userDTO.getName());
        if (null == serverUser) {
            // convert to domain data
            User partnerUser = new User();
            partnerUser.setName(userDTO.getName());
            partnerUser.setActive(userDTO.isActive());
            partnerUser.setEmailAddress(userDTO.getEmailAddress());
            partnerUser.setFullName(userDTO.getFullName());
            partnerUser.setExternalId(userDTO.getExternalId());
            userService.saveUser(partnerUser);
        } else {
            throw new IllegalArgumentException("UserDTO already exists with name: " + userDTO.getName());
        }
    }

    @Override
    public void updateUser(UserDTO userDTO) {
    }

    @Override
    public List<UserDTO> searchUsers(UserDTO userDTO, TableModelHelper.Request request) {
        List<? extends User> serverUsers = null;
        return toClientUsers(new ArrayList<UserDTO>(), serverUsers);
    }

    @Override
    public int countUsers() {
        return userService.countUsers();
    }

    @Override
    public int countLexmarkUsers() {
        return userService.countLexmarkUsers();
    }

    @Override
    public int countPartnerUsers() {
        return userService.countPartnerUsers();
    }

    private List<UserDTO> toClientUsers(List<UserDTO> userDTOs,
                                     List<? extends User> serverUsers) {
        for (User serverUser : serverUsers) {
            UserDTO clientUserDTO = new UserDTO();
            clientUserDTO.setUserId(serverUser.getExternalId());
            clientUserDTO.setName(serverUser.getName());
            clientUserDTO.setFullName(serverUser.getFullName());
            clientUserDTO.setEmailAddress(serverUser.getEmailAddress());
            clientUserDTO.setActive(serverUser.isActive());
            userDTOs.add(clientUserDTO);
        }
        return userDTOs;
    }

}