package xyz.phanta.cmt

import mu.KLogger
import mu.KotlinLogging
import xyz.phanta.cmt.command.*

val LOGGER: KLogger = KotlinLogging.logger("CMT")

private class CmtMain : CmtDelegatingCommand(
    "cmt", "Manages a modpack using CurseForge-hosted mods.", listOf(
        CommandAdd(),
        CommandAddAll(),
        CommandBuild(),
        CommandDepCheck(),
        CommandInit(),
        CommandList(),
        CommandLoader(),
        CommandProp(),
        CommandRemove()
    )
)

fun main(args: Array<String>) = CmtMain().main(args)
