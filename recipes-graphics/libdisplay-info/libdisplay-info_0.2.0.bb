SUMMARY = "EDID and DisplayID library"
DESCRIPTION = "A library for parsing display EDID and DisplayID information"
HOMEPAGE = "https://gitlab.freedesktop.org/emersion/libdisplay-info"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=e4426409957080ee0352128354cea2de"

SRC_URI = "https://gitlab.freedesktop.org/emersion/libdisplay-info/-/archive/${PV}/libdisplay-info-${PV}.tar.gz"
SRC_URI[sha256sum] = "f7331fcaf5527251b84c8fb84238d06cd2f458422ce950c80e86c72927aa8c2b"

S = "${WORKDIR}/libdisplay-info-${PV}"

inherit meson pkgconfig
