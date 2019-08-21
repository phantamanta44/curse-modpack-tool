package xyz.phanta.cmt.command

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import xyz.phanta.cmt.LOGGER
import xyz.phanta.cmt.model.GameVersion
import xyz.phanta.cmt.model.ModpackModel
import xyz.phanta.cmt.workspace.ModpackWorkspace
import java.nio.file.Path
import java.nio.file.Paths

class CommandInit : CmtCommand("init", "Initializes an empty modpack in the current directory.") {
    private val force: Boolean by option("-f", "--force", help = "Force empty modpack initialization.").flag()
    private val gameVersion: GameVersion by argument().convert { GameVersion.parse(it) }
    private val name: String by argument().default("Modpack Name")
    private val author: String by argument().default("Modpack Author")
    private val version: String by argument().default("1.0.0")

    override fun run() {
        val workingDir = Paths.get(".").toAbsolutePath()
        ModpackWorkspace.loadGracefully(workingDir)?.let {
            if (force) {
                LOGGER.warn { "Workspace already exists for pack \"${it.model.name}\"! Continuing anyways..." }
                doInit(workingDir)
            } else {
                LOGGER.error { "Workspace already exists for pack \"${it.model.name}\"!" }
                LOGGER.error { "Pass `--force` if you still want to continue." }
            }
        } ?: doInit(workingDir)
    }

    private fun doInit(workingDir: Path) {
        ModpackWorkspace(workingDir, ModpackModel(gameVersion, name, author, version)).write()
        LOGGER.info { "Workspace initialized." }
    }
}
