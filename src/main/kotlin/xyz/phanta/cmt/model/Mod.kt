package xyz.phanta.cmt.model

import com.beust.klaxon.JsonObject
import com.beust.klaxon.json
import xyz.phanta.cmt.util.License

data class Mod(val slug: String, val name: String, val projectId: Long, val author: String, val license: License) {
    companion object {
        fun deserialize(dto: JsonObject): Mod =
            Mod(
                dto.string("slug") ?: throw IllegalArgumentException("Expected mod slug!"),
                dto.string("name") ?: throw IllegalArgumentException("Expected mod name!"),
                dto.long("project_id") ?: throw IllegalArgumentException("Expected mod project id!"),
                dto.string("author") ?: throw IllegalArgumentException("Expected mod author!"),
                enumValueOf(dto.string("license") ?: throw IllegalArgumentException("Expected mod license!"))
            )
    }

    fun serialize(): JsonObject = json {
        obj(
            "slug" to slug,
            "name" to name,
            "project_id" to projectId,
            "author" to author,
            "license" to license.name
        )
    }
}
