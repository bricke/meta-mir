SUMMARY = "Mir display server and compositor framework"
DESCRIPTION = "Mir is a set of shared libraries for building Wayland compositors"
HOMEPAGE = "https://github.com/canonical/mir"
LICENSE = "GPL-2.0-only & LGPL-2.1-only"
LIC_FILES_CHKSUM = " \
    file://COPYING.GPL2;md5=b234ee4d69f5fce4486a80fdaf4a4263 \
    file://COPYING.LGPL2;md5=5f30f0716dfdd0d91eb439ebec522ec2 \
"

SRC_URI = "git://github.com/canonical/mir.git;protocol=https;branch=main \
           file://0001-make-examples-optional.patch \
           file://0002-allow-external-wayland-generator.patch \
           file://0003-skip-generator-build-when-external.patch \
"
SRCREV = "a73013b3f287b96b89885945e7b2461334f47363"
S = "${WORKDIR}/git"

inherit cmake pkgconfig


DEPENDS = " \
    boost \
    protobuf \
    protobuf-native \
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

# libxml++-2.6 headers use std::auto_ptr which is deprecated in C++17/23.
# Suppress the warning to avoid -Werror turning it into a build failure.
CXXFLAGS:append = " -Wno-deprecated-declarations"

EXTRA_OECMAKE = " \
    -DMIR_WAYLAND_GENERATOR_EXECUTABLE=${B}/bin/mir_wayland_generator \
    -DMIR_PLATFORM=gbm-kms \
    -DMIR_ENABLE_TESTS=OFF \
    -DMIR_ENABLE_WLCS_TESTS=OFF \
    -DMIR_ENABLE_EXAMPLES=OFF \
    -DMIR_USE_PREBUILT_GOOGLETEST=OFF \
    -DProtobuf_PROTOC_EXECUTABLE=${STAGING_BINDIR_NATIVE}/protoc \
"

# Build mir_wayland_generator using the host compiler before cross-compiling.
# Pre-generate lttng tracepoint files so ninja does not need lttng-gen-tp in PATH.
# Host requirements: sudo apt install libxml++2.6-dev liblttng-ust-dev
do_compile:prepend() {
    mkdir -p ${B}/bin
    gen_src="${S}/src/wayland/generator"
    ${BUILD_CXX} -std=c++17 \
        $(pkg-config --cflags libxml++-2.6) \
        ${gen_src}/wrapper_generator.cpp \
        ${gen_src}/utils.cpp \
        ${gen_src}/enum.cpp \
        ${gen_src}/argument.cpp \
        ${gen_src}/method.cpp \
        ${gen_src}/request.cpp \
        ${gen_src}/event.cpp \
        ${gen_src}/interface.cpp \
        ${gen_src}/global.cpp \
        ${gen_src}/emitter.cpp \
        -o ${B}/bin/mir_wayland_generator \
        $(pkg-config --libs libxml++-2.6)

    # Pre-generate lttng tracepoint .h/.c files using the full host path.
    # CMake add_custom_command calls lttng-gen-tp without a full path; ninja's
    # sanitized shell may not find it even with HOSTTOOLS. Pre-generating the
    # outputs here causes ninja to skip the generation step (outputs already exist).
    find ${S} -name "*.tp" | while read tp_file; do
        rel=$(realpath --relative-to="${S}" "${tp_file}")
        out_dir="${B}/$(dirname ${rel})"
        mkdir -p "${out_dir}"
        /usr/bin/lttng-gen-tp "${tp_file}" \
            -o "${out_dir}/$(basename ${tp_file}).h" \
            -o "${out_dir}/$(basename ${tp_file}).c"
    done
}

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
