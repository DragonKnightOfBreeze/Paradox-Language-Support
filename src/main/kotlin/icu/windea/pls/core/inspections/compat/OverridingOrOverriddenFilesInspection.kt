package icu.windea.pls.core.inspections.compat

import com.intellij.codeInspection.*
import com.intellij.psi.*

class OverridingOrOverriddenFilesInspection : LocalInspectionTool() {
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        return super.checkFile(file, manager, isOnTheFly)
    }
}
