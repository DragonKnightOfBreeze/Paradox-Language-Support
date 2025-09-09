package icu.windea.pls.images.dds.info

import icu.windea.pls.images.dds.info.DdsConstants.make4CC

/**
 * 定义各种类型的图面格式。
 *
 * 参见：[D3DFORMAT (D3D9Types.h) - Win32 apps | Microsoft Learn](https://learn.microsoft.com/windows/win32/direct3d9/d3dformat)
 */
enum class D3dFormat(val value: Int) {
    UNKNOWN(0),
    R8G8B8(20),
    A8R8G8B8(21),
    X8R8G8B8(22),
    R5G6B5(23),
    X1R5G5B5(24),
    A1R5G5B5(25),
    A4R4G4B4(26),
    R3G3B2(27),
    A8(28),
    A8R3G3B2(29),
    X4R4G4B4(30),
    A2B10G10R10(31),
    A8B8G8R8(32),
    X8B8G8R8(33),
    G16R16(34),
    A2R10G10B10(35),
    A16B16G16R16(36),
    A8P8(40),
    P8(41),
    L8(50),
    A8L8(51),
    A4L4(52),
    V8U8(60),
    L6V5U5(61),
    X8L8V8U8(62),
    Q8W8V8U8(63),
    V16U16(64),
    A2W10V10U10(67),
    UYVY(make4CC("UYVY")),
    R8G8_B8G8(make4CC("RGBG")),
    YUY2(make4CC("YUY2")),
    G8R8_G8B8(make4CC("GRGB")),
    DXT1(make4CC("DXT1")),
    DXT2(make4CC("DXT2")),
    DXT3(make4CC("DXT3")),
    DXT4(make4CC("DXT4")),
    DXT5(make4CC("DXT5")),
    D16_LOCKABLE(70),
    D32(71),
    D15S1(73),
    D24S8(75),
    D24X8(77),
    D24X4S4(79),
    D16(80),
    L16(81),
    D32F_LOCKABLE(82),
    D24FS8(83),
    D32_LOCKABLE(84),
    S8_LOCKABLE(85),
    VERTEXDATA(100),
    INDEX16(101),
    INDEX32(102),
    Q16W16V16U16(110),
    MULTI2_ARGB8(make4CC("MET1")),
    R16F(111),
    G16R16F(112),
    A16B16G16R16F(113),
    R32F(114),
    G32R32F(115),
    A32B32G32R32F(116),
    CxV8U8(117),
    A1(118),
    A2B10G10R10_XR_BIAS(119),
    BINARYBUFFER(199),
    FORCE_DWORD(-1),
    ;

    companion object {
        @JvmStatic
        val map = entries.associateBy { it.value }

        @JvmStatic
        fun get(value: Int) = map.get(value) ?: throw NoSuchElementException("unknown d3d format from value $value")
    }
}
