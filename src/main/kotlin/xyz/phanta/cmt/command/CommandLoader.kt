package xyz.phanta.cmt.command

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import xyz.phanta.cmt.LOGGER
import xyz.phanta.cmt.model.ModLoader
import xyz.phanta.cmt.model.ModLoaderVersion
import xyz.phanta.cmt.workspace.ModpackWorkspace

class CommandLoader : CmtDelegatingCommand(
    "loader",
    "Add and remove mod loaders from the pack.",
    listOf(CommandLoaderAdd(), CommandLoaderRemove(), CommandLoaderList()),
    "ml"
)

private class CommandLoaderAdd : CmtWorkspaceCommand("add", "Add a mod loader.", "i") {
    private val update: Boolean
            by option("-u", "--update", help = "Attempt to re-add loaders that are already present.").flag()
    val loader: ModLoader by argument().choice(enumValues<ModLoader>().associateBy { it.id })
    val version: String by argument()

    override fun runInWorkspace(workspace: ModpackWorkspace): Boolean =
        if (update || loader !in workspace.model.modLoaders) {
            ModLoaderVersion(loader, version).let {
                workspace.model.modLoaders[loader] = it
                LOGGER.info { "Added mod loader $it." }
            }
            true
        } else {
            LOGGER.error { "Loader \"${workspace.model.modLoaders[loader]}\" already added!" }
            LOGGER.error { "Pass `--update` if you still want to continue." }
            false
        }
}

private class CommandLoaderRemove : CmtWorkspaceCommand("remove", "Remove a mod loader.", "r") {
    val loader: ModLoader by argument().choice(enumValues<ModLoader>().associateBy { it.id })

    override fun runInWorkspace(workspace: ModpackWorkspace): Boolean =
        workspace.model.modLoaders.remove(loader)?.let {
            LOGGER.info { "Removed mod loader $it." }
            true
        } ?: run {
            LOGGER.error { "Could not find loader matching \"$loader\"!" }
            false
        }
}

private class CommandLoaderList : CmtWorkspaceCommand("list", "Lists mod loaders.", "l") {
    override fun runInWorkspace(workspace: ModpackWorkspace): Boolean {
        LOGGER.info { "Found ${workspace.model.modLoaders.size} mod loader(s)." }
        workspace.model.modLoaders.values.toList().sortedBy { it.loader }.forEach {
            LOGGER.info { "${it.loader.id} @ ${it.version}" }
        }
        return false
    }
}
