package icu.windea.pls.ep.config

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.lang.annotations.WithGameTypeEP

/**
 * 用于基于上下文提供重载后的规则。
 *
 * 说明：
 * - 基于脚本文件的上下文，有时需要重载特定的脚本表达式对应的规则。
 * - 这里得到的规则会覆盖原始规则。
 */
@WithGameTypeEP
interface CwtOverriddenConfigProvider {
    /**
     * 得到重载后的规则列表。
     *
     * @param contextElement  上下文 PSI 元素。
     * @param config 原始规则。
     * @return 重载后的规则列表。如果为空，则表示未进行重载。
     */
    fun <T : CwtMemberConfig<*>> getOverriddenConfigs(contextElement: PsiElement, config: T): List<T>

    /**
     * 是否跳过缺失的表达式的代码检查。
     *
     * @see icu.windea.pls.lang.inspections.script.common.MissingExpressionInspection
     */
    fun skipMissingExpressionCheck(configs: List<CwtMemberConfig<*>>, configExpression: CwtDataExpression) = false

    /**
     * 是否跳过过多的表达式的代码检查。
     *
     * @see icu.windea.pls.lang.inspections.script.common.TooManyExpressionInspection
     */
    fun skipTooManyExpressionCheck(configs: List<CwtMemberConfig<*>>, configExpression: CwtDataExpression) = false

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtOverriddenConfigProvider>("icu.windea.pls.overriddenConfigProvider")
    }
}
