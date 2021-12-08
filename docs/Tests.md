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

# Assertions

## Types of assertion methods

- Suppliers
- Conditions
- Logic

## Suppliers

Convert from on assertion type to a derived assertion type

ex:
Entity -> Instance the entity is in Block -> Position of the block

```
env.expect(entity).instance().toHaveTimeGreaterThan(20);
```

From an EntityAssertion to and InstanceAssertion using the .instance() supplier

## Condition

Tests some condition on the supplied data

```
env.expect(entity).toHaveHealth(10);
```

Checks if the given entity has 10 health

## Logic

Used to combine the results of conditions

# How assertions are parsed

Operators further right have higher Items further to the A AND B AND C => ((A AND B) AND C)
A AND B OR C => ((A AND B) OR C)

```
         AND
        /   \
      AND    C
     /   \
    A     B
```

Operators Binary Operators:

- and
- or
- then

Unary Operators

- not
- always

```
Grammar:
terminals: {and, or, then, not, always, cond} //cond stands in for any condition
E -> A | O | T | U 
A -> E and E 
O -> E or E 
T -> E then E 
U -> not U | always U | cond
```

This grammar has the notable property that unary operators do not operate on general expressions. You can not write an
expression that parses as (NOT (A AND B)), you would instead get ((NOT A) AND B). The logically equivalent, and possible
to write expression that is equivalent to (NOT (A AND B)) would be ((NOT A) OR (NOT B))
. [De Morgan's laws](https://en.wikipedia.org/wiki/De_Morgan%27s_laws)

### Operator Behavior

**AND**
Logical and, && in java, contains no state

**OR**
Logical or, || in java, contains no state

**THEN**
A stateful operator used to make assertions about things being true after other things. See [this page](Then) for more
details, in particular with how it interacts with always.

**NOT**
Logical not, ! in java, contains no state

**ALWAYS**
A stateful operator to express that some condition is always true while it is being tested. Is the only 'blocking'
operator in that it cannot pass immediately, can only pass after the entire lifetime of the test has elapsed, but may be
able to fail earlier.

```
expect(player).always().toHaveHealth(10)
```

Will run for the entire lifespan of the test, only passing once the lifetime has been reached, and the player's health
has always been 10. If at any point the player's health stops being 10, then will fail for the rest of the test.


