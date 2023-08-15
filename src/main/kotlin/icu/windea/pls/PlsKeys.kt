package icu.windea.pls

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import java.awt.*

object PlsKeys

val PlsKeys.library by lazy { Key.create<ParadoxLibrary>("paradox.library") }

val PlsKeys.rootInfo by lazy { Key.create<Any>("paradox.rootInfo") }
val PlsKeys.fileInfo by lazy { Key.create<Any>("paradox.fileInfo") }
val PlsKeys.localeConfig by lazy { Key.create<Any>("paradox.localeConfig") }

//用于为临时文件（VirtualFile）嵌入根目录信息
val PlsKeys.injectedRootInfo by lazy { Key.create<ParadoxRootInfo>("paradox.injected.rootInfo") }
//用于为临时文件（VirtualFile）嵌入文件信息
val PlsKeys.injectedFileInfo by lazy { Key.create<ParadoxFileInfo>("paradox.injected.fileInfo") }
//用于为脚本文件（VirtualFile）嵌入语言区域
val PlsKeys.injectedLocaleConfig by lazy { Key.create<CwtLocalisationLocaleConfig>("paradox.injected.localeConfig") }
//用于为脚本文件（VirtualFile）嵌入元素路径前缀
val PlsKeys.injectedElementPathPrefix by lazy { Key.create<ParadoxElementPath>("paradox.injected.elementPathPrefix") }

val PlsKeys.cachedDefinitionInfo by lazy { Key.create<CachedValue<ParadoxDefinitionInfo>>("paradox.cached.definitionInfo") }
val PlsKeys.cachedDefinitionPrimaryLocalisationKey by lazy { Key.create<CachedValue<String>>("paradox.cached.definition.primaryLocalisationKey") }
val PlsKeys.cachedDefinitionPrimaryLocalisation by lazy { Key.create<CachedValue<ParadoxLocalisationProperty>>("paradox.cached.definition.primaryLocalisation") }
val PlsKeys.cachedDefinitionPrimaryLocalisations by lazy { Key.create<CachedValue<Set<ParadoxLocalisationProperty>>>("paradox.cached.definition.primaryLocalisations") }
val PlsKeys.cachedDefinitionLocalizedNames by lazy { Key.create<CachedValue<Set<String>>>("paradox.cached.definition.primaryLocalisations") }
val PlsKeys.cachedDefinitionPrimaryImage by lazy { Key.create<CachedValue<PsiFile>>("paradox.cached.definition.primaryImage") }
val PlsKeys.cachedLocalisationInfo by lazy { Key.create<CachedValue<ParadoxLocalisationInfo>>("paradox.cached.localisationInfo") }
val PlsKeys.cachedComplexEnumValueInfo by lazy { Key.create<CachedValue<ParadoxComplexEnumValueInfo>>("paradox.cached.complexEnumValueInfo") }
val PlsKeys.cachedValueSetValueInfos by lazy { Key.create<CachedValue<List<ParadoxValueSetValueInfo>>>("paradox.cached.valueSetValueInfos") }
val PlsKeys.cachedElementPath by lazy { Key.create<CachedValue<ParadoxElementPath>>("paradox.cached.elementPath") }
val PlsKeys.cachedConfigContext by lazy { Key.create<CachedValue<ParadoxConfigContext>>("paradox.cached.configContext") }
val PlsKeys.cachedConfigsCache by lazy { Key.create<CachedValue<MutableMap<String, List<CwtMemberConfig<*>>>>>("paradox.cached.configs.cache") }
val PlsKeys.cachedChildOccurrenceMapCache by lazy { Key.create<CachedValue<MutableMap<String, Map<CwtDataExpression, Occurrence>>>>("paradox.cached.childOccurrenceMap.cache") }
val PlsKeys.cachedScopeContext by lazy { Key.create<CachedValue<ParadoxScopeContext>>("paradox.cached.scopeContext") }
val PlsKeys.cachedTextColorInfo by lazy { Key.create<CachedValue<ParadoxTextColorInfo>>("paradox.cached.textColorInfo") }
val PlsKeys.cachedDefineValues by lazy { Key.create<CachedValue<MutableMap<String, Any?>>>("paradox.cached.defineValues") }
val PlsKeys.cachedColor by lazy { Key.create<CachedValue<Color>>("paradox.cached.color") }
val PlsKeys.cachedParameterContextInfo by lazy { Key.create<CachedValue<ParadoxParameterContextInfo>>("paradox.cached.parameterContextInfo") }
val PlsKeys.cachedGameConceptAlias by lazy { Key.create<CachedValue<Set<String>>>("paradox.cached.gameConcept.alias") }

//用于将CWT规则临时写入到CWT元素的userData中（例如，解析引用为枚举值后，将会是对应的CwtEnumConfig）
val PlsKeys.cwtConfig by lazy { Key.create<CwtConfig<*>>("paradox.cwtConfig") }
//用于在进行代码补全时标记一个property的propertyValue未填写
val PlsKeys.isIncomplete by lazy { Key.create<Boolean>("paradox.isIncomplete") }
//用于在进行代码补全时标记作用域不匹配
val PlsKeys.scopeMismatched by lazy { Key.create<Boolean>("paradox.scopeMismatched") }
//用于在进行颜色高亮时标记参数在脚本表达式中的文本范围
val PlsKeys.parameterRanges by lazy { Key.create<List<TextRange>>("paradox.parameterRanges") }
//用于标记图标的帧数以便后续对原始的DDS图片进行切分
val PlsKeys.iconFrame by lazy { Key.create<Int>("paradox.icon.frame") }