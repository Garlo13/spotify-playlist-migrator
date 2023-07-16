package deezer.decoders

import cats.Traverse
import deezer.model.Track
import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor, Json}

object SearchDecoder {
  
  implicit val tracksDecoder: Decoder[List[Track]] = new Decoder[List[Track]] {
    override def apply(c: HCursor): Result[List[Track]] =
      for {
        data <- c.downField("data").as[List[Json]]
        tracks <- Traverse[List].traverse(data)(_.as[Track])
      } yield tracks
  }

  implicit val trackDecoder: Decoder[Track] = new Decoder[Track] {
    override def apply(c: HCursor): Result[Track] =
      for {
        id <- c.downField("id").as[BigInt]
        title <- c.downField("title").as[String]
        artist <- c.downField("artist").downField("name").as[String]
        album <- c.downField("album").downField("title").as[String]
      } yield Track(id = id.toString, title = title, artist = artist, album = album)
  }
}
