package com.patalog.backend

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.atomic.AtomicLong

/**
 * Cliente para comunicacion con el backend Python.
 * 
 * Gestiona el proceso hijo y el protocolo JSON sobre stdin/stdout.
 */
class BackendClient(
    private val pythonPath: String = "python",
    private val scriptPath: String = "backend/src/main.py",
    private val defaultTimeoutMs: Long = 180_000L // 3 minutos
) {
    private var process: Process? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null
    private val mutex = Mutex()
    private val idCounter = AtomicLong(0)
    
    // Callback para logs del backend (stderr)
    var onLog: ((String) -> Unit)? = null
    
    // --- Ciclo de vida ---
    
    fun start() {
        val pb = ProcessBuilder(pythonPath, scriptPath)
            .redirectErrorStream(false)
            .also { it.environment()["PYTHONUNBUFFERED"] = "1" }
        
        process = pb.start()
        writer = BufferedWriter(OutputStreamWriter(process!!.outputStream, Charsets.UTF_8))
        reader = BufferedReader(InputStreamReader(process!!.inputStream, Charsets.UTF_8))
        
        // Hilo para stderr (logs)
        Thread({
            process!!.errorStream.bufferedReader().forEachLine { line ->
                onLog?.invoke(line) ?: println("[python] $line")
            }
        }, "backend-stderr").apply { isDaemon = true }.start()
    }
    
    fun stop() {
        runCatching { sendRaw(BackendRequest("0", "shutdown").toJson()) }
        writer?.close()
        reader?.close()
        process?.destroy()
        process = null
    }
    
    val isAlive: Boolean 
        get() = process?.isAlive == true
    
    // --- API publica ---
    
    data class Status(
        val whisperReady: Boolean,
        val ollamaReady: Boolean,
        val ollamaModel: String
    )
    
    suspend fun getStatus(): Status {
        val response = request("status")
        return Status(
            whisperReady = response.getBoolean("whisper_ready") ?: false,
            ollamaReady = response.getBoolean("ollama_ready") ?: false,
            ollamaModel = response.getString("ollama_model") ?: ""
        )
    }
    
    suspend fun transcribe(audioPath: String, language: String = "es"): String {
        val response = request("transcribe", "audio_path" to audioPath, "language" to language)
        return response.getString("transcript") 
            ?: throw BackendException("Respuesta sin campo 'transcript'")
    }
    
    suspend fun generateReport(transcript: String): String {
        val response = request("report", "transcript" to transcript)
        return response.getString("report")
            ?: throw BackendException("Respuesta sin campo 'report'")
    }
    
    suspend fun generateSummary(history: String): String {
        val response = request("summary", "history" to history)
        return response.getString("summary")
            ?: throw BackendException("Respuesta sin campo 'summary'")
    }
    
    suspend fun listModels(): List<String> {
        val response = request("list_models")
        return response.getStringList("models") ?: emptyList()
    }
    
    suspend fun setModel(model: String) {
        request("set_model", "model" to model)
    }
    
    suspend fun exportPdf(
        outputPath: String,
        animalName: String,
        animalSpecies: String = "",
        animalBreed: String = "",
        ownerName: String = "",
        ownerPhone: String = "",
        consultationDate: String = "",
        report: String,
        notes: String = "",
        clinicName: String = "Clinica Veterinaria",
        vetLicense: String = ""
    ): String {
        val response = request(
            "export_pdf",
            "output_path" to outputPath,
            "animal_name" to animalName,
            "animal_species" to animalSpecies,
            "animal_breed" to animalBreed,
            "owner_name" to ownerName,
            "owner_phone" to ownerPhone,
            "consultation_date" to consultationDate,
            "report" to report,
            "notes" to notes,
            "clinic_name" to clinicName,
            "vet_license" to vetLicense
        )
        return response.getString("pdf_path")
            ?: throw BackendException("Respuesta sin campo 'pdf_path'")
    }
    
    // --- Comunicacion interna ---
    
    private suspend fun request(
        action: String,
        vararg params: Pair<String, String>,
        timeoutMs: Long = defaultTimeoutMs
    ): BackendResponse = mutex.withLock {
        withContext(Dispatchers.IO) {
            withTimeout(timeoutMs) {
                val id = idCounter.incrementAndGet().toString()
                val request = BackendRequest(id, action, params.toMap())
                sendRaw(request.toJson())
                readResponse()
            }
        }
    }
    
    private fun sendRaw(line: String) {
        val w = writer ?: throw BackendException("Backend no iniciado")
        w.write(line)
        w.newLine()
        w.flush()
    }
    
    private fun readResponse(): BackendResponse {
        val line = reader?.readLine()
            ?: throw BackendException("Backend cerro la conexion")
        val response = BackendResponse.fromJson(line)
        if (!response.ok) {
            throw BackendException(response.error ?: "Error desconocido")
        }
        return response
    }
}
