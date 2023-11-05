package icu.windea.pls.lang.configGroup

import com.intellij.openapi.editor.toolbar.floating.*
import com.intellij.psi.impl.*
import icu.windea.pls.cwt.psi.*

class CwtConfigGroupPsiTreeChangePreprocessor: PsiTreeChangePreprocessor {
    //这个方法应当尽可能地快
    override fun treeChanged(event: PsiTreeChangeEventImpl) {
        if(!PsiModificationTrackerImpl.canAffectPsi(event)) return
        
        val file = event.file ?: return
        if(file !is CwtFile) return
        val vFile = file.virtualFile ?: return
        val project = file.project
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        val changed = fileProviders.any { fileProvider ->
            fileProvider.onFileChange(project, vFile)
        }
        if(changed) {
            FloatingToolbarProvider.getProvider<ConfigGroupRefreshFloatingProvider>()
                .updateToolbarComponents(project)
        }
    }
}