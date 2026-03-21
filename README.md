# meta-mir

Yocto layer providing [Canonical Mir](https://github.com/canonical/mir) — a set of shared libraries for building Wayland compositors.

## Layer dependencies

| Layer | Purpose |
|-------|---------|
| `poky/meta` (oe-core) | Base layer |
| `meta-openembedded/meta-oe` | yaml-cpp, lttng-ust, glm, glibmm, libsigc++ |
| `meta-openembedded/meta-python` | python3-pillow (if examples are enabled) |

## Build machine requirements

Tested on **Ubuntu 24.04 LTS**. Required host packages:

```bash
sudo apt install -y \
    gawk wget git diffstat unzip texinfo gcc build-essential \
    chrpath socat cpio python3 python3-pip python3-pexpect \
    xz-utils debianutils iputils-ping python3-git python3-jinja2 \
    python3-subunit zstd liblz4-tool file locales libacl1 \
    python3-distutils-extra
```

### Ubuntu 24.04 AppArmor fix

Yocto requires unprivileged user namespaces, which Ubuntu 24.04 restricts by default:

```bash
# Apply immediately
sudo sysctl -w kernel.apparmor_restrict_unprivileged_userns=0

# Persist across reboots
echo 'kernel.apparmor_restrict_unprivileged_userns=0' | sudo tee /etc/sysctl.d/99-yocto.conf
```

### `local.conf` requirements

`systemd` requires `usrmerge` in Scarthgap. Add to `build/conf/local.conf`:

```
DISTRO_FEATURES:append = " systemd usrmerge"
VIRTUAL-RUNTIME_init_manager = "systemd"
```

## Usage

Add to `bblayers.conf`:

```
BBLAYERS += " \
    /path/to/meta-openembedded/meta-oe \
    /path/to/meta-openembedded/meta-python \
    /path/to/meta-mir \
"
```

Build:

```bash
bitbake mir
```

## Provided recipes

| Recipe | Description |
|--------|-------------|
| `mir` | Mir display server libraries and Miral toolkit (cross-compiled for target) |
| `mir-wayland-generator-native` | Wayland protocol wrapper generator (built for host, used during cross-compilation) |
| `libxml++-2.6` | C++ XML library 2.6 API series (required by Mir's wayland generator) |
| `libdisplay-info` | EDID/DisplayID parsing library (required by Mir platform) |

## Packages

| Package | Contents |
|---------|----------|
| `mir` | Runtime shared libraries |
| `mir-dev` | Headers and pkg-config files |
| `mir-graphics-drivers-gbm-kms` | KMS/DRM platform plugin |

## Notes

- Builds only the `gbm-kms` platform — no X11, no Wayland nesting, no NVIDIA egl-wayland
- Tests and examples disabled
- `mir_wayland_generator` is built natively (x86_64) to avoid cross-compilation exec format errors
- `libxml++-2.6` uses `std::auto_ptr` which is deprecated in C++23 — `-Wno-deprecated-declarations` is applied to the Mir build to suppress the `-Werror` failure
