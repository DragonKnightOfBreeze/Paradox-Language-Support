package icu.windea.pls.inject.injectors

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.util.InjectionUtils
import icu.windea.pls.core.cast
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.staticFunction
import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.InjectMethod
import icu.windea.pls.inject.annotations.InjectTarget
import icu.windea.pls.script.psi.ParadoxScriptFile

@InjectTarget("org.intellij.plugins.intelliLang.inject.InjectLanguageAction", pluginId = "org.intellij.intelliLang")
class InjectLanguageActionCodeInjector : CodeInjectorBase() {
    // https://github.com/JetBrains/intellij-community/pull/3366

    // org.intellij.plugins.intelliLang.inject.InjectLanguageAction#findInjectionHost
    private fun Any.findInjectionHost(editor: Editor, file: PsiFile): PsiLanguageInjectionHost? {
        val function = staticFunction<Any>("findInjectionHost")
        return runCatchingCancelable { function(editor, file) }.getOrNull()?.cast()
    }

    @Suppress("unused")
    @InjectMethod(pointer = InjectMethod.Pointer.BEFORE)
    fun isAvailable(project: Project, editor: Editor, psiFile: PsiFile): Boolean {
        run {
            if (psiFile !is ParadoxScriptFile) return@run
            val host = findInjectionHost(editor, psiFile) ?: return false
            if (!InjectionUtils.isInjectLanguageActionEnabled(host)) return false
        }
        continueInvocation()
    }
}
