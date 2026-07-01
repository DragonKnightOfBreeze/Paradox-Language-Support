package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StringStubIndexExtension
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 基于名字索引封装变量声明。
 */
class ParadoxScriptedVariableNameIndex : StringStubIndexExtension<ParadoxScriptScriptedVariable>() {
    override fun getKey() = ChronicleIndexKeys.ScriptedVariableName

    override fun getVersion() = ChronicleIndexVersions.ScriptStub
}
