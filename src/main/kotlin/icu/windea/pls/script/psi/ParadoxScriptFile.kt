package icu.windea.pls.script.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IFileElementType
import icu.windea.pls.core.findChild
import icu.windea.pls.lang.psi.ParadoxBaseFile
import icu.windea.pls.lang.psi.PlsPsiManager
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.navigation.ParadoxScriptItemPresentation

class ParadoxScriptFile(
    viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, ParadoxScriptLanguage), ParadoxBaseFile, ParadoxScriptMember, ParadoxScriptDefinitionElement, ParadoxScriptMemberContainer {
    companion object {
        @JvmField
        val ELEMENT_TYPE: IFileElementType = IFileElementType("PARADOX_SCRIPT_FILE", ParadoxScriptLanguage)
    }

    override val block: ParadoxScriptRootBlock? get() = findChild<ParadoxScriptRootBlock>()
    override val memberList: List<ParadoxScriptMember> get() = block?.memberList.orEmpty()
    override val propertyList: List<ParadoxScriptProperty> get() = block?.propertyList.orEmpty()
    override val valueList: List<ParadoxScriptValue> get() = block?.valueList.orEmpty()

    override fun getFileType() = ParadoxScriptFileType

    override fun getPresentation() = ParadoxScriptItemPresentation(this)

    override fun toString() = PlsPsiManager.toPresentableString(this)

    override fun isEquivalentTo(another: PsiElement?): Boolean {
        return super.isEquivalentTo(another) || another is ParadoxScriptFile && ParadoxFileManager.isEquivalentFile(this, another)
    }
}
