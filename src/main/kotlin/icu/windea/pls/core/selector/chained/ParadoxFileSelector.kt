package icu.windea.pls.core.selector.chained

import com.intellij.openapi.vfs.*

class ParadoxFileSelector: ChainedParadoxSelector<VirtualFile>()

fun fileSelector() = ParadoxFileSelector()