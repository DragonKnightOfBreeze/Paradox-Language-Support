package icu.windea.pls.model

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*

enum class ParadoxLocalisationCategory(
    val text: String
) {
    Localisation("localisation"),
    SyncedLocalisation("localisation_synced"),
    ;

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
            return if (flag) Localisation else SyncedLocalisation
        }

        @JvmStatic
        fun resolve(path: ParadoxPath): ParadoxLocalisationCategory? {
            return when {
                ParadoxFileManager.inLocalisationPath(path, synced = false) -> Localisation
                ParadoxFileManager.inLocalisationPath(path, synced = true) -> SyncedLocalisation
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
        fun resolve(element: ParadoxLocalisationParameter): ParadoxLocalisationCategory? {
            val root = element.fileInfo?.path ?: return null
            return resolve(root)
        }

        @JvmStatic
        fun placeholder() = Localisation
    }
}
