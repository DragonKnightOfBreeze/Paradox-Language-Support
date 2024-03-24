package icu.windea.pls.extension.diagram

import com.intellij.diagram.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*

class ParadoxRootVfsResolver() : DiagramVfsResolver<PsiElement> {
    //based on rootFile
    
    override fun getQualifiedName(element: PsiElement?): String? {
        if(element == null) return null
        val rootInfo = element.fileInfo?.rootInfo ?: return null
        val rootPath = rootInfo.rootFile.path
        return rootPath
    }
    
    override fun resolveElementByFQN(s: String, project: Project): PsiDirectory? {
        val rootPath = s
        return try {
            rootPath.toVirtualFile()?.toPsiDirectory(project)
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            null
        }
    }
}