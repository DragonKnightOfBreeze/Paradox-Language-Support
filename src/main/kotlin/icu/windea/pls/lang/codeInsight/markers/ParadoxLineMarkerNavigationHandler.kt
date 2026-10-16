package icu.windea.pls.lang.codeInsight.markers

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.navigation.PsiTargetNavigator
import com.intellij.codeInsight.navigation.impl.PsiTargetPresentationRenderer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import java.awt.event.MouseEvent

class ParadoxLineMarkerNavigationHandler<T : PsiElement>(
    val title: @NlsContexts.PopupTitle String,
    val findUsagesTitle: String,
    val project: Project,
    val targetsProvider: () -> Collection<T>,
) : GutterIconNavigationHandler<T> {
    // org.jetbrains.kotlin.idea.highlighter.markers.SuperDeclarationMarkerNavigationHandler
    // org.jetbrains.kotlin.idea.codeInsight.lineMarkers.shared.NavigationPopupDescriptor

    override fun navigate(e: MouseEvent?, element: T?) {
        if (e == null || element == null) return
        val targets = targetsProvider()
        val renderer = PsiTargetPresentationRenderer<T>()
        val findUsagesTitle = findUsagesTitle
        val title = title + "  " + ChronicleBundle.message("lineMarker.N.found", targets.size)
        PsiTargetNavigator { targets }
            .presentationProvider(renderer)
            .tabTitle(findUsagesTitle)
            .navigate(e, title, project)
    }
}
