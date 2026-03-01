package com.patalog.backend

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

/**
 * Protocolo de comunicacion JSON con el backend Python.
 */

@Serializable
data class BackendRequest(
    val id: String,
    val action: String,
    val params: Map<String, String> = emptyMap()
) {
    fun toJson(): String = buildJsonObject {
        put("id", id)
        put("action", action)
        params.forEach { (k, v) -> put(k, v) }
    }.toString()
}

@Serializable
data class BackendResponse(
    val id: String,
    val ok: Boolean,
    val error: String? = null,
    val data: JsonObject? = null
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        
        fun fromJson(line: String): BackendResponse {
            val obj = json.parseToJsonElement(line).jsonObject
            return BackendResponse(
                id = obj["id"]?.jsonPrimitive?.content ?: "",
                ok = obj["ok"]?.jsonPrimitive?.boolean ?: false,
                error = obj["error"]?.jsonPrimitive?.content,
                data = obj
            )
        }
    }
    
    fun getString(key: String): String? = data?.get(key)?.jsonPrimitive?.content
    fun getBoolean(key: String): Boolean? = data?.get(key)?.jsonPrimitive?.boolean
    fun getStringList(key: String): List<String>? = data?.get(key)?.jsonArray?.map { it.jsonPrimitive.content }
}

/**
 * Excepcion cuando el backend devuelve un error.
 */
class BackendException(message: String) : Exception(message)
