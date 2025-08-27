package icu.windea.pls.model

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.paths.*

enum class ParadoxLocalisationType(
    val id: String
) {
    Normal("localisation"),
    Synced("localisation_synced"),
    ;

    override fun toString(): String {
        return id
    }

    companion object {
        @JvmStatic
        fun resolve(id: Byte): ParadoxLocalisationType {
            return entries[id.toInt()]
        }

        @JvmStatic
        fun resolve(path: ParadoxPath): ParadoxLocalisationType? {
            return when {
                path.matches(ParadoxPathMatcher.InNormalLocalisationPath) -> Normal
                path.matches(ParadoxPathMatcher.InSyncedLocalisationPath) -> Synced
                else -> null
            }
        }

        @JvmStatic
        fun resolve(file: VirtualFile): ParadoxLocalisationType? {
            val root = file.fileInfo?.path ?: return null
            return resolve(root)
        }

        @JvmStatic
        fun resolve(file: PsiFile): ParadoxLocalisationType? {
            if (file !is ParadoxLocalisationFile) return null
            val root = file.fileInfo?.path ?: return null
            return resolve(root)
        }

        @JvmStatic
        fun resolve(element: ParadoxLocalisationProperty): ParadoxLocalisationType? {
            val root = element.fileInfo?.path ?: return null
            return resolve(root)
        }

    }
}
