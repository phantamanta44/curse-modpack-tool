package xyz.phanta.cmt.command

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import xyz.phanta.cmt.LOGGER
import xyz.phanta.cmt.util.ModProjectId
import xyz.phanta.cmt.util.ModSlug
import xyz.phanta.cmt.workspace.ModpackWorkspace

class CommandList : CmtWorkspaceCommand("list", "Lists mods that have been added to the pack.", "l") {
    private val leafOnly: Boolean
            by option("-L", "--leaf-only", help = "Only list mods that no other mods depend on.").flag()
    private val query: String? by argument().optional()

    override fun runInWorkspace(workspace: ModpackWorkspace): Boolean {
        val allMods = if (leafOnly) {
            val depIds = mutableSetOf<Long>()
            val depSlugs = mutableSetOf<String>()
            workspace.model.mods.forEach { mod ->
                mod.dependencies.forEach {
                    when (it) {
                        is ModProjectId -> depIds += it.id
                        is ModSlug -> depSlugs += it.slug
                    }
                }
            }
            workspace.model.mods.filter { it.mod.projectId !in depIds && it.mod.slug !in depSlugs }
        } else {
            workspace.model.mods
        }
        val mods = query?.let { allMods.filter { mod -> it in mod.mod.slug } } ?: allMods
        LOGGER.info { "Found ${mods.size} matching mod(s)." }
        mods.toList().sortedBy { it.mod.slug }.forEach {
            LOGGER.info { "${it.mod.slug} (${it.mod.name}) @${it.fileId} (${it.version})" }
        }
        return false
    }
}
