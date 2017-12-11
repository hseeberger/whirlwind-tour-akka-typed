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
import akka.serialization.SerializerWithStringManifest
import de.heikoseeberger.wtat.proto.{ User => UserProto }
import java.io.NotSerializableException

final class UserSerializer(system: ExtendedActorSystem) extends SerializerWithStringManifest {

  override val identifier = 4242

  private final val UserManifest = "User"

  override def manifest(o: AnyRef) =
    o match {
      case _: User => UserManifest
      case _       => throw new IllegalArgumentException(s"Unknown class: ${o.getClass}!")
    }

  override def toBinary(o: AnyRef) =
    o match {
      case User(username, nickname) => UserProto(username.value, nickname.value).toByteArray
      case _                        => throw new IllegalArgumentException(s"Unknown class: ${o.getClass}!")
    }

  override def fromBinary(bytes: Array[Byte], manifest: String) = {
    def user(pb: UserProto) = User(pb.username, pb.nickname)
    manifest match {
      case UserManifest => user(UserProto.parseFrom(bytes))
      case _            => throw new NotSerializableException(s"Unknown manifest: $manifest!")
    }
  }
}
