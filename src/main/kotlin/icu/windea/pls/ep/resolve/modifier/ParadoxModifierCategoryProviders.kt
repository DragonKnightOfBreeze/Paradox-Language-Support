package icu.windea.pls.ep.resolve.modifier

import icu.windea.pls.ChronicleFacade
import icu.windea.pls.base.annotations.ForGameType
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxModifierInfo
import icu.windea.pls.model.economicCategoryInfo
import icu.windea.pls.model.modifierConfig

/**
 * 适用于预定义的修正。
 *
 * @see ParadoxPredefinedModifierSupport
 */
class ParadoxPredefinedModifierCategoryProvider: ParadoxModifierCategoryProvider {
    override fun getModifierCategories(modifier: ParadoxModifierLightElement): Map<String, CwtModifierCategoryConfig>? {
        return modifier.modifierConfig?.categoryConfigMap
    }

    override fun getModifierCategories(modifierInfo: ParadoxModifierInfo): Map<String, CwtModifierCategoryConfig>? {
        return modifierInfo.modifierConfig?.categoryConfigMap
    }
}

/***
 * 适用于从模板表达式生成的修正。
 *
 * @see ParadoxTemplateModifierSupport
 */
class ParadoxTemplateModifierCategoryProvider: ParadoxModifierCategoryProvider {
    override fun getModifierCategories(modifier: ParadoxModifierLightElement): Map<String, CwtModifierCategoryConfig>? {
        return modifier.modifierConfig?.categoryConfigMap
    }

    override fun getModifierCategories(modifierInfo: ParadoxModifierInfo): Map<String, CwtModifierCategoryConfig>? {
        return modifierInfo.modifierConfig?.categoryConfigMap
    }
}

/**
 * （仅限 Stellaris）适用于从经济分类（`economic_category`）生成的修正.
 *
 * @see ParadoxEconomicCategoryModifierSupport
 */
@ForGameType(ParadoxGameType.Stellaris)
class ParadoxEconomicCategoryModifierCategoryProvider: ParadoxModifierCategoryProvider {
    override fun getModifierCategories(modifier: ParadoxModifierLightElement): Map<String, CwtModifierCategoryConfig>? {
        val economicCategoryInfo = modifier.economicCategoryInfo ?: return null
        val modifierCategory = economicCategoryInfo.modifierCategory // may be null
        val configGroup = ChronicleFacade.getConfigGroup(modifier.project, modifier.gameType)
        return ParadoxConfigManager.getModifierCategory(modifierCategory, configGroup)
    }

    override fun getModifierCategories(modifierInfo: ParadoxModifierInfo): Map<String, CwtModifierCategoryConfig>? {
        val economicCategoryInfo = modifierInfo.economicCategoryInfo ?: return null
        val modifierCategory = economicCategoryInfo.modifierCategory // may be null
        val configGroup = ChronicleFacade.getConfigGroup(modifierInfo.project, modifierInfo.gameType)
        return ParadoxConfigManager.getModifierCategory(modifierCategory, configGroup)
    }
}
