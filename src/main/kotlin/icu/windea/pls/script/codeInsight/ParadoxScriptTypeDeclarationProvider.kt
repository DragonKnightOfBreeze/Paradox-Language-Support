package icu.windea.pls.script.codeInsight

import com.intellij.codeInsight.navigation.actions.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.script.psi.*

/**
 * 脚本文件的类型声明提供器。用于导航到类型声明（`Navigate > Type Declaration`）。
 *
 * 支持的PSI元素：
 * * 属性（property） - 导航到定义的类型声明，包括子类型（如果是定义）。
 * * 表达式（key/value） - 导航到枚举、值集的等类型声明（如果对应的CWT规则匹配）。
 */
class ParadoxScriptTypeDeclarationProvider : TypeDeclarationProvider {
	override fun getSymbolTypeDeclarations(symbol: PsiElement): Array<PsiElement>? {
		//注意这里的symbol是解析引用后得到的PSI元素，因此无法定位到定义元素对应的规则声明
		when {
			symbol is ParadoxScriptProperty -> {
				val definitionInfo = symbol.definitionInfo
				if(definitionInfo != null) {
					if(definitionInfo.types.size == 1) {
						return definitionInfo.typeConfig.pointer.element?.toSingletonArray()
					} else {
						//这里的element可能是null，以防万一，处理是null的情况
						return buildList {
							definitionInfo.typeConfig.pointer.element?.let { add(it) }
							definitionInfo.subtypeConfigs.forEach { subtypeConfig ->
								subtypeConfig.pointer.element?.let { add(it) }
							}
						}.toTypedArray()
					}
				}
				return null
			}
			symbol is ParadoxScriptStringExpressionElement -> {
				if(symbol is ParadoxScriptString && symbol.isDefinitionName()) {
					val definition = symbol.findParentDefinition()
					if(definition is ParadoxScriptProperty) return getSymbolTypeDeclarations(definition)
				}
				val complexEnumValueInfo = symbol.complexEnumValueInfo
				if(complexEnumValueInfo != null) {
					val gameType = complexEnumValueInfo.gameType ?: return null
					val configGroup = getCwtConfig(symbol.project).getValue(gameType)
					val enumName = complexEnumValueInfo.enumName
					val config = configGroup.complexEnums[enumName] ?: return null //unexpected
					val resolved = config.pointer.element ?: return null
					return arrayOf(resolved)
				}
				if(symbol is ParadoxScriptPropertyKey) return getSymbolTypeDeclarations(symbol.parent)
			}
			symbol is CwtValue -> {
				val configType = CwtConfigType.resolve(symbol)
				return when(configType) {
					CwtConfigType.EnumValue -> symbol.parent?.let { arrayOf(it) }
					CwtConfigType.ValueSetValue -> symbol.parent?.let { arrayOf(it) }
					else -> return null
				}
			}
		}
		return null
	}
}