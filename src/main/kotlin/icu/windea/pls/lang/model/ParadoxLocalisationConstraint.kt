package icu.windea.pls.lang.model

import com.intellij.psi.stubs.*
import icu.windea.pls.core.index.*
import icu.windea.pls.localisation.psi.*

/**
 * 用于过滤本地化索引并进行必要的特殊处理。
 */
enum class ParadoxLocalisationConstraint(
    val indexKey: StubIndexKey<String, ParadoxLocalisationProperty>,
    val predicate: (String) -> Boolean,
    val ignoreCase: Boolean = false,
) {
    Default(ParadoxLocalisationNameIndex.KEY, { true }),
    Modifier(ParadoxLocalisationNameIndex.ModifierIndex.KEY, { it.startsWith("mod_", true) }, ignoreCase = true);
    
    companion object {
        val values = values()
    }
}
