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
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.psi.members
import icu.windea.pls.lang.psi.values
import icu.windea.pls.lang.resolve.ParadoxDefineService
import icu.windea.pls.lang.search.ParadoxDefineNamespaceSearch
import icu.windea.pls.lang.search.ParadoxDefineVariableSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.model.ParadoxDefineInfo
import icu.windea.pls.model.ParadoxDefineNamespaceInfo
import icu.windea.pls.model.ParadoxDefineVariableInfo
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.constraints.ParadoxPathConstraint
import icu.windea.pls.model.paths.ParadoxPath
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.propertyValue

object ParadoxDefineManager {
    object Keys : KeyRegistry() {
        val cachedDefineInfo by registerKey<CachedValue<ParadoxDefineInfo>>(Keys)
    }

    @Suppress("unused")
    fun isDefinesFile(file: VirtualFile): Boolean {
        if (file.fileType != ParadoxScriptFileType) return false
        val filePath = file.fileInfo?.path ?: return false
        return isDefinesFilePath(filePath)
    }

    fun isDefinesFile(file: PsiFile): Boolean {
        if (file !is ParadoxScriptFile) return false
        val filePath = file.fileInfo?.path ?: return false
        return isDefinesFilePath(filePath)
    }

    fun isDefinesFilePath(filePath: ParadoxPath): Boolean {
        return ParadoxPathConstraint.ForDefine.test(filePath)
    }

    @Suppress("unused")
    fun getGlobalScriptedVariablesDirectory(contextFile: VirtualFile): VirtualFile? {
        val fileInfo = contextFile.fileInfo ?: return null
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxRootInfo.MetadataBased) return null
        val entryPath = fileInfo.entryPath ?: return null
        val path = entryPath.resolve("common/defines")
        return VirtualFileService.findDirectory(path)
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
        val defineSelector = ParadoxDefineNamespaceSearch.selector(project, contextElement).contextSensitive()
        return ParadoxDefineNamespaceSearch.search(namespace, defineSelector).find()
    }

    fun findDefineVariableElement(namespace: String, variable: String, contextElement: PsiElement, project: Project): ParadoxScriptProperty? {
        if (namespace.isEmpty() || variable.isEmpty()) return null
        val defineSelector = ParadoxDefineVariableSearch.selector(project, contextElement).contextSensitive()
        return ParadoxDefineVariableSearch.search(namespace, variable, defineSelector).find()
    }

    fun findDefineVariableElement(expression: String, contextElement: PsiElement, project: Project): ParadoxScriptProperty? {
        val (namespace, variable) = splitExpression(expression)
        if (variable == null) return null
        return findDefineVariableElement(namespace, variable, contextElement, project)
    }

    fun isArrayDefine(element: ParadoxScriptProperty): Boolean {
        val propertyValue = element.propertyValue<ParadoxScriptBlock>() ?: return false
        return propertyValue.members().all { it is ParadoxScriptValue }
    }

    fun getArrayValue(element: ParadoxScriptProperty, index: Int): ParadoxScriptValue? {
        val propertyValue = element.propertyValue<ParadoxScriptBlock>() ?: return null
        return propertyValue.values().elementAtOrNull(index)
    }

    fun getArrayLength(element: ParadoxScriptProperty): Int? {
        val propertyValue = element.propertyValue<ParadoxScriptBlock>() ?: return null
        return propertyValue.values().count()
    }
}
