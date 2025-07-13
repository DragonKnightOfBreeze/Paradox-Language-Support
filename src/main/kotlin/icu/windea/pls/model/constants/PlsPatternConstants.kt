package icu.windea.pls.model.constants

object PlsPatternConstants {
    val scriptedVariableName = """[a-zA-Z_][a-zA-Z0-9_]*""".toRegex()
    val localisationName = """[a-zA-Z0-9_.\-']+""".toRegex()
    val localisationPropertyName = """[a-zA-Z0-9_.\-']+""".toRegex()
    val parameterName = """[a-zA-Z_][a-zA-Z0-9_]*""".toRegex()
    val localisationParameterName = """[a-zA-Z0-9_.\-']+""".toRegex()
}
