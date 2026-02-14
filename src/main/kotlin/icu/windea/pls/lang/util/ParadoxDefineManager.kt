@file:Suppress("unused")

package icu.windea.pls.lang.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.runReadActionSmartly
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.search.ParadoxDefineSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.ParadoxDefineInfo
import icu.windea.pls.model.constraints.ParadoxPathConstraint
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptRootBlock
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.greenStub
import icu.windea.pls.script.psi.stubs.ParadoxScriptPropertyStub

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

    fun getExpression(element: ParadoxScriptProperty): String? {
        val stub = runReadActionSmartly { getStub(element) }
        stub?.let { return getExpression(stub.namespace, stub.variable) }
        return getInfo(element)?.expression
    }

    fun getInfo(element: ParadoxScriptProperty): ParadoxDefineInfo? {
        // from cache (invalidated on file modification)
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefineInfo) {
            ProgressManager.checkCanceled()
            runReadActionSmartly {
                val file = element.containingFile
                val value = resolveInfo(element, file)
                value.withDependencyItems(file)
            }
        }
    }

    private fun resolveInfo(element: ParadoxScriptProperty, file: PsiFile): ParadoxDefineInfo? {
        resolveInfoFromStub(element)?.let { return it }
        return resolveInfoFromPsi(element, file)
    }

    private fun resolveInfoFromStub(element: ParadoxScriptProperty): ParadoxDefineInfo? {
        val stub = getStub(element) ?: return null
        return ParadoxDefineInfo(stub.namespace, stub.variable, stub.gameType)
    }

    private fun resolveInfoFromPsi(element: ParadoxScriptProperty, file: PsiFile): ParadoxDefineInfo? {
        if (!isDefineFile(file)) return null
        val gameType = selectGameType(file) ?: return null
        val parent = element.parent
        if (parent is ParadoxScriptRootBlock) {
            val namespace = element.name
            if (namespace.isEmpty() || namespace.isParameterized()) return null
            return ParadoxDefineInfo(namespace, null, gameType)
        } else if (parent is ParadoxScriptBlock) {
            val namespaceElement = parent.parent
            if (namespaceElement !is ParadoxScriptProperty) return null
            if (namespaceElement.parent !is ParadoxScriptRootBlock) return null
            val namespace = namespaceElement.name
            if (namespace.isEmpty() || namespace.isParameterized()) return null
            val variable = element.name
            if (variable.isEmpty() || variable.isParameterized()) return null
            return ParadoxDefineInfo(namespace, variable, gameType)
        }
        return null
    }

    fun getStub(element: ParadoxScriptProperty): ParadoxScriptPropertyStub.Define? {
        return element.greenStub?.castOrNull()
    }

    fun findDefineElement(expression: String, contextElement: PsiElement, project: Project): ParadoxScriptProperty? {
        val defineSelector = selector(project, contextElement).define().contextSensitive()
        return ParadoxDefineSearch.search(expression, defineSelector).find()
    }

    fun findDefineValueElement(expression: String, contextElement: PsiElement, project: Project): ParadoxScriptValue? {
        val defineElement = findDefineElement(expression, contextElement, project) ?: return null
        return defineElement.propertyValue
    }
}
