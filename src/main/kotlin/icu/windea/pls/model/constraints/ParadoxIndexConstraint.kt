package icu.windea.pls.model.constraints

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubIndexKey
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

interface ParadoxIndexConstraint<T : PsiElement> {
    val indexKey: StubIndexKey<String, T>
    val ignoreCase: Boolean
    val inferred: Boolean

    enum class Definition(
        override val indexKey: StubIndexKey<String, ParadoxScriptDefinitionElement>,
        val definitionType: String? = null,
        override val ignoreCase: Boolean = false,
        override val inferred: Boolean = false,
    ) : ParadoxIndexConstraint<ParadoxScriptDefinitionElement> {
        Resource(PlsIndexKeys.DefinitionNameForResource, ParadoxDefinitionTypes.resource),
        EconomicCategory(PlsIndexKeys.DefinitionNameForEconomicCategory, ParadoxDefinitionTypes.economicCategory),
        GameConcept(PlsIndexKeys.DefinitionNameForGameConcept, ParadoxDefinitionTypes.gameConcept),
        TextColor(PlsIndexKeys.DefinitionNameForTextColor, ParadoxDefinitionTypes.textColor),
        TextIcon(PlsIndexKeys.DefinitionNameForTextIcon, ParadoxDefinitionTypes.textIcon),
        TextFormat(PlsIndexKeys.DefinitionNameForTextFormat, ParadoxDefinitionTypes.textFormat, ignoreCase = true),
        ;

        open fun supports(definitionType: String): Boolean = definitionType == this.definitionType

        companion object {
            @JvmStatic
            private val map = entries.associateBy { it.definitionType }

            @JvmStatic
            fun get(definitionType: String): Definition? = map[definitionType]
        }
    }

    enum class Localisation(
        override val indexKey: StubIndexKey<String, ParadoxLocalisationProperty>,
        override val ignoreCase: Boolean = false,
        override val inferred: Boolean = false,
    ) : ParadoxIndexConstraint<ParadoxLocalisationProperty> {
        Modifier(PlsIndexKeys.LocalisationNameForModifier, ignoreCase = true) {
            override fun supports(name: String) = name.startsWith("mod_", true)
        },
        Event(PlsIndexKeys.LocalisationNameForEvent, inferred = true) {
            private val regex = """[\w.]+\.\d+(\.[\w.]*)?""".toRegex()
            override fun supports(name: String) = name.contains('.') && name.matches(regex)
        },
        Tech(PlsIndexKeys.LocalisationNameForTech) {
            override fun supports(name: String) = name.startsWith("tech_")
        },
        ;

        abstract fun supports(name: String): Boolean
    }
}
