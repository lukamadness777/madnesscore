# Madness Core

A multiloader library mod for Minecraft **1.21.1**, built for both **Fabric** and **NeoForge** on top of a shared `common` module. Author: **LukaMadness**.

Madness Core adds:

- A **data-driven equipment slot system** (accessories), with support for slot groups, item/tag requirements, attributes applied on equip, and client/server synchronization.
- **Technology blocks**: a Heat Generator, an Alloy Smeltery, and an Energy Converter, with their own heat and energy (CE) systems.
- **New materials**: steel and bronze (ingots, nuggets and plates), plus a full set of colored fabrics (one per vanilla dye color).
- A **bundled tabs** system: collapsible side tabs inside any `CreativeModeTab`, exposed as a public API for other mods to use.

Some parts of this project were adapted from other mods; see [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md) for the full attributions and licenses.

> License: PolyForm Shield License 1.0.0, with an additional version-porting exception. See [LICENCE.md](LICENCE.md).

---

## Requirements

| | |
|---|---|
| Minecraft | 1.21.1 |
| Java | 21 |
| Fabric Loader | ≥ 0.19.3 (requires Fabric API) |
| NeoForge | 21.1.248 or later |

## Project structure

```
madnesscore-master/
├── common/       # Shared logic, registries, API and assets between loaders
│   ├── src/main/java/...common/     # Common code (server+client)
│   └── src/client/java/...common/client/  # Client-only code
├── fabric/       # Fabric-specific entry points and adapters
├── neoforge/     # NeoForge-specific entry points and adapters
├── buildSrc/     # Shared Gradle plugins (multiloader-common / multiloader-loader)
├── libs/         # Pre-built reference jars (common/fabric/neoforge)
└── gradle.properties
```

The project follows the standard multiloader pattern (platform services `IPlatformHelper`, `ISlotAttachment`, `ISlotNetwork`, `RegistryHelper`, etc. in `common/.../platform/services/`, with one implementation per loader).

## Building

```bash
./gradlew build
```

The resulting jars are placed in `fabric/build/libs/` and `neoforge/build/libs/`.

To run a test client:

```bash
./gradlew fabric:runClient
# or
./gradlew neoforge:runClient
```

## Main technical features

### Slot system (accessories)

Fully defined via datapack in `common/src/main/resources/data/madnesscore/`:

- `slots/<group>/group.json` — slot group metadata (id, order).
- `slots/<group>/<slot>.json` — individual slot definition.
- `slot_assignments/*.json` — assigns slot groups to entity types (supports `#` tags).

Public entry point: `SlotsApi` (`common/src/main/java/dev/lukamadness/madnesscore/common/slots/SlotsApi.java`), which exposes the per-item behavior registry (`Slottable`) and access to an entity's `SlotComponent` through the `ISlotAttachment` platform service.

Slots included out of the box: head (face, hat), chest (necklace, back, cape), legs (belt), feet (aglet, shoes), and hand/offhand (glove, ring).

### Technology (heat and energy)

- **Heat Generator**: produces heat (`Heat`) from fuels (`HeatFuel`).
- **Alloy Smeltery**: smelts alloys using the received heat (`AlloySmeltingRecipe` recipes).
- **Energy Converter**: converts heat into energy (`CE`) or vice versa.

Each block exposes tooltips with stored/input/output heat and energy, and requires a minimum temperature to operate.

### Bundled Tabs (API for other mods)

`BundledTabsAPI` (`common/src/client/.../bundledtabs/`) lets any mod depending on Madness Core register collapsible side tabs inside its own `CreativeModeTab`:

```java
BundledTabGroup group = BundledTabsAPI.registerGroup(
        MyModCreativeTabs.MY_TAB,
        () -> ResourceLocation.fromNamespaceAndPath("mymod", "textures/gui/bundled_tabs/interface.png"));

group.addTab(BundledTab.builder()
        .title(Component.translatable("bundledTab.mymod.tools"))
        .icon(() -> new ItemStack(MyItems.MY_PICKAXE.get()))
        .displayItems((provider, output) -> output.accept(MyItems.MY_PICKAXE.get()))
        .build());
```

## Credits

See [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md) for the licenses of third-party code incorporated into this project.
