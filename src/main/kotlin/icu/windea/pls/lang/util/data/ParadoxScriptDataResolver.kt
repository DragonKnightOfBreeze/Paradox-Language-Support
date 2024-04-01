package icu.windea.pls.lang.util.data

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

@Suppress("unused")
object ParadoxScriptDataResolver {
	/**
	 * 解析脚本文件的数据。跳过不合法的[PsiElement]。
	 */
	fun resolve(file: PsiFile, conditional: Boolean = false, inline: Boolean = false): ParadoxScriptData? {
		if(file !is ParadoxScriptFile) throw IllegalArgumentException("Invalid file type (expect: 'ParadoxScriptFile')")
		val rootBlock = file.findChild<ParadoxScriptRootBlock>() ?: return null
		return resolveBlock(rootBlock, conditional, inline)
	}
	
	fun resolveBlock(element: ParadoxScriptBlockElement, conditional: Boolean = false, inline: Boolean = false): ParadoxScriptData {
		val value = element as? ParadoxScriptBlock
		val children: MutableList<ParadoxScriptData> = mutableListOf()
		element.processData(conditional, inline) p@{ e ->
			when{
				e is ParadoxScriptValue -> resolveValue(e).let{ children.add(it) }
				e is ParadoxScriptProperty -> resolveProperty(e)?.let{ children.add(it) }
			}
			true
		}
		return ParadoxScriptDataImpl(null, value, children)
	}
	
	fun resolveValue(element: ParadoxScriptValue, conditional: Boolean = false, inline: Boolean = false): ParadoxScriptData {
		if(element is ParadoxScriptBlock) return resolveBlock(element, conditional, inline)
		return ParadoxScriptDataImpl(null, element, null)
	}
	
	fun resolveProperty(element: ParadoxScriptProperty, conditional: Boolean = false, inline: Boolean = false): ParadoxScriptData? {
		val propertyKey = element.propertyKey
		val propertyValue = element.propertyValue
		if(propertyValue == null) return null //ignore
		
		val children = when {
			propertyValue is ParadoxScriptBlock -> resolveBlock(propertyValue, conditional, inline).children
			else -> null
		}
		return ParadoxScriptDataImpl(propertyKey, propertyValue, children)
	}
}
