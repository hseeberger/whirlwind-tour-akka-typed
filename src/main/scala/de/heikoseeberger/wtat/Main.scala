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

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props, Terminated }
import akka.stream.{ ActorMaterializer, Materializer }
import pureconfig.loadConfigOrThrow

object Main {

  final class Root(config: Config) extends Actor with ActorLogging {
    import akka.typed.scaladsl.adapter._

    private implicit val mat: Materializer = ActorMaterializer()

    private val api = {
      import config.api._
      context.spawn(Api(address, port), Api.Name)
    }

    context.watch(api)
    log.info(s"${context.system.name} up and running")

    override def receive = {
      case Terminated(actor) =>
        log.error(s"Shutting down, because actor ${actor.path} terminated!")
        context.system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    sys.props += "log4j2.contextSelector" -> "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector"
    val config = loadConfigOrThrow[Config]("wtat")
    val system = ActorSystem("wtat")
    system.actorOf(Props(new Root(config)), "root")
  }
}
