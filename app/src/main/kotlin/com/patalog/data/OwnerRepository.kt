package com.patalog.data

import com.patalog.domain.models.Owner
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Repositorio para operaciones CRUD de propietarios.
 */
class OwnerRepository {
    
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    fun getAll(): List<Owner> {
        val conn = Database.getConnection()
        val owners = mutableListOf<Owner>()
        
        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery("SELECT * FROM owners ORDER BY name")
            while (rs.next()) {
                owners.add(mapRowToOwner(rs))
            }
        }
        
        return owners
    }
    
    fun getById(id: Long): Owner? {
        val conn = Database.getConnection()
        
        conn.prepareStatement("SELECT * FROM owners WHERE id = ?").use { stmt ->
            stmt.setLong(1, id)
            val rs = stmt.executeQuery()
            if (rs.next()) {
                return mapRowToOwner(rs)
            }
        }
        
        return null
    }
    
    fun search(query: String): List<Owner> {
        val conn = Database.getConnection()
        val owners = mutableListOf<Owner>()
        val searchPattern = "%${query}%"
        
        conn.prepareStatement("""
            SELECT * FROM owners 
            WHERE name LIKE ? OR phone LIKE ? OR email LIKE ?
            ORDER BY name
        """.trimIndent()).use { stmt ->
            stmt.setString(1, searchPattern)
            stmt.setString(2, searchPattern)
            stmt.setString(3, searchPattern)
            val rs = stmt.executeQuery()
            while (rs.next()) {
                owners.add(mapRowToOwner(rs))
            }
        }
        
        return owners
    }
    
    fun insert(owner: Owner): Long {
        val conn = Database.getConnection()
        
        conn.prepareStatement(
            "INSERT INTO owners (name, phone, email) VALUES (?, ?, ?)",
            java.sql.Statement.RETURN_GENERATED_KEYS
        ).use { stmt ->
            stmt.setString(1, owner.name)
            stmt.setString(2, owner.phone)
            stmt.setString(3, owner.email)
            stmt.executeUpdate()
            
            val keys = stmt.generatedKeys
            if (keys.next()) {
                return keys.getLong(1)
            }
        }
        
        return -1
    }
    
    fun update(owner: Owner): Boolean {
        val conn = Database.getConnection()
        
        conn.prepareStatement(
            "UPDATE owners SET name = ?, phone = ?, email = ? WHERE id = ?"
        ).use { stmt ->
            stmt.setString(1, owner.name)
            stmt.setString(2, owner.phone)
            stmt.setString(3, owner.email)
            stmt.setLong(4, owner.id)
            return stmt.executeUpdate() > 0
        }
    }
    
    fun delete(id: Long): Boolean {
        val conn = Database.getConnection()
        
        conn.prepareStatement("DELETE FROM owners WHERE id = ?").use { stmt ->
            stmt.setLong(1, id)
            return stmt.executeUpdate() > 0
        }
    }
    
    private fun mapRowToOwner(rs: java.sql.ResultSet): Owner {
        val createdAtStr = rs.getString("created_at")
        val createdAt = try {
            LocalDateTime.parse(createdAtStr, dateFormatter)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
        
        return Owner(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            phone = rs.getString("phone") ?: "",
            email = rs.getString("email") ?: "",
            createdAt = createdAt
        )
    }
}
