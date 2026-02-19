package icu.windea.pls.lang.psi

import com.intellij.psi.PsiFile
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.util.PlsFileManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.model.constraints.ParadoxPathConstraint
import icu.windea.pls.script.psi.ParadoxScriptFile
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object ParadoxPsiFileMatcher {
    /**
     * 是否是直接位于游戏或模组目录（或者对应的入口目录）中的文件。
     */
    fun isTopFile(file: PsiFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        return fileInfo.path.length <= 1
    }

    /**
     * 是否是可接受的脚本文件。
     *
     * @param constraint 需要符合的路径约束（如果可以获取文件信息）。
     * @param injectable 是否兼容注入的文件（如果无法获取文件信息）。
     */
    @OptIn(ExperimentalContracts::class)
    fun isScriptFile(file: PsiFile, constraint: ParadoxPathConstraint = ParadoxPathConstraint.ScriptFile, injectable: Boolean = false): Boolean {
        contract {
            returns(true) implies (file is ParadoxScriptFile)
        }
        if (file !is ParadoxScriptFile) return false
        val fileInfo = file.fileInfo
        if (fileInfo != null) {
            if (!constraint.test(fileInfo.path)) return false
            return true
        } else {
            if (injectable && PlsFileManager.isInjectedFile(file.virtualFile)) return true
            return false
        }
    }

    /**
     * 是否是可接受的本地化文件。
     *
     * @param constraint 需要符合的路径约束（如果可以获取文件信息）。
     * @param injectable 是否兼容注入的文件（如果无法获取文件信息）。
     */
    @OptIn(ExperimentalContracts::class)
    fun isLocalisationFile(file: PsiFile, constraint: ParadoxPathConstraint = ParadoxPathConstraint.LocalisationFile, injectable: Boolean = false): Boolean {
        contract {
            returns(true) implies (file is ParadoxLocalisationFile)
        }
        if (file !is ParadoxLocalisationFile) return false
        val fileInfo = file.fileInfo
        if (fileInfo != null) {
            if (!constraint.test(fileInfo.path)) return false
            return true
        } else {
            if (injectable && PlsFileManager.isInjectedFile(file.virtualFile)) return true
            return false
        }
    }

    /**
     * 是否是可接受的 CSV 文件。
     *
     * @param constraint 需要符合的路径约束（如果可以获取文件信息）。
     * @param injectable 是否兼容注入的文件（如果无法获取文件信息）。
     */
    @OptIn(ExperimentalContracts::class)
    fun isCsvFile(file: PsiFile, constraint: ParadoxPathConstraint = ParadoxPathConstraint.CsvFile, injectable: Boolean = false): Boolean {
        contract {
            returns(true) implies (file is ParadoxCsvFile)
        }
        if (file !is ParadoxCsvFile) return false
        val fileInfo = file.fileInfo
        if (fileInfo != null) {
            if (!constraint.test(fileInfo.path)) return false
            return true
        } else {
            if (injectable && PlsFileManager.isInjectedFile(file.virtualFile)) return true
            return false
        }
    }

    private fun checkFilePath(file: PsiFile, constraint: ParadoxPathConstraint): Boolean {
        val fileInfo = file.fileInfo
        return fileInfo != null && constraint.test(fileInfo.path)
    }
}
