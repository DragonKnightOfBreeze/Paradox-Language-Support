package com.windea.plugin.idea.pls.model

import com.windea.plugin.idea.pls.*
import com.windea.plugin.idea.pls.script.psi.*
import java.util.*

@Suppress("UNCHECKED_CAST")
data class ParadoxDefinitionInfo(
	val name: String,
	val type: ParadoxType,
	val subtypes: List<ParadoxType>,
	val localisation: Map<String, String>,
	val properties: Map<String, Any?>,
	val scopes: Map<String, Map<String,String>>,
	val fromVersion: String
){
	val typeText = buildTypeText()
	val resolvedLocalisation = mutableListOf<Pair<ConditionalExpression,String>>()
	val resolvedLocalisationNames = mutableListOf<String>()
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxDefinitionInfo && name == other.name && type == other.type
	}
	
	override fun hashCode(): Int {
		return Objects.hash(name,type)
	}
	
	override fun toString(): String {
		return "$name: $type"
	}
	
	
	private fun buildTypeText(): String {
		return buildString {
			append(type.name)
			if(subtypes.isNotEmpty()) {
				subtypes.joinTo(this, ", ", ", ") { subtype -> subtype.name }
			}
		}
	}
	
	fun resolveLocalisation(element: ParadoxScriptProperty) {
		for((key,value) in localisation) {
			//如果value以.开始，表示对应的属性的值是localisationKey，否则直接表示localisationKey，$为名字的占位符
			when{
				value.startsWith(".") -> {
					val k = key.toConditionalExpression()
					val propName = value.drop(1)
					val prop = element.findProperty(propName)?.propertyValue?.value ?: continue
					when{
						prop is ParadoxScriptBlock && prop.isArray -> {
							for(propValue in prop.valueList) {
								if(propValue is ParadoxScriptString) {
									val v = propValue.value
									resolvedLocalisation.add(k to v)
									resolvedLocalisationNames.add(v)
								}  
							}
						}
						prop is ParadoxScriptString -> {
							val v = prop.value
							resolvedLocalisation.add(k to v)
							resolvedLocalisationNames.add(v)
						}
					}
				}
				else -> {
					val k = key.toConditionalExpression()
					val v = formatPlaceholder(value,name)
					resolvedLocalisation.add(k to v)
					resolvedLocalisationNames.add(v)
				}
			}
		}
	}
	
	private fun formatPlaceholder(placeholder: String, name: String): String {
		return buildString {
			for(c in placeholder) if(c == '$') append(name) else append(c)
		}
	}
	
	fun resolvePropertiesList(subpaths:List<String>):List<Map<String,Any?>> {
		var propertiesList = listOf(properties)
		for(path in subpaths) {
			val propertiesList1 = mutableListOf<Map<String, Any?>>()
			for(properties in propertiesList) {
				//注意这里的properties的key是expression，而不是单纯的pattern
				val props = properties.findOrNull { (k,_) -> k.toConditionalExpression().value == path }
				when {
					props is Map<*, *> -> {
						props as? Map<String, Any?> ?: continue
						propertiesList1.add(props)
					}
					props is List<*> -> {
						for(prop in props) {
							when {
								prop is Map<*, *> -> {
									prop as? Map<String, Any?> ?: continue
									propertiesList1.add(prop)
								}
							}
						}
					}
				}
			}
			propertiesList = propertiesList1
		}
		return propertiesList
	}
}