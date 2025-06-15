package icu.windea.pls.lang

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.indexInfo.*
import icu.windea.pls.script.injection.*
import java.awt.*

object PlsKeys : KeyRegistry()

val PlsKeys.library by createKey<ParadoxLibrary>(PlsKeys)
val PlsKeys.configGroupLibrary by createKey<CwtConfigGroupLibrary>(PlsKeys)

val PlsKeys.rootInfo by createKey<Any>(PlsKeys)
val PlsKeys.fileInfo by createKey<Any>(PlsKeys)
val PlsKeys.localeConfig by createKey<Any>(PlsKeys)

//用于为临时文件（VirtualFile）嵌入根目录信息
val PlsKeys.injectedRootInfo by createKey<ParadoxRootInfo>(PlsKeys)
//用于为临时文件（VirtualFile）嵌入文件信息
val PlsKeys.injectedFileInfo by createKey<ParadoxFileInfo>(PlsKeys)
//用于为脚本文件（VirtualFile）嵌入语言区域
val PlsKeys.injectedLocaleConfig by createKey<CwtLocaleConfig>(PlsKeys)
//用于为脚本文件（VirtualFile）嵌入表达式路径前缀
val PlsKeys.injectedElementPathPrefix by createKey<ParadoxExpressionPath>(PlsKeys)

val PlsKeys.cachedDefinitionInfo by createKey<CachedValue<ParadoxDefinitionInfo>>(PlsKeys)
val PlsKeys.cachedDefinitionPrimaryLocalisationKey by createKey<CachedValue<String>>(PlsKeys)
val PlsKeys.cachedDefinitionPrimaryLocalisation by createKey<CachedValue<ParadoxLocalisationProperty>>(PlsKeys)
val PlsKeys.cachedDefinitionPrimaryLocalisations by createKey<CachedValue<Set<ParadoxLocalisationProperty>>>(PlsKeys)
val PlsKeys.cachedDefinitionLocalizedNames by createKey<CachedValue<Set<String>>>(PlsKeys)
val PlsKeys.cachedDefinitionPrimaryImage by createKey<CachedValue<PsiFile>>(PlsKeys)
val PlsKeys.cachedLocalisationInfo by createKey<CachedValue<ParadoxLocalisationInfo>>(PlsKeys)
val PlsKeys.cachedComplexEnumValueInfo by createKey<CachedValue<ParadoxComplexEnumValueIndexInfo>>(PlsKeys)
val PlsKeys.cachedDynamicValueInfos by createKey<CachedValue<List<ParadoxDynamicValueIndexInfo>>>(PlsKeys)
val PlsKeys.cachedElementPath by createKey<CachedValue<ParadoxExpressionPath>>(PlsKeys)
val PlsKeys.cachedScopeContext by createKey<CachedValue<ParadoxScopeContext>>(PlsKeys)
val PlsKeys.cachedTextColorInfo by createKey<CachedValue<ParadoxTextColorInfo>>(PlsKeys)
val PlsKeys.cachedDefineValues by createKey<CachedValue<MutableMap<String, Any?>>>(PlsKeys)
val PlsKeys.cachedColor by createKey<CachedValue<Color>>(PlsKeys)
val PlsKeys.cachedParameterContextInfo by createKey<CachedValue<ParadoxParameterContextInfo>>(PlsKeys)
val PlsKeys.cachedGameConceptAlias by createKey<CachedValue<Set<String>>>(PlsKeys)
val PlsKeys.cachedParameterRanges by createKey<CachedValue<List<TextRange>>>(PlsKeys)

val PlsKeys.parameterValueInjectionInfos by createKey<List<ParadoxParameterValueInjectionInfo>>(PlsKeys)

//用于将CWT规则临时写入到CWT元素的userData中（例如，解析引用为枚举值后，将会是对应的CwtEnumConfig）
val PlsKeys.bindingConfig by createKey<CwtConfig<*>>(PlsKeys)
//用于标记快速文档使用的本地化语言区域
val PlsKeys.documentationLocale by createKey<String>(PlsKeys)
//用于标记图片的帧数信息以便后续进行切分
val PlsKeys.imageFrameInfo by createKey<ImageFrameInfo>(PlsKeys)
