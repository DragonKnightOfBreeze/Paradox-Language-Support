package icu.windea.pls.lang.resolve

import com.intellij.psi.PsiFile
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.CwtComplexEnumConfigMatchContext
import icu.windea.pls.lang.match.ParadoxConfigMatchService
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.model.ParadoxComplexEnumValueInfo
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

object ParadoxComplexEnumValueService {
    fun resolveInfo(element: ParadoxScriptStringExpressionElement, file: PsiFile): ParadoxComplexEnumValueInfo? {
        val name = element.value
        if (name.isParameterized()) return null // 排除可能带参数的情况
        val project = file.project
        val fileInfo = file.fileInfo ?: return null
        val path = fileInfo.path
        val gameType = fileInfo.rootInfo.gameType
        if (ParadoxInlineScriptManager.isMatched(name, gameType)) return null // 排除是内联脚本用法的情况
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val matchContext = CwtComplexEnumConfigMatchContext(configGroup, path)
        val config = ParadoxConfigMatchService.getMatchedComplexEnumConfig(matchContext, element) ?: return null
        val enumName = config.name
        return ParadoxComplexEnumValueInfo(name, enumName, config)
    }
}
