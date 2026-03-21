SUMMARY = "C++ wrapper for libxml library (2.6 API)"
DESCRIPTION = "C++ wrapper for libxml library, 2.6 API series"
HOMEPAGE = "http://libxmlplusplus.sourceforge.net"
SECTION = "libs"
LICENSE = "LGPL-2.1-or-later"
LIC_FILES_CHKSUM = "file://COPYING;md5=7fbc338309ac38fefcd64b04bb903e34"

DEPENDS = "libxml2 glibmm"

GNOMEBN = "libxml++"
inherit gnomebase

S = "${WORKDIR}/libxml++-${PV}"

SRC_URI[archive.sha256sum] = "74b95302e24dbebc56e97048e86ad0a4121fc82a43e58d381fbe1d380e8eba04"

FILES:${PN}-dev += "${libdir}/libxml++-2.6/include/libxml++config.h"

BBCLASSEXTEND = "native"
