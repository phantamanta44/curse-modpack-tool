package xyz.phanta.cmt.curse

import java.net.URL

class ApiUrlBuilder(vararg pathNodes: String) {
    private val pathNodes: MutableList<String> = pathNodes.toMutableList()
    private val queryParams: MutableMap<String, String> = mutableMapOf()

    fun path(vararg nodes: String): ApiUrlBuilder = also { pathNodes += nodes.flatMap { it.split('/') } }

    fun query(key: String, value: String): ApiUrlBuilder = also { queryParams[key] = value }

    fun build(): URL {
        val url = StringBuilder("https://addons-ecs.forgesvc.net/api/v2")
        pathNodes.forEach { url.append('/').append(it) }
        if (queryParams.isNotEmpty()) {
            url.append('?').append(queryParams.entries.joinToString("&") { (k, v) -> "$k=$v" })
        }
        return URL(url.toString())
    }
}
