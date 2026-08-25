package icu.windea.pls.lang.scope

import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.equalsFast
import icu.windea.pls.model.scope.ParadoxScope
import icu.windea.pls.model.scope.ParadoxScopeConstants
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.model.scope.promotions

@Suppress("unused")
@Optimized
object ParadoxScopeMatchService {
    // NOTE 3.0.2 support to match union scopes (super scopes will not match sub scopes)

    // 作用域的匹配逻辑（scope -> scopeToMatch）：
    // - scope 是输入的数据（从上下文中获取或推断）。
    // - scopeToMatch 应来自可信的数据源（规则文件或代码实现）。
    // - 如果 scope 为 `null`，则直接匹配。
    // - 如果 scopeToMatch 为 `any`，则直接匹配（scopeToMatch 不应为 `?`，不处理这种情况）。
    // - 如果 scope 为 `any` 或 `?`，则直接匹配。
    // - 如果 scope 的 ID 完全匹配 scopeToMatch，则匹配（比较字符串，值相等，不忽略大小写）。
    // - 如果 scope 存在别名（来自 `scopeConfig.aliases`），则尝试用这些别名完全匹配 scopeToMatch。
    // - 如果 scope 存在父作用域（来自 `scopeConfig.is_subscope_of`），则尝试用父作用域的 ID 和别名完全匹配 scopeToMatch。这需要递归进行。
    // - 如果 scope 存在提升（来自 `scopeContext.promotions`，而这来自 `localisationPromotionConfig.supportedScopes`），则尝试用这些提升的 ID 和别名完全匹配 scopeToMatch。

    /**
     * 匹配作用域。兼容通配形式和别名形式。兼容继承关系（父作用域不会匹配子作用域）。
     *
     * [scope] 是输入的作用域，[scopeToMatch] 应是来自可行数据源（规则文件或代码实现）的要匹配的作用域。
     * 两者都应是规范化后的作用域的 ID。
     *
     * @see ParadoxScope
     * @see ParadoxScopeContext
     */
    fun matchesScopeId(scope: String?, scopeToMatch: String, configGroup: CwtConfigGroup): Boolean {
        // 3.0.2 for any, all, union (e.g., carrier), and other normal scopes
        if (scope == null) return true
        if (scopeToMatch.equalsFast(ParadoxScopeConstants.anyScope)) return true
        if (scope.equalsFast(ParadoxScopeConstants.anyScope)) return true
        if (scope.equalsFast(ParadoxScopeConstants.unknownScope)) return true
        if (scope.equalsFast(scopeToMatch)) return true
        if (matchesScopeFromModel(ParadoxScope.resolve(scope), scopeToMatch, configGroup)) return true
        return false
    }

    /**
     * 匹配作用域。兼容通配形式和别名形式。兼容继承关系（父作用域不会匹配子作用域）。
     *
     * [scope] 是输入的作用域，[scopeToMatch] 应是来自可行数据源（规则文件或代码实现）的要匹配的作用域。
     * [scopeToMatch] 应是规范化后的作用域的 ID。
     *
     * @see ParadoxScope
     * @see ParadoxScopeContext
     */
    fun matchesScope(scope: ParadoxScope?, scopeToMatch: String, configGroup: CwtConfigGroup): Boolean {
        // 3.0.2 for any, all, union (e.g., carrier), and other normal scopes
        if (scope == null) return true
        if (scopeToMatch.equalsFast(ParadoxScopeConstants.anyScope)) return true
        if (scope === ParadoxScope.Any) return true
        if (scope === ParadoxScope.Unknown) return true
        if (scope.id.equalsFast(scopeToMatch)) return true
        if (matchesScopeFromModel(scope, scopeToMatch, configGroup)) return true
        return false
    }

    /**
     * 匹配作用域。兼容通配形式和别名形式。兼容继承关系（父作用域不会匹配子作用域）和提升关系。
     *
     * [scopeContext] 是输入的作用域上下文，[scopeToMatch] 应是来自可行数据源（规则文件或代码实现）的要匹配的作用域。
     * [scopeToMatch] 应是规范化后的作用域的 ID。
     *
     * @see ParadoxScope
     * @see ParadoxScopeContext
     */
    fun matchesScope(scopeContext: ParadoxScopeContext?, scopeToMatch: String, configGroup: CwtConfigGroup): Boolean {
        if (scopeContext == null) return true
        val scope = scopeContext.scope
        if (scopeToMatch.equalsFast(ParadoxScopeConstants.anyScope)) return true
        if (scope === ParadoxScope.Any) return true
        if (scope === ParadoxScope.Unknown) return true
        if (scope.id.equalsFast(scopeToMatch)) return true
        if (matchesScopeFromModel(scope, scopeToMatch, configGroup)) return true
        if (matchesScopeFromPromotions(scopeContext, scopeToMatch, configGroup)) return true
        return false
    }

