package icu.windea.pls.model

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.model.constraints.ParadoxPathConstraint
import icu.windea.pls.model.constraints.matchesBy
import icu.windea.pls.model.paths.ParadoxPath
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

enum class ParadoxScriptedVariableType(val id: String) {
    Local("local"),
    Global("global"),
    ;

    override fun toString() = id

    companion object {
        @JvmStatic
        fun resolve(path: ParadoxPath?): ParadoxScriptedVariableType? {
            if (path == null) return null
            return when {
                path matchesBy ParadoxPathConstraint.ForScriptedVariable -> Global
                else -> Local
            }
        }

        @JvmStatic
        fun resolve(file: VirtualFile?): ParadoxScriptedVariableType? {
            if (file?.fileType !== ParadoxScriptFileType) return null
            return resolve(file.fileInfo?.path)
        }

        @JvmStatic
        fun resolve(file: PsiFile?): ParadoxScriptedVariableType? {
            if (file?.fileType !== ParadoxScriptFileType) return null
            return resolve(file.fileInfo?.path)
        }

        @JvmStatic
        fun resolve(element: ParadoxScriptScriptedVariable): ParadoxScriptedVariableType? {
            return resolve(element.fileInfo?.path)
        }
    }
}
