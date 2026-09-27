package com.retailwave.fce.shared.rpc;
/**
 * $Id: UserServiceRemoteAsync.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/shared/rpc/UserServiceRemoteAsync.java $
 */

import com.google.gwt.gen2.table.client.TableModelHelper;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.retailwave.fce.shared.dto.UserDTO;

import java.util.List;

public interface UserServiceRemoteAsync {

    void getUser(AsyncCallback<UserDTO> asyncCallback);

    void getUser(String id, AsyncCallback<UserDTO> async);

    void saveUser(UserDTO userDTO, AsyncCallback<Void> asyncCallback);

    void updateUser(UserDTO userDTO, AsyncCallback<Void> asyncCallback);

    void searchUsers(UserDTO userDTO, TableModelHelper.Request request, AsyncCallback<List<UserDTO>> asyncCallback);

    void countUsers(AsyncCallback<Integer> async);

    void countPartnerUsers(AsyncCallback<Integer> async);

    void countLexmarkUsers(AsyncCallback<Integer> async);
}
