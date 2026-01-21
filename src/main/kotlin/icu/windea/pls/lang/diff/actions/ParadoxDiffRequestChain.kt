package icu.windea.pls.lang.diff.actions

import com.intellij.diff.actions.impl.GoToChangePopupBuilder
import com.intellij.diff.chains.DiffRequestSelectionChain
import com.intellij.openapi.ListSelection
import com.intellij.openapi.util.UserDataHolderBase

abstract class ParadoxDiffRequestChain(
    producers: List<ParadoxDiffRequestProducer>,
    defaultIndex: Int = 0,
) : UserDataHolderBase(), DiffRequestSelectionChain, GoToChangePopupBuilder.Chain {
    private val listSelection = ListSelection.createAt(producers, defaultIndex)

    override fun getListSelection() = listSelection

    override fun getRequests() = listSelection.list
}
