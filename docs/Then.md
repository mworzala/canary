### This is just a place for me to collect and record my thoughts about THEN

Basic definition we wrote down before:
(A THEN B) = A passes and then B passes on or after the tick A passed on

In the case of just PASS and FAIL, this definition works fine. Once A passes, we stop checking A and start checking B,
once B passes, (A THEN B) passes.

The complication of course is in the case of SOFT_PASS, and the potential desire to avoid having to continue testing
assertions that will never pass.

#### Giving intuitive definitions for various use of THEN and ALWAYS

((ALWAYS A) THEN B) = "A is true from tick 0 until the tick where B is true"
(A THEN (ALWAYS B))
I see two possible meanings for this:

1. "A is true and then on that same tick, or a later tick, B is true and then remains true for the lifetime"
   X = FAIL, O = PASS A: XXOXXXXXXXXX B: XXXXXOOOOOOO
2. "A is true and then on that same tick B is true and remain true for the lifetime"
   A: XXOXXXXXXXXX B: XXOOOOOOOOOO I don't have a strong feeling in this case about which is the better option, although
   probably 2

((ALWAYS A) THEN (ALWAYS B))
We have the same choice between two options here

1. "A is true from tick 0 until the tick where B is true, and then B is true every tick until the lifespan of the test"
   A: OOOOOOOXXXXXXX B: XXXXXXOOOOOOOO
2. "A is true from tick 0 until some point, and at some point B is true and stays true every tick until the lifespan of
   the test"
   A: OOOOXXXXXXXXXX B: XXXXXXOOOOOOOO I think option 1 is the only thing that makes any sense here

#### Trying to derive general rules from these ALWAYS cases

- When there is an ALWAYS to the left of a THEN, we want that always to be true up until the rhs of the THEN is true
- When there is an ALWAYS To the right of a THEN, we want the rhs to be true from some point after the lhs is true,
  until the lifespan of the test
- In the case of an ALWAYS to the right of THEN, I have a preference for the always having to pass on the same tick (or
  maybe the following tick) as the left hand side passing (or continuing to soft pass)

### Proposed behavior

State associated with THEN: testingRight = we only need to test the right hand side pseudocode:

```
test()  {
  // once we start testing the right, that's all we do, and we do a full evaluate
  if (testingRight) {
    return rhs.evaluate();
  }
  else {
    left = lhs.evaluate();
    // once the left passes, we start testing the right, and fully evaluating
    // this means that in the case of (A THEN (ALWAYS B)) that B needs to be true on the same tick A is true for the first time 
    if (left == PASS) {
      testingRight = true;
      return rhs.evaluate();
    }
    // on soft pass we check to see if the rhs is "ready" for us to start evaluating it
    // this handles ((ALWAYS A) THEN B), by saying that A has to be true up to and include the tick where B is true
    // similarly in the case of ((ALWAYS A) THEN (ALWAYS B)), A must be true up to and including the first tick where B is true
    if (left == SOFT_PASS) {
      right = rhs.test();
      if (right != FAIL) {
        testingRight = true;
        return right;
      }
      else {
        // if the right does fail, we don't go into a testingRight state, and continue testing the left
      }
    }

    // if left FAILS, return FAIL
    return left;
  }
}

```

#### Other caveats

Possible situatation for early exit:
In the case of ((ALWAYS A) THEN (ALWAYS B)), if A stops being true before B is true, then the test can never pass. Given
that we currently only have the FAIL state, which is treated as a retry by expect(), the assertion will continue to be
tested for the rest of time, even though it can't ever pass, this could be mitigated with a HARD_FAIL state, although I
don't think it is strictly neccessary for correct behavior, just for being able to stop a failed test sooner.
