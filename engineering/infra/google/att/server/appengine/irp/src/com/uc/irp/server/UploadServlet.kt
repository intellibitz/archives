package com.uc.irp.server

import com.google.appengine.api.datastore.Text
import org.mortbay.util.ajax.JSON
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.logging.Logger
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class UploadServlet : HttpServlet() {

    @Throws(IOException::class)
    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val content = req.getParameter("content")
        log.info("Content received from clients: $content")

        @Suppress("UNCHECKED_CAST")
        val jsonMap = JSON.parse(content) as Map<*, *>

        val textContent = Text(content)
        val incidentReport = IncidentReport(textContent)
        val sid = jsonMap["Subscriber Id"]
        if (null != sid) {
            incidentReport.subscriberId = sid.toString()
        }
        val event = jsonMap["Event"]
        if (null != event) {
            incidentReport.event = event.toString()
        }
        val location = jsonMap["Location"]
        if (null != location) {
            val loc = location.toString()
            var cloc = ""
            val loca = loc.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (items in loca) {
                if (items.contains("mLatitude") || items.contains("mLongitude")) {
                    cloc += "$items,"
                }
            }
            if (null != loc) {
                val s = loc.indexOf("mLatitude=")
            }
            incidentReport.location = cloc
        }
        try {
            val tm = jsonMap["Time"]
            if (tm != null) {
                incidentReport.captureTime = SimpleDateFormat
                    .getDateTimeInstance()
                    .parse(tm.toString())
            }
        } catch (e: ParseException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

        val pm = PMF.get().persistenceManager

        var result = "FAIL"
        try {
            pm.makePersistent(incidentReport)
            result = "OK"
        } finally {
            pm.close()
        }

        resp.contentType = "text/plain"
        resp.writer.print(result)
    }

    companion object {
        private val log = Logger.getLogger(UploadServlet::class.java.name)
    }
}
