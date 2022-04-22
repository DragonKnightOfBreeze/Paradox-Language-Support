package icu.windea.pls

val locationClass = PlsBundle::class.java

const val cwtName = "Cwt"
const val cwtDescription = "Cwt config"
const val cwtId = "CWT"
const val cwtExtension = "cwt"
val cwtDemoText = "/demoText/Cwt.txt".toUrl(locationClass).readText()

const val paradoxLocalisationName = "Paradox Localisation"
const val paradoxLocalisationDescription = "Paradox localisation"
const val paradoxLocalisationId = "PARADOX_LOCALISATION"
const val paradoxLocalisationExtension = "yml"
val paradoxLocalisationDemoText = "/demoText/ParadoxLocalisation.txt".toUrl(locationClass).readText()

const val paradoxScriptName = "Paradox Script"
const val paradoxScriptDescription = "Paradox script"
const val paradoxScriptId = "PARADOX_SCRIPT"
const val paradoxScriptExtension = "txt"
val paradoxScriptDemoText = "/demoText/ParadoxScript.txt".toUrl(locationClass).readText()

const val keywordPriority = 80.0
const val propertyPriority = 40.0
const val modifierPriority = 20.0

const val dummyIdentifier = "windea"
const val dummyIdentifierLength = dummyIdentifier.length
const val commentFolder = "#..."
const val blockFolder = "{...}"
const val anonymousString = "<anonymous>"
const val anonymousEscapedString = "&lt;anonymous&gt;"
const val unknownString = "<unknown>"
const val unknownEscapedString = "&lt;unknown&gt;"
const val unresolvedString = "<unresolved>"
const val unresolvedEscapedString = "&lt;unresolved&gt;"
val utf8Bom = byteArrayOf(0xef.toByte(), 0xbb.toByte(), 0xbf.toByte())
val booleanValues = arrayOf("yes", "no")
val localisationFileExtensions = arrayOf("yml")
val scriptFileExtensions = arrayOf("txt", "mod", "gfx", "gui", "asset", "dlc","settings")
const val descriptorFileName = "descriptor.mod"
const val truncateLimit = 30

//TODO 可以设置
val ignoredScriptFileNameRegex = """(readme|changelog|license|credits).*\.txt""".toRegex(RegexOption.IGNORE_CASE)
val relatedLocalisationNamesToInfer = arrayOf("title","desc","effect") //不包括name，因为可能不是localisation