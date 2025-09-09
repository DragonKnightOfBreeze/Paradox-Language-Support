package icu.windea.pls.images.dds.info

/**
 * Surface 像素格式。
 *
 * 参见：[(Dds.h) DDS_PIXELFORMAT 结构 - Win32 apps | Microsoft Learn](https://learn.microsoft.com/windows/win32/direct3ddds/dds-pixelformat)
 */
data class DdsPixelFormat(
    val dwSize: Int,
    val dwFlags: Int,
    val dwFourCC: Int,
    val dwRGBBitCount: Int,
    val dwRBitMask: Int,
    val dwGBitMask: Int,
    val dwBBitMask: Int,
    val dwABitMask: Int,
) {
    fun shouldLoadHeader10(): Boolean {
        return dwFlags == DdsConstants.DDPF_FOURCC && dwFourCC == DdsConstants.DDS_DX10
    }

    fun d3dFormat(): D3dFormat {
        return when {
            (dwFlags and DdsConstants.DDS_RGBA) == DdsConstants.DDS_RGBA -> {
                when (dwRGBBitCount) {
                    16 -> when {
                        dwRBitMask == 0x7c00 && dwGBitMask == 0x3e0 && dwBBitMask == 0x1f && dwABitMask == 0x8000 -> D3dFormat.A1R5G5B5
                        dwRBitMask == 0xf00 && dwGBitMask == 0xf0 && dwBBitMask == 0xf && dwABitMask == 0xf000 -> D3dFormat.A4R4G4B4
                        dwRBitMask == 0xe0 && dwGBitMask == 0x1c && dwBBitMask == 0x3 && dwABitMask == 0xff00 -> D3dFormat.A8R3G3B2
                        else -> D3dFormat.UNKNOWN
                    }
                    32 -> when {
                        dwRBitMask == 0xff && dwGBitMask == 0xff00 && dwBBitMask == 0xff0000 && dwABitMask == -0x1000000 -> D3dFormat.A8B8G8R8
                        dwRBitMask == 0xffff && dwGBitMask == -0x10000 -> D3dFormat.G16R16
                        dwRBitMask == 0x3ff && dwGBitMask == 0xffc00 && dwBBitMask == 0x3ff00000 -> D3dFormat.A2B10G10R10
                        dwRBitMask == 0xff0000 && dwGBitMask == 0xff00 && dwBBitMask == 0xff && dwABitMask == -0x1000000 -> D3dFormat.A8R8G8B8
                        dwRBitMask == 0x3ff00000 && dwGBitMask == 0xffc00 && dwBBitMask == 0x3ff && dwABitMask == -0x40000000 -> D3dFormat.A2R10G10B10
                        else -> D3dFormat.UNKNOWN
                    }
                    else -> D3dFormat.UNKNOWN
                }
            }
            (dwFlags and DdsConstants.DDPF_RGB) == DdsConstants.DDPF_RGB -> {
                when (dwRGBBitCount) {
                    16 -> when {
                        dwRBitMask == 0xf800 && dwGBitMask == 0x7e0 && dwBBitMask == 0x1f -> D3dFormat.R5G6B5
                        dwRBitMask == 0x7c00 && dwGBitMask == 0x3e0 && dwBBitMask == 0x1f -> D3dFormat.X1R5G5B5
                        dwRBitMask == 0xf00 && dwGBitMask == 0xf0 && dwBBitMask == 0xf -> D3dFormat.X4R4G4B4
                        else -> D3dFormat.UNKNOWN
                    }
                    24 -> when {
                        dwRBitMask == 0xff0000 && dwGBitMask == 0xff00 && dwBBitMask == 0xff -> D3dFormat.R8G8B8
                        else -> D3dFormat.UNKNOWN
                    }
                    32 -> when {
                        dwRBitMask == 0xffff && dwGBitMask == -0x10000 -> D3dFormat.G16R16
                        dwRBitMask == 0xff0000 && dwGBitMask == 0xff00 && dwBBitMask == 0xff -> D3dFormat.X8R8G8B8
                        dwRBitMask == 0xff && dwGBitMask == 0xff00 && dwBBitMask == 0xff0000 -> D3dFormat.X8B8G8R8
                        else -> D3dFormat.UNKNOWN
                    }
                    else -> D3dFormat.UNKNOWN
                }
            }
            (dwFlags and DdsConstants.DDPF_ALPHA) == DdsConstants.DDPF_ALPHA -> {
                when (dwRGBBitCount) {
                    8 -> when (dwABitMask) {
                        0xff -> D3dFormat.A8
                        else -> D3dFormat.UNKNOWN
                    }
                    else -> D3dFormat.UNKNOWN
                }
            }
            (dwFlags and DdsConstants.DDPF_LUMINANCE) == DdsConstants.DDPF_LUMINANCE -> {
                when (dwRGBBitCount) {
                    8 -> when {
                        dwRBitMask == 0xf && dwABitMask == 0xf0 -> D3dFormat.A4L4
                        dwRBitMask == 0xff -> D3dFormat.L8
                        else -> D3dFormat.UNKNOWN
                    }
                    16 -> when {
                        dwRBitMask == 0xff && dwABitMask == 0xff00 -> D3dFormat.A8L8
                        dwRBitMask == 0xffff -> D3dFormat.L16
                        else -> D3dFormat.UNKNOWN
                    }
                    else -> D3dFormat.UNKNOWN
                }
            }
            (dwFlags and DdsConstants.DDPF_FOURCC) == DdsConstants.DDPF_FOURCC -> {
                try {
                    D3dFormat.get(dwFourCC)
                } catch (_: NoSuchElementException) {
                    D3dFormat.UNKNOWN
                }
            }
            else -> D3dFormat.UNKNOWN
        }
    }
}
