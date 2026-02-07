@file:Suppress("unused")

package icu.windea.pls.lang.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.orNull
import icu.windea.pls.ep.resolve.expression.ParadoxPathReferenceExpressionSupport
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.search.ParadoxDefineSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectFile
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue

object ParadoxDefineManager {
    val definePathExpression = CwtDataExpression.resolve("filepath[common/defines/,.txt]", false)

    fun isDefineFile(file: VirtualFile): Boolean {
        val fileType = file.fileType
        if (fileType != ParadoxScriptFileType) return false
        val fileInfo = file.fileInfo ?: return false
        val filePath = fileInfo.path.path
        val configExpression = definePathExpression
        return ParadoxPathReferenceExpressionSupport.get(configExpression)?.extract(configExpression, null, filePath)?.orNull() != null
    }

    fun isDefineFile(file: PsiFile): Boolean {
        if (file !is ParadoxScriptFile) return false
        val vFile = selectFile(file) ?: return false
        return isDefineFile(vFile)
    }

    fun isDefineElement(element: ParadoxScriptProperty, namespace: String, variable: String?): Boolean {
        if(namespace.isEmpty()) return false
        if(variable == null) return element.propertyValue is ParadoxScriptBlock
        if(variable.isEmpty()) return false
        return true
    }

    fun findDefineElement(expression: String, contextElement: PsiElement, project: Project): ParadoxScriptProperty? {
        val defineSelector = selector(project, contextElement).define().contextSensitive()
        val defineInfo = ParadoxDefineSearch.search(expression, defineSelector).find() ?: return null
        return defineInfo.element
    }

    fun findDefineValueElement(expression: String, contextElement: PsiElement, project: Project): ParadoxScriptValue? {
        val defineElement = findDefineElement(expression, contextElement, project) ?: return null
        return defineElement.propertyValue
    }
}
