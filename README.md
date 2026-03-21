# meta-mir

Yocto layer providing [Canonical Mir](https://github.com/canonical/mir) — a set of shared libraries for building Wayland compositors.

## Layer dependencies

| Layer | Purpose |
|-------|---------|
| `poky/meta` (oe-core) | Base layer |
| `meta-openembedded/meta-oe` | yaml-cpp, lttng-ust, glm, glibmm, libsigc++ |
| `meta-openembedded/meta-python` | python3-pillow (if examples are enabled) |

## Setup

Tested on **Ubuntu 24.04 LTS**.

### 1. Install host dependencies

```bash
sudo apt update && sudo apt install -y \
    gawk wget git diffstat unzip texinfo gcc build-essential \
    chrpath socat cpio python3 python3-pip python3-pexpect \
    xz-utils debianutils iputils-ping python3-git python3-jinja2 \
    python3-subunit zstd liblz4-tool file locales libacl1 \
    python3-distutils-extra \
    libxml++2.6-dev
```

### 2. Fix AppArmor user namespace restriction

Ubuntu 24.04 restricts unprivileged user namespaces by default, which breaks Yocto:

```bash
sudo sysctl -w kernel.apparmor_restrict_unprivileged_userns=0
echo 'kernel.apparmor_restrict_unprivileged_userns=0' | sudo tee /etc/sysctl.d/99-yocto.conf
```

### 3. Clone layers

```bash
mkdir -p ~/workspace/yocto && cd ~/workspace/yocto
git clone --depth=1 -b scarthgap https://github.com/yoctoproject/poky.git
git clone --depth=1 -b scarthgap https://github.com/openembedded/meta-openembedded.git
git clone https://github.com/bricke/meta-mir.git
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
