package com.patalog.data

import java.io.File
import java.sql.Connection
import java.sql.DriverManager

/**
 * Gestiona la conexion a SQLite y la creacion de tablas.
 */
object Database {
    
    private const val DB_NAME = "patalog.db"
    private var connection: Connection? = null
    
    /**
     * Obtiene la ruta del archivo de base de datos.
     * En Windows: %APPDATA%/PataLog/patalog.db
     * En Mac/Linux: ~/.patalog/patalog.db
     */
    private fun getDbPath(): String {
        val appDir = if (System.getProperty("os.name").lowercase().contains("win")) {
            File(System.getenv("APPDATA"), "PataLog")
        } else {
            File(System.getProperty("user.home"), ".patalog")
        }
        
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        
        return File(appDir, DB_NAME).absolutePath
    }
    
    /**
     * Inicializa la base de datos y crea las tablas si no existen.
     */
    fun init() {
        val dbPath = getDbPath()
        println("[db] Conectando a: $dbPath")
        
        connection = DriverManager.getConnection("jdbc:sqlite:$dbPath")
        createTables()
        
        println("[db] Base de datos inicializada")
    }
    
    /**
     * Obtiene la conexion activa.
     */
    fun getConnection(): Connection {
        return connection ?: throw IllegalStateException("Database not initialized. Call init() first.")
    }
    
    /**
     * Cierra la conexion.
     */
    fun close() {
        connection?.close()
        connection = null
    }
    
    private fun createTables() {
        val conn = getConnection()
        
        conn.createStatement().use { stmt ->
            // Tabla de propietarios
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS owners (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    phone TEXT DEFAULT '',
                    email TEXT DEFAULT '',
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """.trimIndent())
            
            // Tabla de animales
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS animals (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    species TEXT NOT NULL,
                    breed TEXT DEFAULT '',
                    owner_id INTEGER,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (owner_id) REFERENCES owners(id) ON DELETE SET NULL
                )
            """.trimIndent())
            
            // Tabla de consultas
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS consultations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    animal_id INTEGER NOT NULL,
                    date TEXT DEFAULT CURRENT_TIMESTAMP,
                    transcript TEXT DEFAULT '',
                    report TEXT DEFAULT '',
                    notes TEXT DEFAULT '',
                    FOREIGN KEY (animal_id) REFERENCES animals(id) ON DELETE CASCADE
                )
            """.trimIndent())
            
            // Tabla de configuracion de clinica
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS clinic_config (
                    id INTEGER PRIMARY KEY CHECK (id = 1),
                    name TEXT DEFAULT 'Clinica Veterinaria',
                    address TEXT DEFAULT '',
                    phone TEXT DEFAULT '',
                    logo_path TEXT,
                    vet_license TEXT DEFAULT '',
                    ollama_model TEXT DEFAULT 'llama3',
                    transcription_language TEXT DEFAULT 'es',
                    pdf_output_folder TEXT DEFAULT '',
                    dark_mode INTEGER DEFAULT 0
                )
            """.trimIndent())
            
            // Migracion: añadir columna dark_mode si no existe
            try {
                stmt.executeUpdate("ALTER TABLE clinic_config ADD COLUMN dark_mode INTEGER DEFAULT 0")
            } catch (e: Exception) {
                // Columna ya existe, ignorar
            }
            
            // Insertar configuracion por defecto si no existe
            stmt.executeUpdate("""
                INSERT OR IGNORE INTO clinic_config (id) VALUES (1)
            """.trimIndent())
        }
    }
}
