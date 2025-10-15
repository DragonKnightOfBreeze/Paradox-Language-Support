package icu.windea.pls.lang.actions.cwt

import com.intellij.openapi.project.Project
import icu.windea.pls.config.util.generators.CwtEffectConfigGenerator
import icu.windea.pls.config.util.generators.CwtGameRuleConfigGenerator
import icu.windea.pls.config.util.generators.CwtLocalisationConfigGenerator
import icu.windea.pls.config.util.generators.CwtModifierCategoriesConfigGenerator
import icu.windea.pls.config.util.generators.CwtModifierConfigGenerator
import icu.windea.pls.config.util.generators.CwtOnActionConfigGenerator
import icu.windea.pls.config.util.generators.CwtTriggerConfigGenerator

class GenerateTriggerConfigAction : GenerateConfigActionBase() {
    override fun createGenerator(project: Project) = CwtTriggerConfigGenerator(project)
}

class GenerateEffectConfigAction : GenerateConfigActionBase() {
    override fun createGenerator(project: Project) = CwtEffectConfigGenerator(project)
}

class GenerateLocalisationConfigAction : GenerateConfigActionBase() {
    override fun createGenerator(project: Project) = CwtLocalisationConfigGenerator(project)
}

class GenerateModifierConfigAction : GenerateConfigActionBase() {
    override fun createGenerator(project: Project) = CwtModifierConfigGenerator(project)
}

class GenerateModifierCategoryConfigAction : GenerateConfigActionBase() {
    override fun createGenerator(project: Project) = CwtModifierCategoriesConfigGenerator(project)
}

class GenerateGameRuleConfigAction : GenerateConfigActionBase() {
    override fun createGenerator(project: Project) = CwtGameRuleConfigGenerator(project)
}

class GenerateOnActionConfigAction : GenerateConfigActionBase() {
    override fun createGenerator(project: Project) = CwtOnActionConfigGenerator(project)
}

