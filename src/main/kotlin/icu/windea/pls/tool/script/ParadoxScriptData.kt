package icu.windea.pls.tool.script

import com.intellij.util.SmartList
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.ParadoxScriptBoolean
import icu.windea.pls.script.psi.ParadoxScriptColor
import icu.windea.pls.script.psi.ParadoxScriptFloat
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptInt
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptValue

/**
 * 用于方便地处理脚本数据。
 */
data class ParadoxScriptData(
	val key: ParadoxScriptPropertyKey?,
	val value: ParadoxScriptValue,
	val children: List<ParadoxScriptData>? = null
) {
	val map: Map<String?, List<ParadoxScriptData>>? by lazy { 
		if(children == null) return@lazy null
		val map = mutableMapOf<String?, MutableList<ParadoxScriptData>>()
		for(data in children) {
			if(data.key == null) {
				map.getOrPut(null) { SmartList() }.add(data)
			} else {
				val k = data.key.name.lowercase()
				map.getOrPut(k) { SmartList() }.add(data)
			}
		}
		map
	}
	
	fun getValue(path: String): ParadoxScriptData? {
		val pathList = path.trimStart('/').split('/')
		var current: ParadoxScriptData? = this
		for(p in pathList) {
			if(p == "-") {
				current = current?.map?.get(null)?.firstOrNull()
			}  else {
				current = current?.map?.get(p)?.firstOrNull()
			}
		}
		return current
	}
	
	fun getValues(path: String): List<ParadoxScriptData> {
		val pathList = path.trimStart('/').split('/')
		var result: List<ParadoxScriptData> = listOf(this)
		for(p in pathList) {
			if(p == "-") {
				result = result.flatMap { it.map?.get(null).orEmpty() }
			}  else {
				result = result.flatMap { it.map?.get(p).orEmpty() }
			}
		}
		return result
	}
	
	fun booleanValue() = value.resolved().castOrNull<ParadoxScriptBoolean>()?.booleanValue
	fun intValue() = value.resolved().castOrNull<ParadoxScriptInt>()?.intValue
	fun floatValue() = value.resolved().castOrNull<ParadoxScriptFloat>()?.floatValue
	fun stringValue() = value.resolved().castOrNull<ParadoxScriptString>()
	fun colorValue() = value.castOrNull<ParadoxScriptColor>()
	fun inlineMathValue() = value.castOrNull<ParadoxScriptInlineMath>()
	
	private fun ParadoxScriptValue.resolved(): ParadoxScriptValue? {
		if(this !is ParadoxScriptScriptedVariableReference) return this
		return this.referenceValue
	}
}