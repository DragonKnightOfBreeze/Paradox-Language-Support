package icu.windea.pls.model

import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

enum class ParadoxLocalisationCategory(
	override val key:String,
	override val text:String,
	val flag:Boolean
):Enumerable {
	Localisation("localisation","Localisation",true),
	SyncedLocalisation("localisation_synced","Synced Localisation",false);
	
	override fun toString(): String {
		return text
	}
	
	companion object{
		fun resolve(key:String): ParadoxLocalisationCategory?{
			return when{
				key == "localisation" -> Localisation
				key == "localisation_synced" -> SyncedLocalisation
				else -> null
			}
		}
		
		fun resolve(flag:Boolean):ParadoxLocalisationCategory{
			return if(flag) Localisation else SyncedLocalisation
		}
		
		fun resolve(file:ParadoxLocalisationFile):ParadoxLocalisationCategory?{
			val root = file.paradoxFileInfo?.path?.root?:return null
			return resolve(root)
		}
		
		fun resolve(property: ParadoxLocalisationProperty): ParadoxLocalisationCategory?{
			val root = property.paradoxFileInfo?.path?.root ?: return null
			return resolve(root)
		}
		
		fun resolve(propertyReference: ParadoxLocalisationPropertyReference): ParadoxLocalisationCategory?{
			val root = propertyReference.paradoxFileInfo?.path?.root ?: return null
			return resolve(root)
		}
	}
}