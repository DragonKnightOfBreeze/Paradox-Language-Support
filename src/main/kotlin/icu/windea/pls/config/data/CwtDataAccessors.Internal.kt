package icu.windea.pls.config.data

import icu.windea.pls.config.config.internal.CwtFoldingSettingsConfig
import icu.windea.pls.config.config.internal.CwtPostfixTemplateSettingsConfig
import icu.windea.pls.config.config.internal.CwtSchemaConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate

val CwtConfigGroup.schemas: MutableList<CwtSchemaConfig>
    by createKey(CwtConfigGroup.Keys) { mutableListOf() }

val CwtConfigGroup.foldingSettings: MutableMap<String, MutableMap<@CaseInsensitive String, CwtFoldingSettingsConfig>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }

val CwtConfigGroup.postfixTemplateSettings: MutableMap<String, MutableMap<@CaseInsensitive String, CwtPostfixTemplateSettingsConfig>>
    by createKey(CwtConfigGroup.Keys) { mutableMapOf() }
