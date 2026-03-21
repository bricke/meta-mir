# Enable native build so mir-wayland-generator-native can use glibmm-native
BBCLASSEXTEND = "native"

# Ensure native variant resolves sigc++-2.0 as native too
DEPENDS:class-native = "libsigc++-2.0-native glib-2.0-native mm-common-native"
