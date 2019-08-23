package xyz.phanta.cmt.command

import com.github.ajalt.clikt.parameters.arguments.argument
import xyz.phanta.cmt.LOGGER
import xyz.phanta.cmt.workspace.ModpackWorkspace

class CommandInfo : CmtWorkspaceCommand("info", "Shows info about an added mod.", "s") {
    private val modSlug: String by argument()

    override fun runInWorkspace(workspace: ModpackWorkspace): Boolean {
        workspace.model.mods[modSlug]?.let { mod ->
            LOGGER.info { "[ ${mod.mod.name} @ ${mod.fileId} ]" }
            LOGGER.info { "        Slug: ${mod.mod.slug}" }
            LOGGER.info { "  Project ID: ${mod.mod.projectId}" }
            LOGGER.info { "     Version: ${mod.version}" }
            LOGGER.info { "Game Version: ${mod.gameVersion}" }
            LOGGER.info { "Release Date: ${mod.timestamp}" }
            LOGGER.info { "      Author: ${mod.mod.author}" }
            LOGGER.info { "     License: ${mod.mod.license}" }
            if (mod.dependencies.isNotEmpty()) {
                LOGGER.info { "[ Dependencies ]" }
                mod.dependencies.forEach { LOGGER.info { "> $it" } }
            }
        } ?: LOGGER.info { "Could not find mod matching \"$modSlug\"!" }
        return false
    }
}
