package io.arrowkt.example

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Cat(val name: String, val age: Int, val ownerName: String, val breed: String)

fun main() {
  val cat = Cat("Chonk", 3, "Max", "British Shorthair")
  println(Json.encodeToString(cat))
}
