package com.patalog.data

import com.patalog.domain.models.Consultation
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Repositorio para operaciones CRUD de consultas.
 */
class ConsultationRepository {
    
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val dateOnlyFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    
    fun getAll(): List<Consultation> {
        val conn = Database.getConnection()
        val consultations = mutableListOf<Consultation>()
        
        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery("SELECT * FROM consultations ORDER BY date DESC")
            while (rs.next()) {
                consultations.add(mapRowToConsultation(rs))
            }
        }
        
        return consultations
    }
    
    fun getById(id: Long): Consultation? {
        val conn = Database.getConnection()
        
        conn.prepareStatement("SELECT * FROM consultations WHERE id = ?").use { stmt ->
            stmt.setLong(1, id)
            val rs = stmt.executeQuery()
            if (rs.next()) {
                return mapRowToConsultation(rs)
            }
        }
        
        return null
    }
    
    fun getByAnimalId(animalId: Long): List<Consultation> {
        val conn = Database.getConnection()
        val consultations = mutableListOf<Consultation>()
        
        conn.prepareStatement(
            "SELECT * FROM consultations WHERE animal_id = ? ORDER BY date DESC"
        ).use { stmt ->
            stmt.setLong(1, animalId)
            val rs = stmt.executeQuery()
            while (rs.next()) {
                consultations.add(mapRowToConsultation(rs))
            }
        }
        
        return consultations
    }
    
    /**
     * Busca consultas por rango de fechas.
     * @param startDate Fecha inicio (inclusive)
     * @param endDate Fecha fin (inclusive)
     */
    fun getByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<Consultation> {
        val conn = Database.getConnection()
        val consultations = mutableListOf<Consultation>()
        
        conn.prepareStatement("""
            SELECT * FROM consultations 
            WHERE date >= ? AND date <= ?
            ORDER BY date DESC
        """.trimIndent()).use { stmt ->
            stmt.setString(1, startDate.format(dateFormatter))
            stmt.setString(2, endDate.format(dateFormatter))
            val rs = stmt.executeQuery()
            while (rs.next()) {
                consultations.add(mapRowToConsultation(rs))
            }
        }
        
        return consultations
    }
    
    /**
     * Busca consultas de un dia especifico.
     */
    fun getByDate(date: LocalDateTime): List<Consultation> {
        val startOfDay = date.toLocalDate().atStartOfDay()
        val endOfDay = startOfDay.plusDays(1).minusSeconds(1)
        return getByDateRange(startOfDay, endOfDay)
    }
    
    /**
     * Busca en el contenido de las consultas.
     */
    fun search(query: String): List<Consultation> {
        val conn = Database.getConnection()
        val consultations = mutableListOf<Consultation>()
        val searchPattern = "%${query}%"
        
        conn.prepareStatement("""
            SELECT * FROM consultations 
            WHERE transcript LIKE ? OR report LIKE ? OR notes LIKE ?
            ORDER BY date DESC
        """.trimIndent()).use { stmt ->
            stmt.setString(1, searchPattern)
            stmt.setString(2, searchPattern)
            stmt.setString(3, searchPattern)
            val rs = stmt.executeQuery()
            while (rs.next()) {
                consultations.add(mapRowToConsultation(rs))
            }
        }
        
        return consultations
    }
    
    fun insert(consultation: Consultation): Long {
        val conn = Database.getConnection()
        
        conn.prepareStatement(
            "INSERT INTO consultations (animal_id, date, transcript, report, notes) VALUES (?, ?, ?, ?, ?)",
            java.sql.Statement.RETURN_GENERATED_KEYS
        ).use { stmt ->
            stmt.setLong(1, consultation.animalId)
            stmt.setString(2, consultation.date.format(dateFormatter))
            stmt.setString(3, consultation.transcript)
            stmt.setString(4, consultation.report)
            stmt.setString(5, consultation.notes)
            stmt.executeUpdate()
            
            val keys = stmt.generatedKeys
            if (keys.next()) {
                return keys.getLong(1)
            }
        }
        
        return -1
    }
    
    fun update(consultation: Consultation): Boolean {
        val conn = Database.getConnection()
        
        conn.prepareStatement(
            "UPDATE consultations SET animal_id = ?, date = ?, transcript = ?, report = ?, notes = ? WHERE id = ?"
        ).use { stmt ->
            stmt.setLong(1, consultation.animalId)
            stmt.setString(2, consultation.date.format(dateFormatter))
            stmt.setString(3, consultation.transcript)
            stmt.setString(4, consultation.report)
            stmt.setString(5, consultation.notes)
            stmt.setLong(6, consultation.id)
            return stmt.executeUpdate() > 0
        }
    }
    
    fun delete(id: Long): Boolean {
        val conn = Database.getConnection()
        
        conn.prepareStatement("DELETE FROM consultations WHERE id = ?").use { stmt ->
            stmt.setLong(1, id)
            return stmt.executeUpdate() > 0
        }
    }
    
    /**
     * Obtiene el historial completo de un animal como texto para generar resumen.
     */
    fun getHistoryAsText(animalId: Long): String {
        val consultations = getByAnimalId(animalId)
        if (consultations.isEmpty()) return ""
        
        return consultations.joinToString("\n\n---\n\n") { consultation ->
            val date = consultation.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            """
            Fecha: $date
            
            ${consultation.report}
            
            ${if (consultation.notes.isNotBlank()) "Notas: ${consultation.notes}" else ""}
            """.trimIndent()
        }
    }
    
    private fun mapRowToConsultation(rs: java.sql.ResultSet): Consultation {
        val dateStr = rs.getString("date")
        val date = try {
            LocalDateTime.parse(dateStr, dateFormatter)
        } catch (e: Exception) {
            try {
                LocalDateTime.parse(dateStr)
            } catch (e2: Exception) {
                LocalDateTime.now()
            }
        }
        
        return Consultation(
            id = rs.getLong("id"),
            animalId = rs.getLong("animal_id"),
            date = date,
            transcript = rs.getString("transcript") ?: "",
            report = rs.getString("report") ?: "",
            notes = rs.getString("notes") ?: ""
        )
    }
}
