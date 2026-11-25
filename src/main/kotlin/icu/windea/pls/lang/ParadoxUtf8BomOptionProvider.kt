package icu.windea.pls.lang

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.encoding.Utf8BomOptionProvider
import com.intellij.util.ThreeState
import icu.windea.pls.core.matchesPath
import icu.windea.pls.lang.analysis.ParadoxGameManager
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.model.ParadoxGameType

/**
 * 基于文件的扩展名以及相对于入口目录的路径，判断创建新的脚本文件或本地化文件时，是否需要添加BOM。
 */
class ParadoxUtf8BomOptionProvider : Utf8BomOptionProvider {
    override fun getBOMDecisionForNewUtf8File(file: VirtualFile): ThreeState {
        val fileInfo = file.fileInfo ?: return ThreeState.UNSURE
        return when (fileInfo.group) {
            ParadoxFileGroup.Script -> {
                if (fileInfo.gameType == ParadoxGameType.Stellaris && "common/name_lists".matchesPath(fileInfo.path.parent)) {
                    val gameVersion = fileInfo.gameVersion
                    if (gameVersion != null && ParadoxGameManager.compareGameVersion(gameVersion, "4.6") < 0) ThreeState.YES else ThreeState.UNSURE
                } else {
                    ThreeState.UNSURE
                }
            }
            ParadoxFileGroup.Localisation -> ThreeState.YES
            else -> ThreeState.UNSURE
        }
    }
}
