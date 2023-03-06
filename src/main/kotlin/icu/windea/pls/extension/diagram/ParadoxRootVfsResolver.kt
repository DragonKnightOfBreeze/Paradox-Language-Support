package icu.windea.pls.extension.diagram

import com.intellij.diagram.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*

class ParadoxRootVfsResolver(private val name: String) : DiagramVfsResolver<PsiElement> {
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
            null
        }
    }
}