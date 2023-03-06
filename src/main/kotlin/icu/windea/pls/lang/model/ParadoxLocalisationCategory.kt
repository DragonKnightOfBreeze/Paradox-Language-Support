package icu.windea.pls.lang.model

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

enum class ParadoxLocalisationCategory(
	val id: String,
	val text: String,
	val flag: Boolean
) {
	Localisation("localisation", PlsDocBundle.message("prefix.localisation"), true),
	SyncedLocalisation("localisation_synced", PlsDocBundle.message("prefix.localisationSynced"), false);
	
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
		
		fun resolve(file: VirtualFile): ParadoxLocalisationCategory? {
			val root = file.fileInfo?.entryPath ?: return null
			return resolve(root)
		}
		
		fun resolve(file: PsiFile): ParadoxLocalisationCategory? {
			if(file !is ParadoxLocalisationFile) return null
			val root = file.fileInfo?.entryPath ?: return null
			return resolve(root)
		}
		
		fun resolve(property: ParadoxLocalisationProperty): ParadoxLocalisationCategory? {
			val root = property.fileInfo?.entryPath ?: return null
			return resolve(root)
		}
		
		fun resolve(propertyReference: ParadoxLocalisationPropertyReference): ParadoxLocalisationCategory? {
			val root = propertyReference.fileInfo?.entryPath ?: return null
			return resolve(root)
		}
	}
}

fun ParadoxLocalisationCategory?.orDefault() = this ?: ParadoxLocalisationCategory.Localisation