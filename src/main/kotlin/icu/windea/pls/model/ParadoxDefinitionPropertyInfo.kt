package icu.windea.pls.model

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.cwt.config.*
import icu.windea.pls.cwt.expression.*
import icu.windea.pls.script.psi.*
import java.util.*

data class ParadoxDefinitionPropertyInfo(
	val name:String,
	val path: ParadoxPropertyPath,
	val propertyConfigs:List<CwtPropertyConfig>, //基于keyExpression，valueExpression可能不同
	val childPropertyConfigs:List<CwtPropertyConfig>, //基于上一级keyExpression，keyExpression一定唯一
	val childValueConfigs:List<CwtValueConfig>, //基于上一级keyExpression，valueExpression一定唯一
	val childPropertyOccurrence:Map<CwtKeyExpression,Int>,
	val childValueOccurrence:Map<CwtValueExpression,Int>,
	val gameType: ParadoxGameType,
	private val pointer: SmartPsiElementPointer<ParadoxDefinitionProperty>
) {
	val keyExpression: CwtKeyExpression? = resolveKeyExpression()
	val valueExpression: CwtValueExpression? = resolveValueExpression()
	val valueExpressions:List<CwtValueExpression> = resolveValueExpressions()
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxDefinitionPropertyInfo && name == other.name && path == other.path
	}
	
	override fun hashCode(): Int {
		return Objects.hash(name, path)
	}
	
	fun resolveKeyExpression(): CwtKeyExpression? {
		//如果propertyConfigs不为空，则直接取其中第一个的keyExpression
		return propertyConfigs.firstOrNull()?.keyExpression
	}
	
	fun resolveValueExpression():CwtValueExpression?{
		//NOTE 如果变更了其他definitionProperty导致definition的类型发生变更，valueExpression会过时
		//必须要匹配
		val element = pointer.element?:return null
		val configGroup = getConfig(element.project).getValue(gameType)
		if(element !is ParadoxScriptProperty) return null
		val propertyValue = element.propertyValue ?: return null
		if(propertyConfigs.isEmpty()) return null
		return propertyConfigs.find { matchesValue(it.valueExpression,propertyValue.value,configGroup) }?.valueExpression
	}
	
	fun resolveValueExpressions(): List<CwtValueExpression> {
		//直接映射即可
		return childValueConfigs.map { it.valueExpression }
	}
}