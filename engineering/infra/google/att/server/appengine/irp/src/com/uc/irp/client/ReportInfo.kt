package com.uc.irp.client

import java.io.Serializable
import java.util.Date

class ReportInfo : Serializable {
    var id: Long? = null
    var subscriberId: String? = null
    var event: String? = null
    var location: String? = null
    var captureTime: Date? = null
    var content: String? = null
}
