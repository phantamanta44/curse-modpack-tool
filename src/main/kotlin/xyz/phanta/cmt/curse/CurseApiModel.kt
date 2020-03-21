package xyz.phanta.cmt.curse

import com.beust.klaxon.JsonObject
import java.time.Instant

class CurseApiMod(
    val id: Long,
    val name: String,
    val slug: String,
    val authors: List<CurseApiAuthor>,
    val websiteUrl: String,
    val summary: String,
    val downloadCount: Int,
    val gameVersionLatestFiles: List<CurseApiGameVersionFile>
) {
    companion object {
        fun deserialize(dto: JsonObject): CurseApiMod = CurseApiMod(
            dto.long("id")!!,
            dto.string("name")!!,
            dto.string("slug")!!,
            dto.array<JsonObject>("authors")!!.map { CurseApiAuthor.deserialize(it) },
            dto.string("websiteUrl")!!,
            dto.string("summary")!!,
            dto.int("downloadCount")!!,
            dto.array<JsonObject>("gameVersionLatestFiles")!!.map { CurseApiGameVersionFile.deserialize(it) }
        )
    }
}

class CurseApiAuthor(val userId: Long, val name: String, val url: String) {
    companion object {
        fun deserialize(dto: JsonObject): CurseApiAuthor = CurseApiAuthor(
            dto.long("userId")!!,
            dto.string("name")!!,
            dto.string("url")!!
        )
    }
}

class CurseApiGameVersionFile(val gameVersion: String, val projectFileId: Long) {
    companion object {
        fun deserialize(dto: JsonObject): CurseApiGameVersionFile = CurseApiGameVersionFile(
            dto.string("gameVersion")!!,
            dto.long("projectFileId")!!
        )
    }
}

class CurseApiFile(
    val id: Long,
    val displayName: String,
    val fileName: String,
    val fileDate: Instant,
    val downloadUrl: String,
    val dependencies: List<CurseApiDependency>,
    val gameVersion: List<String>
) {
    companion object {
        fun deserialize(dto: JsonObject): CurseApiFile = CurseApiFile(
            dto.long("id")!!,
            dto.string("displayName")!!,
            dto.string("fileName")!!,
            Instant.parse(dto.string("fileDate"))!!,
            dto.string("downloadUrl")!!,
            dto.array<JsonObject>("dependencies")!!.map { CurseApiDependency.deserialize(it) },
            dto.array<String>("gameVersion")!!
        )
    }
}

class CurseApiDependency(val addonId: Long, val type: Int) {
    companion object {
        fun deserialize(dto: JsonObject): CurseApiDependency = CurseApiDependency(
            dto.long("addonId")!!,
            dto.int("type")!!
        )
    }
}
