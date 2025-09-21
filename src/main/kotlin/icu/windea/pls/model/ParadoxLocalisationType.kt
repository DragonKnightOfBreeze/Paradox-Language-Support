package icu.windea.pls.model

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.paths.ParadoxPath
import icu.windea.pls.model.paths.ParadoxPathMatcher
import icu.windea.pls.model.paths.matches

enum class ParadoxLocalisationType(val id: String) {
    Normal("localisation"),
    Synced("localisation_synced"),
    ;

    override fun toString() = id

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

