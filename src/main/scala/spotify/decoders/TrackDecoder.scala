package spotify.decoders

import cats.Traverse
import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor, Json}
import spotify.model.{Track, TrackPage}

import java.time.LocalDateTime

object TrackDecoder {
  implicit val trackDecoder: Decoder[Track] = new Decoder[Track] {
    override def apply(c: HCursor): Result[Track] =
      for {
        addedAt <- c.downField("added_at").as[String]
        album <- c.downField("track").downField("album").downField("name").as[String]
        artists <- c.downField("track").downField("artists").as[List[Json]]
        artistsNames <- Traverse[List].traverse(artists)(_.hcursor.downField("name").as[String])
        name <- c.downField("track").downField("name").as[String]
        id <- c.downField("track").downField("id").as[Option[String]]
      } yield Track(id = id.getOrElse(""), name = name, artists = artistsNames, album = album, addedAt = LocalDateTime.parse(addedAt.init))
  }

  implicit val trackPageDecoder: Decoder[TrackPage] = new Decoder[TrackPage] {
    override def apply(c: HCursor): Result[TrackPage] =
      for {
        total <- c.downField("total").as[Int]
        next <- c.downField("next").as[Option[String]]
        items <- c.downField("items").as[List[Json]]
        tracks <- Traverse[List].traverse(items)(_.as[Track])
      } yield TrackPage(total = total, tracks = tracks, nextPage = next)
  }
}
