package xyz.phanta.cmt.command

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.types.choice
import xyz.phanta.cmt.LOGGER
import xyz.phanta.cmt.model.ModpackModel
import xyz.phanta.cmt.workspace.ModpackWorkspace
import kotlin.reflect.KMutableProperty1

private enum class PackProperty(val key: String, val prop: KMutableProperty1<ModpackModel, String>) {
    NAME("name", ModpackModel::name),
    AUTHOR("author", ModpackModel::author),
    VERSION("version", ModpackModel::version)
}

class CommandProp : CmtWorkspaceCommand("prop", "Retrieve and modify properties of the pack.", "p") {
    private val property: PackProperty by argument().choice(enumValues<PackProperty>().associateBy { it.key })
    private val value: String? by argument().optional()

    override fun runInWorkspace(workspace: ModpackWorkspace): Boolean {
        value?.let {
            property.prop.set(workspace.model, it)
            LOGGER.info { "Property updated to \"$it\"." }
            return true
        }
        LOGGER.info { "${property.key} = \"${property.prop.get(workspace.model)}\"" }
        return false
    }
}
