package icu.windea.pls.ep.configGroup

import icu.windea.pls.config.configGroup.*

/**
 * 用于初始规则分组中预先定义的那些数据。
 */
class PredefinedCwtConfigGroupDataProvider: CwtConfigGroupDataProvider {
    override fun process(configGroup: CwtConfigGroup): Boolean {
        run {
            with(configGroup.aliasNamesSupportScope) {
                this += "modifier" //也支持，但不能切换作用域
                this += "trigger"
                this += "effect"
            }
            with(configGroup.definitionTypesSupportScope) {
                this += "scripted_effect"
                this += "scripted_trigger"
                this += "game_rule"
            }
            with(configGroup.definitionTypesIndirectSupportScope) {
                this += "on_action" //也支持，其中调用的事件的类型要匹配
                this += "event" //事件
            }
            with(configGroup.definitionTypesSkipCheckSystemLink) {
                this += "event"
                this += "scripted_trigger"
                this += "scripted_effect"
                this += "script_value"
                this += "game_rule"
            }
            with(configGroup.definitionTypesSupportParameters) {
                this += "script_value"
                //this += "inline_script" //内联脚本也支持参数（但它不是定义）
            }
        }
        
        return true
    }
}