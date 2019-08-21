package xyz.phanta.cmt.curse

import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Collector
import org.jsoup.select.Elements
import org.jsoup.select.Evaluator
import xyz.phanta.cmt.model.GameVersion
import xyz.phanta.cmt.model.Mod
import xyz.phanta.cmt.model.ModVersion
import xyz.phanta.cmt.util.ClosedModVersionRef
import xyz.phanta.cmt.util.License
import java.net.URL
import java.time.Instant

object CurseReader {
    fun readLatestModVersion(modSlug: String, gameVersion: GameVersion): ClosedModVersionRef? =
        retrieveProject(modSlug).getElementsByClass("cf-recentfiles")
            .firstOrNull {
                GameVersion.parseGracefully(it.previousElementSibling().text().substring(10))
                    ?.let(gameVersion::isCompatibleWith) ?: false
            }
            ?.let { tag ->
                val pathNodes = tag.getElementsByTag("a").first { it.hasClass("overflow-tip") }.attr("href")
                    .trim('/').split('/')
                ClosedModVersionRef(pathNodes[pathNodes.lastIndex - 2], pathNodes.last().toLong())
            }

    fun readModVersionData(modSlug: String, fileVersion: Long, defaultVersion: GameVersion): ModVersion {
        val page = retrieveFile(modSlug, fileVersion)
        val depsPage = retrieveDeps(modSlug)
        val versionTitle = page.getElementsByClass("text-primary-500").first { it.tagName() == "h3" }
        val urlPath = versionTitle.parent().attr("href").trim('/').split('/')
        val sidebar = page.getElementsByTag("aside").first()
        val projProps = sidebar.getElementsWithExactOwnText("About Project").first().parent().nextElementSibling()
            .children().associate { it.child(0).text() to it.child(1) }
        val article = page.getElementsByTag("article").first()
        return ModVersion(
            Mod(
                urlPath[urlPath.lastIndex - 2],
                page.getElementsByClass("game-header").first().getElementsByClass("text-lg").first().text(),
                projProps.getValue("Project ID").text().toLong(),
                sidebar.getElementsWithExactOwnText("Owner").first().previousElementSibling().child(0).text(),
                License.parse(projProps.getValue("License").text())
            ),
            versionTitle.text(),
            urlPath.last().toLong(),
            article.child(2).children().flatMap {
                it.children().let { v -> v.subList(1, v.size) }.map { v -> v.text() }
            }.mapNotNull { GameVersion.parseGracefully(it) }.firstOrNull() ?: defaultVersion,
            Instant.ofEpochMilli(
                article.child(1).getElementsWithExactOwnText("Uploaded").first()
                    .nextElementSibling().child(0).attr("data-epoch").toLong()
            ),
            depsPage.getElementsByClass("project-listing-row").map {
                it.getElementsByClass("project-avatar").first().child(0).attr("href").trim('/').split('/').last()
            }.toSet()
        )
    }

    private fun Element.getElementsWithExactOwnText(text: String): Elements =
        Collector.collect(object : Evaluator() {
            override fun matches(root: Element, element: Element): Boolean = element.ownText() == text
        }, this)

    private fun retrieveProject(modSlug: String): Document = CurseUrlBuilder(modSlug).build().retrieve()

    private fun retrieveFile(modSlug: String, fileId: Long): Document =
        CurseUrlBuilder(modSlug).path("files", fileId.toString()).build().retrieve()

    private fun retrieveDeps(modSlug: String, req: Boolean = true): Document {
        val urlBuilder = CurseUrlBuilder(modSlug).path("relations", "dependencies")
        if (req) {
            urlBuilder.query("filter-related-dependencies", "3")
        }
        return urlBuilder.build().retrieve()
    }

    private fun URL.retrieve(): Document = try {
        Jsoup.parse(this, 3000)
    } catch (e: HttpStatusException) {
        throw IllegalStateException("Encountered HTTP ${e.statusCode} at url: ${e.url}", e)
    } catch (e: Exception) {
        throw IllegalStateException("Failed to read url: $this", e)
    }
}
