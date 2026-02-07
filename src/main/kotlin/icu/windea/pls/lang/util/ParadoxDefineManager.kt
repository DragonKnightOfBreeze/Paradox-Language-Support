@file:Suppress("unused")

package icu.windea.pls.lang.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.fileInfo
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

    fun isDefineElement(element: ParadoxScriptProperty, namespace: String, variable: String?): Boolean {
        if (variable == null) return element.propertyValue is ParadoxScriptBlock
        return element.parent is ParadoxScriptRootBlock
    }

    fun findDefineElement(expression: String, contextElement: PsiElement, project: Project): ParadoxScriptProperty? {
        val defineSelector = selector(project, contextElement).define().contextSensitive()
        val defineInfo = ParadoxDefineSearch.search(expression, defineSelector).find() ?: return null
        return defineInfo
    }

    fun findDefineValueElement(expression: String, contextElement: PsiElement, project: Project): ParadoxScriptValue? {
        val defineElement = findDefineElement(expression, contextElement, project) ?: return null
        return defineElement.propertyValue
    }

    fun getInfo(element: ParadoxScriptProperty): ParadoxDefineInfo? {
        // get from cache
        return doGetInfoFromCache(element)
    }

    private fun doGetInfoFromCache(element: ParadoxScriptProperty): ParadoxDefineInfo? {
        return CachedValuesManager.getCachedValue(element, Keys.cachedDefineInfo) {
            ProgressManager.checkCanceled()
            val file = element.containingFile
            val value = doGetInfo(element, file)
            value.withDependencyItems(file)
        }
    }

    private fun doGetInfo(element: ParadoxScriptProperty, file: PsiFile): ParadoxDefineInfo? {
        doGetInfoFromStub(element)?.let { return it }
        return doGetInfoFromPsi(element, file)
    }

    private fun doGetInfoFromStub(element: ParadoxScriptProperty): ParadoxDefineInfo? {
        val stub = element.greenStub ?: return null
        return when (stub) {
            is ParadoxScriptPropertyStub.DefineNamespace -> {
                ParadoxDefineInfo(stub.namespace, null, stub.gameType)
            }
            is ParadoxScriptPropertyStub.DefineVariable -> {
                ParadoxDefineInfo(stub.namespace, stub.variable, stub.gameType)
            }
            else -> null
        }
    }

    private fun doGetInfoFromPsi(element: ParadoxScriptProperty, file: PsiFile): ParadoxDefineInfo? {
        if (!isDefineFile(file)) return null
        val gameType = selectGameType(file) ?: return null
        val parent = element.parent
        if (parent is ParadoxScriptRootBlock) {
            return ParadoxDefineInfo(element.name, null, gameType)
        } else if (parent is ParadoxScriptBlock) {
            val namespaceElement = parent.parent
            if (namespaceElement is ParadoxScriptProperty) {
                return ParadoxDefineInfo(namespaceElement.name, element.name, gameType)
            }
        }
        return null
    }

    fun getExpression(element: ParadoxScriptProperty): String? {
        return getInfo(element)?.expression
    }
}
