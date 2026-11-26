package icu.windea.pls.lang.vfs

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.lang.analysis.ParadoxGameManager
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constraints.ParadoxGameTypeConstraint
import icu.windea.pls.model.constraints.matchesBy
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
     *
     * 根据文件信息 [fileInfo] 的文件分组 [ParadoxFileInfo.group] 判断，而非 PSI 文件类型，
     * 以便在尚无 PSI 的场景（如创建新文件时的 BOM 决策）中复用同一套逻辑。
     * 参数 [file] 仅用于保持调用方的 API 对称性，实际决策只依赖 [fileInfo]。
     */
    @Suppress("UNUSED_PARAMETER")
    fun useBom(file: VirtualFile, fileInfo: ParadoxFileInfo): Boolean? {
        val gameType = fileInfo.gameType
        val path = fileInfo.path
        return when (fileInfo.group) {
            // may be BOM or NO BOM
            ParadoxFileGroup.Script -> {
                when {
                    // restricted
                    gameType == ParadoxGameType.Stellaris -> {
                        if (path.matchesParent("common/name_lists")) {
                            val gameVersion = fileInfo.gameVersion
                            gameVersion != null && ParadoxGameManager.compareGameVersion(gameVersion, "4.6") < 0
                        } else {
                            false
                        }
                    }
                    // lenient
                    gameType matchesBy ParadoxGameTypeConstraint.JominiBased -> null
                    // lenient
                    else -> null
                }
            }
            // always -> BOM
            ParadoxFileGroup.Localisation -> true
            // other -> NO BOM
            ParadoxFileGroup.Csv -> false
            else -> null
        }
    }
}
