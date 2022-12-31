package icu.windea.pls.core.selector.chained

import com.intellij.openapi.vfs.*
import icu.windea.pls.*

class ParadoxFileSelector: ChainedParadoxSelector<VirtualFile>()

fun fileSelector() = ParadoxFileSelector()

fun ParadoxFileSelector.distinctByFilePath() =
	distinctBy { it.fileInfo?.path }