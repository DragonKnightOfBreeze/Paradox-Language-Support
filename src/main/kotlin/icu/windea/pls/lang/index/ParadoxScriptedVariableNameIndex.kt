package icu.windea.pls.lang.index

import com.intellij.psi.stubs.StringStubIndexExtension
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 基于名字索引封装变量声明。
 */
class ParadoxScriptedVariableNameIndex : StringStubIndexExtension<ParadoxScriptScriptedVariable>() {
    override fun getKey() = ParadoxIndexKeys.ScriptedVariableName

    override fun getVersion() = 76 // VERSION for 2.0.6
}
