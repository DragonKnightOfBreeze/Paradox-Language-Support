package icu.windea.pls.lang.util

import com.intellij.psi.PsiFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptFile
import java.nio.charset.Charset

object ParadoxFileEncodingManager {
    fun useCharset(): Charset {
        return Charsets.UTF_8
    }

    fun useBom(file: PsiFile, fileInfo: ParadoxFileInfo): Boolean {
        val gameType = fileInfo.rootInfo.gameType
        val path = fileInfo.path
        return when {
            // name list file -> BOM, others -> NO BOM
            file is ParadoxScriptFile -> {
                if (gameType == ParadoxGameType.Stellaris && path.matchesParent("common/name_lists")) return true
                false
            }
            // always -> BOM
            file is ParadoxLocalisationFile -> {
                true
            }
            // other -> NO BOM
            else -> false
        }
    }
}
