package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StringStubIndexExtension
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 定值命名空间的索引（基于命名空间）。
 */
class ParadoxDefineNamespaceIndex : StringStubIndexExtension<ParadoxScriptProperty>() {
    override fun getKey() = PlsIndexKeys.DefineNamespace

    override fun getVersion() = PlsIndexVersions.ScriptStub
}
