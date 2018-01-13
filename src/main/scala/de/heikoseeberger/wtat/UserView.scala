/*
 * Copyright 2017 Heiko Seeberger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.heikoseeberger.wtat

import akka.actor.typed.scaladsl.Actor
import akka.actor.typed.{ ActorRef, Behavior }
import akka.cluster.ddata.ORSet
import akka.cluster.ddata.typed.scaladsl.{ DistributedData, Replicator }
import org.apache.logging.log4j.scala.Logging

object UserView extends Logging {

  sealed trait Command

  final case class GetUsers(replyTo: ActorRef[Users]) extends Command
  final case class Users(users: Set[User])

  private final case class UsersChanged(users: Set[User]) extends Command

  final val Name = "user-view"

  def apply(users: Set[User] = Set.empty): Behavior[Command] =
    Actor.deferred { context =>
      val changedAdapter =
        context.spawnAdapter { (changed: Replicator.Changed[ORSet[User]]) =>
          UsersChanged(changed.dataValue.elements)
        }
      val replicator = DistributedData(context.system).replicator
      replicator ! Replicator.Subscribe(UserProjection.usersKey, changedAdapter)

      Actor.immutable {
        case (_, GetUsers(replyTo)) =>
          replyTo ! Users(users)
          Actor.same

        case (_, UsersChanged(users)) =>
          UserView(users)
      }
    }
}
