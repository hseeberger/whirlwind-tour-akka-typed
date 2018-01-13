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

import akka.actor.ExtendedActorSystem
import akka.actor.typed.{ ActorRef, ActorRefResolver }
import akka.serialization.SerializerWithStringManifest
import de.heikoseeberger.wtat.proto.userrepository.{
  AddUser => AddUserProto,
  RemoveUser => RemoveUserProto,
  UserAdded => UserAddedProto,
  UserRemoved => UserRemovedProto,
  UsernameTaken => UsernameTakenProto,
  UsernameUnknown => UsernameUnknownProto
}
import de.heikoseeberger.wtat.proto.{ User => UserProto }
import java.io.NotSerializableException

final class UserRepositorySerializer(system: ExtendedActorSystem)
    extends SerializerWithStringManifest {
  import UserRepository._
  import akka.actor.typed.scaladsl.adapter._

  override val identifier = 4243

  private final val AddUserManifest         = "AddUser"
  private final val UsernameTakenManifest   = "UsernameTaken"
  private final val UserAddedManifest       = "UserAdded"
  private final val RemoveUserManifest      = "RemoveUser"
  private final val UsernameUnknownManifest = "UsernameUnknown"
  private final val UserRemovedManifest     = "UserRemoved"

  private val resolver = ActorRefResolver(system.toTyped)

  override def manifest(o: AnyRef) =
    o match {
      case serializable: Serializable =>
        serializable match {
          case _: AddUser         => AddUserManifest
          case _: UsernameTaken   => UsernameTakenManifest
          case _: UserAdded       => UserAddedManifest
          case _: RemoveUser      => RemoveUserManifest
          case _: UsernameUnknown => UsernameUnknownManifest
          case _: UserRemoved     => UserRemovedManifest
        }
      case _ => throw new IllegalArgumentException(s"Unknown class: ${o.getClass}!")
    }

  override def toBinary(o: AnyRef) = {
    def userProto(user: User)      = UserProto(user.username.value, user.nickname.value)
    def toBinary(ref: ActorRef[_]) = resolver.toSerializationFormat(ref)
    val proto =
      o match {
        case serializable: Serializable =>
          serializable match {
            case AddUser(user, replyTo)        => AddUserProto(Some(userProto(user)), toBinary(replyTo))
            case UsernameTaken(username)       => UsernameTakenProto(username)
            case UserAdded(user)               => UserAddedProto(Some(userProto(user)))
            case RemoveUser(username, replyTo) => RemoveUserProto(username, toBinary(replyTo))
            case UsernameUnknown(username)     => UsernameUnknownProto(username)
            case UserRemoved(username)         => UserRemovedProto(username)
          }
        case _ => throw new IllegalArgumentException(s"Unknown class: ${o.getClass}!")
      }
    proto.toByteArray
  }

  override def fromBinary(bytes: Array[Byte], manifest: String) = {
    def addUser(proto: AddUserProto)                 = AddUser(user(proto.user.get), fromBinary(proto.replyTo))
    def usernameTaken(proto: UsernameTakenProto)     = UsernameTaken(proto.username)
    def userAdded(proto: UserAddedProto)             = UserAdded(user(proto.user.get))
    def user(proto: UserProto)                       = User(proto.username, proto.nickname).valueOr(_.fail)
    def removeUser(proto: RemoveUserProto)           = RemoveUser(proto.username, fromBinary(proto.replyTo))
    def usernameUnknown(proto: UsernameUnknownProto) = UsernameUnknown(proto.username)
    def userRemoved(proto: UserRemovedProto)         = UserRemoved(proto.username)
    def fromBinary(ref: String)                      = resolver.resolveActorRef(ref)
    manifest match {
      case AddUserManifest         => addUser(AddUserProto.parseFrom(bytes))
      case UsernameTakenManifest   => usernameTaken(UsernameTakenProto.parseFrom(bytes))
      case UserAddedManifest       => userAdded(UserAddedProto.parseFrom(bytes))
      case RemoveUserManifest      => removeUser(RemoveUserProto.parseFrom(bytes))
      case UsernameUnknownManifest => usernameUnknown(UsernameUnknownProto.parseFrom(bytes))
      case UserRemovedManifest     => userRemoved(UserRemovedProto.parseFrom(bytes))
      case _                       => throw new NotSerializableException(s"Unknown manifest: $manifest!")
    }
  }
}
