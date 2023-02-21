package icu.windea.pls.core.selector.chained

import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*

class ParadoxFileSelector(project: Project, context: Any? = null) : ChainedParadoxSelector<VirtualFile>(project, context)

fun fileSelector(project: Project, context: Any? = null) = ParadoxFileSelector(project, context)

fun ParadoxFileSelector.withFileExtensions(fileExtensions: Set<String>) =
    if(fileExtensions.isNotEmpty()) filterBy { it.extension?.let { e -> ".$e" }.orEmpty() in fileExtensions }
    else this

fun ParadoxFileSelector.distinctByFilePath() =
    distinctBy { it.fileInfo?.path }
