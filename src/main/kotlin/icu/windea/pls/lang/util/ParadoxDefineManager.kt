@file:Suppress("unused")

package icu.windea.pls.lang.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.orNull
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.ep.expression.ParadoxPathReferenceExpressionSupport
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.search.ParadoxDefineSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.define
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectFile
import icu.windea.pls.model.indexInfo.ParadoxDefineIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.resolveValue

object ParadoxDefineManager {
    val definePathExpression = CwtDataExpression.resolve("filepath[common/defines/,.txt]", false)

    fun isDefineElement(define: ParadoxDefineIndexInfo, defineElement: ParadoxScriptProperty): Boolean {
        if(define.variable == null) return defineElement.propertyValue is ParadoxScriptBlock
        return true
    }

    fun getDefineElement(define: ParadoxDefineIndexInfo, project: Project): ParadoxScriptProperty? {
        val file = define.virtualFile?.toPsiFile(project) ?: return null
        val elementOffset = define.elementOffsets.lastOrNull() ?: return null
        return file.findElementAt(elementOffset)?.parentOfType<ParadoxScriptProperty>()?.takeIf { isDefineElement(define, it) }
    }

    fun getDefineElements(define: ParadoxDefineIndexInfo, project: Project): List<ParadoxScriptProperty> {
        val file = define.virtualFile?.toPsiFile(project) ?: return emptyList()
        val elementOffsets = define.elementOffsets
        return elementOffsets.mapNotNull { elementOffset ->
            file.findElementAt(elementOffset)?.parentOfType<ParadoxScriptProperty>()?.takeIf { isDefineElement(define, it) }
        }
    }

    fun getDefineElements(defines: Collection<ParadoxDefineIndexInfo>, project: Project): List<ParadoxScriptProperty> {
        return defines.flatMap { define -> getDefineElements(define, project) }
    }

    fun getDefineValue(expression: String, contextElement: PsiElement, project: Project): Any? {
        val defineSelector = selector(project, contextElement).define().contextSensitive()
        val define = ParadoxDefineSearch.search(expression, defineSelector).find() ?: return null
        return getDefineValue(define, project)
    }

    fun getDefineValue(defineInfo: ParadoxDefineIndexInfo, project: Project): Any? {
        val defineProperty = getDefineElement(defineInfo, project) ?: return null
        val definePropertyValue = defineProperty.propertyValue ?: return null
        return definePropertyValue.resolveValue()
    }

    fun getDefineValue(define: ParadoxScriptProperty): Any? {
        val definePropertyValue = define.propertyValue ?: return null
        return definePropertyValue.resolveValue()
    }

    fun isDefineFile(file: VirtualFile): Boolean {
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
}
