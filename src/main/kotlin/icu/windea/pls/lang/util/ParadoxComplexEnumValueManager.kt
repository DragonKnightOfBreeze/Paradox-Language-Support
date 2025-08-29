@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.lang.util

import com.google.common.cache.CacheBuilder
import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.Access
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parents
import com.intellij.psi.util.startOffset
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtComplexEnumConfig
import icu.windea.pls.config.config.CwtLocaleConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.booleanValue
import icu.windea.pls.config.config.floatValue
import icu.windea.pls.config.config.intValue
import icu.windea.pls.config.config.isBlock
import icu.windea.pls.config.config.properties
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.config.values
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.complexEnums
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.buildCache
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isInlineUsage
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.dataFlow.options
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.indexInfo.ParadoxComplexEnumValueIndexInfo
import icu.windea.pls.model.paths.ParadoxPath
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptBlockElement
import icu.windea.pls.script.psi.ParadoxScriptBoolean
import icu.windea.pls.script.psi.ParadoxScriptFloat
import icu.windea.pls.script.psi.ParadoxScriptInt
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptRootBlock
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.booleanValue
import icu.windea.pls.script.psi.floatValue
import icu.windea.pls.script.psi.intValue
import icu.windea.pls.script.psi.isBlockMember
import icu.windea.pls.script.psi.isPropertyValue
import icu.windea.pls.script.psi.properties
import icu.windea.pls.script.psi.propertyValue
import icu.windea.pls.script.psi.values

object ParadoxComplexEnumValueManager {
    object Keys : KeyRegistry() {
        val cachedComplexEnumValueInfo by createKey<CachedValue<ParadoxComplexEnumValueIndexInfo>>(Keys)
    }

    private val CwtConfigGroup.complexEnumConfigsCache by createKey(CwtConfigGroup.Keys) {
        CacheBuilder.newBuilder().buildCache<ParadoxPath, List<CwtComplexEnumConfig>> { path ->
            complexEnums.values.filter { CwtConfigManager.matchesFilePathPattern(it, path) }.optimized()
        }
    }

    fun getInfo(element: ParadoxComplexEnumValueElement): ParadoxComplexEnumValueIndexInfo {
        return ParadoxComplexEnumValueIndexInfo(element.name, element.enumName, element.readWriteAccess, element.startOffset, element.gameType)
    }

    fun getInfo(element: ParadoxScriptStringExpressionElement): ParadoxComplexEnumValueIndexInfo? {
        return doGetInfoFromCache(element)
    }

    private fun doGetInfoFromCache(element: ParadoxScriptStringExpressionElement): ParadoxComplexEnumValueIndexInfo? {
        //invalidated on file modification
        return CachedValuesManager.getCachedValue(element, Keys.cachedComplexEnumValueInfo) {
            val file = element.containingFile
            val value = doGetInfo(element, file)
            value.withDependencyItems(file)
        }
    }

    private fun doGetInfo(element: ParadoxScriptStringExpressionElement, file: PsiFile = element.containingFile): ParadoxComplexEnumValueIndexInfo? {
        if (element.text.isParameterized()) return null //排除可能带参数的情况
        if (element.text.isInlineUsage()) return null //排除是内联调用的情况
        val project = file.project
        val fileInfo = file.fileInfo ?: return null
        val path = fileInfo.path
        val gameType = fileInfo.rootInfo.gameType
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val complexEnumConfig = getMatchedComplexEnumConfig(element, configGroup, path)
        if (complexEnumConfig == null) return null
        val name = getName(element.value) ?: return null
        val enumName = complexEnumConfig.name
        val readWriteAccess = Access.Write //write (declaration)
        val elementOffset = element.startOffset
        return ParadoxComplexEnumValueIndexInfo(name, enumName, readWriteAccess, elementOffset, gameType)
    }

    //NOTE 这里匹配时并不兼容向下内联的情况

