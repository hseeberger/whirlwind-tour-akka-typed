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

import cats.data.{ NonEmptyList, Validated }
import cats.syntax.apply._
import cats.syntax.either._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.And
import eu.timepit.refined.char.LetterOrDigit
import eu.timepit.refined.collection.{ Forall, NonEmpty }
import eu.timepit.refined.refineV

object User {

  type Username           = Refined[String, UsernameRefinement]
  type UsernameRefinement = And[NonEmpty, Forall[LetterOrDigit]]

  type Nickname           = Refined[String, NicknameRefinement]
  type NicknameRefinement = NonEmpty

  def apply(username: String, nickname: String): Validated[NonEmptyList[String], User] =
    (validateUsername(username), validateNickname(nickname)).mapN(new User(_, _))

  def validateUsername(username: String): Validated[NonEmptyList[String], Username] =
    refineV[UsernameRefinement](username).toValidatedNel

  def validateNickname(nickname: String): Validated[NonEmptyList[String], Nickname] =
    refineV[NicknameRefinement](nickname).toValidatedNel
}

final case class User(username: User.Username, nickname: User.Nickname)
