# Render Rules

<!--description:Technical documentation of the render rules, for resource pack maker.-->
<!--extension:prism-->

Aurora's Decorations render rules allow to use custom item models in some situations like if the item is rendered on a shelf, etc.

## Format

Render rules are JSON files placed in the `assets/<namespace>/aurorasdeco/render_rules` directory of a resource pack.

The JSON has the following structure:

- `match`
  - `item` or `tag`: the identifier of the item/tag to replace.  
    or `items`: an array of identifiers of the items to replace.
- `models`: an array of model identifiers or the structure
  - `model`: the model identifier
  - `restrict_to`?
    - `block` or `tag`: the identifier of the block/tag to restrict this model to. May be used to restrict a model to shelves only for example.

## Examples

```json
{
  "match": {
    "items": [
      "minecraft:book",
      "minecraft:written_book"
    ]
  },
  "models": [
    "aurorasdeco:special/book/blue_book",
    "aurorasdeco:special/book/book1",
    {
      "model": "aurorasdeco:special/book/book2",
      "restrict_to": {
        "tag": "aurorasdeco:shelves"
      }
    },
    {
      "model": "aurorasdeco:special/book/book2_red",
      "restrict_to": {
        "tag": "aurorasdeco:shelves"
      }
    },
    "aurorasdeco:special/book/green_book",
    "aurorasdeco:special/book/green_medium_book",
    "aurorasdeco:special/book/red_medium_book"
  ]
}
```
