package xyz.phanta.cmt.command

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import xyz.phanta.cmt.LOGGER
import xyz.phanta.cmt.util.ModRef
import xyz.phanta.cmt.workspace.ModpackWorkspace

class CommandRemove : CmtWorkspaceCommand("remove", "Remove Curse mods from the pack.", "r") {
    private val modsRefs: List<String> by argument().multiple(true)

    override fun runInWorkspace(workspace: ModpackWorkspace): Boolean {
        LOGGER.info { "Removing mods..." }
        var countSuccess = 0
        var countFailure = 0
        modsRefs.forEach {
            try {
                if (workspace.model.removeMod(ModRef.parse(it))) {
                    ++countSuccess
                } else {
                    LOGGER.warn { "Could not find mod matching \"$it\"!" }
                    ++countFailure
                }
            } catch (e: IllegalArgumentException) {
                LOGGER.warn(e) { "Failed to parse mod ref \"$it\"!" }
            }
        }
        LOGGER.info { "Removed $countSuccess mod(s); skipped $countFailure." }
        return true
    }
}
