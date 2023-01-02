package spotify.decoders

import cats.Traverse
import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor, Json}
import spotify.model.Playlist

object SpotifyPlaylistDecoder {

  implicit val playlistPageDecoder: Decoder[List[Playlist]] = new Decoder[List[Playlist]] {
    override def apply(c: HCursor): Result[List[Playlist]] = {
      for {
        items <- c.downField("items").as[List[Json]]
        playlists <- Traverse[List].traverse(items)(_.as[Playlist])
      } yield playlists
    }
  }
  implicit val playListDecoder: Decoder[Playlist] = new Decoder[Playlist] {
    override def apply(c: HCursor): Result[Playlist] = {
      for {
        id <- c.downField("id").as[String]
        name <- c.downField("name").as[String]
        snapshotId <- c.downField("snapshot_id").as[String]
      } yield Playlist(id = id, name = name, snapshotId = snapshotId)
    }
  }
}
