package icu.windea.pls.lang.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.cwt.psi.CwtMember
import javax.swing.Icon

object CwtConfigElementPresentationService {
    fun getIcon(element: PsiElement): Icon? {
        run {
            // from config type
            if (element !is CwtMember) return@run
            val configType = CwtConfigManager.getConfigType(element) ?: return@run
            return configType.icon
        }

        return null
    }
}
