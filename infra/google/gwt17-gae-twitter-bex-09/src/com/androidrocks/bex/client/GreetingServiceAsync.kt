package com.androidrocks.bex.client

import com.google.gwt.user.client.rpc.AsyncCallback

/**
 * The async counterpart of <code>GreetingService</code>.
 */
interface GreetingServiceAsync {
    fun greetServer(input: String?, callback: AsyncCallback<String>?)
}
