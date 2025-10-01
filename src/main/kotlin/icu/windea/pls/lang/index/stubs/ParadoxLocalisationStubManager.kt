package icu.windea.pls.lang.index.stubs

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationLightTreeUtil
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList
import icu.windea.pls.localisation.psi.stubs.ParadoxLocalisationPropertyListStub
import icu.windea.pls.localisation.psi.stubs.ParadoxLocalisationPropertyStub

object ParadoxLocalisationStubManager {
    fun createPropertyListStub(psi: ParadoxLocalisationPropertyList, parentStub: StubElement<out PsiElement>?): ParadoxLocalisationPropertyListStub {
        val locale = selectLocale(psi)?.id
        return ParadoxLocalisationPropertyListStub.create(parentStub, locale)
    }

    fun createPropertyListStub(node: LighterASTNode, tree: LighterAST, parentStub: StubElement<*>): ParadoxLocalisationPropertyListStub {
        val locale = ParadoxLocalisationLightTreeUtil.getLocaleFromPropertyListNode(node, tree)
        return ParadoxLocalisationPropertyListStub.create(parentStub, locale)
    }

    fun createPropertyStub(psi: ParadoxLocalisationProperty, parentStub: StubElement<out PsiElement>?): ParadoxLocalisationPropertyStub {
        val name = psi.name
        if (name.isEmpty()) return ParadoxLocalisationPropertyStub.createDummy(parentStub)
        return ParadoxLocalisationPropertyStub.create(parentStub, name)
    }

    fun createPropertyStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): ParadoxLocalisationPropertyStub {
        val name = ParadoxLocalisationLightTreeUtil.getNameFromPropertyNode(node, tree).orEmpty()
        if (name.isEmpty()) return ParadoxLocalisationPropertyStub.createDummy(parentStub)
        return ParadoxLocalisationPropertyStub.create(parentStub, name)
    }
}
