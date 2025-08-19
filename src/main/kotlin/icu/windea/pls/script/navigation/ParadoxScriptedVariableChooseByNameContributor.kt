package icu.windea.pls.script.navigation

import com.intellij.navigation.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import com.intellij.util.*
import com.intellij.util.indexing.*
import icu.windea.pls.lang.index.ParadoxIndexKeys
import icu.windea.pls.script.psi.*

/**
 * 用于在随处搜索（Search Everywhere）中查找对应名字的封装变量。
 */
class ParadoxScriptedVariableChooseByNameContributor : ChooseByNameContributorEx {
    //com.intellij.ide.util.gotoByName.JavaModuleNavigationContributor

    private val indexKey = ParadoxIndexKeys.ScriptedVariableName

    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        StubIndex.getInstance().processAllKeys(indexKey, processor, scope, filter)
    }

    override fun processElementsWithName(name: String, processor: Processor<in NavigationItem>, parameters: FindSymbolParameters) {
        val project = parameters.project
        val scope = parameters.searchScope
        val idFilter = parameters.idFilter
        val requiredClass = ParadoxScriptScriptedVariable::class.java
        StubIndex.getInstance().processElements(indexKey, name, project, scope, idFilter, requiredClass, processor)
    }
}
