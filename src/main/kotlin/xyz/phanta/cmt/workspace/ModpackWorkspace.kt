package xyz.phanta.cmt.workspace

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import xyz.phanta.cmt.LOGGER
import xyz.phanta.cmt.curse.CurseReader
import xyz.phanta.cmt.curse.parseMod
import xyz.phanta.cmt.curse.parseVersion
import xyz.phanta.cmt.model.GameVersion
import xyz.phanta.cmt.model.ModpackModel
import xyz.phanta.cmt.util.*
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
                model.getMod(it.modRef)?.let {
                    LOGGER.warn { "Skipping mod \"${it.mod.slug}\" already installed @${it.fileId} (${it.version})!" }
                    false
                } ?: true
            }, recurseDeps)
        }
    }

    private fun addResolveMods(refs: List<ModVersionRef>, recurseDeps: Boolean) {
        var countSuccess = 0
        var countFailure = 0

        val dupeCheck = mutableSetOf<ModRef>()
        for (ref in refs) {
            require(ref.modRef !in dupeCheck) { "Duplicate installation requests for \"${ref.modRef}\"!" }
            dupeCheck += ref.modRef
        }

        val refQueue = LinkedList(refs)
        val visitedIds = mutableSetOf<Long>()
        val visitedSlugs = mutableSetOf<String>()
        queueLoop@ while (refQueue.isNotEmpty()) {
            val ref = refQueue.pop()
            if (ref.modRef.let {
                    when (it) {
                        is ModProjectId -> it.id in visitedIds
                        is ModSlug -> it.slug in visitedSlugs
                    }
                }) {
                continue
            }

            LOGGER.info { "Resolving reference [${refQueue.size}]: $ref" }
            val (mod, fileId) = when (ref) {
                is OpenModVersionRef -> try {
                    LOGGER.info { "Retrieving mod details..." }
                    val modDto = CurseReader.retrieveMod(ref.modRef)
                    val latestFileDto = modDto.gameVersionLatestFiles.firstOrNull { fileDto ->
                        GameVersion.parseGracefully(fileDto.gameVersion)?.let {
                            model.gameVersion.isCompatibleWith(it)
                        } ?: false
                    }
                    if (latestFileDto != null) {
                        modDto.parseMod() to latestFileDto.projectFileId
                    } else {
                        LOGGER.warn { "Could not find suitable ${model.gameVersion} version for \"$ref\"!" }
                        ++countFailure
                        continue@queueLoop
                    }
                } catch (e: Exception) {
                    LOGGER.warn(e) { "Could not retrieve mod details for \"$ref\"!" }
                    ++countFailure
                    continue@queueLoop
                }
                is ClosedModVersionRef -> model.getMod(ref.modRef)?.let { it.mod to ref.fileId } ?: try {
                    LOGGER.info { "Retrieving mod details..." }
                    CurseReader.retrieveMod(ref.modRef).parseMod() to ref.fileId
                } catch (e: Exception) {
                    LOGGER.warn(e) { "Could not retrieve mod details for \"$ref\"!" }
                    ++countFailure
                    continue@queueLoop
                }
            }
            visitedIds += mod.projectId
            visitedSlugs += mod.slug

            LOGGER.info { "Retrieving mod file data..." }
            try {
                CurseReader.retrieveModFile(mod.projectId, fileId).let { fileDto ->
                    val modVersion = fileDto.parseVersion(mod, model.gameVersion)
                    LOGGER.info { "Added mod $modVersion" }
                    model.putMod(modVersion)
                    if (recurseDeps) {
                        val deps = modVersion.dependencies.filter { model.getMod(it) == null }
                        if (deps.isNotEmpty()) {
                            LOGGER.info { "Found ${deps.size} missing dependency(s): ${deps.joinToString(", ")}" }
                            deps.forEach { dep ->
                                refQueue += OpenModVersionRef(dep)
                            }
                        }
                    }
                    ++countSuccess
                }
            } catch (e: Exception) {
                LOGGER.warn(e) { "Could not retrieve version data for \"${mod.slug}@$fileId\"!" }
                ++countFailure
            }
        }

        LOGGER.info { "Added $countSuccess mod(s); skipped $countFailure." }
    }

    fun write() {
        Files.writeString(workingDir.resolve(MODEL_FILE), model.serialize().toJsonString(true))
    }
}
