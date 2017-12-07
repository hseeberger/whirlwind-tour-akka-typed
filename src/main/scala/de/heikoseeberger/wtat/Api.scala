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

import akka.actor.{ ActorSystem, Scheduler }
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.StatusCodes.{ Conflict, Created, NoContent, NotFound }
import akka.http.scaladsl.server.{ Directives, Route }
import akka.stream.Materializer
import akka.typed.scaladsl.Actor
import akka.typed.scaladsl.AskPattern.Askable
import akka.typed.{ ActorRef, Behavior }
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import java.net.InetSocketAddress
import org.apache.logging.log4j.scala.Logging
import scala.concurrent.duration.FiniteDuration
import scala.util.{ Failure, Success }

object Api extends Logging {

  sealed trait Command
  private final case object HandleBindFailure                      extends Command
  private final case class HandleBound(address: InetSocketAddress) extends Command

  final val Name = "api"

  def apply(address: String,
            port: Int,
            userRepository: ActorRef[UserRepository.Command],
            userView: ActorRef[UserView.Command],
            askTimeout: FiniteDuration)(implicit mat: Materializer): Behavior[Command] =
    Actor.deferred { context =>
      import akka.typed.scaladsl.adapter._
      import context.executionContext
      implicit val s: ActorSystem = context.system.toUntyped

      val self = context.self
      Http()
        .bindAndHandle(route(userRepository, userView)(askTimeout, context.system.scheduler),
                       address,
                       port)
        .onComplete {
          case Failure(_)                      => self ! HandleBindFailure
          case Success(ServerBinding(address)) => self ! HandleBound(address)
        }

      Actor.immutable {
        case (_, HandleBindFailure) =>
          logger.error(s"Stopping, because cannot bind to $address:$port!")
          Actor.stopped

        case (_, HandleBound(address)) =>
          logger.info(s"Bound to $address")
          Actor.ignore
      }
    }

  def route(
      userRepository: ActorRef[UserRepository.Command],
      userView: ActorRef[UserView.Command]
  )(implicit askTimeout: Timeout, scheduler: Scheduler): Route = {
    import Directives._
    import ErrorAccumulatingCirceSupport._
    import io.circe.generic.auto._
    import io.circe.refined._

    pathEndOrSingleSlash {
      get {
        complete {
          import UserView._
          (userView ? GetUsers).mapTo[Users]
        }
      } ~
      post {
        entity(as[User]) { user =>
          import UserRepository._
          onSuccess(userRepository ? addUser(user)) {
            case UsernameTaken(_) => complete(Conflict)
            case UserAdded(_)     => complete(Created)
          }
        }
      }
    } ~
    path(Segment) { username =>
      delete {
        import UserRepository._
        onSuccess(userRepository ? removeUser(username)) {
          case UsernameUnknown(_) => complete(NotFound)
          case UserRemoved(_)     => complete(NoContent)
        }
      }
    }
  }
}
