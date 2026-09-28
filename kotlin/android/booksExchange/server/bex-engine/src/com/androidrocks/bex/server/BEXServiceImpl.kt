package com.androidrocks.bex.server

import java.util.logging.Logger

import com.androidrocks.bex.client.BEXService
import com.androidrocks.bex.client.ReportInfo
import com.google.gwt.user.server.rpc.RemoteServiceServlet

/**
 * The implemenation of the RPC service which runs on the server.
 */
class BEXServiceImpl : RemoteServiceServlet(), BEXService {

    override fun getIncidentReports(): Array<ReportInfo>? {
        // TODO Auto-generated method stub
        return null
    }

    companion object {
        private val LOG: Logger = Logger.getLogger(BEXServiceImpl::class.java.name)
    }
}
