package icu.windea.pls.core

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.util.*
import icons.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.definition.config.*
import icu.windea.pls.core.model.*

object PlsConstants {
	val locationClass = PlsIcons::class.java
	
	val cwtColorSettingsDemoText = "/demoText/Cwt.colorSettings.txt".toClasspathUrl().readText()
	val cwtCodeStyleSettingsDemoText = "/demoText/Cwt.codeStyleSettings.txt".toClasspathUrl().readText()
	
	val paradoxLocalisationColorSettingsDemoText = "/demoText/ParadoxLocalisation.colorSettings.txt".toClasspathUrl().readText()
	val paradoxLocalisationCodeStyleSettingsDemoText = "/demoText/ParadoxLocalisation.codeStyleSettings.txt".toClasspathUrl().readText()
	
	val paradoxScriptColorSettingsDemoText = "/demoText/ParadoxScript.colorSettings.txt".toClasspathUrl().readText()
	val paradoxScriptCodeStyleSettingsDemoText = "/demoText/ParadoxScript.codeStyleSettings.txt".toClasspathUrl().readText()
	
	const val dummyIdentifier = "windea"
	
	val utf8Bom = byteArrayOf(0xef.toByte(), 0xbb.toByte(), 0xbf.toByte())
	val booleanValues = arrayOf("yes", "no")
	
	val scriptFileExtensions = arrayOf("txt", "gfx", "gui", "asset", "dlc", "settings")
	val localisationFileExtensions = arrayOf("yml")
	val ddsFileExtensions = arrayOf("dds")
	
	const val launcherSettingsFileName = "launcher-settings.json"
	const val descriptorFileName = "descriptor.mod"
	
	const val ellipsis = "..."
	const val commentFolder = "#..."
	const val parameterFolder = "$...$"
	const val stringTemplateFolder = "..."
	const val blockFolder = "{...}"
	fun parameterConditionFolder(expression: String) = "[[$expression]...]"
	const val inlineMathFolder = "@[...]"
	
	const val anonymousString = "(anonymous)"
	const val unknownString = "(unknown)"
	const val unresolvedString = "(unresolved)"
	
	const val defaultScriptedVariableName = "var"
	//NOTE 目前认为cwt文件中定义的definition的propertyPath的maxDepth是4（最多跳过3个rootKey）
	const val maxMayBeDefinitionDepth = 4
	const val keysTruncateLimit = 5
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
	val unknownPngUrl by lazy { unknownPngPath.toUri().toURL() }
	
	val unknownPngClasspathUrl = "/$unknownPngName".toClasspathUrl()
}

object PlsPatterns {
	val scriptParameterNameRegex = """[a-zA-Z_][a-zA-Z0-9_]*""".toRegex()
	val scriptedVariableNameRegex = """[a-zA-Z_][a-zA-Z0-9_]*""".toRegex()
	
	val localisationPropertyNameRegex = """[a-zA-Z0-9_.\-']+""".toRegex()
}

object PlsThreadLocals {
	val threadLocalTextEditorContainer = ThreadLocal<TextEditor?>()
}

object PlsKeys {
	val rootInfoKey = Key.create<ParadoxRootInfo>("paradox.rootInfo")
	val descriptorInfoKey = Key.create<ParadoxDescriptorInfo>("paradox.descriptorInfo")
	val fileInfoKey = Key.create<ParadoxFileInfo>("paradox.fileInfo")
	val contentFileKey = Key.create<VirtualFile>("paradox.contentFile")
	
	val cachedDefinitionInfoKey = Key.create<CachedValue<ParadoxDefinitionInfo>>("paradox.cachedDefinitionInfo")
	val cachedDefinitionElementInfoKey = Key.create<CachedValue<ParadoxDefinitionElementInfo>>("paradox.cachedDefinitionElementInfo")
	val cachedLocalisationInfoKey = Key.create<CachedValue<ParadoxLocalisationInfo>>("paradox.cachedLocalisationInfo")
	val cachedComplexEnumValueInfoKey = Key.create<CachedValue<ParadoxComplexEnumValueInfo>>("paradox.cachedComplexEnumValueInfo")
	
	val injectedInfoKey = Key.create<List<String>>("paradox.injectedInfo")
	
	val textColorConfigKey = Key.create<ParadoxTextColorConfig>("paradox.textColorConfig")
	
	val definitionConfigKeys = setOf<Key<out ParadoxDefinitionConfig>>(
		textColorConfigKey
	)
	
	val cwtConfigKey = Key.create<CwtDataConfig<*>>("paradox.cwtConfig")
}

object PlsDataKeys {
	val gameTypePropertyKey = DataKey.create<GraphProperty<ParadoxGameType>>("PARADOX_GAME_TYPE_PROPERTY")
	val rootTypePropertyKey = DataKey.create<GraphProperty<ParadoxRootType>>("PARADOX_ROOT_TYPE_PROPERTY")
}

val AnActionEvent.gameTypeProperty get() = getData(PlsDataKeys.gameTypePropertyKey)
val AnActionEvent.rootTypeProperty get() = getData(PlsDataKeys.rootTypePropertyKey)