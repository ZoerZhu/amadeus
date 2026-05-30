package com.sg.amaduse.agent.tools

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.provider.CalendarContract
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal class AddMemoTool : AgentTool {
    override val name = "add_memo"
    override val description =
        "在 Android 官方 Calendar Provider 中直接创建一个日历任务/事项。必须先收集完整信息：标题、开始时间、结束时间。时间按北京时间解释。"
    override val parameters = JSONObject()
        .put("type", "object")
        .put(
            "properties", JSONObject()
                .put("title", JSONObject().put("type", "string").put("description", "日历任务/事项标题，必须明确。"))
                .put("start_at", JSONObject().put("type", "string").put("description", "北京时间开始时间，格式 yyyy-MM-dd HH:mm。"))
                .put("end_at", JSONObject().put("type", "string").put("description", "北京时间结束时间，格式 yyyy-MM-dd HH:mm，必须晚于开始时间。"))
                .put("description", JSONObject().put("type", "string").put("description", "可选详情、任务说明或备注。"))
                .put("location", JSONObject().put("type", "string").put("description", "可选地点。")),
        )
        .put("required", JSONArray(listOf("title", "start_at", "end_at")))

    override suspend fun execute(args: JSONObject, context: AgentToolContext): ToolResult {
        val title = args.optString("title").trim()
        val startText = args.optString("start_at").trim()
        val endText = args.optString("end_at").trim()
        val missing = buildList {
            if (title.isBlank()) add("标题")
            if (startText.isBlank()) add("开始时间")
            if (endText.isBlank()) add("结束时间")
        }
        if (missing.isNotEmpty()) {
            return ToolResult(false, "日历任务信息不完整，暂不能创建。请向用户追问：${missing.joinToString("、")}。")
        }

        val beijingZone = ZoneId.of("Asia/Shanghai")
        val start = parseBeijingDateTime(startText)
            ?: return ToolResult(false, "日历任务创建失败：开始时间格式无效，请使用 yyyy-MM-dd HH:mm，北京时间。")
        val end = parseBeijingDateTime(endText)
            ?: return ToolResult(false, "日历任务创建失败：结束时间格式无效，请使用 yyyy-MM-dd HH:mm，北京时间。")
        if (!end.isAfter(start)) {
            return ToolResult(false, "日历任务创建失败：结束时间必须晚于开始时间。请向用户确认正确时间段。")
        }

        if (!hasCalendarPermissions(context)) {
            val granted = context.requestCalendarPermissions()
            if (!granted) {
                return ToolResult(false, "日历任务创建失败：没有日历读写权限。请用户授予日历权限后再创建。")
            }
        }

        val calendarId = findWritableCalendarId(context)
            ?: return ToolResult(false, "日历任务创建失败：未找到可写入的系统日历。请先在设备/模拟器中添加或启用 Calendar 日历账号。")

        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DTSTART, start.atZone(beijingZone).toInstant().toEpochMilli())
            put(CalendarContract.Events.DTEND, end.atZone(beijingZone).toInstant().toEpochMilli())
            put(CalendarContract.Events.EVENT_TIMEZONE, beijingZone.id)
            val description = args.optString("description").trim()
            if (description.isNotBlank()) {
                put(CalendarContract.Events.DESCRIPTION, description)
            }
            val location = args.optString("location").trim()
            if (location.isNotBlank()) {
                put(CalendarContract.Events.EVENT_LOCATION, location)
            }
        }

        return try {
            val uri = context.appContext.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                ?: return ToolResult(false, "日历任务创建失败：Calendar Provider 未返回事件 URI。")
            ToolResult(
                true,
                "日历任务已创建：$title，时间 ${formatBeijing(start)} - ${formatBeijing(end)}（北京时间），事件 ID ${uri.lastPathSegment ?: "未知"}。",
            )
        } catch (e: SecurityException) {
            ToolResult(false, "日历任务创建失败：日历权限不足。请用户授予日历读写权限后再试。")
        } catch (e: Exception) {
            ToolResult(false, "日历任务创建失败：${e.message ?: e.javaClass.simpleName}")
        }
    }

    private fun hasCalendarPermissions(context: AgentToolContext): Boolean {
        val appContext = context.appContext
        return appContext.checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
            appContext.checkSelfPermission(Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
    }

    private fun findWritableCalendarId(context: AgentToolContext): Long? {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
            CalendarContract.Calendars.VISIBLE,
            CalendarContract.Calendars.IS_PRIMARY,
        )
        val calendars = mutableListOf<CalendarCandidate>()
        context.appContext.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            null,
            null,
            null,
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
            val accessIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL)
            val visibleIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.VISIBLE)
            val primaryIndex = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.IS_PRIMARY)
            while (cursor.moveToNext()) {
                val accessLevel = cursor.getInt(accessIndex)
                if (accessLevel >= CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR) {
                    calendars += CalendarCandidate(
                        id = cursor.getLong(idIndex),
                        visible = cursor.getInt(visibleIndex) == 1,
                        primary = cursor.getInt(primaryIndex) == 1,
                    )
                }
            }
        }
        return calendars
            .sortedWith(compareByDescending<CalendarCandidate> { it.primary }.thenByDescending { it.visible })
            .firstOrNull()
            ?.id
    }

    private fun parseBeijingDateTime(text: String): LocalDateTime? {
        return runCatching {
            LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        }.getOrNull()
    }

    private fun formatBeijing(time: LocalDateTime): String {
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    }

    private data class CalendarCandidate(
        val id: Long,
        val visible: Boolean,
        val primary: Boolean,
    )
}
