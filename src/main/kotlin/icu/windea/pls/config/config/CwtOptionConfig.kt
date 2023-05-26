package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.model.*
import java.util.concurrent.*

class CwtOptionConfig private constructor(
	override val pointer: SmartPsiElementPointer<CwtOption>, //NOTE 目前并未使用，因此直接传入emptyPointer()即可
	override val info: CwtConfigGroupInfo,
	override val key: String,
	override val value: String,
	override val valueTypeId: Byte,
	override val separatorTypeId: Byte,
	override val options: List<CwtOptionConfig>?,
	override val optionValues: List<CwtOptionValueConfig>?
) : CwtConfig<CwtOption>, CwtPropertyAware, CwtOptionsAware {
	companion object Resolver{
		private val cache = ConcurrentHashMap<String, CwtOptionConfig>()
		
		fun resolve(
			 pointer: SmartPsiElementPointer<CwtOption>, 
			 info: CwtConfigGroupInfo,
			 key: String,
			 value: String,
			 valueTypeId: Byte = CwtType.String.id,
			 separatorTypeId: Byte = CwtSeparatorType.EQUAL.id,
			 options: List<CwtOptionConfig>? = null,
			 optionValues: List<CwtOptionValueConfig>? = null
		): CwtOptionConfig {
			//use cache if possible to optimize memory
			if(options == null || optionValues == null) {
				val cacheKey = "${valueTypeId}#${separatorTypeId}#${key}#${value}"
				return cache.getOrPut(cacheKey) {
					CwtOptionConfig(pointer, info, key, value, valueTypeId, separatorTypeId, options, optionValues)
				}
			}
			return CwtOptionConfig(pointer, info, key, value, valueTypeId, separatorTypeId, options, optionValues)
		}
	}
}