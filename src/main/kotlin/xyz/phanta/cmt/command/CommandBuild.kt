package xyz.phanta.cmt.command

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import xyz.phanta.cmt.LOGGER
import xyz.phanta.cmt.build.BuildFormat
import xyz.phanta.cmt.workspace.ModpackWorkspace
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class CommandBuild : CmtWorkspaceCommand("build", "Renders the modpack into an external format.", "b") {
    private val overwrite: Boolean by option("-O", "--overwrite", help = "Overwrite an existing destination.").flag()
    private val format: BuildFormat by argument().choice(enumValues<BuildFormat>().associateBy { it.key })
    private val destination: Path by argument().convert { Paths.get(it) }

    override fun runInWorkspace(workspace: ModpackWorkspace): Boolean {
        if (!overwrite && Files.exists(destination)) {
            LOGGER.error { "Destination already exists!" }
            LOGGER.error { "Pass `--overwrite` if you still want to continue." }
        } else {
            format.build(workspace, destination)
        }
        return false
    }
}
