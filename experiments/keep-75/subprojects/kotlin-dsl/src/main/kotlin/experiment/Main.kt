package experiment

import api.Project

import kotlin.reflect.KClass

import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.asSuccess
import kotlin.script.experimental.api.baseClass
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.onFailure
import kotlin.script.experimental.api.onSuccess
import kotlin.script.experimental.api.refineConfiguration
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.JvmScriptCompiler


suspend fun main(args: Array<String>) {
    compileProjectScript("""
        version = "1.0"
    """).onFailure { result ->
        reportErrors(result)
    }.onSuccess { scriptClass ->
        evaluate(scriptClass).asSuccess()
    }
}


private
suspend fun compileProjectScript(script: String): ResultWithDiagnostics<KClass<*>> =
    JvmScriptCompiler()
        .invoke(
            script.trimIndent().toScriptSource(),
            GradleScriptConfiguration
        ).onSuccess {
            it.getClass(null)
        }


object GradleScriptConfiguration : ScriptCompilationConfiguration({
    baseClass(GradleScript::class)
    defaultImports()
    jvm {
        dependenciesFromCurrentContext(
            "kotlin-dsl",
            "gradle-api"
        )
    }
    refineConfiguration {
    }
})


@KotlinScript(
    fileExtension = "gradle.kts",
    compilationConfiguration = GradleScriptConfiguration::class
)
abstract class GradleScript(val project: Project) : Project by project


private
fun reportErrors(result: ResultWithDiagnostics<KClass<*>>) {
    result.reports
        .filterNot { it.severity == ScriptDiagnostic.Severity.DEBUG }
        .map { it.toOutputString() }
        .forEach(::println)
}


private
fun evaluate(scriptClass: KClass<*>) {
    val project = DefaultProject()
    scriptClass.constructors.first().call(project)
    println(project)
}


data class DefaultProject(
    private var projectVersion: Any = "undefined"
) : Project {
    override fun getVersion(): Any = projectVersion
    override fun setVersion(version: Any) {
        projectVersion = version
    }
}


private
fun ScriptDiagnostic.toOutputString(): String = "$severity: $message"
