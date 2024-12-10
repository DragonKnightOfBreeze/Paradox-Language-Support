package icu.windea.pls.lang.index

import com.intellij.psi.stubs.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

/**
 * 用于基于类型索引定义声明。
 */
class ParadoxDefinitionTypeIndex : StringStubIndexExtension<ParadoxScriptDefinitionElement>() {
    @Suppress("CompanionObjectInExtension")
    companion object {
        val INSTANCE by lazy { findStubIndex<ParadoxDefinitionTypeIndex>() }
        val KEY = StubIndexKey.createIndexKey<String, ParadoxScriptDefinitionElement>("paradox.definition.type.index")

        private const val VERSION = 58 //1.3.27
        private const val CACHE_SIZE = 1024 //180+ in stellaris@3.6
    }

    override fun getKey() = KEY

    override fun getVersion() = VERSION

    override fun getCacheSize() = CACHE_SIZE
}
