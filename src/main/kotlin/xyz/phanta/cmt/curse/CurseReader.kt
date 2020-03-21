package xyz.phanta.cmt.curse

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.beust.klaxon.internal.firstNotNullResult
import xyz.phanta.cmt.model.GameVersion
import xyz.phanta.cmt.model.Mod
import xyz.phanta.cmt.model.ModVersion
import xyz.phanta.cmt.util.License
import xyz.phanta.cmt.util.ModProjectId
import xyz.phanta.cmt.util.ModRef
import xyz.phanta.cmt.util.ModSlug
import java.io.Reader
import java.net.URL

object CurseReader {
    fun retrieveModById(projectId: Long): CurseApiMod = ApiUrlBuilder("addon", projectId.toString()).build()
        .retrieve { CurseApiMod.deserialize(Klaxon().parseJsonObject(it)) }

    fun retrieveModBySlug(slug: String): CurseApiMod = ApiUrlBuilder("addon", "search")
        .query("categoryId", "0").query("gameId", "432").query("searchFilter", slug).query("sectionId", "6").build()
        .retrieve { Klaxon().parseJsonArray(it) }
        .let { mods ->
            mods.firstNotNullResult { dto -> CurseApiMod.deserialize(dto as JsonObject).takeIf { it.slug == slug } }
                ?: throw NoSuchElementException("Could not resolve mod data for slug: $slug")
        }

    fun retrieveMod(ref: ModRef): CurseApiMod = when (ref) {
        is ModProjectId -> retrieveModById(ref.id)
        is ModSlug -> retrieveModBySlug(ref.slug)
    }

    fun retrieveModFile(projectId: Long, fileId: Long): CurseApiFile =
        ApiUrlBuilder("addon", projectId.toString(), "file", fileId.toString()).build()
            .retrieve { CurseApiFile.deserialize(Klaxon().parseJsonObject(it)) }

    private fun <T> URL.retrieve(action: (Reader) -> T?): T = try {
        action(openStream().bufferedReader()) ?: throw IllegalStateException("Failed to parse JSON at URL: $this")
    } catch (e: Exception) {
        throw IllegalStateException("Failed to read url: $this", e)
    }
}

fun CurseApiMod.parseMod(): Mod = Mod(
    slug, name, id, authors.firstOrNull()?.name ?: "", License.UNKNOWN // TODO find mod license somehow
)

fun CurseApiFile.parseVersion(mod: Mod, defaultGameVersion: GameVersion): ModVersion = ModVersion(
    mod,
    displayName,
    id,
    gameVersion.firstNotNullResult { GameVersion.parseGracefully(it) } ?: defaultGameVersion,
    fileDate,
    dependencies.filter { it.type == 3 }.map { ModProjectId(it.addonId) }.toSet()
)
