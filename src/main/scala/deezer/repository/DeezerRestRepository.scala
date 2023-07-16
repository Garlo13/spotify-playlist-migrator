package deezer.repository

import deezer.converter.DeezerConverter
import deezer.decoders.SearchDecoder.tracksDecoder
import io.circe.parser.parse
import model.{MusicRepository, Playlist, Track}

import scala.util.chaining.scalaUtilChainingOps

class DeezerRestRepository(
  deezerConverter: DeezerConverter,
  playlistId: String,
  accessToken: String
) extends MusicRepository {

  private val SearchEndpoint = "https://api.deezer.com/search/track"
  private val AddTrackToPlaylistEndpoint = s"https://api.deezer.com/playlist/$playlistId/tracks"

  override def getPlaylists(): List[Playlist] = ???

  override def getTracks(playlistId: String): List[Track] = ???

  override def searchTrack(trackName: String, artist: String, album: String): Option[List[Track]] =
    requests
      .get(
        url = SearchEndpoint,
        params = Map(
          "access_token" -> accessToken,
          "q" -> s"artist:\"${artist.split("\\{").head}\" track:\"${trackName.split("\\(").head}\" album:\"$album\""
        )
      )
      .text()
      .pipe(
        parse(_)
          .flatMap(_.as[List[deezer.model.Track]])
          .toOption
      ).pipe(_.map(_.map(deezerConverter.toModel)))

  override def addTrackToPlaylist(id: String, name: String): Unit =
    requests
      .get(
        url = AddTrackToPlaylistEndpoint,
        params = Map(
          "access_token" -> accessToken,
          "songs" -> id,
          "request_method" -> "post"
        )
      )
}
