package icu.windea.pls.core

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.cwt.expression.*
import icu.windea.pls.script.psi.*
import java.util.*

data class ParadoxDefinitionPropertyInfo(
	val path: ParadoxPropertyPath,
	val scope:String? = null,
	val propertyConfigs:List<CwtPropertyConfig>, //基于keyExpression，valueExpression可能不同
	val childPropertyConfigs:List<CwtPropertyConfig>, //基于上一级keyExpression，keyExpression一定唯一
	val childValueConfigs:List<CwtValueConfig>, //基于上一级keyExpression，valueExpression一定唯一
	val childPropertyOccurrence:Map<CwtKeyExpression,Int>,
	val childValueOccurrence:Map<CwtValueExpression,Int>,
	val gameType: ParadoxGameType,
	private val pointer: SmartPsiElementPointer<ParadoxDefinitionProperty>
) {
	val propertyConfig : CwtPropertyConfig? = resolvePropertyConfig()
	val matchedPropertyConfig: CwtPropertyConfig? =resolveMatchedPropertyConfig()
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxDefinitionPropertyInfo && path == other.path
	}
	
	override fun hashCode(): Int {
		return Objects.hash(path)
	}
	
	fun resolvePropertyConfig(): CwtPropertyConfig?{
		//如果propertyConfigs不为空，则直接取第一个
		return propertyConfigs.firstOrNull()
	}
	
	fun resolveMatchedPropertyConfig(): CwtPropertyConfig?{
		//NOTE 如果变更了其他definitionProperty导致definition的类型发生变更，valueExpression会过时
		//需要匹配value
		val element = pointer.element?:return null
		val configGroup = getCwtConfig(element.project).getValue(gameType)
		if(element !is ParadoxScriptProperty) return null
		val propertyValue = element.propertyValue ?: return null
		if(propertyConfigs.isEmpty()) return null
		return propertyConfigs.find { matchesValue(it.valueExpression,propertyValue.value,configGroup) }
	}
}