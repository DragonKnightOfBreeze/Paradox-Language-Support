package icu.windea.pls.script.codeInsight

import com.intellij.codeInsight.navigation.actions.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

/**
 * 脚本文件的类型声明提供器。用于导航到类型声明（`Navigate > Type Declaration`）。
 *
 * 支持的PSI元素：
 * * 属性（property） - 导航到定义的类型，包括子类型（如果是定义）。
 * * 值（value） - 导航到定义元素的类型（如果是定义元素）。
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
						val typeElements = mutableListOf<PsiElement>()
						definitionInfo.typeConfig.pointer.element?.let { typeElements.add(it) }
						definitionInfo.subtypeConfigs.forEach { subtypeConfig ->
							subtypeConfig.pointer.element?.let { typeElements.add(it) }
						}
						return typeElements.toTypedArray()
					}
				}
				//TODO 支持定义元素的类型
				return null
			}
			symbol is ParadoxScriptValue -> {
				//TODO 支持定义元素的类型
				return null
			}
			else -> return null
		}
	}
}