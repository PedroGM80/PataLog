package com.patalog.data

import com.patalog.domain.models.Animal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Repositorio para operaciones CRUD de animales.
 */
class AnimalRepository {
    
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    fun getAll(): List<Animal> {
        val conn = Database.getConnection()
        val animals = mutableListOf<Animal>()
        
        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery("SELECT * FROM animals ORDER BY name")
            while (rs.next()) {
                animals.add(mapRowToAnimal(rs))
            }
        }
        
        return animals
    }
    
    fun getById(id: Long): Animal? {
        val conn = Database.getConnection()
        
        conn.prepareStatement("SELECT * FROM animals WHERE id = ?").use { stmt ->
            stmt.setLong(1, id)
            val rs = stmt.executeQuery()
            if (rs.next()) {
                return mapRowToAnimal(rs)
            }
        }
        
        return null
    }
    
    fun getByOwnerId(ownerId: Long): List<Animal> {
        val conn = Database.getConnection()
        val animals = mutableListOf<Animal>()
        
        conn.prepareStatement("SELECT * FROM animals WHERE owner_id = ? ORDER BY name").use { stmt ->
            stmt.setLong(1, ownerId)
            val rs = stmt.executeQuery()
            while (rs.next()) {
                animals.add(mapRowToAnimal(rs))
            }
        }
        
        return animals
    }
    
    fun search(query: String): List<Animal> {
        val conn = Database.getConnection()
        val animals = mutableListOf<Animal>()
        val searchPattern = "%${query}%"
        
        conn.prepareStatement("""
            SELECT * FROM animals 
            WHERE name LIKE ? OR species LIKE ? OR breed LIKE ?
            ORDER BY name
        """.trimIndent()).use { stmt ->
            stmt.setString(1, searchPattern)
            stmt.setString(2, searchPattern)
            stmt.setString(3, searchPattern)
            val rs = stmt.executeQuery()
            while (rs.next()) {
                animals.add(mapRowToAnimal(rs))
            }
        }
        
        return animals
    }
    
    fun insert(animal: Animal): Long {
        val conn = Database.getConnection()
        
        conn.prepareStatement(
            "INSERT INTO animals (name, species, breed, owner_id) VALUES (?, ?, ?, ?)",
            java.sql.Statement.RETURN_GENERATED_KEYS
        ).use { stmt ->
            stmt.setString(1, animal.name)
            stmt.setString(2, animal.species)
            stmt.setString(3, animal.breed)
            if (animal.ownerId > 0) {
                stmt.setLong(4, animal.ownerId)
            } else {
                stmt.setNull(4, java.sql.Types.INTEGER)
            }
            stmt.executeUpdate()
            
            val keys = stmt.generatedKeys
            if (keys.next()) {
                return keys.getLong(1)
            }
        }
        
        return -1
    }
    
    fun update(animal: Animal): Boolean {
        val conn = Database.getConnection()
        
        conn.prepareStatement(
            "UPDATE animals SET name = ?, species = ?, breed = ?, owner_id = ? WHERE id = ?"
        ).use { stmt ->
            stmt.setString(1, animal.name)
            stmt.setString(2, animal.species)
            stmt.setString(3, animal.breed)
            if (animal.ownerId > 0) {
                stmt.setLong(4, animal.ownerId)
            } else {
                stmt.setNull(4, java.sql.Types.INTEGER)
            }
            stmt.setLong(5, animal.id)
            return stmt.executeUpdate() > 0
        }
    }
    
    fun delete(id: Long): Boolean {
        val conn = Database.getConnection()
        
        conn.prepareStatement("DELETE FROM animals WHERE id = ?").use { stmt ->
            stmt.setLong(1, id)
            return stmt.executeUpdate() > 0
        }
    }
    
    private fun mapRowToAnimal(rs: java.sql.ResultSet): Animal {
        val createdAtStr = rs.getString("created_at")
        val createdAt = try {
            LocalDateTime.parse(createdAtStr, dateFormatter)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
        
        return Animal(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            species = rs.getString("species"),
            breed = rs.getString("breed") ?: "",
            ownerId = rs.getLong("owner_id"),
            createdAt = createdAt
        )
    }
}
