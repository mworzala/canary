The general goal is to have 3 relevant packages
* `canary.api`      - This is the only relevant package for people using Canary on their own
* `canary.server`   - Contains the server implementations for the headless and sandbox server
* `canary.platform` - Contains the junit engine implementation + other relevant platform code which is not for the server

Everything is kind of a mess at the moment


### Notes
 * Execution Stuff
   * `TestEnvironment`: per test execution (not reused)
     * Keeps track of assertions (note below)
     * Ticked to test assertions (is this correct? I think perhaps this logic should be moved to the executor)
   * `TestExecutor`: one per test method (reused), handles instantiating the test class, 
     invoking the before/run/after methods, cleaning up the test for the next execution (replace structure).
     * Executing a test is *not* blocking, it must be ticked until it reports that it has a result.
   * `TestInstance`: N per server, for now does nothing special. In the future would be used for isolation
   * `TestCoordinator`: 1 per server, keeps track of each `TestExecutor` to run tests given to it, also manages
     creation and destruction of `TestInstance`s.
     * Executing tests here will be a blocking operation (`CanaryTestEngine#execute` will be replaced with a single call here)
     * Returns when all test results have been reported
