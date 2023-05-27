package icu.windea.pls.tool.script

import com.intellij.openapi.util.*
import icu.windea.pls.script.psi.*

class ParadoxScriptDataImpl(
    override val key: ParadoxScriptPropertyKey?,
    override val value: ParadoxScriptValue?,
    override val children: List<ParadoxScriptData>? = null
) : UserDataHolderBase(),  ParadoxScriptData {
    override val map: Map<String?, List<ParadoxScriptData>>? by lazy {
        if(children == null) return@lazy null
        val map = mutableMapOf<String?, MutableList<ParadoxScriptData>>()
        for(data in children) {
            val dataKey = data.key
            if(dataKey == null) {
                map.getOrPut(null) { mutableListOf() }.add(data)
            } else {
                val k = dataKey.name.lowercase()
                map.getOrPut(k) { mutableListOf() }.add(data)
            }
        }
        map
    }
    
    override fun getData(path: String): ParadoxScriptData? {
        val pathList = path.trimStart('/').lowercase().split('/')
        var current: ParadoxScriptData? = this
        for(p in pathList) {
            val k = if(p == "-") null else p
            current = current?.map?.get(k)?.firstOrNull()
        }
        if(current == null) return null
        return current
    }
    
    override fun getAllData(path: String): List<ParadoxScriptData> {
        val pathList = path.trimStart('/').lowercase().split('/')
        var result: List<ParadoxScriptData> = listOf(this)
        for(p in pathList) {
            val k = if(p == "-") null else p
            result = result.flatMap { it.map?.get(k).orEmpty() }
        }
        return result
    }
}