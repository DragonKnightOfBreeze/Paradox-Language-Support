package icu.windea.pls.model

import java.util.*

data class ParadoxDefinitionPropertyInfo(
	val name:String,
	val path: ParadoxPath, //可能为空，这时对应的definitionProperty也就是definition
	val propertiesCardinality:Map<String,Int>, //统计相同名字的属性出现的数量
	val definitionInfo: ParadoxDefinitionInfo
){
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxDefinitionPropertyInfo && name == other.name && path == other.path 
			&& definitionInfo == other.definitionInfo
	}
	
	override fun hashCode(): Int {
		return Objects.hash(name, path, definitionInfo)
	}
}