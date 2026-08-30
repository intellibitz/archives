package cc.thevar.blukit.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ConnectionBridge {
    PEER_TO_PEER
}

@Serializable
data class RoomConnection(
    val sourceId: String,
    val targetId: String,
    val bridgeType: ConnectionBridge = ConnectionBridge.PEER_TO_PEER,
    val strength: Float = 1.0f,
)

@Serializable
data class RoomTemplate(
    val id: String,
    val name: String,
    val description: String,
    val roles: List<String> = emptyList(),
    val defaultChannels: List<String> = emptyList(),
    val iconEmoji: String = "⚡"
)

object RoomTemplates {
    val UNIVERSITY = RoomTemplate(
        id = "template_uni",
        name = "University",
        description = "Academic mesh for lectures, study groups, and campus news.",
        roles = listOf("Student", "Faculty", "Staff"),
        defaultChannels = listOf("Class Groups", "Campus News", "Study Sessions"),
        iconEmoji = "🎓"
    )

    val FESTIVAL = RoomTemplate(
        id = "template_fest",
        name = "Festival",
        description = "High-density crowd for schedules, meetups, and broadcasts.",
        roles = listOf("Attendee", "Organizer", "Artist"),
        defaultChannels = listOf("Stage Info", "Food Map", "Lost & Found"),
        iconEmoji = "🎪"
    )

    val VENUE = RoomTemplate(
        id = "template_venue",
        name = "Venue",
        description = "Localized frequency for stadiums, malls, or hubs.",
        roles = listOf("Visitor", "Staff", "Service"),
        defaultChannels = listOf("Live Energy", "Concierge", "Support"),
        iconEmoji = "🏟️"
    )

    val FAMILY = RoomTemplate(
        id = "template_family",
        name = "Family Home",
        description = "Private mesh for the household. Sync lists and share memories.",
        roles = listOf("Parent", "Kid", "Resident"),
        defaultChannels = listOf("Kitchen", "Chores", "General"),
        iconEmoji = "🏠"
    )

    val STUDY = RoomTemplate(
        id = "template_study",
        name = "Study Group",
        description = "Focused room for projects and sharing notes.",
        roles = listOf("Student", "Editor", "Viewer"),
        defaultChannels = listOf("Research", "Drafts", "Deadlines"),
        iconEmoji = "📚"
    )

    val DINING = RoomTemplate(
        id = "template_dining",
        name = "Kitchen",
        description = "Room for recipes, groceries, and dining updates.",
        roles = listOf("Chef", "Foodie", "Helper"),
        defaultChannels = listOf("Menu", "Groceries", "Feedback"),
        iconEmoji = "🍳"
    )

    val ALL = listOf(UNIVERSITY, FESTIVAL, VENUE, FAMILY, STUDY, DINING)
}
