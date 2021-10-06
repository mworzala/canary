package net.minestom.vanilla.entity;

import com.mattworzala.canary.api.InWorldTest;
import com.mattworzala.canary.api.TestEnvironment;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;

public class FallingBlockEntityTest {
    @InWorldTest
    public void testBasicUsage(TestEnvironment env) {
        Point startPos = new Vec(3, 4, 3);
        Point endPos = new Vec(3, 1, 3);

        var entity = env.spawnEntity(() -> new FallingBlockEntity(Block.SAND, startPos));

        env.expect(entity).toBeRemoved();
//        env.expect(endPos).toBeBlock(Block.SAND);
    }
}
