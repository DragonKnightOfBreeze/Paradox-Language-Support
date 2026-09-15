package icu.windea.pls.lang.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.config.util.CwtConfigManager
import javax.swing.Icon

object CwtConfigElementPresentationService {
    fun getIcon(element: PsiElement): Icon? {
        run {
            val configType = CwtConfigManager.getConfigType(element) ?: return@run
            return configType.icon
        }
        return null
    }
}
