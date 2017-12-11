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

import akka.actor.Scheduler
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator.WriteLocal
import akka.cluster.ddata.{ ORSet, ORSetKey }
import akka.persistence.query.EventEnvelope
import akka.persistence.query.scaladsl.EventsByPersistenceIdQuery
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.typed.cluster.ddata.scaladsl.{ DistributedData, Replicator }
import akka.typed.scaladsl.Actor
import akka.typed.scaladsl.AskPattern.Askable
import akka.typed.{ ActorRef, Behavior }
import akka.util.Timeout
import cats.instances.string._
import cats.syntax.eq._
import org.apache.logging.log4j.scala.Logging
import scala.concurrent.duration.FiniteDuration

object UserProjection extends Logging {
  import akka.typed.scaladsl.adapter._

  sealed trait Command
  final case object Stop                              extends Command
  private final case object HandleEventStreamComplete extends Command

  abstract class EventStreamCompleteException
      extends IllegalStateException("Event stream completed unexpectedly!")
  private final case object EventStreamCompleteException extends EventStreamCompleteException

  final val Name = "user-projection"

  final val usersKey: ORSetKey[User] =
    ORSetKey("users")

  def apply(readJournal: EventsByPersistenceIdQuery,
            userView: ActorRef[UserView.Command],
            askTimeout: FiniteDuration)(implicit mat: Materializer): Behavior[Command] =
    Actor.deferred { context =>
      implicit val c: Cluster   = Cluster(context.system.toUntyped)
      implicit val s: Scheduler = context.system.scheduler
      implicit val t: Timeout   = askTimeout
      val replicator            = DistributedData(context.system).replicator
      val self                  = context.self

      readJournal
        .eventsByPersistenceId(UserRepository.Name, 0, Long.MaxValue)
        .collect { case EventEnvelope(_, _, _, event: UserRepository.Event) => event }
        .mapAsync(1) {
          case UserRepository.UserAdded(user) =>
            replicator ? Replicator.Update(usersKey, ORSet.empty[User], WriteLocal)(_ + user)

          case UserRepository.UserRemoved(username) =>
            replicator ? Replicator.Update(usersKey, ORSet.empty[User], WriteLocal) { users =>
              users.elements.find(_.username.value === username).fold(users)(users - _)
            }
        }
        .runWith(Sink.onComplete(_ => self ! HandleEventStreamComplete))
      logger.debug("Running event stream")

      Actor.immutable {
        case (_, Stop)                      => Actor.stopped
        case (_, HandleEventStreamComplete) => throw EventStreamCompleteException
      }
    }
}
