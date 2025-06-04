package icu.windea.pls.script.navigation

import com.intellij.navigation.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import com.intellij.util.*
import com.intellij.util.indexing.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.script.psi.*

/**
 * 用于在随处搜索（Search Everywhere）中查找对应名字的本定义。
 */
class ParadoxDefinitionChooseByNameContributor : ChooseByNameContributorEx {
    //com.intellij.ide.util.gotoByName.JavaModuleNavigationContributor

    private val indexKey = ParadoxIndexManager.DefinitionNameKey

    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        StubIndex.getInstance().processAllKeys(indexKey, processor, scope, filter)
    }

    override fun processElementsWithName(name: String, processor: Processor<in NavigationItem>, parameters: FindSymbolParameters) {
        val project = parameters.project
        val scope = parameters.searchScope
        val idFilter = parameters.idFilter
        val requiredClass = ParadoxScriptDefinitionElement::class.java
        StubIndex.getInstance().processElements(indexKey, name, project, scope, idFilter, requiredClass) p@{
            val definitionInfo = it.definitionInfo ?: return@p true
            processor.process(ParadoxDefinitionNavigationElement(it, definitionInfo))
        }
    }
}
