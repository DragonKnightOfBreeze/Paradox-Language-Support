package icu.windea.pls.lang.priority.impl

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.lang.priority.*

abstract class ParadoxFilePathBasedPriorityProvider: ParadoxPriorityProvider() {
    open val FIOS_PATHS: List<String> = emptyList()
    
    open val ORDERED_PATHS: List<String> = emptyList()
    
    override fun getPriorityForFile(targetFile: PsiFile): ParadoxPriority? {
        val filePath = targetFile.fileInfo?.path?.path ?: return null
        return when {
            FIOS_PATHS.any { it.matchesPath(filePath) } -> ParadoxPriority.FIOS
            ORDERED_PATHS.any { it.matchesPath(filePath) } -> ParadoxPriority.ORDERED
            else -> null
        }
    }
    
    override fun getPriority(target: Any): ParadoxPriority? {
        val filePath = getFilePath(target) ?: return null
        return when {
            filePath in FIOS_PATHS -> ParadoxPriority.FIOS
            filePath in ORDERED_PATHS -> ParadoxPriority.ORDERED
            else -> null
        }
    }
    
    override fun getPriority(searchParameters: ParadoxSearchParameters<*>): ParadoxPriority? {
        val filePath = getFilePath(searchParameters) ?: return null
        return when {
            filePath in FIOS_PATHS -> ParadoxPriority.FIOS
            filePath in ORDERED_PATHS -> ParadoxPriority.ORDERED
            else -> null
        }
    }
}
