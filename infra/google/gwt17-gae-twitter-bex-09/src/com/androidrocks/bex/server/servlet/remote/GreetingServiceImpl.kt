package com.androidrocks.bex.server.servlet.remote

import com.androidrocks.bex.client.GreetingService
import com.google.gwt.user.server.rpc.RemoteServiceServlet

/**
 * The server side implementation of the RPC service.
 */
@Suppress("serial")
class GreetingServiceImpl : RemoteServiceServlet(), GreetingService {

    override fun greetServer(input: String?): String? {
        val serverInfo = servletContext.serverInfo
        val userAgent = threadLocalRequest.getHeader("User-Agent")
        return "Hello, " + input + "!<br><br>I am running " + serverInfo +
                ".<br><br>It looks like you are using:<br>" + userAgent
    }
}
