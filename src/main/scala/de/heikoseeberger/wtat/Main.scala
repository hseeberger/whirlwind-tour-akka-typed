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
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.{ ActorMaterializer, Materializer }
import akka.typed.SupervisorStrategy.restartWithBackoff
import akka.typed.scaladsl.Actor.supervise
import pureconfig.loadConfigOrThrow

object Main {

  final class Root(config: Config) extends Actor with ActorLogging {
    import akka.typed.scaladsl.adapter._

    private implicit val mat: Materializer = ActorMaterializer()

    private val userRepository = context.spawn(UserRepository(), UserRepository.Name)

    private val userView = context.spawn(UserView(), UserView.Name)

    private val userProjection = {
      import config.userProjection._
      val readJournal =
        PersistenceQuery(context.system)
          .readJournalFor[CassandraReadJournal](CassandraReadJournal.Identifier)
      val userProjection =
        supervise(UserProjection(readJournal, userView, askTimeout))
          .onFailure[UserProjection.EventStreamCompleteException](
            restartWithBackoff(minBackoff, maxBackoff, 0)
          )
      context.spawn(userProjection, UserProjection.Name)
    }

    private val api = {
      import config.api._
      context.spawn(Api(address, port, userRepository, userView, askTimeout), Api.Name)
    }

    context.watch(userRepository)
    context.watch(userView)
    context.watch(userProjection)
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
