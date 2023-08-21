package icu.windea.pls

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import java.awt.*
import java.lang.ref.SoftReference

object PlsKeys : KeyAware

val PlsKeys.library by createKey<ParadoxLibrary>("paradox.library")

val PlsKeys.rootInfo by createKey<Any>("paradox.rootInfo")
val PlsKeys.fileInfo by createKey<Any>("paradox.fileInfo")
val PlsKeys.localeConfig by createKey<Any>("paradox.localeConfig")

//用于为临时文件（VirtualFile）嵌入根目录信息
val PlsKeys.injectedRootInfo by createKey<ParadoxRootInfo>("paradox.injected.rootInfo")
//用于为临时文件（VirtualFile）嵌入文件信息
val PlsKeys.injectedFileInfo by createKey<ParadoxFileInfo>("paradox.injected.fileInfo")
//用于为脚本文件（VirtualFile）嵌入语言区域
val PlsKeys.injectedLocaleConfig by createKey<CwtLocalisationLocaleConfig>("paradox.injected.localeConfig")
//用于为脚本文件（VirtualFile）嵌入元素路径前缀
val PlsKeys.injectedElementPathPrefix by createKey<ParadoxElementPath>("paradox.injected.elementPathPrefix")

val PlsKeys.cachedDefinitionInfo by createKey<CachedValue<ParadoxDefinitionInfo>>("paradox.cached.definitionInfo")
val PlsKeys.cachedDefinitionPrimaryLocalisationKey by createKey<CachedValue<String>>("paradox.cached.definition.primaryLocalisationKey")
val PlsKeys.cachedDefinitionPrimaryLocalisation by createKey<CachedValue<ParadoxLocalisationProperty>>("paradox.cached.definition.primaryLocalisation")
val PlsKeys.cachedDefinitionPrimaryLocalisations by createKey<CachedValue<Set<ParadoxLocalisationProperty>>>("paradox.cached.definition.primaryLocalisations")
val PlsKeys.cachedDefinitionLocalizedNames by createKey<CachedValue<Set<String>>>("paradox.cached.definition.primaryLocalisations")
val PlsKeys.cachedDefinitionPrimaryImage by createKey<CachedValue<PsiFile>>("paradox.cached.definition.primaryImage")
val PlsKeys.cachedLocalisationInfo by createKey<CachedValue<ParadoxLocalisationInfo>>("paradox.cached.localisationInfo")
val PlsKeys.cachedComplexEnumValueInfo by createKey<CachedValue<ParadoxComplexEnumValueInfo>>("paradox.cached.complexEnumValueInfo")
val PlsKeys.cachedValueSetValueInfos by createKey<CachedValue<List<ParadoxValueSetValueInfo>>>("paradox.cached.valueSetValueInfos")
val PlsKeys.cachedElementPath by createKey<CachedValue<ParadoxElementPath>>("paradox.cached.elementPath")
val PlsKeys.cachedConfigContext by createKey<CachedValue<SoftReference<ParadoxConfigContext>>>("paradox.cached.configContext")
val PlsKeys.cachedConfigsCache by createKey<CachedValue<SoftReference<MutableMap<String, List<CwtMemberConfig<*>>>>>>("paradox.cached.configs.cache")
val PlsKeys.cachedChildOccurrenceMapCache by createKey<CachedValue<MutableMap<String, Map<CwtDataExpression, Occurrence>>>>("paradox.cached.childOccurrenceMap.cache")
val PlsKeys.cachedScopeContext by createKey<CachedValue<ParadoxScopeContext>>("paradox.cached.scopeContext")
val PlsKeys.cachedTextColorInfo by createKey<CachedValue<ParadoxTextColorInfo>>("paradox.cached.textColorInfo")
val PlsKeys.cachedDefineValues by createKey<CachedValue<MutableMap<String, Any?>>>("paradox.cached.defineValues")
val PlsKeys.cachedColor by createKey<CachedValue<Color>>("paradox.cached.color")
val PlsKeys.cachedParameterContextInfo by createKey<CachedValue<ParadoxParameterContextInfo>>("paradox.cached.parameterContextInfo")
val PlsKeys.cachedGameConceptAlias by createKey<CachedValue<Set<String>>>("paradox.cached.gameConcept.alias")

//用于将CWT规则临时写入到CWT元素的userData中（例如，解析引用为枚举值后，将会是对应的CwtEnumConfig）
val PlsKeys.cwtConfig by createKey<CwtConfig<*>>("paradox.cwtConfig")
//用于在进行代码补全时标记一个property的propertyValue未填写
val PlsKeys.isIncomplete by createKey<Boolean>("paradox.isIncomplete")
//用于在进行代码补全时标记作用域不匹配
val PlsKeys.scopeMismatched by createKey<Boolean>("paradox.scopeMismatched")
//用于在进行颜色高亮时标记参数在脚本表达式中的文本范围
val PlsKeys.parameterRanges by createKey<List<TextRange>>("paradox.parameterRanges")
//用于标记图标的帧数以便后续对原始的DDS图片进行切分
val PlsKeys.iconFrame by createKey<Int>("paradox.icon.frame") 