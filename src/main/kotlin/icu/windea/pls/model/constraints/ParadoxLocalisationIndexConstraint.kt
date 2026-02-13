package icu.windea.pls.model.constraints

import com.intellij.psi.stubs.StubIndexKey
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

enum class ParadoxLocalisationIndexConstraint(
    val indexKey: StubIndexKey<String, ParadoxLocalisationProperty>,
    override val ignoreCase: Boolean = false,
    override val inferred: Boolean = false,
) : ParadoxIndexConstraint<ParadoxLocalisationProperty> {
    Modifier(PlsIndexKeys.LocalisationNameForModifier, ignoreCase = true) {
        override fun test(name: String) = name.startsWith("mod_", true)
    },
    Event(PlsIndexKeys.LocalisationNameForEvent, inferred = true) {
        private val regex = """[\w.]+\.\d+(\.[\w.]*)?""".toRegex()
        override fun test(name: String) = name.contains('.') && name.matches(regex)
    },
    Tech(PlsIndexKeys.LocalisationNameForTech) {
        override fun test(name: String) = name.startsWith("tech_")
    },
    ;

    abstract fun test(name: String): Boolean
}
