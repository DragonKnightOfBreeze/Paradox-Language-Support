package icu.windea.pls.lang.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.psi.CwtMember
import javax.swing.Icon

object CwtConfigElementPresentationService {
    fun getIcon(element: PsiElement): Icon? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        run {
            // from config type
            if (element !is CwtMember) return@run
            val configType = CwtConfigManager.getConfigType(element) ?: return@run
            return configType.icon
        }

        return null
    }

    private fun checkLanguage(element: PsiElement): Boolean = element.language == CwtLanguage
}
