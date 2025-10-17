package icu.windea.pls.lang.util.psi

import com.intellij.psi.PsiFile
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.vfs.PlsVfsManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.model.paths.ParadoxPathMatcher
import icu.windea.pls.model.paths.matches
import icu.windea.pls.script.psi.ParadoxScriptFile
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object ParadoxPsiFileMatcher {
    /**
     * 是否是符合特定约束的脚本文件。
     *
     * @param smart 要求文件路径可以获取且是可接受的。
     * @param injectable 兼容注入的文件。
     */
    @OptIn(ExperimentalContracts::class)
    fun isScriptFile(file: PsiFile, smart: Boolean = false, injectable: Boolean = false): Boolean {
        contract {
            returns(true) implies (file is ParadoxScriptFile)
        }
        if (file !is ParadoxScriptFile) return false
        if (PlsVfsManager.isInjectedFile(file.virtualFile)) return checkInjectedFile(file, smart, injectable)
        if (smart && !checkFilePath(file, ParadoxPathMatcher.ScriptFile)) return false
        return true
    }

    /**
     * 是否是符合特定约束的本地化文件。
     *
     * @param smart 要求文件路径可以获取且是可接受的。
     * @param injectable 兼容注入的文件。
     */
    @OptIn(ExperimentalContracts::class)
    fun isLocalisationFile(file: PsiFile, smart: Boolean = false, injectable: Boolean = false): Boolean {
        contract {
            returns(true) implies (file is ParadoxLocalisationFile)
        }
        if (file !is ParadoxLocalisationFile) return false
        if (PlsVfsManager.isInjectedFile(file.virtualFile)) return checkInjectedFile(file, smart, injectable)
        if (smart && !checkFilePath(file, ParadoxPathMatcher.LocalisationFile)) return false
        return true
    }

    /**
     * 是否是符合特定约束的 CSV 文件。
     *
     * @param smart 要求文件路径可以获取且是可接受的。
     * @param injectable 兼容注入的文件。
     */
    @OptIn(ExperimentalContracts::class)
    fun isCsvFile(file: PsiFile, smart: Boolean = false, injectable: Boolean = false): Boolean {
        contract {
            returns(true) implies (file is ParadoxCsvFile)
        }
        if (file !is ParadoxCsvFile) return false
        if (PlsVfsManager.isInjectedFile(file.virtualFile)) return checkInjectedFile(file, smart, injectable)
        if (smart && !checkFilePath(file, ParadoxPathMatcher.CsvFile)) return false
        return true
    }

    private fun checkInjectedFile(file: PsiFile, smart: Boolean, injectable: Boolean): Boolean {
        return if (injectable) !smart || selectRootFile(file) != null else false
    }

    private fun checkFilePath(file: PsiFile, pathMatcher: ParadoxPathMatcher): Boolean {
        val fileInfo = file.fileInfo
        return fileInfo != null && fileInfo.path.matches(pathMatcher)
    }
}
