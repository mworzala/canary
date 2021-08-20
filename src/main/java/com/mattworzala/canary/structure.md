The general goal is to have 3 relevant packages
* `canary.api`      - This is the only relevant package for people using Canary on their own
* `canary.server`   - Contains the server implementations for the headless and sandbox server
* `canary.platform` - Contains the junit engine implementation + other relevant platform code which is not for the server

Everything is kind of a mess at the moment