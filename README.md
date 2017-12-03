# Whirlwind Tour of Akka Typed #

This demo project shows many of the features of Akka Typed, core ones as well as higher-level ones
like for Akka Persistence or Akka Cluster.

The use case is a simple user repository. The architecture is based upon Event Sourcing and CQRS
with a strictly consistent (CP) write side and an eventually consistent (AP) in-memory read model.

The below diagram shows the main components, most of which are actors.

```
                                    HTTP
+------------------------------------o---------------------------------+
|                                    |                                 |
|                                    |                                 |
|               AddUser              |                                 |
|               RemoveUser  +-----------------+    GetUsers            |
|              +----------->|       Api       |<-----------+           |
|              |            +-----------------+            |           |
|              v                                           v           |
|     +-----------------+             Changed     +-----------------+  |
|     | UserRepository  |            +----------->|    UserView     |  |
|     +-----------------+            |            +-----------------+  |
|              |                     |                                 |
|              |            +-----------------+                        |
|              |            |      DData      |                        |
|              |            +-----------------+                        |
|    UserAdded |                     ^                                 |
|  UserRemoved |                     |            +-----------------+  |
|              |                     +------------| UserProjection  |  |
|              |                      Update      +-----------------+  |
|              |                                           ^           |
|              |                                           |           |
+--------------+-------------------------------------------+-----------+
               v                                           |
      +-----------------+            Source[User.Event, _] |
      |    EventLog     |----------------------------------+
      +-----------------+
```

In order to run the system:
- make sure to have Cassandra up and running properly, e.g. via `docker-compose up -d cassandra`
- use correct configuration settings, e.g. by using the comman aliases `r0` and `r1` from within sbt  

## Contribution policy ##

Contributions via GitHub pull requests are gladly accepted from their original author. Along with
any pull requests, please state that the contribution is your original work and that you license
the work to the project under the project's open source license. Whether or not you state this
explicitly, by submitting any copyrighted material via pull request, email, or other means you
agree to license the material under the project's open source license and warrant that you have the
legal authority to do so.

## License ##

This code is open source software licensed under the
[Apache-2.0](http://www.apache.org/licenses/LICENSE-2.0) license.
