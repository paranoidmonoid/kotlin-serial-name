import arrow.meta.CliPlugin
import arrow.meta.Meta
import arrow.meta.invoke
import arrow.meta.phases.CompilerContext
import arrow.meta.phases.analysis.isAnnotatedWith
import arrow.meta.quotes.Transform
import arrow.meta.quotes.classDeclaration
import org.jetbrains.kotlin.psi.psiUtil.getValueParameters

class SerialNamePlugin : Meta {
    override fun intercept(ctx: CompilerContext): List<CliPlugin> =
        listOf(
            customAnnotationSerialNamePlugin
        )
}

val Meta.serialNamePlugin: CliPlugin
    get() =
        "Serial Name Plugin" {
            meta(
                classDeclaration(this, { isAnnotatedWith("@Serializable".toRegex()) }) { classElement ->
                    val paramList = value.getValueParameters()
                        .map {
                            if (it.isAnnotatedWith("@(kotlinx\\.serialization\\.)?SerialName\\(.+\\)".toRegex())) {
                                it.text
                            } else {
                                "@kotlinx.serialization.SerialName(\"${
                                    it.name?.toSnakeCase()
                                }\") ${it.text}"
                            }
                        }
                    val paramListString = paramList.joinToString(", ", "(", ")")
                    val annotations = classElement.annotationEntries.joinToString("\n") { it.text }
                    val newDeclaration = """
                        |$annotations $kind $name$`(typeParameters)`$paramListString {
                        |    $body
                        |}
                    """.trimMargin()
                    Transform.replace(classElement, newDeclaration.`class`.syntheticScope)
                }
            )
        }

val Meta.customAnnotationSerialNamePlugin: CliPlugin
    get() =
        "Serial Name With Strategy" {
            meta(
                classDeclaration(
                    this,
                    { isAnnotatedWith("@SerializeWithStrategy\\(.+\\)".toRegex()) }) { classElement ->
                    val annotation = classElement.annotationEntries.first {
                        it.text?.contains("SerializeWithStrategy") == true
                    }
                    val case = annotation.valueArguments
                        .first()
                        .getArgumentExpression()!!
                        .text
                        .removePrefix("SerializationStrategy.")
                    val caseFunction = when (case) {
                        "KebabCase" -> String::toKebabCase
                        "SnakeCase" -> String::toSnakeCase
                        else -> error("Unexpected strategy")
                    }
                    val paramList = value.getValueParameters()
                        .map {
                            if (it.isAnnotatedWith("@(kotlinx\\.serialization\\.)?SerialName\\(.+\\)".toRegex())) {
                                it.text
                            } else {
                                "@kotlinx.serialization.SerialName(\"${
                                    it.name?.let(
                                        caseFunction
                                    )
                                }\") ${it.text}"
                            }
                        }
                    val paramListString = paramList.joinToString(", ", "(", ")")

                    val newDeclaration = """
                        |$`@annotations` $kind $name$`(typeParameters)`$paramListString {
                        |    $body
                        |}
                    """.trimMargin()
                    Transform.replace(classElement, newDeclaration.`class`.syntheticScope)
                }
            )
        }


fun String.toSnakeCase() = splitToWords().joinToString("_").lowercase()
fun String.toKebabCase() = splitToWords().joinToString("-").lowercase()

// https://stackoverflow.com/questions/7593969/regex-to-split-camelcase-or-titlecase-advanced#comment57838106_7599674
private fun String.splitToWords() =
    split("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])|(?<=[0-9])(?=[A-Z][a-z])|(?<=[a-zA-Z])(?=[0-9])".toRegex())