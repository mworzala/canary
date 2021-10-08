# Test Builder

## Desired Features

- Create Structures out of blocks
- Allow you to set handler or nbt data on blocks
- ALlow you to name the file that structures go to
- Allow you to "mark" a block by name that gets saved into the structure file and can be retrieved from the test setup
- Spawn entities as part of the structure
- Use some existing blocks as a starting point
- Edit an existing structure
- per-test environment settings
- Custom-ly configurable environment settings
    - user defined actions, setting a custom timer

## Developer Workflow

- Allow seamless transitioning between manually testing and create a test
- Also allow directly just creating a test because you know you want it

Test building is done inside an instance that is empty except for the test building

To get into this instance you have two options

1. start from a blank slate
2. import some existing set of blocks into an instance with you

Once you are done building the structure, you can save it. The game automagically figures out what area of blocks is
that part that you want to save you can see this from a bounding box while you are editing

When you save a structure the server could send you some stub code for the test to copy (with ah correctly named method
and the like)

# Commands

## While in a normal canary instance

/test builder import <new-structure-name>

Gives you an item that lets you select blocks from your current instance to import into the test builder instance. Once
you are done selecting your blocks, you are brought to the test builder instance along with your imported blocks.

/test builder new <new-structure-name>

Brings you to a blank test builder instance

/test builder duplicate <old-structure-name> <new-structure-name>

Brings you to a test builder instance that starts with an already existing structure

## While in the test builder instance

/test builder done

Saves the current test, puts you back where ever you started.








