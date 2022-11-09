package icu.windea.pls.core.tool

import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.script.psi.*

/**
 * Paradox脚本文件的数据解析器。
 */
@Suppress("unused")
object ParadoxScriptDataResolver {
	/**
	 * 解析脚本文件的数据。跳过不合法的[PsiElement]。
	 */
	fun resolve(file: PsiFile): List<BlockEntry<String?, Any>> {
		if(file !is ParadoxScriptFile) throw IllegalArgumentException("Invalid file type (expect: 'ParadoxScriptFile')")
		val rootBlock = file.findOptionalChild<ParadoxScriptRootBlock>() ?: return emptyList()
		return resolveBlock(rootBlock)
	}
	
	private fun resolveBlock(block: IParadoxScriptBlock): List<BlockEntry<String?, Any>> {
		val result: MutableList<BlockEntry<String?, Any>> = SmartList()
		block.processChild block@{ blockItem ->
			if(!blockItem.isValid) return@block true
			when {
				blockItem is ParadoxScriptValue -> {
					val value = resolveValue(blockItem) ?: return@block true
					result.add(BlockEntry(null, value))
				}
				blockItem is ParadoxScriptProperty -> {
					val property = resolveProperty(blockItem) ?: return@block true
					result.add(property)
				}
				blockItem is ParadoxScriptParameterCondition -> {
					blockItem.processChild condition@{ conditionItem ->
						if(!conditionItem.isValid) return@condition true
						when {
							conditionItem is ParadoxScriptValue -> {
								val value = resolveValue(conditionItem) ?: return@condition true
								result.add(BlockEntry(null, value))
							}
							conditionItem is ParadoxScriptProperty -> {
								val property = resolveProperty(conditionItem) ?: return@condition true
								result.add(property)
							}
							else -> true
						}
					}
				}
				else -> true
			}
		}
		return result
	}
	
	private fun resolveProperty(property: ParadoxScriptProperty): BlockEntry<String?, Any>? {
		//注意property的名字可以重复
		val key = property.name
		val value = property.propertyValue?.value?.let { resolveValue(it) } ?: return null
		return BlockEntry(key, value)
	}
	
	private fun resolveValue(value: ParadoxScriptValue): Any? {
		return when(value) {
			is ParadoxScriptBoolean -> value.value.toBooleanYesNo()
			is ParadoxScriptInt -> value.value.toInt()
			is ParadoxScriptFloat -> value.value.toFloat()
			is ParadoxScriptString -> value.value
			is ParadoxScriptColor -> value.color
			is ParadoxScriptScriptedVariableReference -> value.referenceValue?.let { resolveValue(it) }
			is ParadoxScriptBlock -> resolveBlock(value)
			else -> value.value
		}
	}
}