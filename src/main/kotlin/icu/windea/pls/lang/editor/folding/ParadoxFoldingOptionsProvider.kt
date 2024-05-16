package icu.windea.pls.lang.editor.folding

import com.intellij.application.options.editor.*
import com.intellij.openapi.options.*
import icu.windea.pls.*

class ParadoxFoldingOptionsProvider : BeanConfigurable<ParadoxFoldingSettings>, CodeFoldingOptionsProvider {
    constructor() : super(ParadoxFoldingSettings.getInstance(), PlsBundle.message("settings.folding"))
    
    init {
        val settings = instance
        checkBox(PlsBundle.message("settings.folding.parameterConditions"), settings::parameterConditionBlocks)
        checkBox(PlsBundle.message("settings.folding.inlineMathBlocks"), settings::inlineMathBlocks)
        checkBox(PlsBundle.message("settings.folding.scriptedVariableReferences"), settings::scriptedVariableReferences)
        checkBox(PlsBundle.message("settings.folding.localisationReferencesFully"), settings::localisationReferencesFully)
        checkBox(PlsBundle.message("settings.folding.localisationIconsFully"), settings::localisationIconsFully)
        checkBox(PlsBundle.message("settings.folding.variableOperationExpressions"), settings::variableOperationExpressions)
    }
}
