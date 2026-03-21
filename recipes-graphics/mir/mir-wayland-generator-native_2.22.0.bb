SUMMARY = "Mir Wayland protocol wrapper generator (native build tool)"
DESCRIPTION = "Builds mir_wayland_generator for the host — used during cross-compilation of Mir"
HOMEPAGE = "https://github.com/canonical/mir"
LICENSE = "GPL-2.0-only & LGPL-2.1-only"
LIC_FILES_CHKSUM = " \
    file://COPYING.GPL2;md5=b234ee4d69f5fce4486a80fdaf4a4263 \
    file://COPYING.LGPL2;md5=5f30f0716dfdd0d91eb439ebec522ec2 \
"

SRC_URI = "git://github.com/canonical/mir.git;protocol=https;branch=main \
           file://0001-make-examples-optional.patch \
           file://0002-allow-external-wayland-generator.patch \
"
SRCREV = "a73013b3f287b96b89885945e7b2461334f47363"
S = "${WORKDIR}/git"

inherit cmake pkgconfig native

FILESEXTRAPATHS:prepend := "${THISDIR}/mir:"

DEPENDS = " \
    libxml++-2.6-native \
    boost-native \
    protobuf-native \
"

EXTRA_OECMAKE = " \
    -DMIR_PLATFORM='' \
    -DMIR_ENABLE_TESTS=OFF \
    -DMIR_ENABLE_EXAMPLES=OFF \
    -DMIR_ENABLE_WLCS_TESTS=OFF \
    -DMIR_USE_PREBUILT_GOOGLETEST=OFF \
    -DProtobuf_PROTOC_EXECUTABLE=${STAGING_BINDIR_NATIVE}/protoc \
"

EXTRA_OECMAKE:append = " -DMIR_WAYLAND_GENERATOR_EXECUTABLE=${B}/bin/mir_wayland_generator"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${B}/bin/mir_wayland_generator ${D}${bindir}/mir_wayland_generator
}
