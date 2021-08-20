# JSON World Format

```json
{
  "id": "my-test-world",
  "size": [16, 16, 16],
  "blockmap": [
    "minecraft:stone",
    "minecraft:cobblestone_stairs[facing=north]",
    {
      "block": "minecraft:stone_stairs[facing=south,waterlogged=true]",
      "handler": "example:my_block_handler",
      "data": "{name1:123,name2:\"sometext1\",name3:{subname1:456,subname2:\"sometext2\"}}"
    }
  ],
  "blocks": "0,0,0,0;10,0,0,2;0,0,1,1;5,0,1;-1"
}
```

Explanation:
 * `id` - Some unique id to be referenced from the test code
 * `size` - The X,Y,Z size of the area, with a max of 32,32,32 (or 48,48,48, whichever matches the structure outline)
 * `blockmap` - An array where each element represents a block which is used in the structure, block id `-1` represents `minecraft:air` with no properties
 * `blocks` - An encoded view of blocks where each block is represented by `x,y,z,id in blockmap`. The given block is repeated until the next defined coordinate.
   * This could be made really tiny if it was a binary form (x/y/z only need to be 1 byte each given max size)
   * Could also do this by sticking them all inside one int? eg `XXXXXXXXYYYYYYYYZZZZZZZZBLOCKMAP`, 
     although it is easily possible to use more than 256 blocks in a 32x32x32 area. Perhaps something
     like `BLOCKMAPBLOCKMAPBLOCKMAPBLOCKMAPBLOCKMAPXXXXXXXXYYYYYYYYZZZZZZZZ` in a long? but i still dont 
     love it. It also makes it really hard to read for a human