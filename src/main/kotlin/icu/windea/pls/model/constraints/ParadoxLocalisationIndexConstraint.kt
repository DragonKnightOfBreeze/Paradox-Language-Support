package icu.windea.pls.model.constraints

import com.intellij.psi.stubs.StubIndexKey
import icu.windea.pls.lang.index.ChronicleIndexKeys
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

enum class ParadoxLocalisationIndexConstraint(
    val indexKey: StubIndexKey<String, ParadoxLocalisationProperty>,
    val ignoreCase: Boolean = false,
    val inferred: Boolean = false,
) : ParadoxIndexConstraint<ParadoxLocalisationProperty> {
    Modifier(ChronicleIndexKeys.LocalisationNameForModifier, ignoreCase = true) {
        override fun test(name: String) = name.startsWith("mod_", true)
    },
    Event(ChronicleIndexKeys.LocalisationNameForEvent, inferred = true) {
        private val regex = """[\w.]+\.\d+(\.[\w.]*)?""".toRegex()
        override fun test(name: String) = name.contains('.') && name.matches(regex)
    },
    ;

    // NOTE 2.2.0 technologies have different formats in non-Stellaris games (e.g., VIC3), so remove relevant constraint here

    abstract fun test(name: String): Boolean
}
