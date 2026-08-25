package cc.thevar.blukit.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ConnectionBridge {
    PEER_TO_PEER,
    MESH_RELAY,
    GHOST_BRIDGE
}

@Serializable
data class CrowdConnection(
    val sourceId: String,
    val targetId: String,
    val bridgeType: ConnectionBridge = ConnectionBridge.PEER_TO_PEER,
    val strength: Float = 1.0f
)

@Serializable
data class CrowdTemplate(
    val id: String,
    val name: String,
    val description: String,
    val roles: List<String> = emptyList(),
    val defaultChains: List<String> = emptyList(),
    val iconEmoji: String = "⚡"
)

object CrowdTemplates {
    val UNIVERSITY = CrowdTemplate(
        id = "template_uni",
        name = "University",
        description = "Academic mesh for lectures, study groups, and campus news.",
        roles = listOf("Student", "Faculty", "Staff"),
        defaultChains = listOf("Class Groups", "Campus News", "Study Sessions"),
        iconEmoji = "🎓"
    )

    val FESTIVAL = CrowdTemplate(
        id = "template_fest",
        name = "Festival",
        description = "High-density crowd for schedules, meetups, and broadcasts.",
        roles = listOf("Attendee", "Organizer", "Artist"),
        defaultChains = listOf("Stage Info", "Food Map", "Lost & Found"),
        iconEmoji = "🎪"
    )

    val VENUE = CrowdTemplate(
        id = "template_venue",
        name = "Venue",
        description = "Localized frequency for stadiums, malls, or hubs.",
        roles = listOf("Visitor", "Staff", "Service"),
        defaultChains = listOf("Live Energy", "Concierge", "Support"),
        iconEmoji = "🏟️"
    )

    val ALL = listOf(UNIVERSITY, FESTIVAL, VENUE)
}
