package icu.windea.pls.tool

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

/**
 * Paradox脚本文件的数据解析器。
 *
 * 返回值类型：`List<Any>`或`List<Pair<String,Any>>`
 */
@Suppress("unused")
object ParadoxScriptDataResolver {
	fun resolve(file: PsiFile):List<Any>{
		if(file !is ParadoxScriptFile) throw IllegalArgumentException("Invalid file type (expect: 'ParadoxScriptFile')")
		val rootBlock = file.block?:return emptyList()
		return resolveBlock(rootBlock)
	}
	
	private fun resolveBlock(block: ParadoxScriptBlock):List<Any>{
		return when{
			block.isEmpty -> emptyList()
			block.isArray -> block.valueList.mapNotNull{resolveValue(it) }
			block.isObject -> block.propertyList.mapNotNull { resolveProperty(it)}
			else -> emptyList()
		}
	}
	
	private fun resolveProperty(property: ParadoxScriptProperty):Pair<String,Any?>?{
		//注意这里名字可以重复！！
		val name = property.name
		val value = property.propertyValue?.value
		if(name.isEmpty() || value== null) return null
		return name to resolveValue(value)
	}
	
	private fun resolveValue(value: ParadoxScriptValue):Any?{
		return when(value){
			is ParadoxScriptBoolean -> value.value.toBooleanYesNoOrNull()
			is ParadoxScriptNumber -> value.value.toFloat()
			is ParadoxScriptString -> value.value
			is ParadoxScriptColor -> value.color
			//如果引用的变量存在，则使用它的值，否则使用变量名
			is ParadoxScriptVariableReference -> value.referenceValue?.let{resolveValue(it)} ?: value.text
			is ParadoxScriptBlock -> resolveBlock(value)
			else -> value.value
		}
	}
}