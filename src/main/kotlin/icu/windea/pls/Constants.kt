package icu.windea.pls

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.util.*
import icons.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.definition.config.*
import icu.windea.pls.model.*

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

//NOTE 目前认为cwt文件中定义的definition的propertyPath的maxDepth是4（最多跳过3个rootKey）
const val maxMayBeDefinitionDepth = 4

const val defaultScriptedVariableName = "var"

const val keyTruncateLimit = 5

object PlsFolders {
	const val ellipsis = "..."
	const val commentFolder = "#..."
	const val parameterFolder = "$...$"
	const val stringTemplateFolder = "..."
	const val blockFolder = "{...}"
	fun parameterConditionFolder(expression: String) = "[[$expression]...]"
	const val inlineMathFolder = "@[...]"
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

object PlsKeys {
	val rootInfoKey = Key.create<ParadoxRootInfo>("paradoxRootInfo")
	val descriptorInfoKey = Key.create<ParadoxDescriptorInfo>("paradoxDescriptorInfo")
	val fileInfoKey = Key.create<ParadoxFileInfo>("paradoxFileInfo")
	val contentFileKey = Key.create<VirtualFile>("paradoxContentFile")
	
	val cachedDefinitionInfoKey = Key.create<CachedValue<ParadoxDefinitionInfo>>("cachedParadoxDefinitionInfo")
	val cachedLocalisationInfoKey = Key.create<CachedValue<ParadoxLocalisationInfo>>("cachedParadoxLocalisationInfo")
	
	val definitionElementInfoKey = Key.create<ParadoxDefinitionElementInfo>("paradoxDefinitionElementInfo")
	
	val injectedInfoKey = Key.create<List<String>>("paradoxInjectedInfo")
	
	val textColorConfigKey = Key.create<ParadoxTextColorConfig>("paradoxTextColorConfig")
	
	val definitionConfigKeys = setOf<Key<out ParadoxDefinitionConfig>>(
		textColorConfigKey
	)
	
	val cwtConfigKey = Key.create<CwtKvConfig<*>>("cwtConfig")
}

object PlsDataKeys {
	val gameTypePropertyKey = DataKey.create<GraphProperty<ParadoxGameType>>("PARADOX_GAME_TYPE_PROPERTY")
	val rootTypePropertyKey = DataKey.create<GraphProperty<ParadoxRootType>>("PARADOX_ROOT_TYPE_PROPERTY")
}

val AnActionEvent.gameTypeProperty get() = getData(PlsDataKeys.gameTypePropertyKey)
val AnActionEvent.rootTypeProperty get() = getData(PlsDataKeys.rootTypePropertyKey)