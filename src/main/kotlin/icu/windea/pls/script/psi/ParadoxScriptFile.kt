package icu.windea.pls.script.psi

import com.intellij.extapi.psi.*
import com.intellij.psi.*
import com.intellij.psi.tree.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.*
import icu.windea.pls.script.navigation.*

class ParadoxScriptFile(
    viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, ParadoxScriptLanguage), ParadoxScriptDefinitionElement {
    companion object {
        @JvmField
        val ELEMENT_TYPE: IFileElementType = ParadoxScriptFileStubElementType.INSTANCE
    }

    override val block get() = findChild<ParadoxScriptRootBlock>()

    override fun getFileType() = ParadoxScriptFileType

    override fun getPresentation() = ParadoxScriptItemPresentation(this)

    override fun isEquivalentTo(another: PsiElement?): Boolean {
        return super.isEquivalentTo(another) || another is ParadoxScriptFile && ParadoxFileManager.isEquivalentFile(this, another)
    }
}

