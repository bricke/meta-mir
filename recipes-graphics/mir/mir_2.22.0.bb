SUMMARY = "Mir display server and compositor framework"
DESCRIPTION = "Mir is a set of shared libraries for building Wayland compositors"
HOMEPAGE = "https://github.com/canonical/mir"
LICENSE = "GPL-2.0-only & LGPL-2.1-only"
LIC_FILES_CHKSUM = " \
    file://COPYING.GPL2;md5=b234ee4d69f5fce4486a80fdaf4a4263 \
    file://COPYING.LGPL2;md5=5f30f0716dfdd0d91eb439ebec522ec2 \
"

SRC_URI = "git://github.com/canonical/mir.git;protocol=https;branch=main"
SRCREV = "a73013b3f287b96b89885945e7b2461334f47363"
S = "${WORKDIR}/git"

inherit cmake pkgconfig

DEPENDS = " \
    boost \
    protobuf \
    protobuf-native \
    python3-pillow-native \
    glib-2.0 \
    libepoxy \
    libinput \
    libxkbcommon \
    libdrm \
    mesa \
    wayland \
    wayland-native \
    wayland-protocols \
    freetype \
    libxml++-2.6 \
    yaml-cpp \
    lttng-ust \
    libxcb \
    libx11 \
    libxcursor \
    glm \
    systemd \
    libdisplay-info \
    pixman \
"

EXTRA_OECMAKE = " \
    -DMIR_PLATFORM=gbm-kms \
    -DMIR_ENABLE_TESTS=OFF \
    -DMIR_ENABLE_WLCS_TESTS=OFF \
    -DMIR_USE_PREBUILT_GOOGLETEST=OFF \
    -DProtobuf_PROTOC_EXECUTABLE=${STAGING_BINDIR_NATIVE}/protoc \
"

PACKAGES =+ "${PN}-graphics-drivers-gbm-kms"

FILES:${PN}-graphics-drivers-gbm-kms = " \
    ${libdir}/mir/server-platform/graphics-gbm-kms.so* \
    ${libdir}/mir/server-platform/input-evdev.so* \
"

FILES:${PN}-dev += " \
    ${libdir}/pkgconfig/*.pc \
    ${includedir}/mir* \
    ${includedir}/miral* \
"

RDEPENDS:${PN}-graphics-drivers-gbm-kms = "${PN}"
