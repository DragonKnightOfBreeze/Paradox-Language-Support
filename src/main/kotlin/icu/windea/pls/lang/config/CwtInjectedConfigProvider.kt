package icu.windea.pls.lang.config

import com.intellij.openapi.extensions.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*

/**
 * 用于基于CWT规则的上下文注入一些特定的CWT规则。
 * 
 * 这里获取的CWT规则可能覆盖原始的CWT规则，并且一般与其使用相同的文件位置。
 */
@WithGameTypeEP
interface CwtInjectedConfigProvider {
    /**
     * 注入CWT规则。
     * @param parentConfig 作为[configs]的父节点的规则。
     * @param configs 将会加入[parentConfig]的子规则列表的一组规则。
     * @return 是否进行了注入。
     */
    fun injectConfigs(parentConfig: CwtMemberConfig<*>, configs: MutableList<CwtMemberConfig<*>>) : Boolean
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtInjectedConfigProvider>("icu.windea.pls.injectedConfigProvider")
        
        fun injectConfigs(parentConfig: CwtMemberConfig<*>, configs: MutableList<CwtMemberConfig<*>>) : Boolean {
            val gameType = parentConfig.info.configGroup.gameType
            var r = false
            EP_NAME.extensionList.forEachFast f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f
                r = r || ep.injectConfigs(parentConfig, configs)
            }
            return r
        }
    }
}