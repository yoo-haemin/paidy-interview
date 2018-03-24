package users.util.json

import java.time.OffsetDateTime
import io.circe.{ Decoder, Encoder, HCursor, Json }
import io.circe.syntax._
import users.domain._

object instances {
  implicit val offsetDateTimeEncoder: Encoder[OffsetDateTime] =
    Encoder[String].contramap { _.toString }

  implicit val offsetDateTimeDecoder: Decoder[OffsetDateTime] =
    Decoder[String].map { OffsetDateTime.parse(_) }

  implicit val encodeMetadata = new Encoder[User.Metadata] {
    final def apply(md: User.Metadata): Json = Json.obj(
      ("version", Json.fromInt(md.version)),
      ("createdAt", md.createdAt.asJson),
      ("updatedAt", md.updatedAt.asJson),
      ("blockedAt", md.blockedAt.asJson),
      ("deletedAt", md.deletedAt.asJson)
    )
  }

  implicit val decodeMetadata = new Decoder[User.Metadata] {
    final def apply(c: HCursor): Decoder.Result[User.Metadata] =
      for {
        version ← c.downField("version").as[Int]
        createdAt ← c.downField("createdAt").as[OffsetDateTime]
        updatedAt ← c.downField("updatedAt").as[OffsetDateTime]
        blockedAt ← c.downField("blockedAt").as[Option[OffsetDateTime]]
        deletedAt ← c.downField("deletedAt").as[Option[OffsetDateTime]]

      } yield User.Metadata(version, createdAt, updatedAt, blockedAt, deletedAt)
  }

  implicit val encodeUser: Encoder[User] = new Encoder[User] {
    final def apply(u: User): Json = Json.obj(
      ("id", Json.fromString(u.id.value)),
      ("userName", Json.fromString(u.userName.value)),
      ("emailAddress", Json.fromString(u.emailAddress.value)),
      ("metadata", u.metadata.asJson)
    )
  }

  implicit val decodeUser: Decoder[User] = new Decoder[User] {
    final def apply(c: HCursor): Decoder.Result[User] =
      for {
        id ← c.downField("id").as[String].map(User.Id(_))
        userName ← c.downField("userName").as[String].map(UserName(_))
        emailAddress ← c.downField("emailAddress").as[String].map(EmailAddress(_))
        metadata ← c.downField("metadata").as[User.Metadata]
      } yield
        User(
          id = id,
          userName = userName,
          emailAddress = emailAddress,
          password = None,
          metadata = metadata
        )
  }
}
