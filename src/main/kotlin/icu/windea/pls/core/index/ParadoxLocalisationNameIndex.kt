package icu.windea.pls.core.index

import com.intellij.psi.stubs.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationNameIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
    companion object {
        @JvmField val KEY = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.localisation.name.index")
        private const val VERSION = 22 //1.0.0
        private const val CACHE_SIZE = 100 * 1024 //98000+ in stellaris@3.6
    }
    
    override fun getKey() = KEY
    
    override fun getVersion() = VERSION
    
    override fun getCacheSize() = CACHE_SIZE
    
    /**
     * 用于过滤本地化索引。
     */
    enum class Constraint(
        val indexKey: StubIndexKey<String, ParadoxLocalisationProperty>,
        val ignoreCase: Boolean = false
    ) {
        Default(KEY),
        Modifier(ModifierIndex.KEY, true)
    }
    
    /**
     * 用于快速索引修正的名字和描述。它们是忽略大小写的。
     */
    class ModifierIndex : StringStubIndexExtension<ParadoxLocalisationProperty>() {
        companion object {
            @JvmField val KEY = StubIndexKey.createIndexKey<String, ParadoxLocalisationProperty>("paradox.localisation.name.index.modifier")
            private const val VERSION = 22 //1.0.0
        }
        
        override fun getKey() = KEY
        
        override fun getVersion() = VERSION
    }
}