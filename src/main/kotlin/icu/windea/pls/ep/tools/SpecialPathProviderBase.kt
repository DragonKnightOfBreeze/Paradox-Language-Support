package icu.windea.pls.ep.tools

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo

abstract class SpecialPathProviderBase : SpecialPathProvider {
    protected fun selectGameType(file: VirtualFile?, gameType: ParadoxGameType?): ParadoxGameType {
        file?.let { it.fileInfo?.rootInfo?.gameType }?.takeIf { it != ParadoxGameType.Core }?.let { return it }
        gameType?.takeIf { it != ParadoxGameType.Core }?.let { return it }
        return PlsSettings.getInstance().state.defaultGameType
    }

    protected fun selectRootInfo(file: VirtualFile?): ParadoxRootInfo? {
        return file?.fileInfo?.rootInfo
    }
}
