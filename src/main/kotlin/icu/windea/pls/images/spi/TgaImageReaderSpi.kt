package icu.windea.pls.images.spi

import com.twelvemonkeys.imageio.plugins.tga.TGAImageReaderSpi
import icu.windea.pls.inject.injectors.TGAImageReaderCodeInjector

/**
 * @see TGAImageReaderCodeInjector
 */
class TgaImageReaderSpi : DelegatedImageReaderSpi(TGAImageReaderSpi())
