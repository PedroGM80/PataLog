package com.patalog.data

import com.patalog.domain.models.ClinicConfig

/**
 * Repositorio para la configuracion de la clinica.
 * Solo existe una fila (id = 1).
 */
class ClinicConfigRepository {
    
    /**
     * Comprueba si es el primer inicio (necesita onboarding).
     * Se considera primer inicio si el nombre es el default y no hay colegiado.
     */
    fun needsOnboarding(): Boolean {
        val config = get()
        return config.name == "Clinica Veterinaria" && config.vetLicense.isBlank()
    }
    
    fun get(): ClinicConfig {
        val conn = Database.getConnection()
        
        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery("SELECT * FROM clinic_config WHERE id = 1")
            if (rs.next()) {
                return ClinicConfig(
                    name = rs.getString("name") ?: "Clinica Veterinaria",
                    address = rs.getString("address") ?: "",
                    phone = rs.getString("phone") ?: "",
                    logoPath = rs.getString("logo_path"),
                    vetLicense = rs.getString("vet_license") ?: "",
                    ollamaModel = rs.getString("ollama_model") ?: "llama3",
                    transcriptionLanguage = rs.getString("transcription_language") ?: "es",
                    pdfOutputFolder = rs.getString("pdf_output_folder") ?: "",
                    darkMode = rs.getInt("dark_mode") == 1
                )
            }
        }
        
        // Retornar valores por defecto si no existe
        return ClinicConfig()
    }
    
    fun update(config: ClinicConfig): Boolean {
        val conn = Database.getConnection()
        
        conn.prepareStatement("""
            UPDATE clinic_config SET 
                name = ?,
                address = ?,
                phone = ?,
                logo_path = ?,
                vet_license = ?,
                ollama_model = ?,
                transcription_language = ?,
                pdf_output_folder = ?,
                dark_mode = ?
            WHERE id = 1
        """.trimIndent()).use { stmt ->
            stmt.setString(1, config.name)
            stmt.setString(2, config.address)
            stmt.setString(3, config.phone)
            stmt.setString(4, config.logoPath)
            stmt.setString(5, config.vetLicense)
            stmt.setString(6, config.ollamaModel)
            stmt.setString(7, config.transcriptionLanguage)
            stmt.setString(8, config.pdfOutputFolder)
            stmt.setInt(9, if (config.darkMode) 1 else 0)
            return stmt.executeUpdate() > 0
        }
    }
}
