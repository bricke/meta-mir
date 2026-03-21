SUMMARY = "Mir Wayland protocol wrapper generator (native build tool)"
DESCRIPTION = "Builds mir_wayland_generator for the host — used during cross-compilation of Mir"
HOMEPAGE = "https://github.com/canonical/mir"
LICENSE = "GPL-2.0-only & LGPL-2.1-only"
LIC_FILES_CHKSUM = " \
    file://COPYING.GPL2;md5=b234ee4d69f5fce4486a80fdaf4a4263 \
    file://COPYING.LGPL2;md5=5f30f0716dfdd0d91eb439ebec522ec2 \
"

SRC_URI = "git://github.com/canonical/mir.git;protocol=https;branch=main"
SRCREV = "a73013b3f287b96b89885945e7b2461334f47363"
S = "${WORKDIR}/git"

inherit cmake pkgconfig native

DEPENDS = " \
    libxml++-2.6-native \
    libxml2-native \
    glibmm-native \
    boost-native \
    protobuf-native \
"

# Build only the wayland generator tool, nothing else
EXTRA_OECMAKE = " \
    -DMIR_PLATFORM='' \
    -DMIR_ENABLE_TESTS=OFF \
    -DMIR_ENABLE_EXAMPLES=OFF \
    -DMIR_ENABLE_WLCS_TESTS=OFF \
    -DMIR_USE_PREBUILT_GOOGLETEST=OFF \
    -DProtobuf_PROTOC_EXECUTABLE=${STAGING_BINDIR_NATIVE}/protoc \
"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/bin/mir_wayland_generator ${D}${bindir}/mir_wayland_generator
}
