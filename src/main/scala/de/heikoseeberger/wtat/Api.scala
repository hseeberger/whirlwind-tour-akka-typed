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

import akka.http.scaladsl.server.{ Directives, Route }
import akka.stream.Materializer
import akka.typed.Behavior
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import org.apache.logging.log4j.scala.Logging

object Api extends Logging {

  sealed trait Command

  final val Name = "api"

  def apply(address: String, port: Int)(implicit mat: Materializer): Behavior[Command] =
    // - Call Http().bindAndHandle
    // - Ingest the result by sending to be defined commands to self
    // - Handle the ingested result
    ???

  def route: Route = {
    import Directives._
    import ErrorAccumulatingCirceSupport._
    import io.circe.generic.auto._
    import io.circe.refined._

    pathEndOrSingleSlash {
      get {
        complete {
          "GET received"
        }
      } ~
      post {
        entity(as[User]) { user =>
          complete {
            s"POST $user received"
          }
        }
      }
    } ~
    path(Segment) { username =>
      delete {
        complete {
          s"DELETE $username received"
        }
      }
    }
  }
}
