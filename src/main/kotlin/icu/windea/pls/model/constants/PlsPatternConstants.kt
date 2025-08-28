package icu.windea.pls.model.constants

object PlsPatternConstants {
    val scriptedVariableName = """[A-Za-z_][A-Za-z0-9_]*""".toRegex()
    val localisationName = """[A-Za-z0-9_.\-']+""".toRegex()
    val localisationPropertyName = """[A-Za-z0-9_.\-']+""".toRegex()
    val parameterName = """[A-Za-z_][A-Za-z0-9_]*""".toRegex()
    val localisationParameterName = """[A-Za-z0-9_.\-']+""".toRegex()
}
