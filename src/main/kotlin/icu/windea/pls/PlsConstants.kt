package icu.windea.pls

import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icons.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.psi.*
import java.awt.*

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
    
    val scriptFileExtensions = arrayOf("txt", "gfx", "gui", "asset", "dlc", "settings")
    val localisationFileExtensions = arrayOf("yml")
    val ddsFileExtensions = arrayOf("dds")
    
    const val launcherSettingsFileName = "launcher-settings.json"
    const val descriptorFileName = "descriptor.mod"
    
    const val ellipsis = "..."
    //const val commentFolder = "#..."
    const val blockFolder = "{...}"
    fun parameterConditionFolder(expression: String) = "[[$expression]...]"
    const val inlineMathFolder = "@[...]"
    
    const val anonymousString = "(anonymous)"
    const val unknownString = "(unknown)"
    const val unresolvedString = "(unresolved)"
    
    const val defaultScriptedVariableName = "var"
    
    //目前认为定义相对于脚本文件的最大深度是4（最多跳过3个rootKey），在索引之外的某些场合需要加上针挑
    const val maxDefinitionDepth = 4
    //在提示信息中最多显示的键的个数
    const val keysTruncateLimit = 5
    
    const val lazyIndexThreadPoolSize = 4
    
    //val eraseMarker = TextAttributes()
    
    val onlyForegroundAttributesFlags = WithAttributesPresentation.AttributesFlags().withSkipBackground(true).withSkipEffects(true)
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

object PlsThreadLocals

object PlsKeys {
    val libraryKey = Key.create<ParadoxLibrary>("paradox.library")
    val rootInfoKey = Key.create<ParadoxRootInfo>("paradox.rootInfo")
    val fileInfoKey = Key.create<ParadoxFileInfo>("paradox.fileInfo")
    
    val injectedFileInfoKey = Key.create<ParadoxFileInfo>("paradox.injected.fileInfo") //用于为临时文件（VirtualFile）嵌入文件信息
    //val injectedFileTypeKey = Key.create<ParadoxFileType>("paradox.injected.fileType") //用于为临时文件（VirtualFile）嵌入文件类型
    val injectedLocaleConfigKey = Key.create<CwtLocalisationLocaleConfig>("paradox.injected.localeConfig") //用于为脚本文件（VirtualFile）嵌入语言区域
    //val injectedElementPathKey = Key.create<ParadoxElementPath>("paradox.injected.elementPath") //用于为脚本元素（VirtualFile）嵌入元素路径（相对于脚本文件）
    val injectedElementPathPrefixKey = Key.create<ParadoxElementPath>("paradox.injected.elementPathPrefix") //用于为脚本文件（VirtualFile）嵌入元素路径前缀
    
    val launcherSettingsInfoKey = Key.create<ParadoxLauncherSettingsInfo>("paradox.launcherSettingsInfo")
    val descriptorInfoKey = Key.create<ParadoxModDescriptorInfo>("paradox.descriptorInfo")
    
    val cachedDefinitionInfoKey = Key.create<CachedValue<ParadoxDefinitionInfo>>("paradox.cached.definitionInfo")
    val cachedDefinitionPrimaryLocalisationKeyKey = Key.create<CachedValue<String>>("paradox.cached.definition.primaryLocalisationKey")
    val cachedDefinitionPrimaryLocalisationKey = Key.create<CachedValue<ParadoxLocalisationProperty>>("paradox.cached.definition.primaryLocalisation")
    val cachedDefinitionPrimaryLocalisationsKey = Key.create<CachedValue<Set<ParadoxLocalisationProperty>>>("paradox.cached.definition.primaryLocalisations")
    val cachedDefinitionLocalizedNamesKey = Key.create<CachedValue<Set<String>>>("paradox.cached.definition.primaryLocalisations")
    val cachedDefinitionPrimaryImageKey = Key.create<CachedValue<PsiFile>>("paradox.cached.definition.primaryImage")
    val cachedDefinitionMemberInfoKey = Key.create<CachedValue<ParadoxDefinitionMemberInfo>>("paradox.cached.definitionMemberInfo")
    val cachedLocalisationInfoKey = Key.create<CachedValue<ParadoxLocalisationInfo>>("paradox.cached.localisationInfo")
    val cachedComplexEnumValueInfoKey = Key.create<CachedValue<ParadoxComplexEnumValueInfo>>("paradox.cached.complexEnumValueInfo")
    val cachedValueSetValueInfosKey = Key.create<CachedValue<List<ParadoxValueSetValueInfo>>>("paradox.cached.valueSetValueInfos")
    val cachedConfigsCacheKey = Key.create<CachedValue<MutableMap<String, List<CwtConfig<*>>>>>("paradox.cached.configs.cache")
    val cachedChildOccurrenceMapCacheKey = Key.create<CachedValue<MutableMap<String, Map<CwtDataExpression, Occurrence>>>>("paradox.cached.childOccurrenceMap.cache")
    val cachedScopeContextKey = Key.create<CachedValue<ParadoxScopeContext>>("paradox.cached.scopeContext")
    val cachedTextColorInfoKey = Key.create<CachedValue<ParadoxTextColorInfo>>("paradox.cached.textColorInfo")
    val cachedDefineValuesKey = Key.create<CachedValue<MutableMap<String, Any?>>>("paradox.cached.defineValues")
    val cachedColorKey = Key.create<CachedValue<Color>>("paradox.cached.color")
    val cachedParametersKey = Key.create<CachedValue<ParadoxParameterContextInfo>>("paradox.cached.parameterContextInfo")
    val cachedGameConceptAliasKey = Key.create<CachedValue<Set<String>>>("paradox.cached.gameConcept.alias")
    
    //用于将CWT规则临时写入到CWT元素的userData中（例如，解析引用为枚举值后，将会是对应的CwtEnumConfig）
    val cwtConfigKey = Key.create<CwtConfig<*>>("paradox.cwtConfig")
    //用于在进行代码补全时标记一个property的propertyValue未填写
    val isIncompleteKey = Key.create<Boolean>("paradox.isIncomplete")
    //用于在进行代码补全时标记作用域不匹配
    val scopeMismatchedKey = Key.create<Boolean>("paradox.scopeMismatched")
    //用于在进行颜色高亮时标记参数在脚本表达式中的文本范围
    val parameterRangesKey = Key.create<List<TextRange>>("paradox.parameterRanges")
    //用于标记图标的帧数以便后续对原始的DDS图片进行切分
    val iconFrameKey = Key.create<Int>("paradox.icon.frame")
}
