package icu.windea.pls.extensions.json

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory

class ParadoxJsonSchemaProviderFactory : JsonSchemaProviderFactory, DumbAware {
    private val defaultProviders = listOf(
        ParadoxLauncherSettingsJsonSchemaProvider(),
        ParadoxMetadataJsonSchemaProvider(),
    )

    override fun getProviders(project: Project) = defaultProviders
}
