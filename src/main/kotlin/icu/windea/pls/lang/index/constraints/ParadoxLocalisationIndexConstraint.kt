package icu.windea.pls.lang.index.constraints

import com.intellij.psi.stubs.StubIndexKey
import icu.windea.pls.lang.index.ChronicleIndexKeys
import icu.windea.pls.lang.index.ParadoxLocalisationNameConstrainedIndex
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

/**
 * 本地化索引的索引约束。
 *
 * @property indexKey 对应的受约束索引使用的 [StubIndexKey]。
 * @property ignoreCase 对应的受约束索引在索引本地化的名字时，是否忽略大小写。
 * @property inferred 对应的受约束索引中不存在要查询的数据时，作为回退，是否继续查询常规索引。
 *
 * @see ParadoxLocalisationNameConstrainedIndex
 */
enum class ParadoxLocalisationIndexConstraint(
    val indexKey: StubIndexKey<String, ParadoxLocalisationProperty>,
    val ignoreCase: Boolean = false,
    val inferred: Boolean = false,
) : ParadoxIndexConstraint<ParadoxLocalisationProperty> {
    Modifier(ChronicleIndexKeys.LocalisationNameForModifier, ignoreCase = true) {
        override fun test(name: String): Boolean {
            return name.startsWith("mod_", true)
        }
    },
    Event(ChronicleIndexKeys.LocalisationNameForEvent, inferred = true) {
        private val regex = """[\w.]+\.\d+(\.[\w.]*)?""".toRegex()

        override fun test(name: String): Boolean {
            return name.contains('.') && name.matches(regex)
        }
    },
    ;

    // NOTE 2.2.0 technologies have different formats in non-Stellaris games (e.g., VIC3), so remove relevant constraint here

    abstract fun test(name: String): Boolean
}
