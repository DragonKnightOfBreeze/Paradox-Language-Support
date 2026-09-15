package icu.windea.pls.script.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IFileElementType
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.psi.ParadoxFile
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.impl.ParadoxScriptPsiImplUtil

class ParadoxScriptFile(
    viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, ParadoxScriptLanguage), ParadoxFile, ParadoxScriptMemberContext, ParadoxScriptMember, ParadoxDefinitionElement {
    companion object {
        @JvmField val ELEMENT_TYPE: IFileElementType = IFileElementType("PARADOX_SCRIPT_FILE", ParadoxScriptLanguage)
    }

    override val block: ParadoxScriptRootBlock? get() = ParadoxScriptPsiImplUtil.getBlock(this)

    override val memberContainer: ParadoxScriptRootBlock? get() = ParadoxScriptPsiImplUtil.getMemberContainer(this)

    override val members: List<ParadoxScriptMember> get() = ParadoxScriptPsiImplUtil.getMembers(this)

    override fun getFileType() = ParadoxScriptFileType

    override fun getPresentation() = ParadoxScriptElementPresentation(this)

    override fun isEquivalentTo(another: PsiElement?) = ParadoxScriptPsiImplUtil.isEquivalentTo(this, another)

    override fun getResolveScope() = ParadoxScriptPsiImplUtil.getResolveScope(this)

    override fun getUseScope() = ParadoxScriptPsiImplUtil.getUseScope(this)

    override fun toString() = ParadoxScriptPsiImplUtil.toString(this)
}
