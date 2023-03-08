package icu.windea.pls.core.codeInsight

import com.intellij.codeInsight.navigation.actions.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.script.psi.*

/**
 * 用于导航到类型声明（`Navigate > Type Declaration`）。
 */
class ParadoxTypeDeclarationProvider : TypeDeclarationProvider {
	/**
	 * 依次尝试导航到：
	 * * 定义的CWT类型规则
	 * * 定义名对应的定义的CWT类型规则
	 * * 对应的CWT枚举规则
	 * * 对应的CWT复杂枚举贵哦
	 * * 对应的预定义的CWT值集规则
	 */
	override fun getSymbolTypeDeclarations(symbol: PsiElement): Array<PsiElement>? {
		//注意这里的symbol是解析引用后得到的PSI元素，因此无法定位到定义成员对应的规则声明
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
				val configType = symbol.configType
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