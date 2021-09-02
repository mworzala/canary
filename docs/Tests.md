# Tests in Canary

Many of these ideas are inspired by this
talk: [Testing Minecraft in Minecraft | with Henrik Kniberg – Agile with Jimmy](https://www.youtube.com/watch?v=vXaWOJTCYNg)

Minecraft is a game mainly based around interactions. Things like interactions between entities and blocks, blocks and
blocks, the player with entities, the player with block, etc. This means that to test a behavior or functionality, you
will generally need to have some sort of scenario set up for a test. The way that this setup works in Canary is through
a combination of data from files, and code that does other setup.

An example of data from a file would be data that defines the block setup for the tests. This specifies how much size
the test requires, as well as what blocks go inside that area.

An example of code doing setup could be having code that places an entity like a zombie, or having code that pressed a
button in the test.

Once the test all set up, we want to actually be able to declare what the expected behavior is. Since in Minecraft
things happen over time, static assertions (such as those in jUnit) are the generally what we want. The solution to this
is our expect system. Inspired by [jest](https://jestjs.io/), we created a system that allow you to write something
like:

```java
public class TestEntityTest {

    @InWorldTest
    public void testWalkToDiamondBlock(TestEnvironment env) {
        final var diamondBlockPos = env.getPos("diamondBlock");
        final var entity = env.spawnEntity(TestEntity::new);

        env.expect(entity).toBeAt(diamondBlockPos);
    }
}
```

In this somewhat arbitrary example, we are testing that zombies walk towards diamond blocks that are near them. In this
test, we get the position of the diamond in the test (that would be placed there by the block setup file), and then we
place a zombie. Then we say that we expect that entity to be at the position of that diamond block.

The expect statement will be checked every server tick until it either passes (the zombie made it to the diamond block),
or the default test lifetime is reached.

