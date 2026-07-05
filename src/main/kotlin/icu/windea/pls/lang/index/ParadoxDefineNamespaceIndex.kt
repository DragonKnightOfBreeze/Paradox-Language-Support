package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StringStubIndexExtension
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 定值命名空间的索引。基于命名空间。
 */
class ParadoxDefineNamespaceIndex : StringStubIndexExtension<ParadoxScriptProperty>() {
    override fun getKey() = ChronicleIndexKeys.DefineNamespace

    override fun getVersion() = ChronicleIndexVersions.ScriptStub
}
