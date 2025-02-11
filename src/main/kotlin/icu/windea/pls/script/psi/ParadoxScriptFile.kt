package icu.windea.pls.script.psi

import com.intellij.extapi.psi.*
import com.intellij.navigation.*
import com.intellij.openapi.fileTypes.*
import com.intellij.psi.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.*
import icu.windea.pls.script.navigation.*
import javax.swing.*

class ParadoxScriptFile(
    viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, ParadoxScriptLanguage), ParadoxScriptDefinitionElement {
    companion object {
        val ELEMENT_TYPE = ParadoxScriptStubElementTypes.FILE
    }

    override val block get() = findChild<ParadoxScriptRootBlock>()

    override fun getIcon(flags: Int): Icon? {
        //对模组描述符文件使用特定的图标
        if (name.equals(PlsConstants.modDescriptorFileName, true)) return PlsIcons.FileTypes.ModeDescriptor
        return super.getIcon(flags)
    }

    override fun getFileType(): FileType {
        return ParadoxScriptFileType
    }

    override fun getPresentation(): ItemPresentation {
        return ParadoxScriptFilePresentation(this)
    }

    override fun isEquivalentTo(another: PsiElement?): Boolean {
        return super.isEquivalentTo(another) || (another is ParadoxScriptFile && fileInfo == another.fileInfo)
    }
}

