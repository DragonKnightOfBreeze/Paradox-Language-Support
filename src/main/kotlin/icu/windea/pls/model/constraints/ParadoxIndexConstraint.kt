package icu.windea.pls.model.constraints

import com.intellij.psi.*
import com.intellij.psi.stubs.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*

interface ParadoxIndexConstraint<T : PsiElement> {
    val indexKey: StubIndexKey<String, T>
    val ignoreCase: Boolean

    enum class Definition(
        override val indexKey: StubIndexKey<String, ParadoxScriptDefinitionElement>,
        override val ignoreCase: Boolean,
        val predicate: (definitionType: String) -> Boolean
    ) : ParadoxIndexConstraint<ParadoxScriptDefinitionElement> {
        TextFormat(ParadoxIndexManager.DefinitionNameForTextFormatKey, true, { it == ParadoxDefinitionTypes.TextFormat });
    }

    enum class Localisation(
        override val indexKey: StubIndexKey<String, ParadoxLocalisationProperty>,
        override val ignoreCase: Boolean,
        val predicate: (name: String) -> Boolean
    ) : ParadoxIndexConstraint<ParadoxLocalisationProperty> {
        Modifier(ParadoxIndexManager.LocalisationNameForModifierKey, true, { it.startsWith("mod_", true) });
    }
}
