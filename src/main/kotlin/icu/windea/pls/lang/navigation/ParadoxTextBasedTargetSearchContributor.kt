package icu.windea.pls.lang.navigation

import com.intellij.ide.actions.SearchEverywherePsiRenderer
import com.intellij.ide.actions.searcheverywhere.FoundItemDescriptor
import com.intellij.ide.actions.searcheverywhere.PossibleSlowContributor
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereExtendedInfoProvider
import com.intellij.ide.actions.searcheverywhere.SearchEverywherePreviewProvider
import com.intellij.ide.actions.searcheverywhere.SymbolSearchEverywhereContributor
import com.intellij.ide.actions.searcheverywhere.WeightedSearchEverywhereContributor
import com.intellij.ide.actions.searcheverywhere.footer.createPsiExtendedInfo
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.Processor
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.processQuery
import icu.windea.pls.lang.psi.mock.NavigationPsiElement
import icu.windea.pls.lang.search.target.ParadoxTextBasedTargetSearch

/**
 * 提供基于本地化文本片段的随处搜索（Search Everywhere）。
 *
 * 设计要点：
 * - 直接复用 ParadoxTextBasedTargetSearch 提供的查询逻辑，避免重复实现。
 * - 仅从本地化文本（ParadoxLocalisationString 的纯文本）出发，不解析 $KEY$ 等引用。
 * - 渐进式输出，支持取消，保证体验与性能。
 */
class ParadoxTextBasedTargetSearchContributor(val event: AnActionEvent) : WeightedSearchEverywhereContributor<PsiElement>,
    PossibleSlowContributor,
    SearchEverywhereExtendedInfoProvider,
    SearchEverywherePreviewProvider {

    // com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor
    // com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory
    // com.intellij.find.impl.TextSearchContributor
    // com.intellij.ide.actions.searcheverywhere.CalculatorSEContributor

    // 注意这里需要使用 NavigationPsiElement 绕过如下内部检查：
    // com.intellij.diagnostic.PluginException: PSI element for DataKey("selectedItems") is provided on EDT by com.intellij.ide.actions.searcheverywhere.SearchEverywhereUI. Use `DataSink.lazy` to provide such data
    // com.intellij.ide.impl.DataValidators.isDataValid

    private val delegate = SymbolSearchEverywhereContributor(event)

    override fun getSearchProviderId(): String = PROVIDER_ID

    override fun getGroupName(): String = PlsBundle.message("se.group.textBased.groupName")

    override fun getSortWeight(): Int = 2000 // symbol=300 action=400 text=1500

    override fun showInFindResults(): Boolean = true

    override fun isShownInSeparateTab() = true

    override fun getAdvertisement() = PlsBundle.message("se.group.textBased.ad")

    override fun fetchWeightedElements(pattern: String, progressIndicator: ProgressIndicator, consumer: Processor<in FoundItemDescriptor<PsiElement>>) {
        val project = event.project ?: return
        val queryText = pattern.trim()
        if (queryText.isEmpty()) return

        val scope = GlobalSearchScope.projectScope(project)
        val search = ParadoxTextBasedTargetSearch.search(queryText, project, scope)
        search.processQuery p@{ element ->
            progressIndicator.checkCanceled()
            if (element !is NavigatablePsiElement) return@p true
            consumer.process(FoundItemDescriptor(NavigationPsiElement(element), 0))
        }
    }

    override fun processSelectedItem(selected: PsiElement, modifiers: Int, searchText: String): Boolean {
        // com.intellij.ide.actions.searcheverywhere.AbstractGotoSEContributor.processSelectedItem
        val element = selected
        return delegate.processSelectedItem(element, modifiers, searchText)
    }

    // override fun getDataForItem(element: PsiElement, dataId: String): Any? {
    //     if (CommonDataKeys.PSI_ELEMENT.`is`(dataId)) {
    //         return element.parent
    //     }
    //     return null
    // }

    override fun getElementsRenderer() = SearchEverywherePsiRenderer(this)

    override fun createExtendedInfo() = createPsiExtendedInfo(null, null) { it.castOrNull<PsiElement>() }

    companion object {
        const val PROVIDER_ID: String = "Pls.TextBasedTargetSearch"
    }

    class Factory : SearchEverywhereContributorFactory<PsiElement> {
        override fun isAvailable(project: Project?) = PlsFacade.getSettings().navigation.seForTextBasedTarget

        override fun createContributor(initEvent: AnActionEvent): ParadoxTextBasedTargetSearchContributor {
            return ParadoxTextBasedTargetSearchContributor(initEvent)
        }
    }
}
