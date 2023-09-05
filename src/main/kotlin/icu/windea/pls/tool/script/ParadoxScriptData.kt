package icu.windea.pls.tool.script

import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

/**
 * 用于方便地处理脚本数据。
 */
interface ParadoxScriptData : UserDataHolder {
    val key: ParadoxScriptPropertyKey? get() = null
    val value: ParadoxScriptValue? get() = null
    val children: List<ParadoxScriptData>? get() = null
    
    val map: Map<String?, List<ParadoxScriptData>>? get() = null
    
    fun getData(path: String): ParadoxScriptData? = null
    
    fun getAllData(path: String): List<ParadoxScriptData> = emptyList()
    
    object Keys: KeysAware
}