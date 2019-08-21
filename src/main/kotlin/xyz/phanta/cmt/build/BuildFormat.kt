package xyz.phanta.cmt.build

import com.beust.klaxon.json
import org.jsoup.nodes.Element
import xyz.phanta.cmt.LOGGER
import xyz.phanta.cmt.model.ModLoader
import xyz.phanta.cmt.workspace.ModpackWorkspace
import java.io.FilterOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private fun ZipOutputStream.writeEntry(name: String, body: FilterOutputStream.() -> Unit) {
    putNextEntry(ZipEntry(name))
    body(this)
    closeEntry()
}

enum class BuildFormat(val key: String) {
    CURSE("curse") {
        override fun build(workspace: ModpackWorkspace, dest: Path) {
            ZipOutputStream(Files.newOutputStream(dest).buffered()).use { out ->
                LOGGER.info { "Writing modpack manifest..." }
                out.writeEntry("manifest.json") {
                    write(json {
                        obj(
                            "minecraft" to obj(
                                "version" to workspace.model.gameVersion.displayName,
                                "modLoaders" to array(workspace.model.modLoaders.values.map {
                                    obj(
                                        "id" to "${it.loader.id}-${it.version}",
                                        "primary" to (it.loader == ModLoader.FORGE) // TODO other primary loaders?
                                    )
                                }.toList())
                            ),
                            "manifestType" to "minecraftModpack",
                            "manifestVersion" to 1,
                            "name" to workspace.model.name,
                            "version" to workspace.model.version,
                            "author" to workspace.model.author,
                            "overrides" to ".minecraft",
                            "files" to array(workspace.model.mods.values.map {
                                obj(
                                    "projectID" to it.mod.projectId,
                                    "fileID" to it.fileId,
                                    "required" to true // TODO optional mods?
                                )
                            }.toList())
                        )
                    }.toJsonString(true).toByteArray(Charsets.UTF_8))
                }
                LOGGER.info { "Writing mod list..." }
                out.writeEntry("modlist.html") {
                    val list = Element("ul")
                    workspace.model.mods.values.forEach {
                        val entry = list.appendElement("li").appendElement("a")
                        entry.attr("href", "https://www.curseforge.com/minecraft/mc-mods/${it.mod.slug}")
                        entry.text("${it.mod.name} (by ${it.mod.author})")
                    }
                    write(list.outerHtml().toByteArray(Charsets.UTF_8))
                }
                LOGGER.info { "Copying override assets..." }
                Files.walk(workspace.mcDir)
                    .filter { Files.isRegularFile(it) }
                    .forEach { path ->
                        out.writeEntry(workspace.workingDir.relativize(path).toString()) {
                            Files.newInputStream(path).buffered().use { strIn ->
                                strIn.transferTo(this)
                            }
                        }
                    }
            }
        }
    },
    DOT("dot") {
        override fun build(workspace: ModpackWorkspace, dest: Path) {
            PrintStream(Files.newOutputStream(dest).buffered()).use { out ->
                out.println("digraph modpack {")
                LOGGER.info { "Collecting mod nodes..." }
                workspace.model.mods.values.forEach { out.println("  ${it.mod.slug} [label=\"${it.version}\"]") }
                LOGGER.info { "Collecting dependency edges..." }
                workspace.model.mods.values.forEach { mod ->
                    mod.dependencies.forEach {
                        out.println("  $it -> ${mod.mod.slug}")
                    }
                }
                out.println("}")
            }
        }
    };

    abstract fun build(workspace: ModpackWorkspace, dest: Path)
}
