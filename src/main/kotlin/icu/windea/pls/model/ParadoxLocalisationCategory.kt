package icu.windea.pls.model

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*

enum class ParadoxLocalisationCategory(
	val id: String,
	val text: String
) {
	Localisation("localisation", PlsBundle.message("prefix.localisation")),
	SyncedLocalisation("localisation_synced", PlsBundle.message("prefix.localisationSynced"));
	
	override fun toString(): String {
		return text
	}
	
	companion object {
		val values = values()
		
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
		
		fun resolve(file: VirtualFile): ParadoxLocalisationCategory? {
			val root = file.fileInfo?.pathToEntry ?: return null
			return resolve(root)
		}
		
		fun resolve(file: PsiFile): ParadoxLocalisationCategory? {
			if(file !is ParadoxLocalisationFile) return null
			val root = file.fileInfo?.pathToEntry ?: return null
			return resolve(root)
		}
		
		fun resolve(property: ParadoxLocalisationProperty): ParadoxLocalisationCategory? {
			val root = property.fileInfo?.pathToEntry ?: return null
			return resolve(root)
		}
		
		fun resolve(propertyReference: ParadoxLocalisationPropertyReference): ParadoxLocalisationCategory? {
			val root = propertyReference.fileInfo?.pathToEntry ?: return null
			return resolve(root)
		}
		
		fun placeholder() = Localisation
	}
}