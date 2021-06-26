package icu.windea.pls.cwt.config

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*
import java.util.*

data class CwtDefinitionConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name: String,
	val configs: List<Pair<String?,CwtPropertyConfig>> //(subtypeExpression, propConfig)
) : CwtConfig<CwtProperty> {
	//使用WeakHashMap - 减少内存占用
	private val mergeConfigsCache = WeakHashMap<String,List<CwtPropertyConfig>>()
	private val propertyConfigsCache = WeakHashMap<String,List<CwtPropertyConfig>>()
	private val childPropertyConfigsCache = WeakHashMap<String,List<CwtPropertyConfig>>()
	private val childValueConfigsCache = WeakHashMap<String,List<CwtValueConfig>>()
	
	/**
	 * 根据子类型列表合并配置。
	 */
	fun mergeConfigs(subtypes: List<String>): List<CwtPropertyConfig> {
		val cacheKey = subtypes.joinToString(",")
		return mergeConfigsCache.getOrPut(cacheKey){
			val result = mutableListOf<CwtPropertyConfig>()
			for((subtypeExpression, propConfig) in configs) {
				if(subtypeExpression == null || matchesSubtypeExpression(subtypeExpression,subtypes)) {
					result.add(propConfig)
				}
			}
			result
		}
	}
	
	/**
	 * 根据路径解析对应的属性配置列表。
	 */
	fun resolvePropertyConfigs(subtypes: List<String>,path: ParadoxPropertyPath,configGroup: CwtConfigGroup): List<CwtPropertyConfig> {
		val cacheKey = "${subtypes.joinToString(",")}$path"
		return propertyConfigsCache.getOrPut(cacheKey){
			when {
				//这里的属性路径不应该为空
				path.isEmpty() -> emptyList()
				else -> {
					var result = mergeConfigs(subtypes)
					var isTop = true
					for((key, quoted) in path.subPathInfos) {
						if(isTop) isTop = false else result = result.flatMap { it.properties?: emptyList() }
						result = result.filter { matchesKey(it.keyExpression, key, quoted, configGroup) }
					}
					result
				}
			}
		}
	}
	
	/**
	 * 根据路径解析对应的子属性配置列表。（过滤重复的）
	 */
	fun resolveChildPropertyConfigs(subtypes: List<String>,path: ParadoxPropertyPath, configGroup: CwtConfigGroup): List<CwtPropertyConfig> {
		val cacheKey = "${subtypes.joinToString(",")}$path"
		return childPropertyConfigsCache.getOrPut(cacheKey){
			when {
				//这里的属性路径可以为空，这时得到的就是顶级属性列表
				path.isEmpty() -> mergeConfigs(subtypes)
				else -> resolvePropertyConfigs(subtypes,path, configGroup).flatMap { it.properties?:emptyList() }
			}
		}.distinctBy { it.key }
	}
	
	/**
	 * 根据路径解析对应的子值配置列表。（过滤重复的）
	 */
	fun resolveChildValuesConfigs(subtypes: List<String>,path: ParadoxPropertyPath, configGroup: CwtConfigGroup): List<CwtValueConfig> {
		val cacheKey = "${subtypes.joinToString(",")}$path"
		return childValueConfigsCache.getOrPut(cacheKey){
			when {
				//这里的属性路径可以为空，这时得到的是空列表（假定在顶级的是属性不是值）
				path.isEmpty() -> emptyList()
				else -> resolvePropertyConfigs(subtypes,path, configGroup).flatMap { it.values?:emptyList() }
			}
		}.distinctBy{it.value} 
	}
}

