@file:Suppress("unused")

package icu.windea.pls.inject.injectors

import com.intellij.codeInspection.reference.RefElement
import com.intellij.codeInspection.reference.RefFile
import icu.windea.pls.core.orNull
import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.InjectMethod
import icu.windea.pls.inject.annotations.InjectionTarget
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.injection.PlsInjectionManager
import icu.windea.pls.lang.selectFile

/**
 * @see com.intellij.codeInspection.reference.RefManagerImpl
 * @see com.intellij.codeInspection.reference.RefManagerImpl.getGroupName
 */
@InjectionTarget("com.intellij.codeInspection.reference.RefManagerImpl")
class RefManagerImplCodeInjector : CodeInjectorBase() {
    // 如果可行，让代码检查页面中的按目录分组选项按照相对于入口目录的路径分组，而非简单地按照目录名分组

    @InjectMethod(pointer = InjectMethod.Pointer.BEFORE)
    fun getGroupName(entity: RefElement): String? {
        run {
            // 按目录分组时显示相对于入口目录的路径
            if (entity !is RefFile) return@run
            val element = entity.psiElement ?: return@run
            val file = selectFile(element) ?: return@run
            val contextFile = PlsInjectionManager.findTopHostFileOrThis(file)
            val fileInfo = contextFile.fileInfo ?: return@run
            return fileInfo.path.parent.orNull()
        }
        continueInvocation()
    }
}
