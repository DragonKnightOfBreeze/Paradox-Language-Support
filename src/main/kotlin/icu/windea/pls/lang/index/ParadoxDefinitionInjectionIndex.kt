package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StringStubIndexExtension
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 定义注入的索引。使用目标键（如 `type@name`）作为索引键。
 */
class ParadoxDefinitionInjectionIndex : StringStubIndexExtension<ParadoxScriptProperty>() {
    override fun getKey() = PlsIndexKeys.DefinitionInjectionTarget

    override fun getVersion() = PlsIndexVersions.ScriptStub
}
