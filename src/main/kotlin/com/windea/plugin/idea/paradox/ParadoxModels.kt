package com.windea.plugin.idea.paradox

import com.intellij.util.ui.*
import com.windea.plugin.idea.paradox.script.psi.*
import java.awt.*
import java.util.*

//File

data class ParadoxPath(
	val subPaths:List<String>
){
	val path = subPaths.joinToString("/")
	val fileName = subPaths.lastOrNull().orEmpty()
	val fileExtension = fileName.substringAfterLast('.')
	val parentSubPaths = subPaths.dropLast(1)
	val parent = parentSubPaths.joinToString("/")
	val root = parentSubPaths.firstOrNull().orEmpty()
	val length = subPaths.size
	
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

data class ParadoxDefinitionInfo(
	val name: String,
	val type: ParadoxType,
	val subtypes: List<ParadoxType>,
	val localisation: Map<String, String>,
	val properties: Map<String, Any?>,
	val scopes: Map<String, Map<String,String>>,
	val fromVersion: String
){
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
	
	fun resolveProperties(){
		
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