package icu.windea.pls.lang.configGroup

import com.intellij.ide.*
import com.intellij.lang.annotation.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*

/**
 * 当用户打开一个可用于自定义CWT规则分组的CWT文件时，给出提示以及一些参考信息。
 */
class CwtConfigGroupAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when(element) {
            is CwtFile -> annotateFile(element, holder)
        }
    }
    
    private fun annotateFile(file: CwtFile, holder: AnnotationHolder) {
        val project = file.project
        val vFile = file.virtualFile ?: return
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        val configGroups = mutableSetOf<CwtConfigGroup>()
        fileProviders.forEach { fileProvider ->
            configGroups += fileProvider.getConfigGroups(project, vFile)
        }
        if(configGroups.isEmpty()) return
        val message = PlsBundle.message("configGroup.annotator.file.message")
        holder.newAnnotation(HighlightSeverity.INFORMATION, message).fileLevel()
            .withFix(createIntentionAction(PlsBundle.message("configGroup.annotator.file.fix.guidance")) { _, _, _ ->
                val url = "https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/references/cwt/guidance.md"
                //val url = "https://github.com/cwtools/cwtools/wiki/.cwt-config-file-guidance"
                BrowserUtil.browse(url)
            })
            .withFix(createIntentionAction(PlsBundle.message("configGroup.annotator.file.fix.repositories")) { _, _, _ ->
                val url = "https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/tree/master/src/main/resources/config"
                BrowserUtil.browse(url)
            })
            .create()
    }
}
