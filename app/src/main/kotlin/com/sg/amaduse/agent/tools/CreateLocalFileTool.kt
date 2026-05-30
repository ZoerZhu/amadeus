package com.sg.amaduse.agent.tools

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal class CreateLocalFileTool : AgentTool {
    override val name = "create_local_file"
    override val description =
        "创建 txt、md、csv、docx 或 xlsx 文本文件并保存到本地 Documents/Amaduse 文件夹。必须提供文件名、格式和内容。"
    override val parameters = JSONObject()
        .put("type", "object")
        .put(
            "properties", JSONObject()
                .put("file_name", JSONObject().put("type", "string").put("description", "文件名，可带或不带扩展名。"))
                .put(
                    "format",
                    JSONObject()
                        .put("type", "string")
                        .put("enum", JSONArray(listOf("txt", "md", "csv", "docx", "xlsx")))
                        .put("description", "目标文件格式。"),
                )
                .put("content", JSONObject().put("type", "string").put("description", "文件正文。xlsx 可传 CSV/TSV 表格文本或普通文本。"))
                .put("sheet_name", JSONObject().put("type", "string").put("description", "xlsx 工作表名称，默认 Sheet1。"))
                .put("rows", JSONObject().put("type", "array").put("description", "可选 xlsx 表格行数据，二维数组；提供 rows 时优先使用 rows。")),
        )
        .put("required", JSONArray(listOf("file_name", "format", "content")))

    override suspend fun execute(args: JSONObject, context: AgentToolContext): ToolResult = withContext(Dispatchers.IO) {
        val fileNameInput = args.optString("file_name").trim()
        val format = args.optString("format").trim().lowercase(Locale.ROOT)
        val content = args.optString("content")
        val missing = buildList {
            if (fileNameInput.isBlank()) add("文件名")
            if (format.isBlank()) add("文件格式（txt/md/csv/docx/xlsx）")
            if (content.isBlank() && args.optJSONArray("rows") == null) add("文件内容")
        }
        if (missing.isNotEmpty()) {
            return@withContext ToolResult(false, "本地文件创建信息不完整，暂不能创建。请向用户追问：${missing.joinToString("、")}。")
        }
        if (format !in supportedFormats) {
            return@withContext ToolResult(false, "本地文件创建失败：不支持的格式「$format」。支持 txt、md、csv、docx、xlsx。")
        }

        val safeFileName = sanitizeFileName(fileNameInput, format)
        val bytes = runCatching {
            when (format) {
                "txt", "md", "csv" -> content.toByteArray(Charsets.UTF_8)
                "docx" -> buildDocx(content)
                "xlsx" -> buildXlsx(
                    rows = parseRows(args.optJSONArray("rows"), content),
                    sheetName = args.optString("sheet_name").ifBlank { "Sheet1" },
                )
                else -> error("Unsupported format")
            }
        }.getOrElse { error ->
            return@withContext ToolResult(false, "本地文件创建失败：${error.message ?: error.javaClass.simpleName}")
        }

        val result = runCatching {
            saveToLocalDocuments(
                context = context,
                fileName = safeFileName,
                mimeType = mimeType(format),
                bytes = bytes,
            )
        }.getOrElse { error ->
            return@withContext ToolResult(false, "本地文件保存失败：${error.message ?: error.javaClass.simpleName}")
        }

        ToolResult(true, "文件已创建：${result.displayPath}")
    }

    private fun saveToLocalDocuments(
        context: AgentToolContext,
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): SaveResult {
        val resolver = context.appContext.contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/Amaduse")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)
                ?: error("无法创建 MediaStore 文件")
            try {
                resolver.openOutputStream(uri)?.use { it.write(bytes) }
                    ?: error("无法打开文件输出流")
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                return SaveResult("Documents/Amaduse/$fileName")
            } catch (error: Exception) {
                resolver.delete(uri, null, null)
                throw error
            }
        }

        val directory = File(
            context.appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "Amaduse",
        ).apply {
            if (!exists()) {
                mkdirs()
            }
        }
        val file = uniqueFile(directory, fileName)
        file.writeBytes(bytes)
        return SaveResult(file.absolutePath)
    }

    private fun sanitizeFileName(input: String, format: String): String {
        val cleaned = input
            .replace(Regex("""[\\/:*?"<>|]"""), "_")
            .trim()
            .ifBlank { "amaduse-${UUID.randomUUID()}" }
        return if (cleaned.endsWith(".$format", ignoreCase = true)) {
            cleaned
        } else {
            "${cleaned.substringBeforeLast('.', cleaned)}.$format"
        }
    }

    private fun uniqueFile(directory: File, fileName: String): File {
        var candidate = File(directory, fileName)
        if (!candidate.exists()) {
            return candidate
        }
        val base = fileName.substringBeforeLast('.', fileName)
        val ext = fileName.substringAfterLast('.', "")
        var index = 1
        while (candidate.exists()) {
            candidate = File(directory, "$base-$index${if (ext.isBlank()) "" else ".$ext"}")
            index++
        }
        return candidate
    }

    private fun mimeType(format: String): String {
        return when (format) {
            "txt" -> "text/plain"
            "md" -> "text/markdown"
            "csv" -> "text/csv"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            else -> "application/octet-stream"
        }
    }

    private fun buildDocx(content: String): ByteArray {
        val paragraphs = content
            .lineSequence()
            .map { line ->
                "<w:p><w:r><w:t xml:space=\"preserve\">${escapeXml(line)}</w:t></w:r></w:p>"
            }
            .joinToString("")
            .ifBlank { "<w:p/>" }
        val documentXml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:body>$paragraphs<w:sectPr><w:pgSz w:w="11906" w:h="16838"/><w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440"/></w:sectPr></w:body></w:document>
        """.trimIndent()
        return zip(
            "[Content_Types].xml" to """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/></Types>
            """.trimIndent(),
            "_rels/.rels" to """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/></Relationships>
            """.trimIndent(),
            "word/document.xml" to documentXml,
        )
    }

    private fun buildXlsx(rows: List<List<String>>, sheetName: String): ByteArray {
        val safeSheetName = escapeXml(sheetName.take(31).ifBlank { "Sheet1" })
        val sheetRows = rows.ifEmpty { listOf(listOf("")) }
        val sheetData = sheetRows.mapIndexed { rowIndex, row ->
            val cells = row.mapIndexed { columnIndex, value ->
                val cellRef = "${columnName(columnIndex + 1)}${rowIndex + 1}"
                "<c r=\"$cellRef\" t=\"inlineStr\"><is><t xml:space=\"preserve\">${escapeXml(value)}</t></is></c>"
            }.joinToString("")
            "<row r=\"${rowIndex + 1}\">$cells</row>"
        }.joinToString("")

        return zip(
            "[Content_Types].xml" to """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>
            """.trimIndent(),
            "_rels/.rels" to """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>
            """.trimIndent(),
            "xl/_rels/workbook.xml.rels" to """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>
            """.trimIndent(),
            "xl/workbook.xml" to """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="$safeSheetName" sheetId="1" r:id="rId1"/></sheets></workbook>
            """.trimIndent(),
            "xl/worksheets/sheet1.xml" to """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>$sheetData</sheetData></worksheet>
            """.trimIndent(),
        )
    }

    private fun parseRows(rowsJson: JSONArray?, content: String): List<List<String>> {
        if (rowsJson != null) {
            return List(rowsJson.length()) { rowIndex ->
                val row = rowsJson.optJSONArray(rowIndex)
                if (row != null) {
                    List(row.length()) { columnIndex -> row.optString(columnIndex) }
                } else {
                    listOf(rowsJson.optString(rowIndex))
                }
            }
        }
        return content
            .lineSequence()
            .filter { it.isNotBlank() }
            .map { line ->
                when {
                    '\t' in line -> line.split('\t')
                    ',' in line -> parseCsvLine(line)
                    else -> listOf(line)
                }
            }
            .toList()
    }

    private fun parseCsvLine(line: String): List<String> {
        val values = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var index = 0
        while (index < line.length) {
            val char = line[index]
            when {
                char == '"' && inQuotes && index + 1 < line.length && line[index + 1] == '"' -> {
                    current.append('"')
                    index++
                }
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    values += current.toString()
                    current.clear()
                }
                else -> current.append(char)
            }
            index++
        }
        values += current.toString()
        return values
    }

    private fun columnName(index: Int): String {
        var value = index
        val result = StringBuilder()
        while (value > 0) {
            value--
            result.insert(0, ('A'.code + value % 26).toChar())
            value /= 26
        }
        return result.toString()
    }

    private fun zip(vararg entries: Pair<String, String>): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            entries.forEach { (name, text) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(text.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
        }
        return output.toByteArray()
    }

    private fun escapeXml(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private data class SaveResult(
        val displayPath: String,
    )

    private companion object {
        val supportedFormats = setOf("txt", "md", "csv", "docx", "xlsx")
    }
}
