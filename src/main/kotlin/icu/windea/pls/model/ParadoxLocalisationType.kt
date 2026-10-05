package icu.windea.pls.model

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.constraints.ParadoxPathConstraint
import icu.windea.pls.model.constraints.matchesBy
import icu.windea.pls.model.paths.ParadoxPath

enum class ParadoxLocalisationType(val id: String) {
    Normal("localisation"),
    Synced("synced localisation"),
    ;

    override fun toString() = id

    // region Inline Methods

    @Suppress("NOTHING_TO_INLINE", "unused")
    inline fun optimized(): Byte = ordinal.toByte() // 3.0.1 radical optimization

    // endregion

    companion object {
        @JvmStatic
        fun resolve(path: ParadoxPath?): ParadoxLocalisationType? {
            if (path == null) return null
            return when {
                path matchesBy ParadoxPathConstraint.InNormalLocalisationPath -> Normal
                path matchesBy ParadoxPathConstraint.InSyncedLocalisationPath -> Synced
                else -> null
            }
        }

        @JvmStatic
        fun resolve(file: VirtualFile?): ParadoxLocalisationType? {
            if (file?.fileType !== ParadoxLocalisationFileType) return null
            return resolve(file.fileInfo?.path)
        }

        @JvmStatic
        fun resolve(file: PsiFile?): ParadoxLocalisationType? {
            if (file?.fileType !== ParadoxLocalisationFileType) return null
            return resolve(file.fileInfo?.path)
        }

        @JvmStatic
        fun resolve(element: ParadoxLocalisationProperty): ParadoxLocalisationType? {
            return resolve(element.fileInfo?.path)
        }

        // region Inline Methods

        @Suppress("NOTHING_TO_INLINE", "unused")
        inline fun deoptimized(value: Byte): ParadoxLocalisationType = entries[value.toInt()] // 3.0.1 radical optimization

        // endregion
    }
}
