package icu.windea.pls

val locationClass = PlsIcons::class.java

const val ddsName = "DDS"
const val ddsDescription = "DirectDraw Surface"

const val cwtName = "Cwt"
const val cwtDescription = "Cwt config"
const val cwtId = "CWT"
const val cwtExtension = "cwt"
val cwtColorSettingsDemoText = "/demoText/Cwt.colorSettings.txt".toClasspathUrl().readText()
val cwtCodeStyleSettingsDemoText = "/demoText/Cwt.codeStyleSettings.txt".toClasspathUrl().readText()

const val paradoxLocalisationName = "Paradox Localisation"
const val paradoxLocalisationDescription = "Paradox localisation"
const val paradoxLocalisationId = "PARADOX_LOCALISATION"
const val paradoxLocalisationExtension = "yml"
val paradoxLocalisationColorSettingsDemoText = "/demoText/ParadoxLocalisation.colorSettings.txt".toClasspathUrl().readText()
val paradoxLocalisationCodeStyleSettingsDemoText = "/demoText/ParadoxLocalisation.codeStyleSettings.txt".toClasspathUrl().readText()

const val paradoxScriptName = "Paradox Script"
const val paradoxScriptDescription = "Paradox script"
const val paradoxScriptId = "PARADOX_SCRIPT"
const val paradoxScriptExtension = "txt"
val paradoxScriptColorSettingsDemoText = "/demoText/ParadoxScript.colorSettings.txt".toClasspathUrl().readText()
val paradoxScriptCodeStyleSettingsDemoText = "/demoText/ParadoxScript.codeStyleSettings.txt".toClasspathUrl().readText()

const val dummyIdentifier = "windea"
const val dummyIdentifierLength = dummyIdentifier.length

const val anonymousString = "(anonymous)"
const val unknownString = "(unknown)"
const val unresolvedString = "(unresolved)"

val utf8Bom = byteArrayOf(0xef.toByte(), 0xbb.toByte(), 0xbf.toByte())

val booleanValues = arrayOf("yes", "no")

val scriptFileExtensions = arrayOf("txt", "gfx", "gui", "asset", "dlc", "settings")
val localisationFileExtensions = arrayOf("yml")
val ddsFileExtensions = arrayOf("dds")

const val launcherSettingsFileName = "launcher-settings.json"
const val descriptorFileName = "descriptor.mod"

const val defaultScriptedVariableName = "var"

const val indexVersion = 4 //0.6.3

object PlsFolders {
	const val ellipsis = "..."
	const val commentFolder = "#..."
	const val parameterFolder = "$...$"
	const val stringTemplateFolder = "..."
	const val blockFolder = "{...}"
	fun parameterConditionFolder(expression: String) = "[[$expression]...]"
	const val inlineMathFolder = "@[...]"
}

object PlsPriorities {
	const val pinnedPriority = 1000.0
	const val keywordPriority = 80.0
	const val systemScopePriority = 65.0
	const val scopePriority = 60.0
	const val propertyPriority = 40.0
	const val modifierPriority = 20.0
}

object PlsPaths {
	val userHome = System.getProperty("user.home")
	const val dataDirectoryName = ".pls"
	const val imagesDirectoryName = "images"
	const val unknownPngName = "unknown.png"
	
	val userHomePath by lazy { userHome.toPath() }
	val dataDirectoryPath by lazy { userHomePath.resolve(dataDirectoryName) }
	val imagesDirectoryPath by lazy { dataDirectoryPath.resolve(imagesDirectoryName) }
	val unknownPngPath by lazy { imagesDirectoryPath.resolve(unknownPngName) }
	
	val unknownPngUrl = "/${unknownPngName}".toClasspathUrl()
}

object PlsPatterns {
	val scriptParameterNameRegex = """[a-zA-Z_][a-zA-Z0-9_]*""".toRegex()
	val scriptedVariableNameRegex = """[a-zA-Z_][a-zA-Z0-9_]*""".toRegex()
	
	val localisationPropertyNameRegex = """[a-zA-Z0-9_.\-']+""".toRegex()
}