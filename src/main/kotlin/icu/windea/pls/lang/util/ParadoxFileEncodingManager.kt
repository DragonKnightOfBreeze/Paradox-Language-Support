package icu.windea.pls.lang.util

import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxGameType
import java.nio.charset.Charset

object ParadoxFileEncodingManager {
    fun useCharset(): Charset {
        return Charsets.UTF_8
    }

    fun useBom(fileInfo: ParadoxFileInfo): Boolean {
        val fileType = fileInfo.group
        val gameType = fileInfo.rootInfo.gameType
        val path = fileInfo.path
        return when (fileType) {
            // name list file -> BOM, others -> NO BOM
            ParadoxFileGroup.Script -> {
                if (gameType == ParadoxGameType.Stellaris && path.matchesParent("common/name_lists")) return true
                false
            }
            // always -> BOM
            ParadoxFileGroup.Localisation -> {
                true
            }
            // other -> NO BOM
            else -> false
        }
    }
}
