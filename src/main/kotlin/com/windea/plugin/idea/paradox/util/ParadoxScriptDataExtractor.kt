package com.windea.plugin.idea.paradox.util

import com.intellij.psi.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.psi.*

/**
 * Paradox脚本文件的数据提取器。
 *
 * 返回值类型：`List<Any>`或`List<Pair<String,Any>>`
 */
object ParadoxScriptDataExtractor {
	fun extract(file: PsiFile):List<Any>{
		if(file !is ParadoxScriptFile) throw IllegalArgumentException("Invalid file type")
		val rootBlock = file.findChildByClass(ParadoxScriptRootBlock::class.java) ?: return listOf()
		return extractBlock(rootBlock)
	}
	
	private fun extractBlock(block:ParadoxScriptBlock):List<Any>{
		return when{
			block.isEmpty -> listOf()
			block.isArray -> block.valueList.mapNotNull{extractValue(it) }
			block.isObject -> block.propertyList.mapNotNull { extractProperty(it)}
			else -> listOf()
		}
	}
	
	private fun extractValue(value:ParadoxScriptValue):Any?{
		return when(value){
			is ParadoxScriptBoolean -> value.value.toBooleanYesNo()
			is ParadoxScriptNumber -> value.value.toFloat()
			is ParadoxScriptString -> value.value
			is ParadoxScriptColor -> value.color
			//如果引用的变量存在，则使用它的值，否则使用变量名
			is ParadoxScriptVariableReference -> value.referenceValue?.let{extractValue(it)} ?: value.text
			is ParadoxScriptBlock -> extractBlock(value)
			else -> value.value
		}
	}
	
	private fun extractProperty(property:ParadoxScriptProperty):Pair<String,Any?>?{
		//注意这里名字可以重复！！
		val name = property.name
		val value = property.propertyValue?.value
		if(name.isEmpty() || value== null) return null
		return name to extractValue(value)
	}
}