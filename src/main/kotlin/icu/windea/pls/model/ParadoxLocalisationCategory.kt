package icu.windea.pls.model

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
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
		@JvmStatic
		fun resolve(id: Byte): ParadoxLocalisationCategory {
			return entries[id.toInt()]
		}
		
		@JvmStatic
		fun resolve(flag: Boolean): ParadoxLocalisationCategory {
			return if(flag) Localisation else SyncedLocalisation
		}
		
		@JvmStatic
		fun resolve(path: ParadoxPath): ParadoxLocalisationCategory? {
			return when {
                ParadoxFilePathManager.inLocalisationPath(path, synced = false) -> Localisation
                ParadoxFilePathManager.inLocalisationPath(path, synced = true) -> SyncedLocalisation
                else -> null
            }
		}
		
		@JvmStatic
		fun resolve(file: VirtualFile): ParadoxLocalisationCategory? {
			val root = file.fileInfo?.path ?: return null
			return resolve(root)
		}
		
		@JvmStatic
		fun resolve(file: PsiFile): ParadoxLocalisationCategory? {
			if(file !is ParadoxLocalisationFile) return null
			val root = file.fileInfo?.path ?: return null
			return resolve(root)
		}
		
		@JvmStatic
		fun resolve(property: ParadoxLocalisationProperty): ParadoxLocalisationCategory? {
			val root = property.fileInfo?.path ?: return null
			return resolve(root)
		}
		
		@JvmStatic
		fun resolve(propertyReference: ParadoxLocalisationPropertyReference): ParadoxLocalisationCategory? {
			val root = propertyReference.fileInfo?.path ?: return null
			return resolve(root)
		}
		
		@JvmStatic
		fun placeholder() = Localisation
	}
}
