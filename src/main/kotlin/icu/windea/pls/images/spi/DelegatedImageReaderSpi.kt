package icu.windea.pls.images.spi

import java.util.*
import javax.imageio.*
import javax.imageio.spi.*

open class DelegatedImageReaderSpi(
    val delegate: ImageReaderSpi
) : ImageReaderSpi() {
    override fun getDescription(locale: Locale?): String = delegate.getDescription(locale)
    override fun onRegistration(registry: ServiceRegistry?, category: Class<*>?) = delegate.onRegistration(registry, category)
    override fun onDeregistration(registry: ServiceRegistry?, category: Class<*>?) = delegate.onDeregistration(registry, category)
    override fun canDecodeInput(source: Any?): Boolean = delegate.canDecodeInput(source)
    override fun createReaderInstance(extension: Any?): ImageReader = delegate.createReaderInstance(extension)
    override fun createReaderInstance(): ImageReader = delegate.createReaderInstance()
    override fun getVendorName(): String = delegate.vendorName
    override fun getVersion(): String = delegate.version
    override fun getFormatNames(): Array<String> = delegate.formatNames
    override fun getFileSuffixes(): Array<String>? = delegate.fileSuffixes
    override fun getMIMETypes(): Array<String>? = delegate.mimeTypes
    override fun getPluginClassName(): String = delegate.pluginClassName
    override fun getInputTypes(): Array<Class<*>> = delegate.inputTypes
    override fun isStandardStreamMetadataFormatSupported(): Boolean = delegate.isStandardStreamMetadataFormatSupported
    override fun isStandardImageMetadataFormatSupported(): Boolean = delegate.isStandardImageMetadataFormatSupported
    override fun getNativeStreamMetadataFormatName(): String? = delegate.nativeStreamMetadataFormatName
    override fun getNativeImageMetadataFormatName(): String? = delegate.nativeImageMetadataFormatName
    override fun getExtraStreamMetadataFormatNames(): Array<String>? = delegate.extraStreamMetadataFormatNames
    override fun getExtraImageMetadataFormatNames(): Array<String>? = delegate.extraImageMetadataFormatNames
    override fun isOwnReader(reader: ImageReader?): Boolean = delegate.isOwnReader(reader)
}
