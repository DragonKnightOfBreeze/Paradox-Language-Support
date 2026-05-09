package icu.windea.pls.extensions.json

import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.extension.SchemaType
import icu.windea.pls.extensions.PlsExtensionsBundle

class ParadoxMetadataJsonSchemaProvider : JsonSchemaFileProvider {
    override fun getName() = PlsExtensionsBundle.message("json.schema.metadata.name")

    override fun isAvailable(file: VirtualFile) = JsonExtensionManager.isMetadataJson(file)

    override fun getSchemaFile() = JsonSchemaProviderFactory.getResourceFile(javaClass, JsonExtensionManager.getMetadataJsonSchemaPath())

    override fun getSchemaType() = SchemaType.schema
}
