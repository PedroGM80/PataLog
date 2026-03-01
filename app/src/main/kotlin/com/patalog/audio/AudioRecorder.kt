package com.patalog.audio

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.sound.sampled.*

/**
 * Grabador de audio usando Java Sound API.
 * Graba en formato WAV compatible con Whisper (16kHz, 16-bit, mono).
 */
class AudioRecorder {
    
    private var targetDataLine: TargetDataLine? = null
    private var recordingJob: Job? = null
    private var audioData: ByteArrayOutputStream? = null
    
    private val _state = MutableStateFlow(RecordingState.IDLE)
    val state: StateFlow<RecordingState> = _state.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    // Formato de audio compatible con Whisper
    private val audioFormat = AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        16000f,  // Sample rate: 16kHz (bueno para voz)
        16,      // Sample size: 16 bits
        1,       // Channels: mono
        2,       // Frame size: 2 bytes
        16000f,  // Frame rate
        false    // Little endian
    )
    
    enum class RecordingState {
        IDLE,
        RECORDING,
        STOPPED,
        ERROR
    }
    
    /**
     * Verifica si hay microfono disponible.
     */
    fun isMicrophoneAvailable(): Boolean {
        return try {
            val info = DataLine.Info(TargetDataLine::class.java, audioFormat)
            AudioSystem.isLineSupported(info)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Lista los dispositivos de entrada disponibles.
     */
    fun getInputDevices(): List<String> {
        val devices = mutableListOf<String>()
        val mixerInfos = AudioSystem.getMixerInfo()
        
        for (info in mixerInfos) {
            val mixer = AudioSystem.getMixer(info)
            val lineInfo = DataLine.Info(TargetDataLine::class.java, audioFormat)
            if (mixer.isLineSupported(lineInfo)) {
                devices.add(info.name)
            }
        }
        
        return devices
    }
    
    /**
     * Inicia la grabacion.
     */
    fun start(scope: CoroutineScope): Boolean {
        if (_state.value == RecordingState.RECORDING) {
            return false
        }
        
        return try {
            val info = DataLine.Info(TargetDataLine::class.java, audioFormat)
            
            if (!AudioSystem.isLineSupported(info)) {
                _state.value = RecordingState.ERROR
                return false
            }
            
            targetDataLine = AudioSystem.getLine(info) as TargetDataLine
            targetDataLine?.open(audioFormat)
            targetDataLine?.start()
            
            audioData = ByteArrayOutputStream()
            _state.value = RecordingState.RECORDING
            _duration.value = 0
            
            // Job para capturar audio
            recordingJob = scope.launch(Dispatchers.IO) {
                val buffer = ByteArray(4096)
                val startTime = System.currentTimeMillis()
                
                while (isActive && _state.value == RecordingState.RECORDING) {
                    val bytesRead = targetDataLine?.read(buffer, 0, buffer.size) ?: 0
                    if (bytesRead > 0) {
                        audioData?.write(buffer, 0, bytesRead)
                    }
                    
                    // Actualizar duracion
                    _duration.value = System.currentTimeMillis() - startTime
                }
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _state.value = RecordingState.ERROR
            false
        }
    }
    
    /**
     * Detiene la grabacion y devuelve el archivo WAV.
     */
    suspend fun stop(): File? {
        if (_state.value != RecordingState.RECORDING) {
            return null
        }
        
        _state.value = RecordingState.STOPPED
        
        // Esperar a que termine el job de grabacion
        recordingJob?.cancelAndJoin()
        recordingJob = null
        
        // Detener la linea de audio
        targetDataLine?.stop()
        targetDataLine?.close()
        targetDataLine = null
        
        // Obtener los datos grabados
        val data = audioData?.toByteArray() ?: return null
        audioData = null
        
        if (data.isEmpty()) {
            return null
        }
        
        // Crear archivo WAV temporal
        return try {
            val tempFile = withContext(Dispatchers.IO) {
                File.createTempFile("patalog_recording_", ".wav").apply {
                    deleteOnExit()
                }
            }
            
            // Escribir WAV
            withContext(Dispatchers.IO) {
                val audioInputStream = AudioInputStream(
                    ByteArrayInputStream(data),
                    audioFormat,
                    data.size.toLong() / audioFormat.frameSize
                )
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, tempFile)
            }
            
            _state.value = RecordingState.IDLE
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            _state.value = RecordingState.ERROR
            null
        }
    }
    
    /**
     * Cancela la grabacion sin guardar.
     */
    fun cancel() {
        _state.value = RecordingState.IDLE
        recordingJob?.cancel()
        recordingJob = null
        
        targetDataLine?.stop()
        targetDataLine?.close()
        targetDataLine = null
        
        audioData = null
        _duration.value = 0
    }
    
    /**
     * Formatea la duracion en mm:ss.
     */
    fun formatDuration(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / 1000) / 60
        return "%02d:%02d".format(minutes, seconds)
    }
}
