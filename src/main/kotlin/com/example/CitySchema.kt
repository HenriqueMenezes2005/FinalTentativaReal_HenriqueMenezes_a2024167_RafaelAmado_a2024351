package com.example

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement

@Serializable
data class City(val name: String, val population: Int)
class CityService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_CITIES =
            "CREATE TABLE CITIES (ID SERIAL PRIMARY KEY, NAME VARCHAR(255), POPULATION INT);"
        private const val SELECT_CITY_BY_ID = "SELECT name, population FROM cities WHERE id = ?"
        private const val INSERT_CITY = "INSERT INTO cities (name, population) VALUES (?, ?)"
        private const val UPDATE_CITY = "UPDATE cities SET name = ?, population = ? WHERE id = ?"
        private const val DELETE_CITY = "DELETE FROM cities WHERE id = ?"

    }

    init {
        val statement = connection.createStatement()
        //statement.executeUpdate(CREATE_TABLE_CITIES)
    }

    private var newCityId = 0

    // Create new city
    suspend fun create(city: City): Int = withContext(Dispatchers.IO) {
        try {
            val statement = connection.prepareStatement(INSERT_CITY, Statement.RETURN_GENERATED_KEYS)
            statement.setString(1, city.name)
            statement.setInt(2, city.population)
            statement.executeUpdate()

            val generatedKeys = statement.generatedKeys
            if (generatedKeys.next()) {
                return@withContext generatedKeys.getInt(1)
            } else {
                throw Exception("Unable to retrieve the id of the newly inserted city")
            }
        } catch (e: SQLException) {
            throw Exception("Error while creating city: ${e.message}", e)
        }
    }

    // Read a city
    suspend fun read(id: Int): City = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_CITY_BY_ID)
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val name = resultSet.getString("name")
            val population = resultSet.getInt("population")
            return@withContext City(name, population)
        } else {
            throw Exception("Record not found")
        }
    }

    // Update a city
    suspend fun update(id: Int, city: City) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_CITY)
        statement.setString(1, city.name)
        statement.setInt(2, city.population)
        statement.setInt(3, id)
        statement.executeUpdate()
    }

    // Delete a city
    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        println("id recebido: $id")  // This is fine for debugging, but avoid in production.

        try {
            // Check if the city exists before attempting to delete
            val checkStmt = connection.prepareStatement("SELECT id FROM cities WHERE id = ?")
            checkStmt.setInt(1, id)
            val resultSet = checkStmt.executeQuery()

            if (resultSet.next()) {
                // Proceed with delete if the city exists
                val deleteStmt = connection.prepareStatement(DELETE_CITY)
                deleteStmt.setInt(1, id)
                val rowsAffected = deleteStmt.executeUpdate()

                if (rowsAffected > 0) {
                    println("City with ID $id deleted successfully.")
                } else {
                    println("No rows affected. No city with ID $id found.")
                }
            } else {
                println("City with ID $id does not exist.")
            }
        } catch (e: SQLException) {
            // Catch and log any SQL exception
            println("SQL error during delete: ${e.message}")
            e.printStackTrace()
        } catch (e: Exception) {
            // Catch general exceptions and log
            println("Error deleting city with ID $id: ${e.message}")
            e.printStackTrace()
        }
    }

}

