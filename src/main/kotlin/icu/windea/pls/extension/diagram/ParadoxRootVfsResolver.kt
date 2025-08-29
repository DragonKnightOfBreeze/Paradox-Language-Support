package icu.windea.pls.extension.diagram

import com.intellij.diagram.DiagramVfsResolver
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import icu.windea.pls.core.toPsiDirectory
import icu.windea.pls.core.toVirtualFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.model.ParadoxRootInfo

class ParadoxRootVfsResolver : DiagramVfsResolver<PsiElement> {
    //based on rootFile

    override fun getQualifiedName(element: PsiElement?): String? {
        if (element == null) return null
        val rootInfo = element.fileInfo?.rootInfo ?: return null
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
        val rootPath = rootInfo.rootFile.path
        return rootPath
    }

    override fun resolveElementByFQN(s: String, project: Project): PsiDirectory? {
        val rootPath = s
        return try {
            rootPath.toVirtualFile()?.toPsiDirectory(project)
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            null
        }
    }
}