    /**
     * 匹配作用域。兼容通配形式和别名形式。兼容继承关系（父作用域不会匹配子作用域）和提升关系。
     *
     * [scopeContext] 是输入的作用域上下文，[scopesToMatch] 应是来自可行数据源（规则文件或代码实现）的要匹配的作用域。
     * [scopesToMatch] 应是一组规范化后的作用域的 ID。
     *
     * @see ParadoxScope
     * @see ParadoxScopeContext
     */
    fun matchesScope(scopeContext: ParadoxScopeContext?, scopesToMatch: Set<String>?, configGroup: CwtConfigGroup): Boolean {
        if (scopeContext == null) return true
        val scope = scopeContext.scope
        if (scopesToMatch.isNullOrEmpty() || scopesToMatch == ParadoxScopeConstants.anyScopes) return true
        if (scope === ParadoxScope.Any) return true
        if (scope === ParadoxScope.Unknown) return true
        if (scope.id in scopesToMatch) return true
        if (matchesScopeFromModel(scope, scopesToMatch, configGroup)) return true
        if (matchesScopeFromPromotions(scopeContext, scopesToMatch, configGroup)) return true
        return false
    }

    private fun matchesScopeFromModel(scope: ParadoxScope, scopeToMatch: String, configGroup: CwtConfigGroup): Boolean {
        // optimize: access scope model and check scope indexes for better performance
        val scopeModel = configGroup.scopeModel
        val matched = scopeModel.base2MatchedScopes.get(scope.index).orEmpty()
        if(matched.isNotEmpty()) {
            if (matched.contains(ParadoxScope.resolve(scopeToMatch).index)) return true
        }
        return false
    }

    private fun matchesScopeFromModel(scope: ParadoxScope, scopesToMatch: Set<String>, configGroup: CwtConfigGroup): Boolean {
        // optimize: access scope model and check scope indexes for better performance
        val scopeModel = configGroup.scopeModel
        val matched = scopeModel.base2MatchedScopes.get(scope.index).orEmpty()
        if (matched.isNotEmpty()) {
            for (scopeToMatch in scopesToMatch) {
                if (matched.contains(ParadoxScope.resolve(scopeToMatch).index)) return true
            }
        }
        return false
    }

    private fun matchesScopeFromPromotions(scopeContext: ParadoxScopeContext, scopeToMatch: String, configGroup: CwtConfigGroup): Boolean {
        val promotions = scopeContext.promotions
        if (promotions.isEmpty()) return false
        for (promotion in promotions) {
            if (promotion.equalsFast(scopeToMatch)) return true
            if (matchesScopeFromModel(ParadoxScope.resolve(promotion), scopeToMatch, configGroup)) return true
        }
        return false
    }

    private fun matchesScopeFromPromotions(scopeContext: ParadoxScopeContext, scopesToMatch: Set<String>, configGroup: CwtConfigGroup): Boolean {
        val promotions = scopeContext.promotions
        if (promotions.isEmpty()) return false
        for (promotion in promotions) {
            for (scopeToMatch in scopesToMatch) {
                if (promotion.equalsFast(scopeToMatch)) return true
                if (matchesScopeFromModel(ParadoxScope.resolve(promotion), scopeToMatch, configGroup)) return true
            }
        }
        return false
    }

    fun matchesScopeGroup(scopeContext: ParadoxScopeContext?, scopeGroupToMatch: String, configGroup: CwtConfigGroup): Boolean {
        if (scopeContext == null) return true
        val scope = scopeContext.scope
        if (scope === ParadoxScope.Any) return true
        if (scope === ParadoxScope.Unknown) return true
        val scopeGroupConfig = configGroup.scopeGroups[scopeGroupToMatch] ?: return false
        val scopesToMatch = scopeGroupConfig.values
        if (scope.id in scopesToMatch) return true
        if (matchesScopeFromModel(scope, scopesToMatch, configGroup)) return true
        if (matchesScopeFromPromotions(scopeContext, scopesToMatch, configGroup)) return true
        return false
    }
}
