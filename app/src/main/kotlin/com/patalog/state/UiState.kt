package com.patalog.state

/**
 * Estado generico para operaciones asincronas en la UI.
 * Permite distinguir claramente entre: inactivo, cargando, exito y error.
 */
sealed class UiState<out T> {
    
    /** Estado inicial, sin operacion en curso. */
    object Idle : UiState<Nothing>()
    
    /** Operacion en curso. */
    data class Loading(val message: String = "") : UiState<Nothing>()
    
    /** Operacion completada con exito. */
    data class Success<T>(val data: T) : UiState<T>()
    
    /** Operacion fallida. */
    data class Error(val message: String, val cause: Throwable? = null) : UiState<Nothing>()
    
    // Utilidades
    
    val isLoading: Boolean get() = this is Loading
    val isError: Boolean get() = this is Error
    val isSuccess: Boolean get() = this is Success
    val isIdle: Boolean get() = this is Idle
    
    fun getOrNull(): T? = (this as? Success)?.data
    fun errorOrNull(): String? = (this as? Error)?.message
    
    inline fun <R> map(transform: (T) -> R): UiState<R> = when (this) {
        is Idle -> Idle
        is Loading -> Loading(message)
        is Success -> Success(transform(data))
        is Error -> Error(message, cause)
    }
    
    inline fun onSuccess(action: (T) -> Unit): UiState<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onError(action: (String) -> Unit): UiState<T> {
        if (this is Error) action(message)
        return this
    }
    
    inline fun onLoading(action: (String) -> Unit): UiState<T> {
        if (this is Loading) action(message)
        return this
    }
}

/**
 * Extension para convertir Result de Kotlin a UiState.
 */
fun <T> Result<T>.toUiState(): UiState<T> = fold(
    onSuccess = { UiState.Success(it) },
    onFailure = { UiState.Error(it.message ?: "Error desconocido", it) }
)
