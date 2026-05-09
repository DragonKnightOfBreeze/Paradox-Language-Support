package icu.windea.pls.extensions.json

import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.extension.SchemaType
import icu.windea.pls.extensions.PlsExtensionsBundle

class ParadoxLauncherSettingsJsonSchemaProvider : JsonSchemaFileProvider {
    override fun getName() = PlsExtensionsBundle.message("json.schema.launcherSettings.name")

    override fun isAvailable(file: VirtualFile) = JsonExtensionManager.isLauncherSettingsJson(file)

    override fun getSchemaFile() = JsonSchemaProviderFactory.getResourceFile(javaClass, JsonExtensionManager.getLauncherSettingsJsonSchemaPath())

    override fun getSchemaType() = SchemaType.schema
}
