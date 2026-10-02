package com.retailwave.fce.shared.rpc
/**
 * $Id: UserServiceRemoteAsync.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/shared/rpc/UserServiceRemoteAsync.java $
 */

import com.google.gwt.gen2.table.client.TableModelHelper
import com.google.gwt.user.client.rpc.AsyncCallback
import com.retailwave.fce.shared.dto.UserDTO

interface UserServiceRemoteAsync {
    fun getUser(asyncCallback: AsyncCallback<UserDTO>?)
    fun getUser(id: String?, async: AsyncCallback<UserDTO>?)
    fun saveUser(userDTO: UserDTO?, asyncCallback: AsyncCallback<Void>?)
    fun updateUser(userDTO: UserDTO?, asyncCallback: AsyncCallback<Void>?)
    fun searchUsers(userDTO: UserDTO?, request: TableModelHelper.Request?, asyncCallback: AsyncCallback<List<UserDTO>>?)
    fun countUsers(async: AsyncCallback<Int>?)
    fun countPartnerUsers(async: AsyncCallback<Int>?)
    fun countLexmarkUsers(async: AsyncCallback<Int>?)
}
