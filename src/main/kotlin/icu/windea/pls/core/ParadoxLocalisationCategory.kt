package icu.windea.pls.core

import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

enum class ParadoxLocalisationCategory(
	override val id: String,
	override val description: String,
	override val text: String,
	val flag: Boolean
) : IdAware, DescriptionAware, TextAware {
	Localisation("localisation", "Localisation", "localisation", true),
	SyncedLocalisation("localisation_synced", "Synced Localisation", "localisation_synced", false);
	
	override fun toString(): String {
		return text
	}
	
	companion object {
		fun resolve(id: String): ParadoxLocalisationCategory? {
			return when {
				id == "localisation" -> Localisation
				id == "localisation_synced" -> SyncedLocalisation
				else -> null
			}
		}
		
		fun resolve(flag: Boolean): ParadoxLocalisationCategory {
			return if(flag) Localisation else SyncedLocalisation
		}
		
		fun resolve(file: ParadoxLocalisationFile): ParadoxLocalisationCategory? {
			val root = file.fileInfo?.path?.root ?: return null
			return resolve(root)
		}
		
		fun resolve(property: ParadoxLocalisationProperty): ParadoxLocalisationCategory? {
			val root = property.fileInfo?.path?.root ?: return null
			return resolve(root)
		}
		
		fun resolve(propertyReference: ParadoxLocalisationPropertyReference): ParadoxLocalisationCategory? {
			val root = propertyReference.fileInfo?.path?.root ?: return null
			return resolve(root)
		}
	}
}