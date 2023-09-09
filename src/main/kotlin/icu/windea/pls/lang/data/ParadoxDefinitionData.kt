package icu.windea.pls.lang.data

import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.script.*

/**
 * @see ParadoxDefinitionDataProvider
 */
interface ParadoxDefinitionData {
    
}

/**
 * 获取定义的指定类型的数据。
 */
inline fun <reified T : ParadoxDefinitionData> ParadoxScriptDefinitionElement.getData(): T? {
    return ParadoxDefinitionDataProvider.getData(T::class.java, this)
}