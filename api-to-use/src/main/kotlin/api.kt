annotation class SerializeWithStrategy(val strategy: SerializationStrategy)

enum class SerializationStrategy {
    SnakeCase, KebabCase
}