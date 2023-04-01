# Aurora's Decorations Changelog

### 1.0.0-beta.3 - Pre-release 1?

- Initial release.
- Added bunch of decorations.
- Added bunch of worldgen features.

### 1.0.0-beta.4

- Improved dynamic language generation.
- Improved Biomes You'll Go compatibility.

### 1.0.0-beta.5

- Added [EMI] support.
- Added missing French translations.
- Removed redundant axe item mixins.
- Improved accuracy of dynamic language generation after testing with French.

### 1.0.0-beta.6

- Added [Painter's Palette](https://lambdaurora.dev/AurorasDecorations/painter_palette.html).
- Fixed Jacaranda Leaves not being mineable with hoes.
- Fixed shelves not fitting onto the side of chests.

### 1.0.0-beta.7

- Fixed screen handler types not being initialized correctly on dedicated servers.

### 1.0.0-beta.8

- Fixed Painter's Palette 4th tool slot not being serialized ([#34](https://github.com/LambdAurora/AurorasDecorations/issues/34)).
- Fixed startup crash with Blockus and Promenade combined ([#33](https://github.com/LambdAurora/AurorasDecorations/issues/33)).

### 1.0.0-beta.9

- Improved buildscript to make porting easier in preparation to Mojang's new version model.
- Updated virtual resource pack handling to use QSL.
- Fixed incompatibility with Immersive Weathering ([#28](https://github.com/LambdAurora/AurorasDecorations/issues/28)).
- Fixed tables being detected as wood type ([#32](https://github.com/LambdAurora/AurorasDecorations/issues/32)).

### 1.0.0-beta.10

- Bumped required Quilt Loader to 0.17.6.
- Fixed the client virtual resource pack being injected twice.
- Fixed virtual tags being put at the wrong location, failing to load critical tags.

### 1.0.0-beta.11

- Added Stripped Azalea Log and Stripped Jacaranda Log to `c:stripped_logs` block and item tags ([#40](https://github.com/LambdAurora/AurorasDecorations/issues/40)).
- Changed render rules directory from `aurorasdeco_render_rules` to `aurorasdeco/render_rules`.
- Fixed painter's palette assuming the wrong slot when no tool is selected.
- Fixed sign post blocks not being waterloggable.
- Fixed sign post blocks crashing with Sodium when Indium is absent (hopefully) ([#44](https://github.com/LambdAurora/AurorasDecorations/issues/44)).
- Fixed being able to place stump blocks without a supporting block underneath ([#42](https://github.com/LambdAurora/AurorasDecorations/issues/42)).
  - It is still possible to have floating stump blocks by pushing the block or destroying the support block.

### 1.0.0-beta.12

- Added a world generation feature that generates a lonely sign posts indicating directions
  to nearby interesting landmarks with some parts of a road.
- Fixed calcite bricks placement in the creative inventory.
- Optimized ModelLoaderMixin ([#46](https://github.com/LambdAurora/AurorasDecorations/pull/46)).
- Switched to Quilt Point of Interest API for some stuff, reducing code to maintain.

### 1.0.0-beta.13

- \[1.20] Added custom models for the bamboo pile.
- \[1.19.4] Reorganized creative tab injection of Aurora's Decorations items to match the new tabs.
- Fixed Lavender Plains replacing Flower Forest biomes sometimes.
- Improved Painter's Palette texture.

[EMI]: https://modrinth.com/mod/emi "EMI Modrinth page"
