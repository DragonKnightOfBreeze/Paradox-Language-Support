package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.startOffset
import com.intellij.util.Processor
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.index.ParadoxDefineIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.greenStub
import icu.windea.pls.script.psi.stubs.ParadoxScriptPropertyStub

/**
 * 预定义的命名空间与变量的查询器。
 */
class ParadoxDefineSearcher : QueryExecutorBase<ParadoxDefineIndexInfo, ParadoxDefineSearch.SearchParameters>() {
    override fun processQuery(queryParameters: ParadoxDefineSearch.SearchParameters, consumer: Processor<in ParadoxDefineIndexInfo>) {
        ProgressManager.checkCanceled()
        val project = queryParameters.project
        if (project.isDefault) return
        val scope = GlobalSearchScope.projectScope(project)
        if (SearchScope.isEmptyScope(scope)) return

        val variable = queryParameters.variable
        val namespace = queryParameters.namespace

        val separator = "\u0000"

        // variable == "" 表示要查询命名空间本身
        when {
            namespace != null && variable != null -> {
                if (variable.isEmpty()) {
                    val elements = StubIndex.getElements(PlsIndexKeys.DefineNamespace, namespace, project, scope, ParadoxScriptProperty::class.java)
                    elements.forEach { element ->
                        ProgressManager.checkCanceled()
                        if (!processElement(queryParameters, element, consumer)) return
                    }
                } else {
                    val key = namespace + separator + variable
                    val elements = StubIndex.getElements(PlsIndexKeys.DefineVariable, key, project, scope, ParadoxScriptProperty::class.java)
                    elements.forEach { element ->
                        ProgressManager.checkCanceled()
                        if (!processElement(queryParameters, element, consumer)) return
                    }
                }
            }
            namespace != null -> {
                // namespace specified, query all variables under it + namespace element
                run {
                    val elements = StubIndex.getElements(PlsIndexKeys.DefineNamespace, namespace, project, scope, ParadoxScriptProperty::class.java)
                    elements.forEach { element ->
                        ProgressManager.checkCanceled()
                        if (!processElement(queryParameters, element, consumer)) return
                    }
                }
                val prefix = namespace + separator
                val keys = mutableSetOf<String>()
                StubIndex.getInstance().processAllKeys(PlsIndexKeys.DefineVariable, Processor { key ->
                    ProgressManager.checkCanceled()
                    if (key.startsWith(prefix)) keys.add(key)
                    true
                }, scope)
                keys.forEach { key ->
                    ProgressManager.checkCanceled()
                    val elements = StubIndex.getElements(PlsIndexKeys.DefineVariable, key, project, scope, ParadoxScriptProperty::class.java)
                    elements.forEach { element ->
                        ProgressManager.checkCanceled()
                        if (!processElement(queryParameters, element, consumer)) return
                    }
                }
            }
            variable != null -> {
                if (variable.isEmpty()) {
                    // all namespaces
                    val keys = mutableSetOf<String>()
                    StubIndex.getInstance().processAllKeys(PlsIndexKeys.DefineNamespace, Processor { key ->
                        ProgressManager.checkCanceled()
                        keys.add(key)
                        true
                    }, scope)
                    keys.forEach { key ->
                        ProgressManager.checkCanceled()
                        val elements = StubIndex.getElements(PlsIndexKeys.DefineNamespace, key, project, scope, ParadoxScriptProperty::class.java)
                        elements.forEach { element ->
                            ProgressManager.checkCanceled()
                            if (!processElement(queryParameters, element, consumer)) return
                        }
                    }
                } else {
                    // variable specified but namespace not specified: filter keys by suffix
                    val suffix = separator + variable
                    val keys = mutableSetOf<String>()
                    StubIndex.getInstance().processAllKeys(PlsIndexKeys.DefineVariable, Processor { key ->
                        ProgressManager.checkCanceled()
                        if (key.endsWith(suffix)) keys.add(key)
                        true
                    }, scope)
                    keys.forEach { key ->
                        ProgressManager.checkCanceled()
                        val elements = StubIndex.getElements(PlsIndexKeys.DefineVariable, key, project, scope, ParadoxScriptProperty::class.java)
                        elements.forEach { element ->
                            ProgressManager.checkCanceled()
                            if (!processElement(queryParameters, element, consumer)) return
                        }
                    }
                }
            }
            else -> {
                // all defines
                run {
                    val keys = mutableSetOf<String>()
                    StubIndex.getInstance().processAllKeys(PlsIndexKeys.DefineNamespace, Processor { key ->
                        ProgressManager.checkCanceled()
                        keys.add(key)
                        true
                    }, scope)
                    keys.forEach { key ->
                        ProgressManager.checkCanceled()
                        val elements = StubIndex.getElements(PlsIndexKeys.DefineNamespace, key, project, scope, ParadoxScriptProperty::class.java)
                        elements.forEach { element ->
                            ProgressManager.checkCanceled()
                            if (!processElement(queryParameters, element, consumer)) return
                        }
                    }
                }
                run {
                    val keys = mutableSetOf<String>()
                    StubIndex.getInstance().processAllKeys(PlsIndexKeys.DefineVariable, Processor { key ->
                        ProgressManager.checkCanceled()
                        keys.add(key)
                        true
                    }, scope)
                    keys.forEach { key ->
                        ProgressManager.checkCanceled()
                        val elements = StubIndex.getElements(PlsIndexKeys.DefineVariable, key, project, scope, ParadoxScriptProperty::class.java)
                        elements.forEach { element ->
                            ProgressManager.checkCanceled()
                            if (!processElement(queryParameters, element, consumer)) return
                        }
                    }
                }
            }
        }
    }

    private fun processElement(
        queryParameters: ParadoxDefineSearch.SearchParameters,
        element: ParadoxScriptProperty,
        consumer: Processor<in ParadoxDefineIndexInfo>
    ): Boolean {
        val project = queryParameters.project
        val file = element.containingFile?.virtualFile ?: return true
        val stub = element.greenStub?.castOrNull<ParadoxScriptPropertyStub>()

        val gameType = stub?.gameType
            ?: selectGameType(file)
            ?: return true
        if (queryParameters.gameType != null && queryParameters.gameType != gameType) return true

        val (namespace, variable) = when (stub) {
            is ParadoxScriptPropertyStub.DefineNamespace -> stub.namespace to null
            is ParadoxScriptPropertyStub.DefineVariable -> stub.namespace to stub.variable
            else -> {
                // fallback: infer from PSI tree
                val namespaceProperty = if (element.block != null) {
                    element
                } else {
                    element.parentOfType<ParadoxScriptProperty>(withSelf = false)
                } ?: return true
                val inferredNamespace = namespaceProperty.name
                if (inferredNamespace.isEmpty()) return true
                val inferredVariable = if (namespaceProperty === element) null else element.name
                inferredNamespace to inferredVariable
            }
        }

        val expectedNamespace = queryParameters.namespace
        if (expectedNamespace != null && expectedNamespace != namespace) return true
        val expectedVariable = queryParameters.variable
        when {
            expectedVariable == "" -> if (variable != null) return true
            expectedVariable != null -> if (variable != expectedVariable) return true
        }

        val info = ParadoxDefineIndexInfo(namespace, variable, element.startOffset, gameType)
        info.bind(file, project)
        return consumer.process(info)
    }
}
