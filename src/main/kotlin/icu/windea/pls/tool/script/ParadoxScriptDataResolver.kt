package icu.windea.pls.tool.script

import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

/**
 * Paradox脚本文件的数据解析器。
 */
@Suppress("unused")
object ParadoxScriptDataResolver {
	/**
	 * 解析脚本文件的数据。跳过不合法的[PsiElement]。
	 */
	fun resolve(file: PsiFile): List<ParadoxScriptData> {
		if(file !is ParadoxScriptFile) throw IllegalArgumentException("Invalid file type (expect: 'ParadoxScriptFile')")
		val rootBlock = file.findChild<ParadoxScriptRootBlock>() ?: return emptyList()
		return resolveBlock(rootBlock)
	}
	
	fun resolveBlock(element: ParadoxScriptBlockElement): List<ParadoxScriptData> {
		val result: MutableList<ParadoxScriptData> = SmartList()
		element.processData(includeConditional = true, inline = true) p@{ e -> 
			when{
				e is ParadoxScriptValue -> resolveValue(e).let{ result.add(it) }
				e is ParadoxScriptProperty -> resolveProperty(e)?.let{ result.add(it) }
			}
			true
		}
		return result
	}
	
	fun resolveValue(element: ParadoxScriptValue): ParadoxScriptData {
		val children = when {
			element is ParadoxScriptBlock -> resolveBlock(element)
			else -> null
		}
		return ParadoxScriptData(null, element, children)
	}
	
	fun resolveProperty(element: ParadoxScriptProperty): ParadoxScriptData? {
		val propertyKey = element.propertyKey
		val propertyValue = element.propertyValue
		if(propertyValue == null) return null //ignore
		
		val children = when {
			propertyValue is ParadoxScriptBlock -> resolveBlock(propertyValue)
			else -> null
		}
		return ParadoxScriptData(propertyKey, propertyValue, children)
	}
}
