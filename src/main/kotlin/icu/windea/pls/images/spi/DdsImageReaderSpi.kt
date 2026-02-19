package icu.windea.pls.images.spi

import com.twelvemonkeys.imageio.plugins.dds.DDSImageReaderSpi
import icu.windea.pls.inject.injectors.DDSImageReaderCodeInjector

/**
 * @see DDSImageReaderCodeInjector
 */
class DdsImageReaderSpi : DelegatedImageReaderSpi(DDSImageReaderSpi())
