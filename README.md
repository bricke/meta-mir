# meta-mir

Yocto layer providing [Canonical Mir](https://github.com/canonical/mir) — a set of shared libraries for building Wayland compositors.

Tested on **Yocto Scarthgap (5.0 LTS)**, targeting **Raspberry Pi 4 (aarch64)**, built on **Ubuntu 24.04 LTS**.

## Layer dependencies

| Layer | Purpose |
|-------|---------|
| `poky/meta` (oe-core) | Base layer |
| `meta-openembedded/meta-oe` | yaml-cpp, lttng-ust, glm, glibmm, libsigc++ |
| `meta-openembedded/meta-python` | python3-pillow (if examples are enabled) |
| `meta-openembedded/meta-networking` | Optional networking support |

## Provided recipes

| Recipe | Description |
|--------|-------------|
| `mir` | Mir display server libraries and Miral toolkit (cross-compiled for target) |
| `libxml++-2.6` | C++ XML library 2.6 API series (required by Mir's Wayland generator) |
| `libdisplay-info` | EDID/DisplayID parsing library (required by Mir's gbm-kms platform) |

## Packages

| Package | Contents |
|---------|----------|
| `mir` | Runtime shared libraries |
| `mir-dev` | Headers and pkg-config files |
| `mir-graphics-drivers-gbm-kms` | KMS/DRM platform plugin and evdev input plugin |

## Setup

### 1. Install host dependencies

```bash
sudo apt update && sudo apt install -y \
    gawk wget git diffstat unzip texinfo gcc build-essential \
    chrpath socat cpio python3 python3-pip python3-pexpect \
    xz-utils debianutils iputils-ping python3-git python3-jinja2 \
    python3-subunit zstd liblz4-tool file locales libacl1 \
    python3-distutils-extra \
    libxml++2.6-dev \
    lttng-tools \
    liblttng-ust-dev
```

- `libxml++2.6-dev` — required to build `mir_wayland_generator` on the host (x86_64) before cross-compilation
- `lttng-tools` — provides the `lttng` CLI (runtime dependency)
- `liblttng-ust-dev` — provides `/usr/bin/lttng-gen-tp`, the tracepoint code generator

### 2. Fix AppArmor user namespace restriction

Ubuntu 24.04 restricts unprivileged user namespaces by default, which breaks Yocto's sandbox:

```bash
sudo sysctl -w kernel.apparmor_restrict_unprivileged_userns=0
echo 'kernel.apparmor_restrict_unprivileged_userns=0' | sudo tee /etc/sysctl.d/99-yocto.conf
```

### 3. Clone layers

```bash
mkdir -p ~/workspace/yocto && cd ~/workspace/yocto
git clone --depth=1 -b scarthgap https://github.com/yoctoproject/poky.git
git clone --depth=1 -b scarthgap https://github.com/openembedded/meta-openembedded.git
git clone --depth=1 -b scarthgap https://github.com/bricke/meta-mir.git
```

### 4. Initialize the build environment

```bash
cd ~/workspace/yocto
source poky/oe-init-build-env build
```

### 5. Configure `bblayers.conf`

```
BBLAYERS += " \
    /home/<user>/workspace/yocto/poky/meta \
    /home/<user>/workspace/yocto/poky/meta-poky \
    /home/<user>/workspace/yocto/meta-openembedded/meta-oe \
    /home/<user>/workspace/yocto/meta-openembedded/meta-python \
    /home/<user>/workspace/yocto/meta-openembedded/meta-networking \
    /home/<user>/workspace/yocto/meta-mir \
"
```

### 6. Configure `local.conf`

`systemd` requires `usrmerge` in Scarthgap. Add to `build/conf/local.conf`:

```
DISTRO_FEATURES:append = " systemd usrmerge"
VIRTUAL-RUNTIME_init_manager = "systemd"
```

### 7. Build

```bash
bitbake mir
```

## Notes

### Build platform

- Builds only the `gbm-kms` platform — no X11, no Wayland nesting, no NVIDIA egl-wayland
- Tests and examples are disabled

### Cross-compilation workarounds

Several of Mir's build steps require host-architecture tools to generate source files before the cross-compiler runs. Bitbake's sanitized task environment does not reliably expose these tools to ninja subprocesses even when declared via `HOSTTOOLS`, so the recipe pre-generates all outputs in `do_compile:prepend`.

**`mir_wayland_generator`** — Mir's Wayland protocol wrapper generator must run on the host (x86_64). It is compiled from source in `do_compile:prepend` using `${BUILD_CXX}` and the host's `libxml++2.6-dev`. Three patches are applied to allow passing the generator path via a CMake variable and to skip its in-tree build.

**`lttng-gen-tp`** — Generates LTTng tracepoint `.h`/`.c` files from `.tp` sources. Provided by `liblttng-ust-dev` (not `lttng-tools`). Pre-generated in `do_compile:prepend` using `/usr/bin/lttng-gen-tp`. If you ever need to debug this manually, the HOSTTOOLS symlink can be created with:
```bash
ln -sf /usr/bin/lttng-gen-tp ~/workspace/yocto/build/tmp/hosttools/lttng-gen-tp
```

**`gdbus-codegen`** — Generates D-Bus GLib proxy code from `.xml` interface files (used for logind seat/session support). Provided by `glib-2.0-native` (in DEPENDS). Pre-generated in `do_compile:prepend` using `${STAGING_BINDIR_NATIVE}/gdbus-codegen`.

### Dependencies

- `libxml++-2.6` uses `std::auto_ptr` which is deprecated in C++23 — `-Wno-deprecated-declarations` is applied to the Mir build to suppress the `-Werror` failure
- `libdisplay-info` is not available in meta-oe Scarthgap — a custom recipe is provided in this layer
- `libxml++-2.6` (2.x API) is not available in meta-oe Scarthgap (only 5.x) — a custom recipe is provided in this layer
- `gmp` is required by Miral (`gmpxx.h`) and is pulled from `poky/meta`
