package com.uc.irp.server

import com.google.appengine.api.datastore.Text
import java.io.Serializable
import java.util.Date
import javax.jdo.annotations.IdGeneratorStrategy
import javax.jdo.annotations.IdentityType
import javax.jdo.annotations.PersistenceCapable
import javax.jdo.annotations.Persistent
import javax.jdo.annotations.PrimaryKey

@PersistenceCapable(identityType = IdentityType.APPLICATION)
class IncidentReport(content: Text?) : Serializable {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    var id: Long? = null

    @Persistent
    var subscriberId: String? = null

    @Persistent
    var event: String? = null

    @Persistent
    var location: String? = null

    @Persistent
    var captureTime: Date? = null

    @Persistent(serialized = "true")
    var content: Text? = content
}
