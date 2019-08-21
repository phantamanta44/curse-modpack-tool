package xyz.phanta.cmt.command

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import xyz.phanta.cmt.util.ModVersionRef
import xyz.phanta.cmt.workspace.ModpackWorkspace
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class CommandAddAll : CmtWorkspaceCommand("addall", "Adds a list of Curse mods from a file.", "I") {
    private val noDeps: Boolean
            by option("-N", "--no-deps", help = "Don't add the dependencies of selected mods.").flag()
    private val update: Boolean
            by option("-u", "--update", help = "Attempt to re-add mods that are already present.").flag()
    private val modListFile: Path by argument().convert { Paths.get(it) }

    override fun runInWorkspace(workspace: ModpackWorkspace): Boolean {
        workspace.addResolveMods(
            Files.readAllLines(modListFile).filter { it.isNotBlank() }.map { ModVersionRef.parse(it.trim()) },
            !noDeps,
            update
        )
        return true
    }
}
