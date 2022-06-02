package icu.windea.pls.script.codeInsight

import com.intellij.codeInsight.navigation.actions.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.script.psi.*

/**
 * 脚本文件的类型声明提供器。用于导航到类型声明（`Navigate > Type Declaration`）。
 *
 * 支持的PSI元素：
 * * 属性（property） - 导航到定义的类型声明，包括子类型（如果是定义），或者定义元素对应的规则声明（如果是定义元素）。
 * * 值（value） - 导航到定义元素对应的规则声明（如果是定义元素）。
 */
class ParadoxScriptTypeDeclarationProvider : TypeDeclarationProvider {
	override fun getSymbolTypeDeclarations(symbol: PsiElement): Array<PsiElement>? {
		when {
			symbol is ParadoxScriptProperty -> {
				val definitionInfo = symbol.definitionInfo
				if(definitionInfo != null) {
					if(definitionInfo.typeCount == 1) {
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
				val propertyConfig = symbol.getPropertyConfig()
				if(propertyConfig != null) {
					return propertyConfig.pointer.element?.toSingletonArray()
				}
				return null
			}
			symbol is ParadoxScriptValue -> {
				val valueConfig = symbol.getValueConfig()
				if(valueConfig != null) {
					return valueConfig.pointer.element?.toSingletonArray()
				}
				return null
			}
			else -> return null
		}
	}
}