@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.lang.util

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.model.expressionInfo.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

object ParadoxComplexEnumValueManager {
    fun getInfo(element: ParadoxComplexEnumValueElement): ParadoxComplexEnumValueInfo {
        return ParadoxComplexEnumValueInfo(element.name, element.enumName, element.readWriteAccess, element.startOffset, element.gameType)
    }
    
    fun getInfo(element: ParadoxScriptStringExpressionElement): ParadoxComplexEnumValueInfo? {
        return doGetInfoFromCache(element)
    }
    
    private fun doGetInfoFromCache(element: ParadoxScriptStringExpressionElement): ParadoxComplexEnumValueInfo? {
        //invalidated on file modification
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedComplexEnumValueInfo) {
            ProgressManager.checkCanceled()
            val file = element.containingFile ?: return@getCachedValue null
            val value = doGetInfo(element, file)
            CachedValueProvider.Result.create(value, file)
        }
    }
    
    private fun doGetInfo(element: ParadoxScriptStringExpressionElement, file: PsiFile): ParadoxComplexEnumValueInfo? {
        if(element.text.isParameterized()) return null //排除可能带参数的情况
        if(element.text.isInlineUsage()) return null //排除是内联调用的情况
        val project = file.project
        val fileInfo = file.fileInfo ?: return null
        val path = fileInfo.path //这里使用pathToEntry
        val gameType = fileInfo.rootInfo.gameType
        val configGroup = getConfigGroup(project, gameType)
        for(complexEnumConfig in configGroup.complexEnums.values) {
            if(CwtConfigManager.matchesPath(complexEnumConfig, path)) {
                if(matchesComplexEnum(complexEnumConfig, element)) {
                    val name = getName(element.value) ?: continue
                    val enumName = complexEnumConfig.name
                    val readWriteAccess = Access.Write //write (declaration)
                    val elementOffset = element.startOffset
                    return ParadoxComplexEnumValueInfo(name, enumName, readWriteAccess, elementOffset, gameType)
                }
            }
        }
        return null
    }
    
    //NOTE 这里匹配时并不兼容向下内联的情况
    
    fun matchesComplexEnum(complexEnumConfig: CwtComplexEnumConfig, element: ParadoxScriptStringExpressionElement): Boolean {
        for(enumNameConfig in complexEnumConfig.enumNameConfigs) {
            if(doMatchEnumName(complexEnumConfig, enumNameConfig, element)) return true
        }
        return false
    }
    
    private fun doMatchEnumName(complexEnumConfig: CwtComplexEnumConfig, config: CwtMemberConfig<*>, element: ParadoxScriptStringExpressionElement): Boolean {
        if(config is CwtPropertyConfig) {
            if(config.key == "enum_name") {
                if(element !is ParadoxScriptPropertyKey) return false
                val valueElement = element.propertyValue ?: return false
                if(!doMatchValue(complexEnumConfig, config, valueElement)) return false
            } else if(config.stringValue == "enum_name") {
                if(element !is ParadoxScriptString || !element.isPropertyValue()) return false
                val propertyElement = element.parent?.castOrNull<ParadoxScriptProperty>() ?: return false
                if(!doMatchKey(complexEnumConfig, config, propertyElement)) return false
            } else {
                return false
            }
        } else if(config is CwtValueConfig) {
            if(config.stringValue == "enum_name") {
                if(element !is ParadoxScriptString || !element.isBlockMember()) return false
            } else {
                return false
            }
        }
        return doBeforeMatchParent(config, element, complexEnumConfig)
    }
    
    private fun doMatchParent(complexEnumConfig: CwtComplexEnumConfig, config: CwtMemberConfig<*>, element: PsiElement): Boolean {
        if(config is CwtPropertyConfig) {
            //match key only
            if(element !is ParadoxScriptProperty) return false
            if(!doMatchKey(complexEnumConfig, config, element)) return false
        } else if(config is CwtValueConfig) {
            //blockConfig vs blockElement 
            if(element !is ParadoxScriptBlockElement) return false
        } else {
            return false
        }
        return doBeforeMatchParent(config, element, complexEnumConfig)
    }
    
    private fun doBeforeMatchParent(config: CwtMemberConfig<*>, element: PsiElement, complexEnumConfig: CwtComplexEnumConfig): Boolean {
        val parentConfig = config.parentConfig ?: return false
        val parentBlockElement = element.parentOfType<ParadoxScriptBlockElement>() ?: return false
        val parentElement = when {
            parentBlockElement is ParadoxScriptRootBlock -> null
            parentBlockElement is ParadoxScriptBlock && !parentBlockElement.isPropertyValue() -> parentBlockElement
            else -> parentBlockElement.parentOfType<ParadoxScriptProperty>()
        }
        if(parentConfig == complexEnumConfig.nameConfig) {
            if(complexEnumConfig.startFromRoot) {
                return parentElement == null
            } else {
                return parentElement != null && parentElement.findParentPropertyOrBockValue() == null
            }
        }
        if(parentElement == null) return false
        parentConfig.configs?.forEach { c ->
            ProgressManager.checkCanceled()
            when {
                c is CwtPropertyConfig -> {
                    //ignore same config or enum name config
                    if(c == config || c.key == "enum_name" || c.stringValue == "enum_name") return@forEach
                    val notMatched = parentBlockElement.processProperty(inline = true) { propElement ->
                        !doMatchProperty(complexEnumConfig, c, propElement)
                    }
                    if(notMatched) return false
                }
                c is CwtValueConfig -> {
                    //ignore same config or enum name config
                    if(c == config || c.stringValue == "enum_name") return@forEach
                    val notMatched = parentBlockElement.processValue(inline = true) { valueElement ->
                        !doMatchValue(complexEnumConfig, c, valueElement)
                    }
                    if(notMatched) return false
                }
            }
        }
        return doMatchParent(complexEnumConfig, parentConfig, parentElement)
    }
    
    private fun doMatchProperty(complexEnumConfig: CwtComplexEnumConfig, config: CwtPropertyConfig, propertyElement: ParadoxScriptProperty): Boolean {
        return doMatchKey(complexEnumConfig, config, propertyElement)
            && doMatchValue(complexEnumConfig, config, propertyElement.propertyValue ?: return false)
    }
    
    private fun doMatchKey(complexEnumConfig: CwtComplexEnumConfig, config: CwtPropertyConfig, propertyElement: ParadoxScriptProperty): Boolean {
        val key = config.key
        if(key == "scalar" || key == "enum_name") return true
        return key.equals(propertyElement.name, true)
    }
    
    private fun doMatchValue(complexEnumConfig: CwtComplexEnumConfig, config: CwtMemberConfig<*>, valueElement: ParadoxScriptValue): Boolean {
        if(config.isBlock) {
            val blockElement = valueElement.castOrNull<ParadoxScriptBlockElement>() ?: return false
            if(!doMatchBlock(complexEnumConfig, config, blockElement)) return false
            return true
        } else if(config.stringValue != null) {
            val stringElement = valueElement.castOrNull<ParadoxScriptString>() ?: return false
            if(!doMatchString(complexEnumConfig, config, stringElement)) return false
            return true
        } else if(config.booleanValue != null) {
            val booleanElement = valueElement.castOrNull<ParadoxScriptBoolean>() ?: return false
            if(!doMatchBoolean(complexEnumConfig, config, booleanElement)) return false
            return true
        } else {
            return false
        }
    }
    
    private fun doMatchString(complexEnumConfig: CwtComplexEnumConfig, config: CwtMemberConfig<*>, stringElement: ParadoxScriptString): Boolean {
        val stringValue = config.stringValue!!
        if(stringValue == "scalar" || stringValue == "enum_name") return true
        return stringValue.equals(stringElement.value, true)
    }
    
    private fun doMatchBoolean(complexEnumConfig: CwtComplexEnumConfig, config: CwtMemberConfig<*>, booleanElement: ParadoxScriptBoolean): Boolean {
        val booleanValue = config.booleanValue!!
        return booleanValue == booleanElement.booleanValue
    }
    
    private fun doMatchBlock(complexEnumConfig: CwtComplexEnumConfig, config: CwtMemberConfig<*>, blockElement: ParadoxScriptBlockElement): Boolean {
        config.properties?.forEach { propConfig ->
            ProgressManager.checkCanceled()
            val notMatched = blockElement.processProperty(inline = true) { propElement ->
                !doMatchProperty(complexEnumConfig, propConfig, propElement)
            }
            if(notMatched) return false
        }
        config.values?.forEach { valueConfig ->
            ProgressManager.checkCanceled()
            val notMatched = blockElement.processValue(inline = true) { valueElement ->
                !doMatchValue(complexEnumConfig, valueConfig, valueElement)
            }
            if(notMatched) return false
        }
        return true
    }
    
    /**
     * @return [ParadoxScriptProperty] | [ParadoxScriptValue]
     */
    private fun PsiElement.findParentPropertyOrBockValue(): PsiElement? {
        if(language != ParadoxScriptLanguage) return null
        return parents(false).find {
            it is ParadoxScriptProperty || (it is ParadoxScriptValue && it.isBlockMember())
        }
    }
    
    fun getName(expression: String): String? {
        return expression.orNull()
    }
}
