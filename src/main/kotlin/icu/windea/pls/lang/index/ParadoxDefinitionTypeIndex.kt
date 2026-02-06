package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StringStubIndexExtension
import icu.windea.pls.script.psi.ParadoxDefinitionElement

/**
 * 定义声明的类型的索引。
 */
class ParadoxDefinitionTypeIndex : StringStubIndexExtension<ParadoxDefinitionElement>() {
    override fun getKey() = PlsIndexKeys.DefinitionType

    override fun getVersion() = PlsIndexVersions.ScriptStub
}
