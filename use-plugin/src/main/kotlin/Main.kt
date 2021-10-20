package io.arrowkt.example

//import SerializeWithStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
// @SerializeWithStrategy(SerializationStrategy.KebabCase)
data class Cat(val name: String, val age: Int, val ownerName: String, val breed: String)

@Serializable
data class Dog(val name: String, val age: Int, val ownerName: String, val breed: String)

fun main() {
  val cat = Cat("Chonk", 3, "Max", "British Shorthair")
  val dog = Dog("Puff", 3, "Max", "Shiba Inu")
  println(Json.encodeToString(cat))
  println(Json.encodeToString(dog))
}
