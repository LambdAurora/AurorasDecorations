# Redstone Lantern

<!--description:Learn everything about the Redstone Lantern, a quite useful lantern for your redstone builds.-->
<!--thumbnail:images/render/redstone_lantern.png;Picture of a Redstone Lantern.-->

Redstone Lantern is a type of lantern which primary color is red, like redstone!  
They emit some redstone dust particles while placed and lit.

In addition to the cosmetic part of this lantern, the Redstone Lantern introduces new connection mechanics.
Aside from being like a Redstone Torch but waterloggable, they also prefer to transmit the power down rather than up.

<div class="wiki-gallery">
<img alt="Redstone Lantern Item" title="Redstone Lantern Item" class="ls_pixelated" src="../../src/main/resources/assets/aurorasdeco/textures/item/redstone_lantern.png" width="128" height="128" />
<img alt="Lit Redstone Lantern" src="../../images/render/redstone_lantern.png" width="128" height="128" />
<img alt="Redstone Lantern turned off" src="../../images/render/redstone_lantern_off.png" width="128" height="128" />
</div>

## Crafting

<table class="crafting-grid">
<thead>
    <th>Crafting Table</th>
</thead>
<tbody>
    <tr>
        <td>Iron Nugget</td>
        <td>Iron Nugget</td>
        <td>Iron Nugget</td>
    </tr>
    <tr>
        <td>Iron Nugget</td>
        <td>Redstone Torch</td>
        <td>Iron Nugget</td>
    </tr>
    <tr>
        <td>Iron Nugget</td>
        <td>Iron Nugget</td>
        <td>Iron Nugget</td>
    </tr>
</tbody>
</table>

## Mechanics

A redstone lantern never affects the block it is attached to, even if it is a mechanism component.
For example, a redstone lantern attached to a redstone lamp does not activate the lamp.  
In addition to that, when placed on the floor it will never power up the elements above.

A redstone lantern will power up adjacent horizontal elements,
if hanging it will also power up downward, and upward if hung from the side of a block.

Redstone Lanterns experience "burn-out" like Redstone Torches, in doubt:
> A redstone torch experiences "burn-out" when it is forced to change state (by powering and de-powering the block it is attached to) more than eight times in 60 game ticks (three seconds, barring lag).
> <div class="ls_source">
> [Minecraft Wiki](https://minecraft.fandom.com/wiki/Redstone_Torch#Usage)
> </div>

### Pictures

#### Basic connectivity

![Basic connectivity](../../images/render/redstone_lantern/basic_connectivity.png)

#### Redstone Wall Lantern connectivity

![Redstone Wall Lantern connectivity](../../images/render/redstone_lantern/wall_connectivity.png)

#### Emission Directions

![Emission Directions](../../images/render/redstone_lantern/emit_direction.png)

As you can see Redstone Lanterns allow to invert the behavior of Redstone Torches and transmit a signal along an axis downward.