    fun getMatchedComplexEnumConfig(element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup, path: ParadoxPath): CwtComplexEnumConfig? {
        //优先从基于文件路径的缓存中获取
        val configs = configGroup.complexEnumConfigsCache.get(path)
        if (configs.isEmpty()) return null
        return configs.find { config -> matchesComplexEnum(element, config, null) }
    }

    fun matchesComplexEnum(element: ParadoxScriptStringExpressionElement, complexEnumConfig: CwtComplexEnumConfig, path: ParadoxPath?): Boolean {
        if (path != null) {
            if (!CwtConfigManager.matchesFilePathPattern(complexEnumConfig, path)) return false
        }

        for (enumNameConfig in complexEnumConfig.enumNameConfigs) {
            if (doMatchEnumName(element, enumNameConfig, complexEnumConfig)) return true
        }
        return false
    }

    private fun doMatchEnumName(element: ParadoxScriptStringExpressionElement, config: CwtMemberConfig<*>, complexEnumConfig: CwtComplexEnumConfig): Boolean {
        if (config is CwtPropertyConfig) {
            if (config.key == "enum_name") {
                if (element !is ParadoxScriptPropertyKey) return false
                val valueElement = element.propertyValue ?: return false
                if (!doMatchValue(valueElement, config, complexEnumConfig)) return false
            } else if (config.stringValue == "enum_name") {
                if (element !is ParadoxScriptString || !element.isPropertyValue()) return false
                val propertyElement = element.parent?.castOrNull<ParadoxScriptProperty>() ?: return false
                if (!doMatchKey(propertyElement, config, complexEnumConfig)) return false
            } else {
                return false
            }
        } else if (config is CwtValueConfig) {
            if (config.stringValue == "enum_name") {
                if (element !is ParadoxScriptString || !element.isBlockMember()) return false
            } else {
                return false
            }
        }
        return doBeforeMatchParent(element, config, complexEnumConfig)
    }

    private fun doMatchParent(element: PsiElement, config: CwtMemberConfig<*>, complexEnumConfig: CwtComplexEnumConfig): Boolean {
        if (config is CwtPropertyConfig) {
            //match key only
            if (element !is ParadoxScriptProperty) return false
            if (!doMatchKey(element, config, complexEnumConfig)) return false
        } else if (config is CwtValueConfig) {
            //blockConfig vs blockElement
            if (element !is ParadoxScriptBlockElement) return false
        } else {
            return false
        }
        return doBeforeMatchParent(element, config, complexEnumConfig)
    }

    private fun doBeforeMatchParent(element: PsiElement, config: CwtMemberConfig<*>, complexEnumConfig: CwtComplexEnumConfig): Boolean {
        val parentConfig = config.parentConfig ?: return false
        val parentBlockElement = element.parentOfType<ParadoxScriptBlockElement>() ?: return false
        val parentElement = when {
            parentBlockElement is ParadoxScriptRootBlock -> null
            parentBlockElement is ParadoxScriptBlock && !parentBlockElement.isPropertyValue() -> parentBlockElement
            else -> parentBlockElement.parentOfType<ParadoxScriptProperty>()
        }
        if (parentConfig == complexEnumConfig.nameConfig) {
            if (complexEnumConfig.startFromRoot) {
                return parentElement == null
            } else {
                return parentElement != null && parentElement.parents(false).find { isPropertyOrBlockValue(it) } == null
            }
        }
        if (parentElement == null) return false
        parentConfig.configs?.forEach { c ->
            ProgressManager.checkCanceled()
            when {
                c is CwtPropertyConfig -> {
                    //ignore same config or enum name config
                    if (c == config || c.key == "enum_name" || c.stringValue == "enum_name") return@forEach
                    val notMatched = parentBlockElement.properties().options(inline = true).none { propElement ->
                        doMatchProperty(propElement, c, complexEnumConfig)
                    }
                    if (notMatched) return false
                }
                c is CwtValueConfig -> {
                    //ignore same config or enum name config
                    if (c == config || c.stringValue == "enum_name") return@forEach
                    val notMatched = parentBlockElement.values().options(inline = true).none { valueElement ->
                        doMatchValue(valueElement, c, complexEnumConfig)
                    }
                    if (notMatched) return false
                }
            }
        }
        return doMatchParent(parentElement, parentConfig, complexEnumConfig)
    }

