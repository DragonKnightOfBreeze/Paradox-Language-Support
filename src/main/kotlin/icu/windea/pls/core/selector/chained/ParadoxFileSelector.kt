package icu.windea.pls.core.selector.chained

import com.intellij.openapi.vfs.*
import icu.windea.pls.*

class ParadoxFileSelector : ChainedParadoxSelector<VirtualFile>()

fun fileSelector() = ParadoxFileSelector()

fun ParadoxFileSelector.withFileExtensions(fileExtensions: Set<String>) =
    if(fileExtensions.isNotEmpty()) filterBy { it.extension?.let { e -> ".$e" }.orEmpty() in fileExtensions }
    else this

fun ParadoxFileSelector.distinctByFilePath() =
    distinctBy { it.fileInfo?.path }
