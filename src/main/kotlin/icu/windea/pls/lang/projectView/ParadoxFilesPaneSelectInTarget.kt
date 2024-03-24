package icu.windea.pls.lang.projectView

import com.intellij.ide.impl.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*

class ParadoxFilesPaneSelectInTarget(project: Project) : ProjectViewSelectInTarget(project) {
    override fun canSelect(file: PsiFileSystemItem?): Boolean {
        val vFile = PsiUtilCore.getVirtualFile(file)
        if(vFile == null || !vFile.isValid) return false
        val fileInfo = vFile.fileInfo
        if(fileInfo == null) return false
        if(fileInfo.pathToEntry.length == 1 && vFile.isFile) return false //排除直接位于根目录下的文件
        return true
    }
    
    override fun getMinorViewId() = ParadoxFilesViewPane.ID
    
    override fun getWeight() = 100.0f //very low
    
    override fun toString() = PlsBundle.message("select.in.paradox.files")
}