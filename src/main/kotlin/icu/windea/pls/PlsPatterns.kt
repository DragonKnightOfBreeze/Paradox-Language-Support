package icu.windea.pls

object PlsPatterns {
	val scriptParameterNameRegex = """^[a-zA-Z_][a-zA-Z0-9_]*$""".toRegex()
	val scriptedVariableNameRegex = """[a-zA-Z_][a-zA-Z0-9_]*""".toRegex()
	
	val localisationPropertyNameRegex = """[a-zA-Z0-9_.\-']+""".toRegex()
}