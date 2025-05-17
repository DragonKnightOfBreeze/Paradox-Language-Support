package icu.windea.pls.model.constraints

import com.intellij.psi.stubs.*
import icu.windea.pls.lang.index.ParadoxIndexManager
import icu.windea.pls.localisation.psi.*

/**
 * 用于优化本地化查询。
 */
enum class ParadoxLocalisationConstraint(
    val indexKey: StubIndexKey<String, ParadoxLocalisationProperty>,
    val predicate: (String) -> Boolean,
    val ignoreCase: Boolean = false,
) {
    Default(ParadoxIndexManager.LocalisationNameKey, { true }),
    Modifier(ParadoxIndexManager.LocalisationNameForModifierKey, { it.startsWith("mod_", true) }, ignoreCase = true);
}
