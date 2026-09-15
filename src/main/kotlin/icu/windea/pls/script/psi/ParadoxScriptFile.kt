package icu.windea.pls.script.psi

import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.psi.ParadoxFile
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.ParadoxScriptLanguage

interface ParadoxScriptFile : PsiFile, ParadoxFile, ParadoxScriptMemberContext, ParadoxScriptMember, ParadoxDefinitionElement {
    companion object {
        @JvmField val ELEMENT_TYPE: IFileElementType = IFileElementType("PARADOX_SCRIPT_FILE", ParadoxScriptLanguage)
    }

    override val block: ParadoxScriptRootBlock?

    override fun getFileType() = ParadoxScriptFileType
}
