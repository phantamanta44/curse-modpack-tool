package xyz.phanta.cmt.command

import xyz.phanta.cmt.LOGGER
import xyz.phanta.cmt.model.ModVersion
import xyz.phanta.cmt.util.ModRef
import xyz.phanta.cmt.workspace.ModpackWorkspace

class CommandDepCheck : CmtWorkspaceCommand("depcheck", "Searches for missing mod dependencies.") {
    override fun runInWorkspace(workspace: ModpackWorkspace): Boolean {
        val missing = mutableMapOf<ModRef, MutableList<ModVersion>>()
        workspace.model.mods.forEach { mod ->
            mod.dependencies.forEach {
                if (workspace.model.getMod(it) == null) {
                    missing.computeIfAbsent(it) { mutableListOf() } += mod
                }
            }
        }
        if (missing.isEmpty()) {
            LOGGER.info { "No missing dependencies." }
        } else {
            LOGGER.info { "Found ${missing.size} missing dependency(s)." }
            missing.forEach { (dep, mods) ->
                LOGGER.info { "\"$dep\" is required by:" }
                mods.forEach {
                    LOGGER.info { "> $it" }
                }
            }
        }
        return false
    }
}
