package xyz.phanta.cmt.command

import xyz.phanta.cmt.util.OpenModVersionRef
import xyz.phanta.cmt.workspace.ModpackWorkspace

class CommandUpdate : CmtWorkspaceCommand("update", "Updates all Curse mods in the pack.", "u") {
    override fun runInWorkspace(workspace: ModpackWorkspace): Boolean {
        workspace.addResolveMods(workspace.model.mods.values.map { OpenModVersionRef(it.mod.slug) },
            recurseDeps = false,
            update = true
        )
        return true
    }
}
