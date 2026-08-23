package icu.windea.pls.lang.resolve

import com.intellij.psi.util.parentOfType
import icu.windea.pls.base.context.ChronicleThreadContext
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.extended.CwtExtendedInlineScriptConfig
import icu.windea.pls.config.config.extended.CwtExtendedParameterConfig
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.model.containingContextReference
import icu.windea.pls.script.psi.ParadoxScriptMember

@Suppress("unused")
@Optimized
object ParadoxExtendedConfigService {
    /**
     * @see CwtExtendedParameterConfig.getContextContainerConfig
     */
    fun getContextContainerConfig(config: CwtExtendedParameterConfig, parameterElement: ParadoxParameterLightElement): CwtMemberConfig<*> {
        return config.getContextContainerConfig()
    }

    /**
     * 如果 [CwtExtendedParameterConfig.inherit] 为 `true`，则会继承来自 [parameterElement] 的规则上下文。
     *
     * @see CwtExtendedParameterConfig.getContextConfigs
     */
    fun getContextConfigs(config: CwtExtendedParameterConfig, parameterElement: ParadoxParameterLightElement): List<CwtMemberConfig<*>> {
        if (config.inherit) {
            run {
                val contextReferenceElement = parameterElement.containingContextReference?.element ?: return@run
                val parentElement = contextReferenceElement.parentOfType<ParadoxScriptMember>(false) ?: return@run
                val contextConfigs = ParadoxConfigManager.getContextConfigs(parentElement)
                ChronicleThreadContext.resolvingConfigContextStack.get()?.peekLast()?.markDynamic() // NOTE 2.1.2 需要把正在解析的规则上下文标记为动态的
                return contextConfigs
            }
            return emptyList()
        }
        return config.getContextConfigs()
    }

    /**
     * @see CwtExtendedInlineScriptConfig.getContextContainerConfig
     */
    fun getContextContainerConfig(config: CwtExtendedInlineScriptConfig): CwtMemberConfig<*> {
        return config.getContextContainerConfig()
    }

    /**
     * @see CwtExtendedInlineScriptConfig.getContextConfigs
     */
    fun getContextConfigs(config: CwtExtendedInlineScriptConfig): List<CwtMemberConfig<*>> {
        return config.getContextConfigs()
    }
}
