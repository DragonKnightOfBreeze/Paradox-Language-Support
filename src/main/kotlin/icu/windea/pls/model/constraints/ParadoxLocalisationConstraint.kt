package icu.windea.pls.model.constraints

import com.intellij.psi.stubs.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.ep.index.*
import icu.windea.pls.localisation.psi.*

/**
 * 用于优化本地化查询。
 */
enum class ParadoxLocalisationConstraint(
    val indexKey: StubIndexKey<String, ParadoxLocalisationProperty>,
    val predicate: (String) -> Boolean,
    val ignoreCase: Boolean = false,
) {
    Default(ParadoxLocalisationNameIndexKey, { true }),
    Modifier(ParadoxLocalisationNameIndexModifierKey, { it.startsWith("mod_", true) }, ignoreCase = true);
    
    companion object {
        val values = values()
    }
}