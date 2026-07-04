package icu.windea.pls.lang.fixes

import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.modcommand.ModCommand
import com.intellij.modcommand.ModCommandQuickFix
import com.intellij.openapi.project.Project

class BrowseUrlFix(private val name: String, private val url: String) : ModCommandQuickFix() {
    override fun getFamilyName() = name

    override fun perform(project: Project, descriptor: ProblemDescriptor) = ModCommand.openUrl(url)
}
