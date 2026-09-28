package com.androidrocks.bex.client

import com.google.gwt.user.client.rpc.RemoteService
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
interface GreetingService : RemoteService {
    fun greetServer(name: String?): String?
}
