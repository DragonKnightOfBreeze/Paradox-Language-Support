package icu.windea.pls.tool

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

/**
 * Paradox脚本文件的数据解析器。
 */
@Suppress("unused")
object ParadoxScriptDataResolver {
	fun resolve(file: PsiFile): List<Any> { //List<Any | Pair<String, Any>>
		if(file !is ParadoxScriptFile) throw IllegalArgumentException("Invalid file type (expect: 'ParadoxScriptFile')")
		val rootBlock = file.block ?: return emptyList()
		return resolveBlock(rootBlock)
	}
	
	private fun resolveBlock(block: ParadoxScriptBlock): List<Any> {
		return when {
			block.isEmpty -> emptyList()
			block.isArray -> block.mapChildOfTypeNotNull(ParadoxScriptValue::class.java) { resolveValue(it) }
			block.isObject -> block.mapChildOfTypeNotNull(ParadoxScriptProperty::class.java) { resolveProperty(it) }
			else -> emptyList()
		}
	}
	
	private fun resolveProperty(property: ParadoxScriptProperty): Pair<String, Any?>? {
		//注意这里名字可以重复！！
		val name = property.name
		val value = property.propertyValue?.value ?: return null
		return name to resolveValue(value)
	}
	
	private fun resolveValue(value: ParadoxScriptValue): Any? {
		return when(value) {
			is ParadoxScriptBoolean -> value.value.toBooleanYesNo()
			is ParadoxScriptNumber -> value.value.toFloat()
			is ParadoxScriptString -> value.value
			is ParadoxScriptColor -> value.color
			is ParadoxScriptVariableReference -> value.referenceValue?.let { resolveValue(it) }
			is ParadoxScriptBlock -> resolveBlock(value)
			else -> value.value
		}
	}
	
	fun resolveToMap(file: PsiFile): Map<String, Any?> {
		if(file !is ParadoxScriptFile) throw IllegalArgumentException("Invalid file type (expect: 'ParadoxScriptFile')")
		val rootBlock = file.block ?: return emptyMap()
		return when {
			rootBlock.isEmpty -> emptyMap()
			rootBlock.isArray -> emptyMap()
			rootBlock.isObject -> {
				val map = mutableMapOf<String, Any?>()
				for(property in rootBlock.propertyList) resolvePropertyToMap(property, map)
				map
			}
			else -> emptyMap()
		}
	}
	
	private fun resolvePropertyToMap(property: ParadoxScriptProperty, map: MutableMap<String, Any?>) {
		//注意这里名字可以重复！！
		val name = property.name
		val value = property.propertyValue?.value ?: return
		map.put(name, resolveValueToMap(value))
	}
	
	private fun resolveValueToMap(value: ParadoxScriptValue): Any? {
		return when(value) {
			is ParadoxScriptBoolean -> value.value.toBooleanYesNo()
			is ParadoxScriptNumber -> value.value.toFloat()
			is ParadoxScriptString -> value.value
			is ParadoxScriptColor -> value.color ?: value.text
			//如果引用的变量存在，则使用它的值，否则使用变量名
			is ParadoxScriptVariableReference -> value.referenceValue?.let { resolveValueToMap(it) }
			is ParadoxScriptBlock -> resolveBlockToMap(value)
			else -> value.value
		}
	}
	
	private fun resolveBlockToMap(block: ParadoxScriptBlock): Any {
		return when {
			block.isEmpty -> emptyMap<String, Any?>()
			block.isArray -> {
				val list = mutableListOf<Any?>()
				for(value in block.valueList) list.add(resolveValueToMap(value))
				list
			}
			block.isObject -> {
				val map = mutableMapOf<String, Any?>()
				for(property in block.propertyList) resolvePropertyToMap(property, map)
				map
			}
			else -> emptyMap<String, Any?>()
		}
	}
}