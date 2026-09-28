package com.retailwave.fce.shared.rpc
/**
 * $Id: UserServiceRemote.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/shared/rpc/UserServiceRemote.java $
 */

import com.google.gwt.core.client.GWT
import com.google.gwt.gen2.table.client.TableModelHelper
import com.google.gwt.user.client.rpc.RemoteService
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath
import com.retailwave.fce.shared.dto.UserDTO

@RemoteServiceRelativePath("UserServiceRemote.htm")
interface UserServiceRemote : RemoteService {
    /**
     * Utility/Convenience class.
     * Use UserServiceRemote.App.getInstance() to access instance of UserServiceAsync
     */
    object App {
        private val ourInstance = GWT.create(UserServiceRemote::class.java) as UserServiceRemoteAsync

        @JvmStatic
        fun getInstance(): UserServiceRemoteAsync {
            return ourInstance
        }
    }

    fun getUser(): UserDTO?
    fun getUser(id: String?): UserDTO?
    fun saveUser(userDTO: UserDTO?)
    fun updateUser(userDTO: UserDTO?)
    fun searchUsers(userDTO: UserDTO?, request: TableModelHelper.Request?): List<UserDTO>?
    fun countUsers(): Int
    fun countLexmarkUsers(): Int
    fun countPartnerUsers(): Int
}
