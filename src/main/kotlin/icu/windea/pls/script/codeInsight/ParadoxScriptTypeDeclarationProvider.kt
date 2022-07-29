package icu.windea.pls.script.codeInsight

import com.intellij.codeInsight.navigation.actions.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

/**
 * 脚本文件的类型声明提供器。用于导航到类型声明（`Navigate > Type Declaration`）。
 *
 * 支持的PSI元素：
 * * 属性（property） - 导航到定义的类型声明，包括子类型（如果是定义）。
 */
class ParadoxScriptTypeDeclarationProvider : TypeDeclarationProvider {
	override fun getSymbolTypeDeclarations(symbol: PsiElement): Array<PsiElement>? {
		//注意这里的symbol是解析引用后得到的PSI元素，因此无法定位到定义元素对应的规则声明。
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
				return null
			}
			symbol is ParadoxScriptPropertyKey -> {
				return getSymbolTypeDeclarations(symbol.parent)
			}
			//symbol已被解析，不能这样做
			//symbol is ParadoxScriptExpressionElement -> {
			//	val config = symbol.getConfig()
			//	if(config != null) {
			//		return config.pointer.element?.toSingletonArray()
			//	} else {
			//		if(symbol is ParadoxScriptPropertyKey) {
			//			return getSymbolTypeDeclarations(symbol.parent)
			//		}
			//	}
			//}
		}
		return null
	}
}