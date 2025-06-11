package icu.windea.pls.ai.requests

import icu.windea.pls.core.*
import icu.windea.pls.model.*

interface PlsAiRequest {
    val fileInfo: ParadoxFileInfo?

    val filePath get() = fileInfo?.path?.path
    val fileName get() = fileInfo?.path?.fileName
    val modName get() = fileInfo?.rootInfo?.castOrNull<ParadoxRootInfo.Mod>()?.name
    val gameType get() = fileInfo?.rootInfo?.gameType.orDefault()
}
