package icu.windea.pls.tool.script

import com.intellij.util.*
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
    
    fun getData(path: String): ParadoxScriptData? {
        val pathList = path.trimStart('/').split('/')
        var current: ParadoxScriptData? = this
        for(p in pathList) {
            val k = if(p == "-") null else p
            current = current?.map?.get(k)?.firstOrNull()
        }
        if(current == null) return null
        return current
    }
    
    fun getAllData(path: String): List<ParadoxScriptData> {
        val pathList = path.trimStart('/').split('/')
        var result: List<ParadoxScriptData> = listOf(this)
        for(p in pathList) {
            val k = if(p == "-") null else p
            result = result.flatMap { it.map?.get(k).orEmpty() }
        }
        return result
    }
}