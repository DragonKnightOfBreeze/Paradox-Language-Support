package icu.windea.pls.lang.index

import com.intellij.psi.stubs.*
import icu.windea.pls.script.psi.*

/**
 * 用于基于名字索引封装变量声明。
 */
class ParadoxScriptedVariableNameIndex : StringStubIndexExtension<ParadoxScriptScriptedVariable>() {
    @Suppress("CompanionObjectInExtension")
    companion object {
        val INSTANCE by lazy { findStubIndex<ParadoxScriptedVariableNameIndex>() }
        val KEY = StubIndexKey.createIndexKey<String, ParadoxScriptScriptedVariable>("paradox.scriptedVariable.name.index")

        private const val VERSION = 55 //1.3.24
        private const val CACHE_SIZE = 2 * 1024
    }

    override fun getKey() = KEY

    override fun getVersion() = VERSION

    override fun getCacheSize() = CACHE_SIZE
}
