package xyz.phanta.cmt.curse

import java.net.URL

class CurseUrlBuilder(private val modSlug: String, pathNodes: List<String> = listOf()) {
    private val pathNodes: MutableList<String> = pathNodes.toMutableList()
    private val queryParams: MutableMap<String, String> = mutableMapOf()

    fun path(vararg nodes: String): CurseUrlBuilder = also { pathNodes += nodes.flatMap { it.split('/') } }

    fun query(key: String, value: String): CurseUrlBuilder = also { queryParams[key] = value }

    fun build(): URL {
        val url = StringBuilder("https://www.curseforge.com/minecraft/mc-mods/$modSlug")
        pathNodes.forEach { url.append('/').append(it) }
        if (queryParams.isNotEmpty()) {
            url.append('?').append(queryParams.entries.joinToString("&") { (k, v) -> "$k=$v" })
        }
        return URL(url.toString())
    }
}