    private fun isPropertyOrBlockValue(element1: PsiElement): Boolean {
        return element1 is ParadoxScriptProperty || (element1 is ParadoxScriptValue && element1.isBlockMember())
    }

    private fun doMatchProperty(propertyElement: ParadoxScriptProperty, config: CwtPropertyConfig, complexEnumConfig: CwtComplexEnumConfig): Boolean {
        return doMatchKey(propertyElement, config, complexEnumConfig)
            && doMatchValue(propertyElement.propertyValue ?: return false, config, complexEnumConfig)
    }

    private fun doMatchKey(propertyElement: ParadoxScriptProperty, config: CwtPropertyConfig, complexEnumConfig: CwtComplexEnumConfig): Boolean {
        val key = config.key
        when (key) {
            "enum_name" -> return true
            "scalar" -> return true
        }
        return key.equals(propertyElement.name, true)
    }

    private fun doMatchValue(valueElement: ParadoxScriptValue, config: CwtMemberConfig<*>, complexEnumConfig: CwtComplexEnumConfig): Boolean {
        if (config.isBlock) {
            val blockElement = valueElement.castOrNull<ParadoxScriptBlockElement>() ?: return false
            if (!doMatchBlock(blockElement, config, complexEnumConfig)) return false
            return true
        } else if (config.stringValue != null) {
            when (config.stringValue) {
                "enum_name" -> return true
                "scalar" -> return true
                "float" -> return valueElement is ParadoxScriptFloat
                "int" -> return valueElement is ParadoxScriptInt
                "bool" -> return valueElement is ParadoxScriptBoolean
            }
            val stringElement = valueElement.castOrNull<ParadoxScriptString>() ?: return false
            return stringElement.value.equals(config.stringValue, true)
        } else if (config.floatValue != null) {
            val floatElement = valueElement.castOrNull<ParadoxScriptFloat>() ?: return false
            return floatElement.floatValue == config.floatValue
        } else if (config.intValue != null) {
            val intElement = valueElement.castOrNull<ParadoxScriptInt>() ?: return false
            return intElement.intValue == config.intValue
        } else if (config.booleanValue != null) {
            val booleanElement = valueElement.castOrNull<ParadoxScriptBoolean>() ?: return false
            return booleanElement.booleanValue == config.booleanValue
        } else {
            return false
        }
    }

    private fun doMatchBlock(blockElement: ParadoxScriptBlockElement, config: CwtMemberConfig<*>, complexEnumConfig: CwtComplexEnumConfig): Boolean {
        config.properties?.forEach { propConfig ->
            ProgressManager.checkCanceled()
            val notMatched = blockElement.properties().options(inline = true).none { propElement ->
                doMatchProperty(propElement, propConfig, complexEnumConfig)
            }
            if (notMatched) return false
        }
        config.values?.forEach { valueConfig ->
            ProgressManager.checkCanceled()
            val notMatched = blockElement.values().options(inline = true).none { valueElement ->
                doMatchValue(valueElement, valueConfig, complexEnumConfig)
            }
            if (notMatched) return false
        }
        return true
    }

    fun getName(expression: String): String? {
        return expression.orNull()
    }

    fun getNameLocalisation(name: String, contextElement: PsiElement, locale: CwtLocaleConfig): ParadoxLocalisationProperty? {
        val selector = selector(contextElement.project, contextElement).localisation().contextSensitive().preferLocale(locale)
        return ParadoxLocalisationSearch.search(name, selector).find()
    }
}
