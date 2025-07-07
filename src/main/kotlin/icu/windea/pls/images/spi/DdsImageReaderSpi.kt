package icu.windea.pls.images.spi

import com.twelvemonkeys.imageio.plugins.dds.*

/**
 * @see icu.windea.pls.inject.injectors.DDSImageReaderCodeInjector
 */
class DdsImageReaderSpi : DelegatedImageReaderSpi(DDSImageReaderSpi())
