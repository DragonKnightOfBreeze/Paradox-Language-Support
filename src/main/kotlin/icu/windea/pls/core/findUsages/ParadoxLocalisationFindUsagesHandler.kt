package icu.windea.pls.core.findUsages

import com.intellij.find.findUsages.*
import com.intellij.openapi.actionSystem.*
import com.intellij.psi.*
import com.intellij.usageView.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationFindUsagesHandler(
    private val element: ParadoxLocalisationProperty,
    private val factory: ParadoxFindUsagesHandlerFactory
) : ParadoxFindUsagesHandler(element, factory) {
    val locale by lazy { element.localeConfig }
    
    override fun getFindUsagesDialog(isSingleFile: Boolean, toShowInNewTab: Boolean, mustOpenInNewTab: Boolean): AbstractFindUsagesDialog {
        return ParadoxFindLocalisationUsagesDialog(element, project, factory.findLocalisationOptions, toShowInNewTab, mustOpenInNewTab, isSingleFile, this)
    }
    
    override fun getFindUsagesOptions(dataContext: DataContext?): ParadoxLocalisationFindUsagesOptions {
        return factory.findLocalisationOptions
    }
    
    override fun processElementUsages(element: PsiElement, processor: Processor<in UsageInfo>, options: FindUsagesOptions): Boolean {
        options as ParadoxLocalisationFindUsagesOptions
        val finalProcessor = Processor<UsageInfo> p@{
            //如果不跨语言区域，忽略不同语言区域的本地化文件中的引用
            if(!options.isCrossLocales && locale != null) {
                val refElement = it.element
                if(refElement != null && refElement.language == ParadoxLocalisationLanguage) {
                    if(locale != it.file?.localeConfig) return@p true
                }
            }
            processor.process(it)
        }
        return super.processElementUsages(element, finalProcessor, options)
    }
}