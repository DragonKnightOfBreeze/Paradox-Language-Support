package icu.windea.pls.model.constraints

import com.intellij.psi.stubs.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.localisation.psi.*

/**
 * 用于优化本地化查询。
 */
enum class ParadoxLocalisationConstraint(
    val indexKey: StubIndexKey<String, ParadoxLocalisationProperty>,
    val predicate: (String) -> Boolean,
    val ignoreCase: Boolean = false,
) {
    Default(ParadoxLocalisationNameIndex.KEY, { true }),
    Modifier(ParadoxLocalisationNameIndex.ModifierIndex.KEY, { it.startsWith("mod_", true) }, ignoreCase = true);
}