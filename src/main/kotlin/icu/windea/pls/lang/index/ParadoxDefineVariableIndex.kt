package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StringStubIndexExtension
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 定值变量的索引。使用 `namespace\u0000variable` 作为索引键。
 */
class ParadoxDefineVariableIndex : StringStubIndexExtension<ParadoxScriptProperty>() {
    override fun getKey() = PlsIndexKeys.DefineVariable

    override fun getVersion() = PlsIndexVersions.ScriptStub
}
