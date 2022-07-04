package icu.windea.pls.model

import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

enum class ParadoxLocalisationCategory(
	override val id: String,
	override val text: String,
	val flag: Boolean
) : IdAware, TextAware {
	Localisation("localisation", PlsDocBundle.message("name.localisation.localisation"), true),
	SyncedLocalisation("localisation_synced", PlsDocBundle.message("name.localisation.localisationSynced"), false);
	
	override fun toString(): String {
		return text
	}
	
	companion object {
		fun resolve(flag: Boolean): ParadoxLocalisationCategory {
			return if(flag) Localisation else SyncedLocalisation
		}
		
		fun resolve(path: ParadoxPath): ParadoxLocalisationCategory? {
			return when {
				path.canBeLocalisationPath() -> Localisation
				path.canBeSyncedLocalisationPath() -> SyncedLocalisation
				else -> null
			}
		}
		
		fun resolve(file: ParadoxLocalisationFile): ParadoxLocalisationCategory? {
			val root = file.fileInfo?.path ?: return null
			return resolve(root)
		}
		
		fun resolve(property: ParadoxLocalisationProperty): ParadoxLocalisationCategory? {
			val root = property.fileInfo?.path ?: return null
			return resolve(root)
		}
		
		fun resolve(propertyReference: ParadoxLocalisationPropertyReference): ParadoxLocalisationCategory? {
			val root = propertyReference.fileInfo?.path ?: return null
			return resolve(root)
		}
	}
}

fun ParadoxLocalisationCategory?.orDefault() = this ?: ParadoxLocalisationCategory.Localisation