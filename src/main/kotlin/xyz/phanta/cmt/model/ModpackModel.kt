package xyz.phanta.cmt.model

import com.beust.klaxon.JsonObject
import com.beust.klaxon.json
import java.util.*

class ModpackModel(val gameVersion: GameVersion, var name: String, var author: String, var version: String) {
    companion object {
        fun deserialize(dto: JsonObject): ModpackModel {
            val model = ModpackModel(
                enumValueOf(dto.string("game_version") ?: throw IllegalArgumentException("Expected game version!")),
                dto.string("name") ?: throw IllegalArgumentException("Expected name!"),
                dto.string("author") ?: throw IllegalArgumentException("Expected author!"),
                dto.string("version") ?: throw IllegalArgumentException("Expected version!")
            )
            (dto.array<JsonObject>("mod_loaders") ?: throw IllegalArgumentException("Expected mod loader list!"))
                .map { ModLoaderVersion.deserialize(it) }
                .forEach { model.modLoaders[it.loader] = it }
            (dto.array<JsonObject>("mods") ?: throw IllegalArgumentException("Expected mod list!"))
                .map { ModVersion.deserialize(it) }
                .forEach { model.mods[it.mod.slug] = it }
            return model
        }
    }

    val modLoaders: MutableMap<ModLoader, ModLoaderVersion> = EnumMap(ModLoader::class.java)
    val mods: MutableMap<String, ModVersion> = mutableMapOf()

    fun serialize(): JsonObject = json {
        obj(
            "name" to name,
            "author" to author,
            "version" to version,
            "game_version" to gameVersion.name,
            "mod_loaders" to array(modLoaders.values.map { it.serialize() }),
            "mods" to array(mods.values.map { it.serialize() })
        )
    }
}
