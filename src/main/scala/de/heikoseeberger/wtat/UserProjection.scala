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
import akka.persistence.query.EventEnvelope
import akka.persistence.query.scaladsl.EventsByPersistenceIdQuery
import akka.stream.Materializer
import akka.stream.scaladsl.{ Sink, Source }
import akka.typed.scaladsl.Actor
import akka.typed.scaladsl.AskPattern.Askable
import akka.typed.{ ActorRef, Behavior }
import akka.util.Timeout
import org.apache.logging.log4j.scala.Logging
import scala.concurrent.duration.FiniteDuration

object UserProjection extends Logging {

  sealed trait Command
  private final case object HandleEventStreamComplete extends Command

  abstract class EventStreamCompleteException
      extends IllegalStateException("Event stream completed unexpectedly!")
  private final case object EventStreamCompleteException extends EventStreamCompleteException

  final val Name = "user-projection"

  def apply(readJournal: EventsByPersistenceIdQuery,
            userView: ActorRef[UserView.Command],
            askTimeout: FiniteDuration)(implicit mat: Materializer): Behavior[Command] =
    // - Ask UserView for last seqNo
    // - Use it to get event stream from readJournal
    // - Transform EventEnvelope into UserRepository.Event
    // - Ask UserView to add or remove user, respectively
    // - Run stream, what to do on completion?
    ???
}
