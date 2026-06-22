package icu.windea.pls.lang.analysis

import com.intellij.psi.PsiFile
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxGameTypeConstraint
import icu.windea.pls.model.constraints.matchesBy
import icu.windea.pls.script.psi.ParadoxScriptFile
import java.nio.charset.Charset

object ParadoxFileEncodingService {
    /**
     * 得到应当使用的字符集。
     */
    fun useCharset(): Charset {
        return Charsets.UTF_8
    }

    /**
     * 检查当前文件 [file] 是否应当使用 BOM。如果返回 null，则表示不确定。
     */
    fun useBom(file: PsiFile, fileInfo: ParadoxFileInfo): Boolean? {
        val gameType = fileInfo.rootInfo.gameType
        val path = fileInfo.path
        return when {
            // may be BOM or NO BOM
            file is ParadoxScriptFile -> {
                when {
                    // restricted
                    gameType == ParadoxGameType.Stellaris -> path.matchesParent("common/name_lists")
                    // lenient
                    gameType matchesBy ParadoxGameTypeConstraint.JominiBased -> null
                    // lenient
                    else -> null
                }
            }
            // always -> BOM
            file is ParadoxLocalisationFile -> true
            // other -> NO BOM
            file is ParadoxCsvFile -> false
            else -> null
        }
    }
}
