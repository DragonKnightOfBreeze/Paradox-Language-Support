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
	val propertyConfigs:List<CwtPropertyConfig>,
	val childPropertyConfigs:List<CwtPropertyConfig>,
	val childValueConfigs:List<CwtValueConfig>,
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