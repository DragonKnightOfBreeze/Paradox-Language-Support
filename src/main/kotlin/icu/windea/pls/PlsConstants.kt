package icu.windea.pls

import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icons.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.model.*
import java.awt.*

object PlsConstants {
    val pluginId = "icu.windea.pls"
    val locationClass = PlsIcons::class.java
    
    val cwtColorSettingsDemoText = "/demoText/Cwt.colorSettings.txt".toClasspathUrl().readText()
    val cwtCodeStyleSettingsDemoText = "/demoText/Cwt.codeStyleSettings.txt".toClasspathUrl().readText()
    
    val paradoxLocalisationColorSettingsDemoText = "/demoText/ParadoxLocalisation.colorSettings.txt".toClasspathUrl().readText()
    val paradoxLocalisationCodeStyleSettingsDemoText = "/demoText/ParadoxLocalisation.codeStyleSettings.txt".toClasspathUrl().readText()
    
    val paradoxScriptColorSettingsDemoText = "/demoText/ParadoxScript.colorSettings.txt".toClasspathUrl().readText()
    val paradoxScriptCodeStyleSettingsDemoText = "/demoText/ParadoxScript.codeStyleSettings.txt".toClasspathUrl().readText()
    
    const val dummyIdentifier = "windea"
    
    val eraseMarker = TextAttributes()
    
    val utf8Bom = byteArrayOf(0xef.toByte(), 0xbb.toByte(), 0xbf.toByte())
    
    val scriptFileExtensions = arrayOf("txt", "gfx", "gui", "asset", "dlc", "settings")
    val localisationFileExtensions = arrayOf("yml")
    val ddsFileExtensions = arrayOf("dds")
    
    const val launcherSettingsFileName = "launcher-settings.json"
    const val descriptorFileName = "descriptor.mod"
    
    val separatorChars = charArrayOf('=', '<', '>', '!')
    
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
    //NOTE 目前认为cwt文件中定义的definition的elementPath的maxDepth是4（最多跳过3个rootKey）
    const val maxDefinitionDepth = 4
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
    
    const val tmpDirectoryName = "tmp"
    val tmpDirectoryPath by lazy { dataDirectoryPath.resolve(tmpDirectoryName) }
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
    val libraryKey = Key.create<ParadoxLibrary>("paradox.library")
    val rootInfoKey = Key.create<ParadoxRootInfo>("paradox.rootInfo")
    val fileInfoKey = Key.create<ParadoxFileInfo>("paradox.fileInfo")
    
    val injectedFileInfoKey = Key.create<ParadoxFileInfo>("paradox.injected.fileInfo") //用于为临时文件（VirtualFile）嵌入文件信息
    //val injectedFileTypeKey = Key.create<ParadoxFileType>("paradox.injected.fileType") //用于为临时文件（VirtualFile）嵌入文件类型
    val injectedLocaleConfigKey = Key.create<CwtLocalisationLocaleConfig>("paradox.injected.localeConfig") //用于为脚本文件（VirtualFile）嵌入语言区域
    val injectedElementPathKey = Key.create<ParadoxElementPath>("paradox.injected.elementPath") //用于为脚本元素（VirtualFile）嵌入元素路径（相对于脚本文件）
    val injectedElementPathPrefixKey = Key.create<ParadoxElementPath>("paradox.injected.elementPathPrefix") //用于为脚本文件（VirtualFile）嵌入元素路径前缀
    
    val launcherSettingsInfoKey = Key.create<ParadoxLauncherSettingsInfo>("paradox.launcherSettingsInfo")
    val descriptorInfoKey = Key.create<ParadoxModDescriptorInfo>("paradox.descriptorInfo")
    
    val cachedDefinitionInfoKey = Key.create<CachedValue<ParadoxDefinitionInfo>>("paradox.cached.definitionInfo")
    val cachedDefinitionMemberInfoKey = Key.create<CachedValue<ParadoxDefinitionMemberInfo>>("paradox.cached.definitionMemberInfo")
    val cachedLocalisationInfoKey = Key.create<CachedValue<ParadoxLocalisationInfo>>("paradox.cached.localisationInfo")
    val cachedValueSetValueInfoKey = Key.create<CachedValue<ParadoxValueSetValueInfo>>("paradox.cached.valueSetValueInfo")
    val cachedComplexEnumValueInfoKey = Key.create<CachedValue<ParadoxComplexEnumValueInfo>>("paradox.cached.complexEnumValueInfo")
    val cachedConfigsMapKey = Key.create<CachedValue<MutableMap<String, List<CwtConfig<*>>>>>("paradox.cached.configsMap")
    val cachedScopeContextKey = Key.create<CachedValue<ParadoxScopeContext>>("paradox.cached.scopeContext")
    val cachedTextColorInfoKey = Key.create<CachedValue<ParadoxTextColorInfo>>("paradox.cached.textColorInfo")
    val cachedDefineValuesKey = Key.create<CachedValue<MutableMap<String, Any?>>>("paradox.cached.defineValues")
    val cachedColorKey = Key.create<CachedValue<Color>>("paradox.cached.color")
    val cachedScopeContextInferenceInfoKey = Key.create<CachedValue<ParadoxScopeContextInferenceInfo>>("paradox.cached.scopeContextInferenceInfoKey")
    
    //用于将CWT规则临时写入到CWT元素的userData中（例如，解析引用为枚举值后，将会是对应的CwtEnumConfig）
    val cwtConfigKey = Key.create<CwtConfig<*>>("paradox.cwtConfig")
    
    //用于在进行代码补全时标记一个property的propertyValue未填写
    val isIncompleteKey = Key.create<Boolean>("paradox.isIncomplete")
    //用于在进行代码补全时标记作用域不匹配
    val scopeMismatchedKey = Key.create<Boolean>("paradox.scopeMismatched")
    
    val parameterRangesKey = Key.create<List<TextRange>>("paradox.parameterRanges")
    
    val iconFrameKey = Key.create<Int>("paradox.icon.frame")
}
