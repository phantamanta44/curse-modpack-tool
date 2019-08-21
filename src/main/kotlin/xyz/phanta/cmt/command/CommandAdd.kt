package xyz.phanta.cmt.command

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import xyz.phanta.cmt.util.ModVersionRef
import xyz.phanta.cmt.workspace.ModpackWorkspace

class CommandAdd : CmtWorkspaceCommand("add", "Add Curse mods to the pack.", "i") {
    private val noDeps: Boolean
            by option("-N", "--no-deps", help = "Don't add the dependencies of selected mods.").flag()
    private val update: Boolean
            by option("-u", "--update", help = "Attempt to re-add mods that are already present.").flag()
    private val modVersionRefs: List<ModVersionRef> by argument().convert { ModVersionRef.parse(it) }.multiple(true)

    override fun runInWorkspace(workspace: ModpackWorkspace): Boolean {
        workspace.addResolveMods(modVersionRefs, !noDeps, update)
        return true
    }
}
