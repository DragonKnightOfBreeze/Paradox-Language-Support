package icu.windea.pls.ep.config

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*

/**
 * 用于基于上下文为某些特定的脚本表达式提供重载后的CWT规则。
 *
 * 这里获取的CWT规则会覆盖原始的CWT规则。
 */
@WithGameTypeEP
interface CwtOverriddenConfigProvider {
    /**
     * 基于指定的上下文PSI元素[contextElement]和原始的CWT规则[config]获取重载后的CWT规则。
     */
    fun <T : CwtMemberConfig<*>> getOverriddenConfigs(contextElement: PsiElement, config: T): List<T>?

    fun skipMissingExpressionCheck(configs: List<CwtMemberConfig<*>>, configExpression: CwtDataExpression) = false

    fun skipTooManyExpressionCheck(configs: List<CwtMemberConfig<*>>, configExpression: CwtDataExpression) = false

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtOverriddenConfigProvider>("icu.windea.pls.overriddenConfigProvider")

        fun <T : CwtMemberConfig<*>> getOverriddenConfigs(contextElement: PsiElement, config: T): List<T>? {
            val gameType = config.configGroup.gameType ?: return null
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                ep.getOverriddenConfigs(contextElement, config).orNull()
                    ?.onEach {
                        it.originalConfig = config
                        it.overriddenProvider = ep
                    }
                    ?.also { PlsCoreManager.dynamicContextConfigs.set(true) }
            }
        }
    }
}

