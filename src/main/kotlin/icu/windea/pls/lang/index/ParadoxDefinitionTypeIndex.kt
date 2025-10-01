package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StringStubIndexExtension
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 基于类型索引定义声明。
 */
class ParadoxDefinitionTypeIndex : StringStubIndexExtension<ParadoxScriptDefinitionElement>() {
    override fun getKey() = ParadoxIndexKeys.DefinitionType

    override fun getVersion() = 75 // VERSION for 2.0.5
}
