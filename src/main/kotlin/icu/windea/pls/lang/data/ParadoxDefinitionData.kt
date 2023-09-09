package icu.windea.pls.lang.data

import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.script.*

/**
 * @see ParadoxDefinitionDataProvider
 */
abstract class ParadoxDefinitionData {
    private var _data: ParadoxScriptData? = null
    protected val data: ParadoxScriptData get() = _data!!
    
    fun init(data: ParadoxScriptData) {
        this._data = data
    }
}

/**
 * 获取定义的指定类型的数据。
 */
inline fun <reified T : ParadoxDefinitionData> ParadoxScriptDefinitionElement.getData(): T? {
    return ParadoxDefinitionDataProvider.getData(T::class.java, this)
}