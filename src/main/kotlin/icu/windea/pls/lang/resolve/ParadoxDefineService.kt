package icu.windea.pls.lang.resolve

import com.intellij.psi.PsiFile
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefineManager.isDefineFile
import icu.windea.pls.model.ParadoxDefineInfo
import icu.windea.pls.model.ParadoxDefineNamespaceInfo
import icu.windea.pls.model.ParadoxDefineVariableInfo
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptRootBlock
import icu.windea.pls.script.psi.greenStub
import icu.windea.pls.script.psi.stubs.ParadoxScriptPropertyStub

object ParadoxDefineService {
    fun resolveInfo(element: ParadoxScriptProperty, file: PsiFile): ParadoxDefineInfo? {
        resolveInfoFromStub(element)?.let { return it }
        return resolveInfoFromPsi(element, file)
    }

    private fun resolveInfoFromStub(element: ParadoxScriptProperty): ParadoxDefineInfo? {
        val stub = element.greenStub
        return when (stub) {
            is ParadoxScriptPropertyStub.DefineNamespace -> ParadoxDefineNamespaceInfo(stub.namespace, stub.gameType)
            is ParadoxScriptPropertyStub.DefineVariable -> ParadoxDefineVariableInfo(stub.namespace, stub.variable, stub.gameType)
            else -> null
        }
    }

    private fun resolveInfoFromPsi(element: ParadoxScriptProperty, file: PsiFile): ParadoxDefineInfo? {
        if (!isDefineFile(file)) return null
        val gameType = selectGameType(file) ?: return null
        val parent = element.parent
        if (parent is ParadoxScriptRootBlock) {
            val namespace = element.name
            if (namespace.isEmpty() || namespace.isParameterized()) return null
            return ParadoxDefineNamespaceInfo(namespace, gameType)
        } else if (parent is ParadoxScriptBlock) {
            val namespaceElement = parent.parent
            if (namespaceElement !is ParadoxScriptProperty) return null
            if (namespaceElement.parent !is ParadoxScriptRootBlock) return null
            val namespace = namespaceElement.name
            if (namespace.isEmpty() || namespace.isParameterized()) return null
            val variable = element.name
            if (variable.isEmpty() || variable.isParameterized()) return null
            return ParadoxDefineVariableInfo(namespace, variable, gameType)
        }
        return null
    }
}
