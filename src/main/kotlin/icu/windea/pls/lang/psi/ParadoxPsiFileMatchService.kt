package icu.windea.pls.lang.psi

import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.model.constraints.ParadoxPathConstraint
import icu.windea.pls.script.psi.ParadoxScriptFile
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object ParadoxPsiFileMatchService {
    /**
     * 检查规则分组是否已加载完毕。
     */
    fun checkConfigGroupInitialized(file: PsiFile): Boolean {
        return ChronicleFacade.checkConfigGroupInitialized(file.project, file)
    }

    /**
     * 是否是直接位于游戏或模组的根目录下的文件。
     */
    fun isTopFromRootFile(file: PsiFile): Boolean {
        val fileInfo = file.fileInfo ?: return false
        return fileInfo.isTopFromRoot()
    }

    // NOTE 2.2.0 检测逻辑：
    // - 检测 PSI 文件类型是否匹配
    // - 如果可以获取文件信息，则要求匹配指定的路径约束（默认约束：要求文件路径在语义上是有效的）
    // - 如果无法获取文件信息，则要求必须是一个注入的文件（如：内联脚本的参数值对应的注入的文件）

    /**
     * 是否是有效的脚本文件。
     *
     * @param file 需要检测的 PSI 文件。
     * @param constraint 需要匹配的路径约束。默认要求文件路径在语义上是有效的。如果无法获取文件信息，则必须是一个注入的文件。
     */
    @OptIn(ExperimentalContracts::class)
    fun isScriptFile(file: PsiFile, constraint: ParadoxPathConstraint = ParadoxPathConstraint.ScriptFile): Boolean {
        contract {
            returns(true) implies (file is ParadoxScriptFile)
        }
        if (file !is ParadoxScriptFile) return false
        val fileInfo = file.fileInfo
        if (fileInfo == null) return VirtualFileService.isInjectedFile(file.virtualFile)
        return constraint.test(fileInfo.path)
    }

    /**
     * 是否是有效的本地化文件。
     *
     * @param file 需要检测的 PSI 文件。
     * @param constraint 需要匹配的路径约束。默认要求文件路径在语义上是有效的。如果无法获取文件信息，则必须是一个注入的文件。
     */
    @OptIn(ExperimentalContracts::class)
    fun isLocalisationFile(file: PsiFile, constraint: ParadoxPathConstraint = ParadoxPathConstraint.LocalisationFile): Boolean {
        contract {
            returns(true) implies (file is ParadoxLocalisationFile)
        }
        if (file !is ParadoxLocalisationFile) return false
        val fileInfo = file.fileInfo
        if (fileInfo == null) return VirtualFileService.isInjectedFile(file.virtualFile)
        return constraint.test(fileInfo.path)
    }

    /**
     * 是否是有效的CSV文件。
     *
     * @param file 需要检测的 PSI 文件。
     * @param constraint 需要匹配的路径约束。默认要求文件路径在语义上是有效的。如果无法获取文件信息，则必须是一个注入的文件。
     */
    @OptIn(ExperimentalContracts::class)
    fun isCsvFile(file: PsiFile, constraint: ParadoxPathConstraint = ParadoxPathConstraint.CsvFile): Boolean {
        contract {
            returns(true) implies (file is ParadoxCsvFile)
        }
        if (file !is ParadoxCsvFile) return false
        val fileInfo = file.fileInfo
        if (fileInfo == null) return VirtualFileService.isInjectedFile(file.virtualFile)
        return constraint.test(fileInfo.path)
    }

    /**
     * 检查游戏类型是否支持内联脚本。
     */
    fun isInlineScriptSupported(file: PsiFile): Boolean {
        return ParadoxInlineScriptManager.isSupported(file)
    }

    /**
     * 检查游戏类型是否支持内联脚本。
     */
    fun isDefinitionInjectionSupported(file: PsiFile): Boolean {
        return ParadoxDefinitionInjectionManager.isSupported(file)
    }
}
