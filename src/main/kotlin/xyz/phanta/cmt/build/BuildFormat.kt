package xyz.phanta.cmt.build

import com.beust.klaxon.json
import xyz.phanta.cmt.LOGGER
import xyz.phanta.cmt.model.ModLoader
import xyz.phanta.cmt.workspace.ModpackWorkspace
import java.io.FilterOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

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
                            "files" to array(workspace.model.mods.map {
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
                    val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
                    val list = document.createElement("ul")
                    document.appendChild(list)
                    workspace.model.mods.forEach {
                        val entry = document.createElement("li")
                        list.appendChild(entry)
                        val link = document.createElement("a")
                        entry.appendChild(link)
                        link.setAttribute("href", "https://www.curseforge.com/minecraft/mc-mods/${it.mod.slug}")
                        link.appendChild(document.createTextNode("${it.mod.name} (by ${it.mod.author})"))
                    }
                    val transformer = TransformerFactory.newInstance().newTransformer()
                    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes")
                    transformer.transform(DOMSource(document), StreamResult(this))
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
                workspace.model.mods.forEach { out.println("  \"${it.mod.slug}\" [label=\"$it\"]") }
                LOGGER.info { "Collecting dependency edges..." }
                workspace.model.mods.forEach { mod ->
                    mod.dependencies.forEach {
                        out.println("  \"$it\" -> \"${mod.mod.slug}\"")
                    }
                }
                out.println("}")
            }
        }
    };

    abstract fun build(workspace: ModpackWorkspace, dest: Path)
}
