package icu.windea.pls.config.settings

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.util.registry.Registry

/**
 * PLS 内部规则设置。可以通过 Registry 页面进行调整。
 */
@Service
class PlsConfigInternalSettings {
    /**
     * 处理成员规则的选项数据时，是否保留选项规则列表到其用户数据中（默认仅为内部规则保留）。
     */
    val keepOptionConfigs: Boolean get() = Registry.`is`("pls.settings.config.keepOptionConfigs", false)

    /**
     * 通过匹配的规则来检查脚本属性是否允许使用比较运算符作为分隔符（匹配的规则应显式使用 `==` 作为属性分隔符）。
     */
    val checkComparisonOperators: Boolean get() = Registry.`is`("pls.settings.config.checkComparisonOperators", false)

    companion object {
        @JvmStatic
        fun getInstance(): PlsConfigInternalSettings = service()
    }
}
