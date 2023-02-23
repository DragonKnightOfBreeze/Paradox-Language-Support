package icu.windea.pls.script.navigation

import com.intellij.navigation.*
import com.intellij.psi.search.*
import com.intellij.psi.stubs.*
import com.intellij.util.*
import com.intellij.util.indexing.*
import icu.windea.pls.core.index.*
import icu.windea.pls.script.psi.*

//com.intellij.ide.util.gotoByName.JavaModuleNavigationContributor

/**
 * 用于让`Navigate | Class or Navigate | Symbol`可以查找到匹配名字的复杂枚举值。
 */
class ParadoxComplexEnumValueChooseByNameContributor : ChooseByNameContributorEx {
    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        StubIndex.getInstance().processAllKeys(ParadoxComplexEnumValueIndex.KEY, processor, scope, filter)
    }
    
    override fun processElementsWithName(name: String, processor: Processor<in NavigationItem>, parameters: FindSymbolParameters) {
        //这里显示的是property/value的图表，而非complexEnum的图标
        StubIndex.getInstance().processElements(ParadoxComplexEnumValueIndex.KEY, name, parameters.project, parameters.searchScope, parameters.idFilter, ParadoxScriptStringExpressionElement::class.java, processor)
    }
}