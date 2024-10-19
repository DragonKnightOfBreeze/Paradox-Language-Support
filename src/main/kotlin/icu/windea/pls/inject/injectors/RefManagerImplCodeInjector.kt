package icu.windea.pls.inject.injectors

import com.intellij.codeInspection.reference.*
import icu.windea.pls.core.*
import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*
import icu.windea.pls.lang.*

/**
 * @see com.intellij.codeInspection.reference.RefManagerImpl
 * @see com.intellij.codeInspection.reference.RefManagerImpl.getGroupName
 */
@InjectTarget("com.intellij.codeInspection.reference.RefManagerImpl")
class RefManagerImplCodeInjector : CodeInjectorBase() {
    //如果可行，让代码检查页面中的按目录分组选项按照相对于游戏或模组目录的路径分组，而非简单地按照目录名分组

    @InjectMethod(pointer = InjectMethod.Pointer.BEFORE)
    fun getGroupName(entity: RefElement): String? {
        if (entity is RefFile) {
            //按目录分组时显示相对于游戏或模组目录的路径
            val element = entity.psiElement
            val file = selectFile(element)
            val contextFile = file?.findTopHostFileOrThis()
            val fileInfo = contextFile?.fileInfo
            if (fileInfo != null) return fileInfo.relPath.parent.orNull()
        }
        continueInvocation()
    }
}
