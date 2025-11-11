package icu.windea.pls.ep.configGroup

import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.CwtConfigGroupInitializer

/**
 * 用于初始化规则分组中预先定义的那些数据。
 */
class CwtPredefinedConfigGroupDataProvider : CwtConfigGroupDataProvider {
    override suspend fun process(initializer: CwtConfigGroupInitializer, configGroup: CwtConfigGroup): Boolean {
        with(initializer.aliasNamesSupportScope) {
            this += "modifier" // 也支持，但不能切换作用域
            this += "trigger"
            this += "effect"
        }
        with(initializer.definitionTypesModel) {
            with(supportScope) {
                this += "scripted_effect"
                this += "scripted_trigger"
                this += "game_rule"
            }
            with(indirectSupportScope) {
                this += "on_action" // 也支持，其中调用的事件的类型要匹配
                this += "event" // 事件
            }
            with(skipCheckSystemScope) {
                this += "event"
                this += "scripted_trigger"
                this += "scripted_effect"
                this += "script_value"
                this += "game_rule"
            }
            with(supportParameters) {
                this += "script_value"
                // this += "inline_script" // 内联脚本也支持参数（但它不是定义）
            }
        }

        return true
    }
}
