package icu.windea.pls.lang.config

import com.intellij.openapi.extensions.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.script.psi.*

/**
 * 用于在某些极个别情况下基于另外的逻辑获取脚本表达式对应的CWT规则。
 * 这里获取的CWT规则会覆盖原始的CWT规则。
 *
 * 这里的处理逻辑不是向下或者向上内联脚本成员元素，获取的相关元素路径、定义信息等是不会发生变化的。
 */
@WithGameTypeEP
interface ParadoxOverriddenConfigProvider {
    /**
     * 从指定的定义成员元素[element]和原始的CWT规则[rawConfig]获取重载后的CWT规则。
     */
    fun <T : CwtDataConfig<*>> getOverriddenConfigs(element: ParadoxScriptMemberElement, rawConfig: T): List<T>?
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxOverriddenConfigProvider>("icu.windea.pls.overridenConfigProvider")
        
        fun <T: CwtDataConfig<*>> getOverriddenConfigs(element: ParadoxScriptMemberElement, rawConfig: T): List<T>? {
            val gameType = rawConfig.info.configGroup.gameType ?: return null
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                ep.getOverriddenConfigs(element, rawConfig).takeIfNotEmpty()
            }?.onEach { it.isOverridden = true }
        }
    }
}