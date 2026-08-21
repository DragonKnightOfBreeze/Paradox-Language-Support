package icu.windea.pls.lang.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder

data class ParadoxFileInspectionContext(
    val tool: LocalInspectionTool,
    val holder: ProblemsHolder,
    val ignoredFilePaths: String = "",
) {
    interface Aware {
        fun createContext(holder: ProblemsHolder): ParadoxFileInspectionContext
    }
}
