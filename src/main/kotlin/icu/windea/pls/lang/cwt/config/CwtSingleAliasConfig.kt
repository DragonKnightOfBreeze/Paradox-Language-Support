package icu.windea.pls.lang.cwt.config

import com.intellij.psi.*
import com.intellij.util.containers.ContainerUtil
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*

class CwtSingleAliasConfig(
	override val pointer: SmartPsiElementPointer<out CwtProperty>,
	override val info: CwtConfigGroupInfo,
	override val config: CwtPropertyConfig,
	override val name: String
): CwtInlineableConfig<CwtProperty> {
	private val inlinedConfigCache by lazy { ContainerUtil.createConcurrentSoftKeySoftValueMap<CwtPropertyConfig, CwtPropertyConfig>() }
	
	fun inline(config: CwtPropertyConfig): CwtPropertyConfig {
		return inlinedConfigCache.computeIfAbsent(config) { doInline(config) }
	}
	
	private fun doInline(config: CwtPropertyConfig): CwtPropertyConfig {
		//inline all value and configs
		val other = this.config
		val inlined = config.copy(
			value = other.value,
			configs = ParadoxConfigGenerator.deepCopyConfigs(other),
			documentation = config.documentation ?: other.documentation,
			options = config.options
		)
		inlined.parentConfig = config.parentConfig
		inlined.configs?.forEachFast { it.parentConfig = inlined }
		inlined.inlineableConfig = config.inlineableConfig //should not set to this - a single alias config do not inline property key  
		return inlined
	}
}