package icu.windea.pls.model.constants

object PlsPatterns {
    val scriptedVariableName = """[A-Za-z_][A-Za-z0-9_]*""".toRegex()
    val localisationName = """[A-Za-z0-9_.\-']+""".toRegex()
    val argumentName = """[A-Za-z_][A-Za-z0-9_]*""".toRegex()
    val parameterName = """[A-Za-z_][A-Za-z0-9_]*""".toRegex()
    val localisationParameterName = """[A-Za-z0-9_.\-']+""".toRegex()
}
