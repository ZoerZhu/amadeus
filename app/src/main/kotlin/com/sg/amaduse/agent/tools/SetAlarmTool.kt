package com.sg.amaduse.agent.tools

import android.content.Intent
import android.provider.AlarmClock
import org.json.JSONArray
import org.json.JSONObject
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal class SetAlarmTool : AgentTool {
    override val name = "set_alarm"
    override val description = "直接创建 Android 系统闹钟提醒。工具内部以北京时间为基准计算目标时刻，并转换为设备本地时区写入系统闹钟。"
    override val parameters = JSONObject()
        .put("type", "object")
        .put(
            "properties", JSONObject()
                .put("hour", JSONObject().put("type", "integer").put("description", "北京时间小时（0-23）。用于绝对时间，例如北京时间 8:30。"))
                .put("minute", JSONObject().put("type", "integer").put("description", "北京时间分钟（0-59）。用于绝对时间，例如北京时间 8:30。"))
                .put("relative_minutes", JSONObject().put("type", "integer").put("description", "从当前北京时间开始延后多少分钟。用于“30 分钟后提醒我”。"))
                .put("relative_hours", JSONObject().put("type", "integer").put("description", "从当前北京时间开始延后多少小时。用于“2 小时后提醒我”。"))
                .put("day_offset", JSONObject().put("type", "integer").put("description", "绝对时间的日期偏移：0 表示今天，1 表示明天。省略时使用下一次出现的北京时间。"))
                .put("label", JSONObject().put("type", "string").put("description", "闹钟标签")),
        )
        .put("required", JSONArray())

    override suspend fun execute(args: JSONObject, context: AgentToolContext): ToolResult {
        val label = args.optString("label", "Amaduse 提醒").ifBlank { "Amaduse 提醒" }
        val beijingZone = ZoneId.of("Asia/Shanghai")
        val deviceZone = ZoneId.systemDefault()
        val nowBeijing = ZonedDateTime.now(beijingZone)
        val targetBeijing = resolveTargetBeijing(args, nowBeijing)
            ?: return ToolResult(false, "闹钟设置失败：请提供北京时间 hour/minute，或 relative_minutes/relative_hours。")

        if (!targetBeijing.isAfter(nowBeijing)) {
            return ToolResult(false, "闹钟设置失败：目标时间必须晚于当前北京时间。当前北京时间 ${formatBeijing(nowBeijing)}。")
        }

        val targetDevice = targetBeijing.withZoneSameInstant(deviceZone)
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, targetDevice.hour)
                putExtra(AlarmClock.EXTRA_MINUTES, targetDevice.minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, label)
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.appContext.startActivity(intent)
            ToolResult(
                true,
                "闹钟已创建：北京时间 ${formatBeijing(targetBeijing)}，标签「$label」。设备时区 ${deviceZone.id} 中写入为 ${formatLocalTime(targetDevice)}。",
            )
        } catch (e: Exception) {
            ToolResult(false, "闹钟设置失败：${e.message ?: e.javaClass.simpleName}")
        }
    }

    private fun resolveTargetBeijing(
        args: JSONObject,
        nowBeijing: ZonedDateTime,
    ): ZonedDateTime? {
        val relativeMinutes = args.optNullableInt("relative_minutes")
        val relativeHours = args.optNullableInt("relative_hours")
        if (relativeMinutes != null || relativeHours != null) {
            val minutes = (relativeMinutes ?: 0) + (relativeHours ?: 0) * 60
            if (minutes <= 0) {
                return null
            }
            return nowBeijing.plusMinutes(minutes.toLong()).withSecond(0).withNano(0)
        }

        val hour = args.optNullableInt("hour") ?: return null
        val minute = args.optNullableInt("minute") ?: return null
        if (hour !in 0..23 || minute !in 0..59) {
            return null
        }

        val dayOffset = args.optNullableInt("day_offset")
        var target = nowBeijing
            .plusDays((dayOffset ?: 0).toLong())
            .withHour(hour)
            .withMinute(minute)
            .withSecond(0)
            .withNano(0)
        if (dayOffset == null && !target.isAfter(nowBeijing)) {
            target = target.plusDays(1)
        }
        return target
    }

    private fun JSONObject.optNullableInt(name: String): Int? {
        if (!has(name) || isNull(name)) {
            return null
        }
        return runCatching { getInt(name) }.getOrNull()
    }

    private fun formatBeijing(time: ZonedDateTime): String {
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    }

    private fun formatLocalTime(time: ZonedDateTime): String {
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    }
}
