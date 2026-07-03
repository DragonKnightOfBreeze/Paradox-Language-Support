package icu.windea.pls.script.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IFileElementType
import icu.windea.pls.core.findChild
import icu.windea.pls.core.findChildren
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.lang.psi.ParadoxFile
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.ParadoxScriptLanguage

class ParadoxScriptFile(
    viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, ParadoxScriptLanguage), ParadoxFile, ParadoxScriptMemberContext, ParadoxScriptMember, ParadoxDefinitionElement {
    companion object {
        @JvmField val ELEMENT_TYPE: IFileElementType = IFileElementType("PARADOX_SCRIPT_FILE", ParadoxScriptLanguage)
    }

    override val block: ParadoxScriptRootBlock? get() = findChild<_>()
    override val memberContainer: ParadoxScriptRootBlock? get() = findChild<_>()
    override val members: List<ParadoxScriptMember> get() = memberContainer?.findChildren<ParadoxScriptMember>().orEmpty()

    override fun getFileType() = ParadoxScriptFileType

    override fun getPresentation() = ParadoxScriptPsiPresentation(this)

    override fun toString() = PsiService.toPresentableString(this)

    override fun isEquivalentTo(another: PsiElement?): Boolean {
        return super.isEquivalentTo(another) || another is ParadoxScriptFile && ParadoxFileManager.isEquivalentFile(this, another)
    }
}
