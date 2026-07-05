package icu.windea.pls.ep.config.config

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.model.ParadoxGameType

/**
 * 用于基于上下文注入规则。
 *
 * 说明：
 * - 基于规则的上下文，有时需要在特定位置注入规则。
 * - 这里得到的规则可能覆盖原始的规则，并且在引用解析时，一般使用相同的文件位置。
 */
interface CwtInjectedConfigProvider {
    fun supports(gameType: ParadoxGameType): Boolean = true

    fun supports(parentConfig: CwtMemberConfig<*>): Boolean = supports(parentConfig.configGroup.gameType)

    /**
     * 注入规则。
     *
     * @param parentConfig 原始的父规则。
     * @param containerConfig 新的容器规则。
     * @param configs 将会加入入作为 [containerConfig] 的子规则的一组规则。
     * @return 是否进行了注入。
     */
    fun injectConfigs(parentConfig: CwtMemberConfig<*>, containerConfig: CwtMemberConfig<*>, configs: MutableList<CwtMemberConfig<*>>): Boolean

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<CwtInjectedConfigProvider>("icu.windea.pls.injectedConfigProvider")
    }
}
