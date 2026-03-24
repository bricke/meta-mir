SUMMARY = "Mir display server and compositor framework"
DESCRIPTION = "Mir is a set of shared libraries for building Wayland compositors"
HOMEPAGE = "https://github.com/canonical/mir"
LICENSE = "GPL-2.0-only & LGPL-2.1-only"
LIC_FILES_CHKSUM = " \
    file://COPYING.GPL2;md5=570a9b3749dd0463a1778803b12a6dce \
    file://COPYING.LGPL2;md5=9ec28527f3d544b51ceb0e1907d0bf3f \
"

SRC_URI = "git://github.com/canonical/mir.git;protocol=https;branch=main \
           file://0001-make-examples-optional.patch \
           file://0002-allow-external-wayland-generator.patch \
           file://0003-skip-generator-build-when-external.patch \
           file://libxmlpp_compat.h \
           file://lttng-gen-tp \
"
SRCREV = "067796760870c314d4e4da42d22bedefdbb3129e"
S = "${WORKDIR}/git"

inherit cmake pkgconfig


DEPENDS = " \
    boost \
    protobuf \
    protobuf-native \
    glib-2.0 \
    glib-2.0-native \
    libepoxy \
    libinput \
    libxkbcommon \
    libdrm \
    mesa \
    wayland \
    wayland-native \
    wayland-protocols \
    freetype \
    yaml-cpp \
    lttng-ust \
    ${@bb.utils.contains('DISTRO_FEATURES', 'x11', 'libxcb libx11 libxcursor', '', d)} \
    glm \
    systemd \
    libdisplay-info \
    pixman \
    gmp \
    libxml2-native \
"


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
# No manual host packages required — all build tools come from Yocto native recipes.
do_compile:prepend() {
    mkdir -p ${B}/bin
    gen_src="${S}/src/wayland/generator"

    # Provide libxmlpp_compat.h as a drop-in for <libxml++/libxml++.h> so the
    # generator compiles against libxml2-native with no host libxml++2.6-dev.
    mkdir -p ${B}/libxml_compat_include/libxml++
    cp ${WORKDIR}/libxmlpp_compat.h ${B}/libxml_compat_include/libxml++/libxml++.h

    ${BUILD_CXX} -std=c++17 \
        -I${B}/libxml_compat_include \
        -I${STAGING_INCDIR_NATIVE}/libxml2 \
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
        -L${STAGING_LIBDIR_NATIVE} \
        -Wl,-rpath,${STAGING_LIBDIR_NATIVE} \
        -lxml2

    # Pre-generate lttng tracepoint .h/.c files using the full host path.
    # CMake add_custom_command calls lttng-gen-tp without a full path; ninja's
    # sanitized shell may not find it even with HOSTTOOLS. Pre-generating the
    # outputs here causes ninja to skip the generation step (outputs already exist).
    find ${S} -name "*.tp" | while read tp_file; do
        rel=$(realpath --relative-to="${S}" "${tp_file}")
        out_dir="${B}/$(dirname ${rel})"
        mkdir -p "${out_dir}"
        python3 ${WORKDIR}/lttng-gen-tp "${tp_file}" \
            -o "${out_dir}/$(basename ${tp_file}).h" \
            -o "${out_dir}/$(basename ${tp_file}).c"
    done

    # Pre-generate gdbus proxy files from D-Bus XML interfaces.
    # gdbus-codegen is called by CMake without a full path; use the native sysroot tool.
    gdbus="${STAGING_BINDIR_NATIVE}/gdbus-codegen"
    console_src="${S}/src/server/console"
    console_out="${B}/src/server/console"
    mkdir -p "${console_out}"
    "${gdbus}" --interface-prefix org.freedesktop.login1 --c-namespace Logind \
        --header --output "${console_out}/logind-seat.h" \
        "${console_src}/logind-seat.xml"
    "${gdbus}" --interface-prefix org.freedesktop.login1 --c-namespace Logind \
        --body --output "${console_out}/logind-seat.c" \
        "${console_src}/logind-seat.xml"
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
