package icu.windea.pls.script.navigation

import com.intellij.navigation.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import com.intellij.util.*
import com.intellij.util.indexing.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.script.psi.*

//com.intellij.ide.util.gotoByName.JavaModuleNavigationContributor

/**
 * 用于让`Navigate | Class or Navigate | Symbol`可以查找到匹配名字的定义。
 */
class ParadoxDefinitionChooseByNameContributor : ChooseByNameContributorEx {
    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        StubIndex.getInstance().processAllKeys(ParadoxDefinitionNameIndex.KEY, processor, scope, filter)
    }
    
    override fun processElementsWithName(name: String, processor: Processor<in NavigationItem>, parameters: FindSymbolParameters) {
        StubIndex.getInstance().processElements(
            ParadoxDefinitionNameIndex.KEY, name, parameters.project, parameters.searchScope, parameters.idFilter,
            ParadoxScriptDefinitionElement::class.java
        ) p@{
            val definitionInfo = it.definitionInfo ?: return@p true
            processor.process(ParadoxDefinitionNavigationElement(it, definitionInfo))
        }
    }
}
