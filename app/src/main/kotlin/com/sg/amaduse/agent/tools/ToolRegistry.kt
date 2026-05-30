package com.sg.amaduse.agent.tools

import org.json.JSONArray
import org.json.JSONObject

internal object ToolRegistry {
    private val tools = mutableListOf<AgentTool>()

    fun register(tool: AgentTool) {
        tools.removeAll { it.name == tool.name }
        tools.add(tool)
    }

    fun all(): List<AgentTool> = tools.toList()

    fun find(name: String): AgentTool? = tools.find { it.name == name }

    fun toJsonArray(): JSONArray {
        val array = JSONArray()
        for (tool in tools) {
            array.put(
                JSONObject()
                    .put("type", "function")
                    .put(
                        "function", JSONObject()
                            .put("name", tool.name)
                            .put("description", tool.description)
                            .put("parameters", tool.parameters),
                    ),
            )
        }
        return array
    }
}
