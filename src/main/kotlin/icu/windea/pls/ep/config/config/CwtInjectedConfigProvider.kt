package icu.windea.pls.ep.config.config

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.lang.annotations.WithGameTypeEP

/**
 * 用于基于上下文注入规则。
 *
 * 说明：
 * - 基于规则的上下文，有时需要在特定位置注入规则。
 * - 这里得到的规则可能覆盖原始的规则，并且一般与其使用相同的文件位置。
 */
@WithGameTypeEP
interface CwtInjectedConfigProvider {
    /**
     * 注入规则。
     *
     * @param parentConfig 作为 [configs] 的父节点的规则。
     * @param configs 将会加入 [parentConfig] 的子规则列表的一组规则。
     * @return 是否进行了注入。
     */
    fun injectConfigs(parentConfig: CwtMemberConfig<*>, configs: MutableList<CwtMemberConfig<*>>): Boolean

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtInjectedConfigProvider>("icu.windea.pls.injectedConfigProvider")
    }
}
