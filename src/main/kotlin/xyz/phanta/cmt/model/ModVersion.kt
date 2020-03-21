package xyz.phanta.cmt.model

import com.beust.klaxon.JsonObject
import com.beust.klaxon.json
import xyz.phanta.cmt.util.ModRef
import java.time.Instant

data class ModVersion(
    val mod: Mod,
    val version: String,
    val fileId: Long,
    val gameVersion: GameVersion,
    val timestamp: Instant,
    val dependencies: Set<ModRef>
) {
    companion object {
        fun deserialize(dto: JsonObject): ModVersion = ModVersion(
            Mod.deserialize(dto.obj("mod") ?: throw IllegalArgumentException("Expected mod!")),
            dto.string("version") ?: throw IllegalArgumentException("Expected mod version name!"),
            dto.long("file_id") ?: throw IllegalArgumentException("Expected mod version file id!"),
            enumValueOf(
                dto.string("game_version") ?: throw IllegalArgumentException("Expected mod version game version!")
            ),
            Instant.ofEpochSecond(
                dto.long("timestamp") ?: throw IllegalArgumentException("Expected mod version timestamp!")
            ),
            (dto.array<String>("dependencies")?.map { ModRef.parse(it) }
                ?: throw IllegalArgumentException("Expected mod version dependencies!")).toSet()
        )
    }

    fun serialize(): JsonObject = json {
        obj(
            "mod" to mod.serialize(),
            "version" to version,
            "file_id" to fileId,
            "game_version" to gameVersion.name,
            "timestamp" to timestamp.epochSecond,
            "dependencies" to array(dependencies.map { it.toString() })
        )
    }

    override fun toString(): String = "${mod.slug}@$fileId"
}
