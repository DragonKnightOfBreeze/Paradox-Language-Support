package icu.windea.pls.tools.actions

import com.intellij.openapi.project.Project
import icu.windea.pls.tools.config.generators.CwtEffectConfigGenerator
import icu.windea.pls.tools.config.generators.CwtGameRuleConfigGenerator
import icu.windea.pls.tools.config.generators.CwtLocalisationConfigGenerator
import icu.windea.pls.tools.config.generators.CwtModifierCategoriesConfigGenerator
import icu.windea.pls.tools.config.generators.CwtModifierConfigGenerator
import icu.windea.pls.tools.config.generators.CwtOnActionConfigGenerator
import icu.windea.pls.tools.config.generators.CwtTriggerConfigGenerator

interface GenerateConfigActions {
    class Trigger : GenerateConfigActionBase() {
        override fun createGenerator(project: Project) = CwtTriggerConfigGenerator(project)
    }

    class Effect : GenerateConfigActionBase() {
        override fun createGenerator(project: Project) = CwtEffectConfigGenerator(project)
    }

    class Localisation : GenerateConfigActionBase() {
        override fun createGenerator(project: Project) = CwtLocalisationConfigGenerator(project)
    }

    class Modifier : GenerateConfigActionBase() {
        override fun createGenerator(project: Project) = CwtModifierConfigGenerator(project)
    }

    class ModifierCategory : GenerateConfigActionBase() {
        override fun createGenerator(project: Project) = CwtModifierCategoriesConfigGenerator(project)
    }

    class GameRule : GenerateConfigActionBase() {
        override fun createGenerator(project: Project) = CwtGameRuleConfigGenerator(project)
    }

    class OnAction : GenerateConfigActionBase() {
        override fun createGenerator(project: Project) = CwtOnActionConfigGenerator(project)
    }
}
