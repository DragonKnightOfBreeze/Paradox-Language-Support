package icu.windea.pls.tool.script

import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

/**
 * 用于方便地处理脚本数据。
 */
data class ParadoxScriptData(
    val key: ParadoxScriptPropertyKey?,
    val value: ParadoxScriptValue?,
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
    
    fun getValue(path: String, validKey: Boolean = false, valid: Boolean = false): ParadoxScriptData? {
        val pathList = path.trimStart('/').split('/')
        var current: ParadoxScriptData? = this
        for(p in pathList) {
            val k = if(p == "-") null else p
            current = current?.map?.get(k)?.firstOrNull()
        }
        if(current == null) return null
        if(validKey && current.key?.isValidExpression() == false) return null
        if(valid && current.value?.isValidExpression() == false) return null
        return current
    }
    
    fun getValues(path: String, validKey: Boolean = false, valid: Boolean = false): List<ParadoxScriptData> {
        val pathList = path.trimStart('/').split('/')
        var result: List<ParadoxScriptData> = listOf(this)
        for(p in pathList) {
            val k = if(p == "-") null else p
            result = buildList { 
                result.forEach r@{ r ->
                    r.map?.get(k)?.forEach rr@{ rr ->
                        if(validKey && rr.key?.isValidExpression() == false) return@rr
                        if(valid && rr.value?.isValidExpression() == false) return@rr
                        add(rr)
                    }
                }
            }
        }
        return result
    }
    
    fun booleanValue() = value?.resolved()?.castOrNull<ParadoxScriptBoolean>()?.booleanValue
    fun intValue() = value?.resolved()?.castOrNull<ParadoxScriptInt>()?.intValue
    fun floatValue() = value?.resolved()?.castOrNull<ParadoxScriptFloat>()?.floatValue
    fun stringText() = value?.resolved()?.castOrNull<ParadoxScriptString>()?.text
    fun stringValue() = value?.resolved()?.castOrNull<ParadoxScriptString>()?.stringValue
    fun colorValue() = value.castOrNull<ParadoxScriptColor>()?.color
    
    private fun ParadoxScriptValue.resolved(): ParadoxScriptValue? {
        if(this !is ParadoxScriptScriptedVariableReference) return this
        return this.referenceValue
    }
}