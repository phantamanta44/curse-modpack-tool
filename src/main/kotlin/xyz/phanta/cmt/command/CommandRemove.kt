package xyz.phanta.cmt.command

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import xyz.phanta.cmt.LOGGER
import xyz.phanta.cmt.workspace.ModpackWorkspace

class CommandRemove : CmtWorkspaceCommand("remove", "Remove Curse mods from the pack.", "r") {
    private val modSlugs: List<String> by argument().multiple(true)

    override fun runInWorkspace(workspace: ModpackWorkspace): Boolean {
        LOGGER.info { "Removing mods..." }
        var countSuccess = 0
        var countFailure = 0
        modSlugs.forEach {
            if (workspace.model.mods.remove(it) == null) {
                LOGGER.warn { "Could not find mod matching \"$it\"!" }
                ++countFailure
            } else {
                ++countSuccess
            }
        }
        LOGGER.info { "Removed $countSuccess mod(s); skipped $countFailure." }
        return true
    }
}
