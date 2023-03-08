@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.lang

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

/**
 * 用于处理复杂枚举信息。
 */
object ParadoxComplexEnumValueHandler {
	@JvmStatic
	fun getInfo(element: ParadoxScriptStringExpressionElement): ParadoxComplexEnumValueInfo? {
		ProgressManager.checkCanceled()
		if(!element.isExpression()) return null
		element.stub?.complexEnumValueInfo?.let { return it }
		return getInfoFromCache(element)
	}
	
	private fun getInfoFromCache(element: ParadoxScriptStringExpressionElement): ParadoxComplexEnumValueInfo? {
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedComplexEnumValueInfoKey) {
			val file = element.containingFile
			val value = resolveInfo(element, file)
			//invalidated on file modification
			CachedValueProvider.Result.create(value, file)
		}
	}
	
	@JvmStatic
	fun resolveInfo(element: ParadoxScriptStringExpressionElement, file: PsiFile = element.containingFile): ParadoxComplexEnumValueInfo? {
		//排除带参数的情况
		if(element.isParameterAwareExpression()) return null
		
		val project = file.project
		
		//首先尝试直接基于stub进行解析
		val stub = runCatching { element.stub }.getOrNull()
		if(stub != null) {
			return resolveByStub(element, stub)
		}
		
		ProgressManager.checkCanceled()
		val fileInfo = file.fileInfo ?: return null
		val path = fileInfo.entryPath //这里使用entryPath
		val gameType = fileInfo.rootInfo.gameType
		val configGroup = getCwtConfig(project).getValue(gameType)
		return doResolve(element, path, configGroup)
	}
	
	private fun resolveByStub(element: ParadoxScriptStringExpressionElement, stub: ParadoxScriptStringExpressionElementStub<*>): ParadoxComplexEnumValueInfo? {
		return stub.complexEnumValueInfo
	}
	
	private fun doResolve(element: ParadoxScriptStringExpressionElement, path: ParadoxPath, configGroup: CwtConfigGroup): ParadoxComplexEnumValueInfo? {
		for(complexEnumConfig in configGroup.complexEnums.values) {
			if(matchesComplexEnumByPath(complexEnumConfig, path)) {
				if(matchesComplexEnum(complexEnumConfig, element)) {
					val name = element.value
					val enumName = complexEnumConfig.name
					return ParadoxComplexEnumValueInfo(name, enumName, configGroup.gameType)
				}
			}
		}
		return null
	}
	
	@JvmStatic
	fun matchesComplexEnumByPath(complexEnum: CwtComplexEnumConfig, path: ParadoxPath): Boolean {
		return complexEnum.path.any {
			(complexEnum.pathFile == null || complexEnum.pathFile == path.fileName)
				&& (if(complexEnum.pathStrict) it == path.parent else it.matchesPath(path.parent))
		}
	}
	
	@JvmStatic
	fun matchesComplexEnum(complexEnumConfig: CwtComplexEnumConfig, element: ParadoxScriptStringExpressionElement): Boolean {
		for(enumNameConfig in complexEnumConfig.enumNameConfigs) {
			if(doMatchEnumName(complexEnumConfig, enumNameConfig, element)) return true
		}
		return false
	}
	
	private fun doMatchEnumName(complexEnumConfig: CwtComplexEnumConfig, config: CwtDataConfig<*>, element: ParadoxScriptStringExpressionElement): Boolean {
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
				if(element !is ParadoxScriptString || !element.isBlockValue()) return false
			} else {
				return false
			}
		}
		return doBeforeMatchParent(config, element, complexEnumConfig)
	}
	
	private fun doMatchParent(complexEnumConfig: CwtComplexEnumConfig, config: CwtDataConfig<*>, element: PsiElement): Boolean {
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
	
	private fun doBeforeMatchParent(config: CwtDataConfig<*>, element: PsiElement, complexEnumConfig: CwtComplexEnumConfig): Boolean {
		val parentConfig = config.parent ?: return false
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
		parentConfig.properties?.forEach { propConfig ->
			//ignore same config or enum name config
			if(propConfig == config || propConfig.key == "enum_name" || propConfig.stringValue == "enum_name") return@forEach
			val notMatched = parentBlockElement.processProperty(inline = true) { propElement ->
				!doMatchProperty(complexEnumConfig, propConfig, propElement)
			}
			if(notMatched) return false
		}
		parentConfig.values?.forEach { valueConfig ->
			//ignore same config or enum name config
			if(valueConfig == config || valueConfig.stringValue == "enum_name") return@forEach
			val notMatched = parentBlockElement.processValue(inline = true) { valueElement ->
				!doMatchValue(complexEnumConfig, valueConfig, valueElement)
			}
			if(notMatched) return false
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
	
	private fun doMatchValue(complexEnumConfig: CwtComplexEnumConfig, config: CwtDataConfig<*>, valueElement: ParadoxScriptValue): Boolean {
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
	
	private fun doMatchString(complexEnumConfig: CwtComplexEnumConfig, config: CwtDataConfig<*>, stringElement: ParadoxScriptString): Boolean {
		val stringValue = config.stringValue!!
		if(stringValue == "scalar" || stringValue == "enum_name") return true
		return stringValue.equals(stringElement.value, true)
	}
	
	private fun doMatchBoolean(complexEnumConfig: CwtComplexEnumConfig, config: CwtDataConfig<*>, booleanElement: ParadoxScriptBoolean): Boolean {
		val booleanValue = config.booleanValue!!
		return booleanValue == booleanElement.booleanValue
	}
	
	private fun doMatchBlock(complexEnumConfig: CwtComplexEnumConfig, config: CwtDataConfig<*>, blockElement: ParadoxScriptBlockElement): Boolean {
		config.properties?.forEach { propConfig ->
			val notMatched = blockElement.processProperty(inline = true) { propElement ->
				!doMatchProperty(complexEnumConfig, propConfig, propElement)
			}
			if(notMatched) return false
		}
		config.values?.forEach { valueConfig ->
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
			it is ParadoxScriptProperty || (it is ParadoxScriptValue && it.isBlockValue())
		}
	}
	
	@JvmStatic
	fun getName(element: ParadoxScriptStringExpressionElement): String? {
		val stub     = runCatching { element.stub }.getOrNull()
		val name = stub?.complexEnumValueInfo?.name ?: element.value
		return name.takeIfNotEmpty()
	}
}
