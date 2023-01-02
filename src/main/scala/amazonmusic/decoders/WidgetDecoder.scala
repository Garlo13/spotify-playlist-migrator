package amazonmusic.decoders

import amazonmusic.model.{Track, Widget}
import cats.Traverse
import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor, Json}

object WidgetDecoder {
  implicit val WidgetsDecoder: Decoder[List[Widget]] = new Decoder[List[Widget]] {
    override def apply(c: HCursor): Result[List[Widget]] =
      for {
        widgets <- c.downField("methods").downArray.downField("template").downField("widgets").as[List[Json]]
        widgets <- Traverse[List].traverse(widgets)(_.as[Widget])
      } yield widgets
  }
  implicit val WidgetDecoder: Decoder[Widget] = new Decoder[Widget] {
    override def apply(c: HCursor): Result[Widget] =
      for {
        header <- c.downField("header").as[String]
        maybeItems <- c.downField("items").as[Option[List[Json]]]
        items = maybeItems.getOrElse(List.empty)
        amazonTracks <- Traverse[List].traverse(items)(_.as[Track])
      } yield Widget(header = header, tracks = amazonTracks)
  }
  implicit val amazonTrackDecoder: Decoder[Track] = new Decoder[Track] {
    override def apply(c: HCursor): Result[Track] =
      for {
        id <- c.downField("iconButton").downField("observer").downField("storageKey").as[String]
        name <- c.downField("primaryText").downField("text").as[String]
        artist <- c.downField("secondaryText").as[Option[String]]
      } yield Track(id = id, name = name, artist = artist.getOrElse(""))
  }
}
