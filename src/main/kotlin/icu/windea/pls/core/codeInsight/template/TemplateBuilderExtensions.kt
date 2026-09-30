package icu.windea.pls.core.codeInsight.template

import com.intellij.codeInsight.template.TemplateBuilder
import com.intellij.codeInsight.template.TemplateBuilderImpl
import icu.windea.pls.core.cast

fun TemplateBuilder.buildTemplate() = cast<TemplateBuilderImpl>().buildTemplate()

fun TemplateBuilder.buildInlineTemplate() = cast<TemplateBuilderImpl>().buildInlineTemplate()
