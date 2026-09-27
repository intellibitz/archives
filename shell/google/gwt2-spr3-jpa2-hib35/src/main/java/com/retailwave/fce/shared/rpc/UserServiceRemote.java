package com.retailwave.fce.shared.rpc;
/**
 * $Id: UserServiceRemote.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/shared/rpc/UserServiceRemote.java $
 */

import com.google.gwt.core.client.GWT;
import com.google.gwt.gen2.table.client.TableModelHelper;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.retailwave.fce.shared.dto.UserDTO;

import java.util.List;

@RemoteServiceRelativePath("UserServiceRemote.htm")
public interface UserServiceRemote extends RemoteService {
    /**
     * Utility/Convenience class.
     * Use UserServiceRemote.App.getInstance() to access static instance of UserServiceAsync
     */
    public static class App {
        private static final UserServiceRemoteAsync ourInstance = (UserServiceRemoteAsync) GWT.create(UserServiceRemote.class);

        public static UserServiceRemoteAsync getInstance() {
            return ourInstance;
        }
    }

    UserDTO getUser();

    public UserDTO getUser(String id);

    void saveUser(UserDTO userDTO);

    void updateUser(UserDTO userDTO);

    List<UserDTO> searchUsers(UserDTO userDTO, TableModelHelper.Request request);

    int countUsers();

    int countLexmarkUsers();

    int countPartnerUsers();
}
