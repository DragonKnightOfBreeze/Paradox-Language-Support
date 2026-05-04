package icu.windea.pls.lang.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.resolve.ParadoxDefineService
import icu.windea.pls.lang.search.ParadoxDefineNamespaceSearch
import icu.windea.pls.lang.search.ParadoxDefineVariableSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectFile
import icu.windea.pls.model.ParadoxDefineInfo
import icu.windea.pls.model.ParadoxDefineNamespaceInfo
import icu.windea.pls.model.ParadoxDefineVariableInfo
import icu.windea.pls.model.constraints.ParadoxPathConstraint
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty

object ParadoxDefineManager {
    object Keys : KeyRegistry() {
        val cachedDefineInfo by registerKey<CachedValue<ParadoxDefineInfo>>(Keys)
    }

    fun isDefineFile(file: VirtualFile): Boolean {
        val fileType = file.fileType
        if (fileType != ParadoxScriptFileType) return false
        val fileInfo = file.fileInfo ?: return false
        return ParadoxPathConstraint.ForDefine.test(fileInfo.path)
    }

    fun isDefineFile(file: PsiFile): Boolean {
        if (file !is ParadoxScriptFile) return false
        val vFile = selectFile(file) ?: return false
        return isDefineFile(vFile)
    }

    fun getExpression(namespace: String, variable: String?): String {
        return if (variable == null) namespace else "$namespace.$variable"
    }

    fun splitExpression(expression: String): Tuple2<String, String?> {
        val index = expression.indexOf('.')
        if (index == -1) return expression to null
        return expression.substring(0, index) to expression.substring(index + 1)
    }

    fun getExpression(element: ParadoxScriptProperty): String? {
        return getInfo(element)?.expression
    }

    fun getInfo(element: ParadoxScriptProperty): ParadoxDefineInfo? {
        // from cache (invalidated on file modification)
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefineInfo) {
            ProgressManager.checkCanceled()
            runSmartReadAction {
                val file = element.containingFile
                val value = ParadoxDefineService.resolveInfo(element, file)
                value.withDependencyItems(file)
            }
        }
    }

    fun getNamespaceInfo(element: ParadoxScriptProperty): ParadoxDefineNamespaceInfo? {
        return getInfo(element) as? ParadoxDefineNamespaceInfo
    }

    fun getVariableInfo(element: ParadoxScriptProperty): ParadoxDefineVariableInfo? {
        return getInfo(element) as? ParadoxDefineVariableInfo
    }

    @Suppress("unused")
    fun findDefineNamespaceElement(namespace: String, contextElement: PsiElement, project: Project): ParadoxScriptProperty? {
        if (namespace.isEmpty()) return null
        val defineSelector = selector(project, contextElement).define().contextSensitive()
        return ParadoxDefineNamespaceSearch.search(namespace, defineSelector).find()
    }

    fun findDefineVariableElement(namespace: String, variable: String, contextElement: PsiElement, project: Project): ParadoxScriptProperty? {
        if (namespace.isEmpty() || variable.isEmpty()) return null
        val defineSelector = selector(project, contextElement).define().contextSensitive()
        return ParadoxDefineVariableSearch.search(namespace, variable, defineSelector).find()
    }

    fun findDefineVariableElement(expression: String, contextElement: PsiElement, project: Project): ParadoxScriptProperty? {
        val (namespace, variable) = splitExpression(expression)
        if (variable == null) return null
        return findDefineVariableElement(namespace, variable, contextElement, project)
    }
}
