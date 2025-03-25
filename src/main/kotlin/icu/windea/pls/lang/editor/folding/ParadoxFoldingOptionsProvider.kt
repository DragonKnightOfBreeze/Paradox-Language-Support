package icu.windea.pls.lang.editor.folding

import com.intellij.application.options.editor.*
import com.intellij.openapi.options.*
import icu.windea.pls.*

class ParadoxFoldingOptionsProvider : BeanConfigurable<ParadoxFoldingSettings>, CodeFoldingOptionsProvider {
    constructor() : super(ParadoxFoldingSettings.getInstance(), PlsBundle.message("settings"))

    init {
        val settings = instance
        checkBox(PlsBundle.message("settings.folding.parameterConditionBlocks"), settings::parameterConditionBlocks)
        checkBox(PlsBundle.message("settings.folding.inlineMathBlocks"), settings::inlineMathBlocks)
        checkBox(PlsBundle.message("settings.folding.localisationReferencesFullyEnabled"), settings::localisationReferencesFullyEnabled)
        checkBox(PlsBundle.message("settings.folding.localisationReferencesFully"), settings::localisationReferencesFully)
        checkBox(PlsBundle.message("settings.folding.localisationIconsFullyEnabled"), settings::localisationIconsFullyEnabled)
        checkBox(PlsBundle.message("settings.folding.localisationIconsFully"), settings::localisationIconsFully)
        checkBox(PlsBundle.message("settings.folding.localisationCommandsEnabled"), settings::localisationCommandsEnabled)
        checkBox(PlsBundle.message("settings.folding.localisationCommands"), settings::localisationCommands)
        checkBox(PlsBundle.message("settings.folding.localisationConceptsEnabled"), settings::localisationConceptsEnabled)
        checkBox(PlsBundle.message("settings.folding.localisationConcepts"), settings::localisationConcepts)
        checkBox(PlsBundle.message("settings.folding.localisationConceptTextsEnabled"), settings::localisationConceptTextsEnabled)
        checkBox(PlsBundle.message("settings.folding.localisationConceptTexts"), settings::localisationConceptTexts)
        checkBox(PlsBundle.message("settings.folding.scriptedVariableReferencesEnabled"), settings::scriptedVariableReferencesEnabled)
        checkBox(PlsBundle.message("settings.folding.scriptedVariableReferences"), settings::scriptedVariableReferences)
        checkBox(PlsBundle.message("settings.folding.variableOperationExpressionsEnabled"), settings::variableOperationExpressionsEnabled)
        checkBox(PlsBundle.message("settings.folding.variableOperationExpressions"), settings::variableOperationExpressions)
    }
}
