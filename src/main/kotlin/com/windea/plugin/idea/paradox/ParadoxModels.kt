package com.windea.plugin.idea.paradox

import com.intellij.util.ui.*
import com.windea.plugin.idea.paradox.script.psi.*
import java.awt.*
import java.util.*

//File

data class ParadoxPath(
	val subpaths:List<String>
){
	val path = subpaths.joinToString("/")
	val fileName = subpaths.lastOrNull().orEmpty()
	val fileExtension = fileName.substringAfterLast('.')
	val parentSubpaths = subpaths.dropLast(1)
	val parent = parentSubpaths.joinToString("/")
	val root = parentSubpaths.firstOrNull().orEmpty()
	val length = subpaths.size
	val parentLength = parentSubpaths.size
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxPath && path == other.path
	}
	
	override fun hashCode(): Int {
		return path.hashCode()
	}
	
	override fun toString(): String {
		return path
	}
}

enum class ParadoxFileType{
	Script,
	Localisation,
	ScriptRule
}

enum class ParadoxGameType(
	val key: String,
	val text: String
) {
	Stellaris("stellaris", "Stellaris")
}

enum class ParadoxRootType(
	val key:String,
	val text: String
) {
	Stdlib("stdlib","Stdlib"),
	Mod("mod","Mod"),
	PdxLauncher("pdx_launcher","Paradox Launcher"),
	PdxOnlineAssets("pdx_online_assets","Paradox Online Assets"),
	TweakerGuiAssets("tweakergui_assets","Tweaker GUI Assets")
}

data class ParadoxFileInfo(
	val name: String,
	val path: ParadoxPath,
	val fileType: ParadoxFileType,
	val rootType: ParadoxRootType,
	val gameType: ParadoxGameType
)

//Script

data class ParadoxType(
	val name:String,
	val aliases:List<String> = emptyList()
){
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxType && name == other.name
	}
	
	override fun hashCode(): Int {
		return name.hashCode()
	}
	
	override fun toString(): String {
		return name
	}
}

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

//Localisation
//rules/core/enums.yml

class ParadoxLocale(data:Map<String,Any>) {
	val name:String by data
	val description:String by data
	val popupText = "$name - $description"
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxLocale && name == other.name
	}
	
	override fun hashCode(): Int {
		return name.hashCode()
	}
	
	override fun toString(): String {
		return name
	}
}

class ParadoxSequentialNumber(data:Map<String,Any>) {
	val name: String by data
	val description:String by data
	val placeholderText :String by data
	val popupText = "$name - $description"
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxSequentialNumber && name == other.name
	}
	
	override fun hashCode(): Int {
		return name.hashCode()
	}
	
	override fun toString(): String {
		return name
	}
}

class ParadoxColor(data:Map<String,Any>){
	val name: String by data
	val description: String by data
	val colorRgb:Int by data
	val colorText:String by data

	val popupText = "$name - $description"
	val color: Color = Color(colorRgb)
	val icon = ColorIcon(16, color)
	val gutterIcon = ColorIcon(12, color)
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxColor && name == other.name
	}
	
	override fun hashCode(): Int {
		return name.hashCode()
	}
	
	override fun toString(): String {
		return name
	}
}

class ParadoxCommandScope(data:Map<String,Any>) {
	private val dataWithDefault = data.withDefault { key->
		when(key){
			"isPrimary" -> false
			"isSecondary" -> false
			else -> null
		}
	}
	
	val name:String by dataWithDefault
	val description: String by dataWithDefault
	val isPrimary:Boolean by dataWithDefault
	val isSecondary:Boolean by dataWithDefault
	
	override fun equals(other: Any?) = this === other || other is ParadoxCommandScope && name == other.name
	
	override fun hashCode() = name.hashCode()
	
	override fun toString() = name
}

class ParadoxCommandField(data:Map<String,Any>){
	private val dataWithDefault = data.withDefault { key->
		when(key){
			"description" -> ""
			else -> null
		}
	}
	
	val name:String by dataWithDefault
	val description:String by dataWithDefault
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxCommandField && name == other.name
	}
	
	override fun hashCode(): Int {
		return name.hashCode()
	}
	
	override fun toString(): String {
		return name
	}
}

//Others

enum class ValidateState{
	Ok, Unresolved,Dupliate
}