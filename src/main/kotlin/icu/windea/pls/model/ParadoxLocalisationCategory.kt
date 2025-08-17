package icu.windea.pls.model

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*

enum class ParadoxLocalisationCategory(
    val id: String
) {
    Localisation("localisation"),
    SyncedLocalisation("localisation_synced"),
    ;

    override fun toString(): String {
        return id
    }

    companion object {
        @JvmStatic
        fun resolve(id: Byte): ParadoxLocalisationCategory {
            return entries[id.toInt()]
        }

        @JvmStatic
        fun resolve(path: ParadoxPath): ParadoxLocalisationCategory? {
            return when {
                path.matches(ParadoxPathMatcher.InNormalLocalisationPath) -> Localisation
                path.matches(ParadoxPathMatcher.InSyncedLocalisationPath) -> SyncedLocalisation
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
            if (file !is ParadoxLocalisationFile) return null
            val root = file.fileInfo?.path ?: return null
            return resolve(root)
        }

        @JvmStatic
        fun resolve(element: ParadoxLocalisationProperty): ParadoxLocalisationCategory? {
            val root = element.fileInfo?.path ?: return null
            return resolve(root)
        }

        @JvmStatic
        fun placeholder() = Localisation
    }
}
