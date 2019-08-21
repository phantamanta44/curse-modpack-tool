package xyz.phanta.cmt.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import xyz.phanta.cmt.LOGGER
import xyz.phanta.cmt.workspace.ModpackWorkspace
import java.nio.file.Paths

abstract class CmtCommand(name: String, help: String, vararg val aliasSet: String) :
    CliktCommand(name = name, help = help)

abstract class CmtWorkspaceCommand(name: String, help: String, vararg aliasSet: String) :
    CmtCommand(name, help, *aliasSet) {
    override fun run() {
        LOGGER.info { "Loading workspace..." }
        val workspace = ModpackWorkspace.load(Paths.get(".").toAbsolutePath())
        if (runInWorkspace(workspace)) {
            LOGGER.info { "Serializing workspace..." }
            workspace.write()
        }
    }

    abstract fun runInWorkspace(workspace: ModpackWorkspace): Boolean
}

abstract class CmtDelegatingCommand(
    name: String,
    help: String,
    private val subCommandSet: List<CmtCommand>,
    vararg aliasSet: String
) : CmtCommand(name, help, *aliasSet) {
    init {
        subcommands(subCommandSet)
    }

    override fun run() = Unit

    override fun aliases(): Map<String, List<String>> =
        subCommandSet.flatMap { it.aliasSet.map { alias -> alias to listOf(it.commandName) } }.associate { it }
}
