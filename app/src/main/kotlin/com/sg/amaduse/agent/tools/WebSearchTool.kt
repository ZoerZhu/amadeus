package com.sg.amaduse.agent.tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder

internal class WebSearchTool : AgentTool {
    override val name = "web_search"
    override val description = "联网搜索公开网页并返回搜索结果。适合查询最新信息、新闻、事实核对、网页资料。"
    override val parameters = JSONObject()
        .put("type", "object")
        .put(
            "properties", JSONObject()
                .put("query", JSONObject().put("type", "string").put("description", "搜索关键词或问题。"))
                .put("max_results", JSONObject().put("type", "integer").put("description", "返回结果数量，1-8，默认 5。")),
        )
        .put("required", JSONArray(listOf("query")))

    override suspend fun execute(args: JSONObject, context: AgentToolContext): ToolResult = withContext(Dispatchers.IO) {
        val query = args.optString("query").trim()
        if (query.isBlank()) {
            return@withContext ToolResult(false, "联网搜索失败：query 不能为空。请向用户追问要搜索什么。")
        }
        val maxResults = args.optInt("max_results", 5).coerceIn(1, 8)

        val results = runCatching {
            searchDuckDuckGoHtml(query, maxResults)
        }.getOrElse { error ->
            return@withContext ToolResult(false, "联网搜索失败：${error.message ?: error.javaClass.simpleName}")
        }

        if (results.isEmpty()) {
            return@withContext ToolResult(false, "联网搜索没有找到可用结果：$query")
        }

        val output = buildString {
            appendLine("联网搜索结果：$query")
            results.forEachIndexed { index, result ->
                appendLine("${index + 1}. ${result.title}")
                appendLine("   ${result.url}")
                if (result.snippet.isNotBlank()) {
                    appendLine("   ${result.snippet}")
                }
            }
        }.trim()
        ToolResult(true, output)
    }

    private fun searchDuckDuckGoHtml(
        query: String,
        maxResults: Int,
    ): List<SearchResult> {
        val encoded = URLEncoder.encode(query, Charsets.UTF_8.name())
        val html = getText("https://html.duckduckgo.com/html/?q=$encoded")
        return parseDuckDuckGoHtml(html)
            .distinctBy { it.url }
            .take(maxResults)
    }

    private fun getText(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12_000
            readTimeout = 18_000
            setRequestProperty("Accept", "text/html,application/xhtml+xml")
            setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Android) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36 Amaduse/1.0",
            )
        }
        return try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                error("HTTP $responseCode ${errorText.take(160)}")
            }
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseDuckDuckGoHtml(html: String): List<SearchResult> {
        val itemRegex = Regex(
            """(?is)<div[^>]*class="[^"]*result[^"]*"[^>]*>.*?<a[^>]*class="[^"]*result__a[^"]*"[^>]*href="([^"]+)"[^>]*>(.*?)</a>(.*?)(?=<div[^>]*class="[^"]*result[^"]*"|</body>)""",
        )
        val snippetRegex = Regex("""(?is)<a[^>]*class="[^"]*result__snippet[^"]*"[^>]*>(.*?)</a>|<div[^>]*class="[^"]*result__snippet[^"]*"[^>]*>(.*?)</div>""")
        val results = mutableListOf<SearchResult>()
        for (match in itemRegex.findAll(html)) {
            val url = normalizeDuckDuckGoUrl(htmlDecode(match.groupValues[1]))
            val title = cleanHtml(match.groupValues[2])
            val block = match.groupValues[3]
            val snippetMatch = snippetRegex.find(block)
            val snippet = snippetMatch?.groupValues
                ?.drop(1)
                ?.firstOrNull { it.isNotBlank() }
                ?.let { cleanHtml(it) }
                .orEmpty()
            if (title.isNotBlank() && url.startsWith("http")) {
                results += SearchResult(title = title, url = url, snippet = snippet)
            }
        }
        return results
    }

    private fun normalizeDuckDuckGoUrl(rawUrl: String): String {
        val url = when {
            rawUrl.startsWith("//") -> "https:$rawUrl"
            rawUrl.startsWith("/") -> "https://duckduckgo.com$rawUrl"
            else -> rawUrl
        }
        val marker = "uddg="
        val index = url.indexOf(marker)
        if (index < 0) {
            return url
        }
        val encodedTarget = url.substring(index + marker.length).substringBefore('&')
        return runCatching { URLDecoder.decode(encodedTarget, Charsets.UTF_8.name()) }.getOrDefault(url)
    }

    private fun cleanHtml(value: String): String {
        return htmlDecode(
            value
                .replace(Regex("(?is)<script.*?</script>"), "")
                .replace(Regex("(?is)<style.*?</style>"), "")
                .replace(Regex("(?is)<[^>]+>"), " ")
                .replace(Regex("\\s+"), " ")
                .trim(),
        )
    }

    private fun htmlDecode(value: String): String {
        return value
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#x27;", "'")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
    }

    private data class SearchResult(
        val title: String,
        val url: String,
        val snippet: String,
    )
}
