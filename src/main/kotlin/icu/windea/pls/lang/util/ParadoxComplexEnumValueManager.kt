@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.lang.util

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.indexInfo.*
import icu.windea.pls.script.psi.*

object ParadoxComplexEnumValueManager {
    object Keys : KeyRegistry() {
        val cachedComplexEnumValueInfo by createKey<CachedValue<ParadoxComplexEnumValueIndexInfo>>(Keys)
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
            ProgressManager.checkCanceled()
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
        for (complexEnumConfig in configGroup.complexEnums.values) {
            if (!matchesComplexEnum(element, complexEnumConfig, path)) continue
            return complexEnumConfig
        }
        return null
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
                    val notMatched = parentBlockElement.processProperty(inline = true) { propElement ->
                        !doMatchProperty(propElement, c, complexEnumConfig)
                    }
                    if (notMatched) return false
                }
                c is CwtValueConfig -> {
                    //ignore same config or enum name config
                    if (c == config || c.stringValue == "enum_name") return@forEach
                    val notMatched = parentBlockElement.processValue(inline = true) { valueElement ->
                        !doMatchValue(valueElement, c, complexEnumConfig)
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
            val notMatched = blockElement.processProperty(inline = true) { propElement ->
                !doMatchProperty(propElement, propConfig, complexEnumConfig)
            }
            if (notMatched) return false
        }
        config.values?.forEach { valueConfig ->
            ProgressManager.checkCanceled()
            val notMatched = blockElement.processValue(inline = true) { valueElement ->
                !doMatchValue(valueElement, valueConfig, complexEnumConfig)
            }
            if (notMatched) return false
        }
        return true
    }

    fun getName(expression: String): String? {
        return expression.orNull()
    }

    fun getNameLocalisation(name: String, contextElement: PsiElement, locale: CwtLocaleConfig): ParadoxLocalisationProperty? {
        val selector = selector(contextElement.project, contextElement).localisation().contextSensitive()
            .preferLocale(locale)
        return ParadoxLocalisationSearch.search(name, selector).find()
    }

    fun getNameLocalisationFromExtendedConfig(name: String, enumName: String, contextElement: PsiElement): ParadoxLocalisationProperty? {
        val hint = getHintFromExtendedConfig(name, enumName, contextElement) //just use file as contextElement here
        if (hint.isNullOrEmpty()) return null
        val hintLocalisation = ParadoxLocalisationElementFactory.createProperty(contextElement.project, "hint", hint)
        //it's necessary to inject fileInfo here (so that gameType can be got later)
        hintLocalisation.containingFile.virtualFile.putUserData(PlsKeys.injectedFileInfo, contextElement.fileInfo)
        return hintLocalisation
    }

    fun getHintFromExtendedConfig(name: String, enumName: String, contextElement: PsiElement): String? {
        if (name.isEmpty()) return null
        val gameType = selectGameType(contextElement) ?: return null
        val configGroup = PlsFacade.getConfigGroup(contextElement.project, gameType)
        val configs = configGroup.extendedComplexEnumValues[enumName] ?: return null
        val config = configs.findFromPattern(name, contextElement, configGroup) ?: return null
        return config.hint
    }
}
