package icu.windea.pls.inject.injectors

import com.intellij.codeInspection.reference.*
import icu.windea.pls.core.*
import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*

/**
 * 如果可行，让代码检查页面中的按目录分组选项按照相对于游戏或模组根目录的路径分组，而非简单地按照目录名分组。
 */
@InjectTarget("com.intellij.codeInspection.reference.RefManagerImpl")
class RefManagerImplCodeInjector : BaseCodeInjector() {
    //com.intellij.codeInspection.reference.RefManagerImpl
    //com.intellij.codeInspection.reference.RefManagerImpl.getGroupName
    
    @Inject(Inject.Pointer.BEFORE)
    fun getGroupName(entity: RefElement): String? {
        if(entity is RefFile) {
            //按目录分组时显示相对于游戏或模组根目录的路径
            val element = entity.psiElement
            val file = selectFile(element)
            val contextFile = file?.findTopHostFileOrThis()
            val fileInfo = contextFile?.fileInfo
            if(fileInfo != null) return fileInfo.path.parent.takeIfNotEmpty()
        }
        continueInvocation()
    }
}
