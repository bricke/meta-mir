# meta-mir

Yocto layer providing [Canonical Mir](https://github.com/canonical/mir) — a set of shared libraries for building Wayland compositors.

## Dependencies

| Layer | Purpose |
|-------|---------|
| `poky/meta` (oe-core) | Base layer |
| `meta-openembedded/meta-oe` | yaml-cpp, lttng-ust, libxml++, glm |

## Usage

Add to `bblayers.conf`:

```
BBLAYERS += "/path/to/meta-mir"
```

## Provided recipes

| Recipe | Description |
|--------|-------------|
| `mir` | Mir display server libraries and Miral toolkit |

## Packages

| Package | Contents |
|---------|----------|
| `mir` | Runtime shared libraries |
| `mir-dev` | Headers and pkg-config files |
| `mir-graphics-drivers-gbm-kms` | KMS/DRM platform plugin |

## Notes

- Builds only the `gbm-kms` platform (no X11, no Wayland nesting)
- Tests disabled — not suitable for running Mir's test suite
- Requires `protobuf-native` for cross-compilation of protobuf code generation
