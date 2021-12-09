# Canary

Canary is a way of writing tests for [Minestom](https://minestom.net/).

## Install

TODO

```
```

## Usage

Example Test:

```java
public class TestEntityTest {
    @InWorldTest
    public void testWalkToEntity(TestEnvironment env) {
        // Test that two spawned entities end up in the same location.
        final var entity = env.spawnEntity(TestEntity::new, new Pos(0, 1, 0));
        final var target = env.spawnEntity(TestEntity::new, new Pos(2, 1, 0));

        expect(entity).toBeAt(target::getPosition);
    }
}

```

## Contributing

PRs accepted.

## License

MIT
