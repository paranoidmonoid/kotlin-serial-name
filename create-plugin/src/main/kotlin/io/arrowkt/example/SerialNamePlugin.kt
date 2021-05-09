package io.arrowkt.example

import arrow.meta.CliPlugin
import arrow.meta.Meta
import arrow.meta.invoke
import arrow.meta.phases.analysis.isAnnotatedWith
import arrow.meta.quotes.Transform
import arrow.meta.quotes.classDeclaration
import org.jetbrains.kotlin.psi.psiUtil.getValueParameters

val Meta.serialNameGenerator: CliPlugin
    get() =
        "Serial Name Plugin" {
            meta(
                classDeclaration(this, { name == "Cat" }) { classElement ->
                    val skip = value.getValueParameters().all { it.isAnnotatedWith(".*SerialName.*".toRegex()) }
                    val paramList = value.getValueParameters()
                        .map { "@kotlinx.serialization.SerialName(\"${it.name?.toSnakeCase()}\") ${it.text}" }
                    val paramListString = paramList.joinToString(", ", "(", ")")
                    val newDeclaration = """
                        |$`@annotations` $kind $name$`(typeParameters)`$paramListString {
                        |    $body
                        |}
                    """.trimMargin()
                    Transform.Companion.replace(classElement, if (skip) this else newDeclaration.`class`.syntheticScope)
                }

            )
        }


fun String.toSnakeCase() = splitToWords().joinToString("_").toLowerCase()

// https://stackoverflow.com/questions/7593969/regex-to-split-camelcase-or-titlecase-advanced#comment57838106_7599674
private fun String.splitToWords() =
    split("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])|(?<=[0-9])(?=[A-Z][a-z])|(?<=[a-zA-Z])(?=[0-9])".toRegex())