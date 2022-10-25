@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.core.handler

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于处理复杂枚举信息。
 */
object ParadoxComplexEnumValueInfoHandler {
	@JvmStatic
	fun get(element: ParadoxScriptExpressionElement): ParadoxComplexEnumValueInfo? {
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedComplexEnumValueInfoKey) {
			val file = element.containingFile
			val value = resolve(element, file)
			CachedValueProvider.Result.create(value, file)//invalidated on file modification
		}
	}
	
	@JvmStatic
	fun resolve(element: ParadoxScriptExpressionElement, file: PsiFile = element.containingFile): ParadoxComplexEnumValueInfo? {
		//排除带参数的情况
		if(element.isParameterAwareExpression()) return null
		
		val project = file.project
		
		//首先尝试直接基于stub进行解析
		val stub = kotlin.runCatching { element.stub }.getOrNull()
		if(stub != null) {
			return resolveByStub(element, stub)
		}
		
		ProgressManager.checkCanceled()
		val fileInfo = file.fileInfo ?: return null
		val path = fileInfo.path
		val gameType = fileInfo.rootInfo.gameType
		val configGroup = getCwtConfig(project).getValue(gameType)
		return doResolve(element, path, configGroup)
	}
	
	private fun resolveByStub(element: ParadoxScriptExpressionElement, stub: ParadoxScriptExpressionElementStub<*>): ParadoxComplexEnumValueInfo? {
		return stub.complexEnumValueInfo
	}
	
	private fun doResolve(element: ParadoxScriptExpressionElement, path: ParadoxPath, configGroup: CwtConfigGroup): ParadoxComplexEnumValueInfo? {
		for(complexEnumConfig in configGroup.complexEnums.values) {
			if(matchComplexEnumByPath(complexEnumConfig, path)) {
				//DEBUG
				//if(element.value != "reform_and_opening_up" || element.findParentDefinitionProperty()?.name != "policy_flags") continue
				if(matchComplexEnum(complexEnumConfig, element)) {
					val name = element.value
					val enumName = complexEnumConfig.name
					return ParadoxComplexEnumValueInfo(name, enumName, configGroup.gameType)
				}
			}
		}
		return null
	}
	
	@JvmStatic
	fun matchComplexEnumByPath(complexEnum: CwtComplexEnumConfig, path: ParadoxPath): Boolean {
		return complexEnum.path.any {
			(complexEnum.pathFile == null || complexEnum.pathFile == path.fileName)
				&& (if(complexEnum.pathStrict) it == path.parent else it.matchesPath(path.parent))
		}
	}
	
	@JvmStatic
	fun matchComplexEnum(complexEnumConfig: CwtComplexEnumConfig, element: ParadoxScriptExpressionElement): Boolean {
		for(enumNameConfig in complexEnumConfig.enumNameConfigs) {
			if(doMatchEnumNameConfig(complexEnumConfig, enumNameConfig, element)) return true
		}
		return false
	}
	
	private fun doMatchEnumNameConfig(complexEnumConfig: CwtComplexEnumConfig, config: CwtKvConfig<*>, element: ParadoxScriptExpressionElement): Boolean {
		if(config is CwtPropertyConfig) {
			if(config.key == "enum_name") {
				if(element !is ParadoxScriptPropertyKey) return false
				val valueElement = element.propertyValue?.value ?: return false
				if(!doMatchValueConfig(complexEnumConfig, config, valueElement)) return false
			} else if(config.stringValue == "enum_name") {
				if(element !is ParadoxScriptString || element.isLonely()) return false
				val propertyElement = element.parentOfType<ParadoxScriptProperty>() ?: return false
				if(!doMatchKeyConfig(complexEnumConfig, config, propertyElement)) return false
			} else {
				return false
			}
		} else if(config is CwtValueConfig) {
			if(config.stringValue == "enum_name") {
				if(element !is ParadoxScriptString || !element.isLonely()) return false
			} else {
				return false
			}
		}
		return doBeforeMatchParentConfig(config, element, complexEnumConfig)
	}
	
	private fun doMatchParentConfig(complexEnumConfig: CwtComplexEnumConfig, config: CwtKvConfig<*>, element: PsiElement): Boolean {
		if(config is CwtPropertyConfig) {
			//match key only
			if(element !is ParadoxScriptProperty) return false
			if(!doMatchKeyConfig(complexEnumConfig, config, element)) return false
		} else if(config is CwtValueConfig) {
			//blockConfig vs blockElement 
			if(element !is ParadoxScriptBlock) return false
		} else {
			return false
		}
		return doBeforeMatchParentConfig(config, element, complexEnumConfig)
	}
	
	private fun doBeforeMatchParentConfig(config: CwtKvConfig<*>, element: PsiElement, complexEnumConfig: CwtComplexEnumConfig): Boolean {
		val parentConfig = config.parent ?: return false
		val parentBlockElement = element.parentOfType<ParadoxScriptBlock>() ?: return false
		val parentElement = if(parentBlockElement.isLonely()) parentBlockElement else parentBlockElement.parentOfType<ParadoxScriptProperty>()
		if(parentConfig == complexEnumConfig.nameConfig) {
			if(complexEnumConfig.startFromRoot) {
				return parentElement == null
			} else {
				return parentElement != null && parentElement.findParentScriptElement() == null
			}
		}
		if(parentElement == null) return false
		parentConfig.properties?.forEach { propConfig ->
			//ignore same config or enum name config
			if(propConfig == config || propConfig.key == "enum_name" || propConfig.stringValue == "enum_name") return@forEach
			val notMatched = parentBlockElement.processProperty { propElement ->
				!doMatchPropertyConfig(complexEnumConfig, propConfig, propElement)
			}
			if(notMatched) return false
		}
		parentConfig.values?.forEach { valueConfig ->
			//ignore same config or enum name config
			if(valueConfig == config || valueConfig.stringValue == "enum_name") return@forEach
			val notMatched = parentBlockElement.processValue { valueElement ->
				!doMatchValueConfig(complexEnumConfig, valueConfig, valueElement)
			}
			if(notMatched) return false
		}
		return doMatchParentConfig(complexEnumConfig, parentConfig, parentElement)
	}
	
	private fun doMatchPropertyConfig(complexEnumConfig: CwtComplexEnumConfig, config: CwtPropertyConfig, propertyElement: ParadoxScriptProperty): Boolean {
		return doMatchKeyConfig(complexEnumConfig, config, propertyElement)
			&& doMatchValueConfig(complexEnumConfig, config, propertyElement.propertyValue?.value ?: return false)
	}
	
	private fun doMatchKeyConfig(complexEnumConfig: CwtComplexEnumConfig, config: CwtPropertyConfig, propertyElement: ParadoxScriptProperty): Boolean {
		val key = config.key
		if(key == "scalar" || key == "enum_name") return true
		return key.equals(propertyElement.name, true)
	}
	
	private fun doMatchValueConfig(complexEnumConfig: CwtComplexEnumConfig, config: CwtKvConfig<*>, valueElement: ParadoxScriptValue): Boolean {
		if(config.isBlock) {
			val blockElement = valueElement.castOrNull<ParadoxScriptBlock>() ?: return false
			if(!doMatchBlockConfig(complexEnumConfig, config, blockElement)) return false
			return true
		} else if(config.stringValue != null) {
			val stringElement = valueElement.castOrNull<ParadoxScriptString>() ?: return false
			if(!doMatchStringConfig(complexEnumConfig, config, stringElement)) return false
			return true
		} else if(config.booleanValue != null) {
			val booleanElement = valueElement.castOrNull<ParadoxScriptBoolean>() ?: return false
			if(!doMatchBooleanConfig(complexEnumConfig, config, booleanElement)) return false
			return true
		} else {
			return false
		}
	}
	
	private fun doMatchStringConfig(complexEnumConfig: CwtComplexEnumConfig, config: CwtKvConfig<*>, stringElement: ParadoxScriptString): Boolean {
		val stringValue = config.stringValue!!
		if(stringValue == "scalar" || stringValue == "enum_name") return true
		return stringValue.equals(stringElement.value, true)
	}
	
	private fun doMatchBooleanConfig(complexEnumConfig: CwtComplexEnumConfig, config: CwtKvConfig<*>, booleanElement: ParadoxScriptBoolean): Boolean {
		val booleanValue = config.booleanValue!!
		return booleanValue == booleanElement.booleanValue
	}
	
	private fun doMatchBlockConfig(complexEnumConfig: CwtComplexEnumConfig, config: CwtKvConfig<*>, blockElement: ParadoxScriptBlock): Boolean {
		config.properties?.forEach { propConfig ->
			val notMatched = blockElement.processProperty { propElement ->
				!doMatchPropertyConfig(complexEnumConfig, propConfig, propElement)
			}
			if(notMatched) return false
		}
		config.values?.forEach { valueConfig ->
			val notMatched = blockElement.processValue { valueElement ->
				!doMatchValueConfig(complexEnumConfig, valueConfig, valueElement)
			}
			if(notMatched) return false
		}
		return true
	}
}