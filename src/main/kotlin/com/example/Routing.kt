package com.example

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection
import java.sql.DriverManager
// Database connection
fun connectToDatabase(): Connection {
    // Replace with your actual database connection details
    val url = "jdbc:mysql://localhost:3306/bank_app"
    val username = "root"
    val password = "@HmAa2022!"
    return DriverManager.getConnection(url, username, password)
}
    fun Application.configureRouting() {
        // Conectar ao banco e criar a instância do CityService
        val connection = connectToDatabase()
        val cityService = CityService(connection)

        routing {
            route("/api") {
                route("/city") {
                    // Rota para deletar uma cidade
                    delete("/{id}") {
                        println("Received DELETE request for city with ID: ${call.parameters["id"]}")
                        val id = call.parameters["id"]?.toIntOrNull()
                        if (id != null) {
                            try {
                                cityService.delete(id)
                                call.respond(HttpStatusCode.NoContent)
                            } catch (e: Exception) {
                                call.respond(HttpStatusCode.NotFound, "City not found")
                            }
                        } else {
                            call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                        }
                    }

                    // Rota para pegar uma cidade pelo ID
                    get("/{id}") {
                        val id = call.parameters["id"]?.toIntOrNull()
                        if (id != null) {
                            try {
                                val city = cityService.read(id)
                                call.respond(city)
                            } catch (e: Exception) {
                                call.respond(HttpStatusCode.NotFound, "City not found")
                            }
                        } else {
                            call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                        }
                    }
                    // Rota para criar uma nova cidade
                    post("/city") {
                        val city = call.receive<City>()
                        try {
                            val newCityId = cityService.create(city)
                            call.respond(HttpStatusCode.Created, "City created with ID: $newCityId")
                        } catch (e: Exception) {
                            call.respond(HttpStatusCode.InternalServerError, "Failed to create city")
                        }
                    }
                    // Rota para atualizar uma cidade
                    put("/{id}") {
                        val id = call.parameters["id"]?.toIntOrNull()
                        val city = call.receive<City>()
                        if (id != null) {
                            try {
                                cityService.update(id, city)
                                call.respond(HttpStatusCode.OK, "City updated")
                            } catch (e: Exception) {
                                call.respond(HttpStatusCode.NotFound, "City not found or update failed")
                            }
                        } else {
                            call.respond(HttpStatusCode.BadRequest, "Invalid ID")
                        }
                    }
              }
            }
        }
    }




