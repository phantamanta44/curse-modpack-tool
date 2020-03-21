package xyz.phanta.cmt.model

import com.beust.klaxon.JsonObject
import com.beust.klaxon.json
import xyz.phanta.cmt.util.ModProjectId
import xyz.phanta.cmt.util.ModRef
import xyz.phanta.cmt.util.ModSlug
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
                .forEach {
                    model.putMod(it)
                }
            return model
        }
    }

    val modLoaders: MutableMap<ModLoader, ModLoaderVersion> = EnumMap(ModLoader::class.java)
    private val modsById: MutableMap<Long, ModVersion> = mutableMapOf()
    private val modsBySlug: MutableMap<String, ModVersion> = mutableMapOf()
    val mods: Collection<ModVersion>
        get() = modsById.values

    fun getModById(projectId: Long): ModVersion? = modsById[projectId]

    fun getModBySlug(slug: String): ModVersion? = modsBySlug[slug]

    fun getMod(ref: ModRef): ModVersion? = when (ref) {
        is ModProjectId -> getModById(ref.id)
        is ModSlug -> getModBySlug(ref.slug)
    }

    fun putMod(mod: ModVersion) {
        modsById[mod.mod.projectId] = mod
        modsBySlug[mod.mod.slug] = mod
    }

    fun removeModById(projectId: Long): Boolean = modsById.remove(projectId)?.let {
        modsBySlug.remove(it.mod.slug)
        true
    } ?: false

    fun removeModBySlug(slug: String): Boolean = modsBySlug.remove(slug)?.let {
        modsById.remove(it.mod.projectId)
        true
    } ?: false

    fun removeMod(ref: ModRef): Boolean = when (ref) {
        is ModProjectId -> removeModById(ref.id)
        is ModSlug -> removeModBySlug(ref.slug)
    }

    fun serialize(): JsonObject = json {
        obj(
            "name" to name,
            "author" to author,
            "version" to version,
            "game_version" to gameVersion.name,
            "mod_loaders" to array(modLoaders.values.map { it.serialize() }),
            "mods" to array(modsById.values.map { it.serialize() })
        )
    }
}
