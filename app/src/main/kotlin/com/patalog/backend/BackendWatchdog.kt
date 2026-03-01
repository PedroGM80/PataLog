package com.patalog.backend

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Watchdog que supervisa el proceso backend y lo reinicia si muere.
 */
class BackendWatchdog(
    private val client: BackendClient,
    private val checkIntervalMs: Long = 5_000L,
    private val maxRestarts: Int = 3,
    private val restartCooldownMs: Long = 10_000L
) {
    private var watchJob: Job? = null
    private var restartCount = 0
    private var lastRestartTime = 0L
    
    private val _state = MutableStateFlow(WatchdogState.STOPPED)
    val state: StateFlow<WatchdogState> = _state.asStateFlow()
    
    // Callbacks
    var onStateChange: ((WatchdogState) -> Unit)? = null
    var onRestart: ((Int) -> Unit)? = null
    var onMaxRestartsReached: (() -> Unit)? = null
    
    enum class WatchdogState {
        STOPPED,
        STARTING,
        RUNNING,
        RESTARTING,
        FAILED
    }
    
    fun start(scope: CoroutineScope) {
        if (watchJob?.isActive == true) return
        
        watchJob = scope.launch {
            _state.value = WatchdogState.STARTING
            onStateChange?.invoke(_state.value)
            
            try {
                client.start()
                _state.value = WatchdogState.RUNNING
                onStateChange?.invoke(_state.value)
                
                // Bucle de supervision
                while (isActive) {
                    delay(checkIntervalMs)
                    
                    if (!client.isAlive) {
                        handleProcessDeath()
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = WatchdogState.FAILED
                onStateChange?.invoke(_state.value)
            }
        }
    }
    
    fun stop() {
        watchJob?.cancel()
        watchJob = null
        client.stop()
        _state.value = WatchdogState.STOPPED
        onStateChange?.invoke(_state.value)
    }
    
    private suspend fun handleProcessDeath() {
        val now = System.currentTimeMillis()
        
        // Reset contador si ha pasado suficiente tiempo
        if (now - lastRestartTime > restartCooldownMs * maxRestarts) {
            restartCount = 0
        }
        
        if (restartCount >= maxRestarts) {
            _state.value = WatchdogState.FAILED
            onStateChange?.invoke(_state.value)
            onMaxRestartsReached?.invoke()
            watchJob?.cancel()
            return
        }
        
        _state.value = WatchdogState.RESTARTING
        onStateChange?.invoke(_state.value)
        
        restartCount++
        lastRestartTime = now
        onRestart?.invoke(restartCount)
        
        // Esperar antes de reiniciar
        delay(restartCooldownMs)
        
        try {
            client.start()
            _state.value = WatchdogState.RUNNING
            onStateChange?.invoke(_state.value)
        } catch (e: Exception) {
            // El siguiente ciclo detectara que sigue muerto
        }
    }
    
    /**
     * Fuerza un reinicio manual del backend.
     */
    suspend fun forceRestart() {
        client.stop()
        delay(1000)
        client.start()
        _state.value = WatchdogState.RUNNING
        onStateChange?.invoke(_state.value)
    }
}
