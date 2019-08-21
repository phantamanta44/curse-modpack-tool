package xyz.phanta.cmt.workspace

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import xyz.phanta.cmt.LOGGER
import xyz.phanta.cmt.curse.CurseReader
import xyz.phanta.cmt.model.ModpackModel
import xyz.phanta.cmt.util.ClosedModVersionRef
import xyz.phanta.cmt.util.ModVersionRef
import xyz.phanta.cmt.util.OpenModVersionRef
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class ModpackWorkspace(val workingDir: Path, val model: ModpackModel) {
    companion object {
        private val PARSER: Parser = Parser.default()
        private const val MODEL_FILE: String = "model.json"

        fun load(workingDir: Path): ModpackWorkspace = Files.newInputStream(workingDir.resolve(MODEL_FILE)).buffered()
            .use {
                ModpackWorkspace(workingDir, ModpackModel.deserialize(PARSER.parse(it) as JsonObject))
            }

        fun loadGracefully(workingDir: Path): ModpackWorkspace? = try {
            load(workingDir)
        } catch (e: Exception) {
            null
        }
    }

    val mcDir: Path = workingDir.resolve(".minecraft")

    init {
        if (!Files.isDirectory(mcDir)) {
            if (Files.notExists(mcDir)) {
                Files.createDirectories(mcDir)
            } else {
                throw IllegalStateException("Could not create .minecraft directory!")
            }
        }
    }

    fun addResolveMods(refs: List<ModVersionRef>, recurseDeps: Boolean, update: Boolean) {
        if (update) {
            addResolveMods(refs, recurseDeps)
        } else {
            addResolveMods(refs.filter {
                model.mods[it.slug]?.let {
                    LOGGER.warn { "Skipping mod \"${it.mod.slug}\" already installed @${it.fileId} (${it.version})!" }
                    false
                } ?: true
            }, recurseDeps)
        }
    }

    private fun addResolveMods(refs: List<ModVersionRef>, recurseDeps: Boolean) {
        var countSuccess = 0
        var countFailure = 0
        val refQueue = LinkedList(refs)
        queueLoop@ while (refQueue.isNotEmpty()) {
            val ref = refQueue.pop()
            LOGGER.info { "Resolving reference [${refQueue.size}]: $ref" }
            val closedRef = when (ref) {
                is OpenModVersionRef -> try {
                    LOGGER.info { "Retrieving latest available version..." }
                    val resolved = CurseReader.readLatestModVersion(ref.slug, model.gameVersion)
                    if (resolved == null) {
                        LOGGER.warn { "Could not find suitable ${model.gameVersion} version for \"$ref\"!" }
                        ++countFailure
                        continue@queueLoop
                    } else {
                        resolved
                    }
                } catch (e: Exception) {
                    LOGGER.warn(e) { "Could not retrieve latest version for \"$ref\"!" }
                    ++countFailure
                    continue@queueLoop
                }
                is ClosedModVersionRef -> ref
            }
            LOGGER.info { "Retrieving mod file data..." }
            try {
                CurseReader.readModVersionData(closedRef.slug, closedRef.fileId, model.gameVersion).let { mod ->
                    LOGGER.info { "Added mod $mod" }
                    model.mods[mod.mod.slug] = mod
                    if (recurseDeps) {
                        val deps = mod.dependencies.filter { it !in model.mods }
                        if (deps.isNotEmpty()) {
                            LOGGER.info { "Found ${deps.size} missing dependency(s): ${deps.joinToString(", ")}" }
                            deps.forEach { refQueue += OpenModVersionRef(it) }
                        }
                    }
                    refQueue.removeIf { it.slug == mod.mod.slug }
                    ++countSuccess
                }
            } catch (e: Exception) {
                LOGGER.warn(e) { "Could not retrieve version data for \"$closedRef\"!" }
                ++countFailure
            }
        }

        LOGGER.info { "Added $countSuccess mod(s); skipped $countFailure." }
    }

    fun write() {
        Files.writeString(workingDir.resolve(MODEL_FILE), model.serialize().toJsonString(true))
    }
}
