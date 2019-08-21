package xyz.phanta.cmt.model

import com.beust.klaxon.JsonObject
import com.beust.klaxon.json

data class ModLoaderVersion(val loader: ModLoader, val version: String) {
    companion object {
        fun deserialize(dto: JsonObject): ModLoaderVersion = ModLoaderVersion(
            enumValueOf(dto.string("loader") ?: throw IllegalArgumentException("Expected mod loader!")),
            dto.string("version") ?: throw IllegalArgumentException("Expected mod loader version!")
        )
    }

    fun serialize(): JsonObject = json { obj("loader" to loader.name, "version" to version) }

    override fun toString(): String = "$loader-$version"
}
