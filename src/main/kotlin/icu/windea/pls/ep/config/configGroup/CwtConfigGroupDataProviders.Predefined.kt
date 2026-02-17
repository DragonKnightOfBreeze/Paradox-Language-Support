package icu.windea.pls.ep.config.configGroup

import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * 用于初始化规则分组中预先定义的那些数据。
 */
class CwtPredefinedConfigGroupDataProvider : CwtConfigGroupDataProvider {
    override suspend fun process(configGroup: CwtConfigGroup) {
        val initializer = configGroup.initializer

        with(initializer.aliasNamesSupportScope) {
            this += "modifier" // 也支持，但不能切换作用域
            this += "trigger"
            this += "effect"
        }
        with(initializer.definitionTypesModel) {
            with(supportScope) {
                this += "scripted_trigger"
                this += "scripted_effect"
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
                this += "scripted_trigger" // 也来自具体的规则（`alias[trigger:<scripted_trigger>] = ...`）
                this += "scripted_effect" // 也来自具体的规则（`alias[effect:<scripted_effect>] = ...`）
                this += "script_value"
                // this += "inline_script" // 也支持参数（但内联脚本不是定义）
            }
            with(supportScopeContextInference) {
                this += "scripted_trigger"
                this += "scripted_effect"
            }
        }
    }

    override suspend fun postProcess(configGroup: CwtConfigGroup) {
        // 2.0.7 nothing now (since it's not very necessary)
    }
}
