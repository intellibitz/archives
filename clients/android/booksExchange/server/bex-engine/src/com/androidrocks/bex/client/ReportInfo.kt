package com.androidrocks.bex.client

import java.io.Serializable
import java.util.Date

class ReportInfo : Serializable {
    var id: Long? = null
    var captureTime: Date? = null
    var content: String? = null
}
