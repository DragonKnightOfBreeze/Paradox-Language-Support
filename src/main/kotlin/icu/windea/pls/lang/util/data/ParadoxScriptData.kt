package icu.windea.pls.lang.util.data

import com.intellij.openapi.util.UserDataHolder
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptValue

/**
 * 脚本数据。
 *
 * 用于通过路径、属性委托等方式，方便地得到需要的数据。
 */
interface ParadoxScriptData : UserDataHolder {
    val keyElement: ParadoxScriptPropertyKey?
    val valueElement: ParadoxScriptValue?
    val children: List<ParadoxScriptData>?
    val map: Map<String?, List<ParadoxScriptData>>?

    fun getData(path: String): ParadoxScriptData?

    fun getAllData(path: String): List<ParadoxScriptData>
}
