package icu.windea.pls.model.constraints

import com.intellij.psi.*
import com.intellij.psi.stubs.*
import icu.windea.pls.lang.index.ParadoxIndexKeys
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*

interface ParadoxIndexConstraint<T : PsiElement> {
    val indexKey: StubIndexKey<String, T>
    val ignoreCase: Boolean
    val inferred: Boolean

    enum class Definition(
        override val indexKey: StubIndexKey<String, ParadoxScriptDefinitionElement>,
        override val ignoreCase: Boolean = false,
        override val inferred: Boolean = false,
    ) : ParadoxIndexConstraint<ParadoxScriptDefinitionElement> {
        TextFormat(ParadoxIndexKeys.DefinitionNameForTextFormat, ignoreCase = true) {
            override fun supports(definitionType: String) = definitionType == ParadoxDefinitionTypes.TextFormat
        },
        ;

        abstract fun supports(definitionType: String): Boolean
    }

    enum class Localisation(
        override val indexKey: StubIndexKey<String, ParadoxLocalisationProperty>,
        override val ignoreCase: Boolean = false,
        override val inferred: Boolean = false,
    ) : ParadoxIndexConstraint<ParadoxLocalisationProperty> {
        Modifier(ParadoxIndexKeys.LocalisationNameForModifier, ignoreCase = true) {
            override fun supports(name: String) = name.startsWith("mod_", true)
        },
        Event(ParadoxIndexKeys.LocalisationNameForEvent, inferred = true) {
            private val regex = """[\w.]+\.\d+(\.[\w.]*)?""".toRegex()
            override fun supports(name: String) = name.contains('.') && name.matches(regex)
        },
        Tech(ParadoxIndexKeys.LocalisationNameForTech) {
            override fun supports(name: String) = name.startsWith("tech_")
        },
        ;

        abstract fun supports(name: String): Boolean
    }
}
