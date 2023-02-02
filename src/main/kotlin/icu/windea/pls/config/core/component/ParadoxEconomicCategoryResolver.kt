package icu.windea.pls.config.core.component

import icu.windea.pls.config.core.config.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.*

/**
 * 通过经济类型（`economic_category`）生成修饰符。
 */
@WithGameType(ParadoxGameType.Stellaris)
class ParadoxEconomicCategoryResolver: ParadoxModifierResolver {
    override fun matchModifier(name: String, configGroup: CwtConfigGroup, matchType: Int): Boolean {
        return false
    }
    
    override fun resolveModifier(name: String, element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxModifierElement? {
        return null
    }
}