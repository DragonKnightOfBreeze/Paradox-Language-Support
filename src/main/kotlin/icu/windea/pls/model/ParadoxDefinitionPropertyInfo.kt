package icu.windea.pls.model

import icu.windea.pls.cwt.config.*
import icu.windea.pls.cwt.expression.*
import java.util.*

/**
 * @property name 名字
 * @property path 属性路径（可能为空，这时对应的definitionProperty也就是definition）
 */
data class ParadoxDefinitionPropertyInfo(
	val name: String,
	val path: ParadoxPropertyPath,
	val propertyConfigs:List<CwtPropertyConfig>, //基于keyExpression，valueExpression可能不同
	val childPropertyConfigs:List<CwtPropertyConfig>, //基于上一级keyExpression，keyExpression一定唯一
	val childValueConfigs:List<CwtValueConfig>, //基于上一级keyExpression，valueExpression一定唯一
	val propertyConfig:CwtPropertyConfig?, //基于keyExpression和valueExpression
	val childPropertyOccurrence:Map<CwtKeyExpression,Int>,
	val childValueOccurrence:Map<CwtValueExpression,Int>,
	val gameType: ParadoxGameType
) {
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxDefinitionPropertyInfo && name == other.name && path == other.path
	}
	
	override fun hashCode(): Int {
		return Objects.hash(name, path)
	}
}