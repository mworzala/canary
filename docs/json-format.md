# JSON World Format

```json
{
    "id": "my-test-world",
    "size": [
        16,
        16,
        16
    ],
    "markers": {
        "diamond_block": 123
    },
    "blockmap": [
        "minecraft:stone",
        "minecraft:cobblestone_stairs[facing=north]",
        {
            "block": "minecraft:stone_stairs[facing=south,waterlogged=true]",
            "handler": "example:my_block_handler",
            "data": "{name1:123,name2:\"sometext1\",name3:{subname1:456,subname2:\"sometext2\"}}"
        }
    ],
    "blocks": "0,256;1,16;0,240;-1,3584"
}
```

Explanation:

* `id` - Some unique id to be referenced from the test code
* `size` - The X,Y,Z size of the area, with a max of 32,32,32 (or 48,48,48, whichever matches the structure outline)
* `blockmap` - An array where each element represents a block which is used in the structure, block id `-1`
  represents `minecraft:air` with no properties
* `blocks` - A run length encoding of the blocks for the test. The semicolons separate each block definitions. A block
  definition starts with a block id followed by a number of blocks
    * The block layout starts at (0, 0, 0) and increments by x, then z, then y
    * So the order of positions is like (0, 0, 0), (1, 0, 0), (2, 0, 0), ..., (0, 1, 0), (1, 1, 0), ..., (0, 0, 1)