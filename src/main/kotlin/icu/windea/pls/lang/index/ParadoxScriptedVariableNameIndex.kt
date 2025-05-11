package icu.windea.pls.lang.index

import com.intellij.psi.stubs.*
import icu.windea.pls.script.psi.*

/**
 * 用于基于名字索引封装变量声明。
 */
class ParadoxScriptedVariableNameIndex : StringStubIndexExtension<ParadoxScriptScriptedVariable>() {
    companion object {
        private const val VERSION = 60 //1.4.0
        private const val CACHE_SIZE = 2 * 1024
    }

    override fun getKey() = ParadoxIndexManager.ScriptedVariableNameKey

    override fun getVersion() = VERSION

    override fun getCacheSize() = CACHE_SIZE
}
