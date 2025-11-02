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
        override val ignoreCase: Boolean = false,
        override val inferred: Boolean = false,
    ) : ParadoxIndexConstraint<ParadoxScriptDefinitionElement> {
        Resource(PlsIndexKeys.DefinitionNameForResource) {
            override fun supports(definitionType: String) = definitionType == ParadoxDefinitionTypes.Resource
        },
        EconomicCategory(PlsIndexKeys.DefinitionNameForEconomicCategory) {
            override fun supports(definitionType: String) = definitionType == ParadoxDefinitionTypes.EconomicCategory
        },
        TextIcon(PlsIndexKeys.DefinitionNameForTextIcon) {
            override fun supports(definitionType: String) = definitionType == ParadoxDefinitionTypes.TextIcon
        },
        TextFormat(PlsIndexKeys.DefinitionNameForTextFormat, ignoreCase = true) {
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
