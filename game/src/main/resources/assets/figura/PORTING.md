# Figura 26.2 assets

This namespace is reserved for the Figura 26.2 port.

Upstream Figura assets are the visual source of truth for the port. Keep the
namespace at assets/figura/ so model, texture, GUI, language, and script
resources can be moved over without coupling them to the old Fabric/Forge
resource layout.

Upstream source: https://github.com/FiguraMC/Figura/tree/1.20/common/src/main/resources/assets/figura

Do not replace upstream Figura assets with unrelated substitutes when an
upstream asset exists. API and rendering changes belong in Java compatibility
code; visual resources should remain Figura-compatible.
