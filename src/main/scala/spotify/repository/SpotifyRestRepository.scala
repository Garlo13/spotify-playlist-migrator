package spotify.repository
import io.circe.Json
import io.circe.parser.parse
import spotify.decoders.SpotifyPlaylistDecoder.playlistPageDecoder
import spotify.decoders.TrackDecoder.trackPageDecoder
import spotify.exception.PlayListNotFoundException
import spotify.model.{Playlist, Track, TrackPage}

import java.nio.charset.StandardCharsets
import java.util.Base64
import scala.annotation.tailrec
import scala.util.chaining._

class SpotifyRestRepository(
  clientId: String,
  secretId: String,
  refreshToken: String
) extends SpotifyRepository {

  private val RefreshTokenEndpoint: String = "https://accounts.spotify.com/api/token"
  private val CurrentUserPlaylistEndpoint: String = "https://api.spotify.com/v1/me/playlists"
  private val PlaylistEndpoint: String = "https://api.spotify.com/v1/playlists/playlist_id/tracks"

  private val Base64Encoder = Base64.getEncoder
  private val EncodedClientSecretId: String = Base64Encoder.encodeToString(s"$clientId:$secretId".getBytes(StandardCharsets.UTF_8))
  private val accessToken: String = getAccessToken()

  override def getTracks(playlistId: String): List[Track] = {
    @tailrec
    def iterate(currentPage: TrackPage, accumulatedTracks: List[Track]): List[Track] = {
      currentPage.nextPage match {
        case Some(nextPageUrl) =>
          val nextPage = getPage(nextPageUrl)
          iterate(nextPage, accumulatedTracks ::: nextPage.tracks)
        case None => accumulatedTracks
      }
    }

    val firstPage = getPage(PlaylistEndpoint.replace("playlist_id", playlistId))

    val accumulatedTracks = firstPage.tracks

    iterate(firstPage, accumulatedTracks)
  }

  override def getPlaylists(): List[Playlist] =
    requests
      .get(
        CurrentUserPlaylistEndpoint,
        headers = Map("Authorization" -> s"Bearer $accessToken")
      )
      .text()
      .pipe(
        parse(_)
          .flatMap(_.as[List[Playlist]])
          .getOrElse(List.empty)
      )

  private def getPage(pageUrl: String): TrackPage =
    requests
      .get(
        pageUrl,
        headers = Map("Authorization" -> s"Bearer $accessToken")
      )
      .text()
      .pipe(
        parse(_)
          .flatMap(_.as[TrackPage]) match {
          case Right(trackPage) => trackPage
          case Left(error) => throw PlayListNotFoundException(error.getMessage)
        }
      )

  private def getAccessToken(): String =
    requests
      .post(
        RefreshTokenEndpoint,
        data = Map("grant_type" -> "refresh_token", "refresh_token" -> refreshToken),
        headers = Map("Authorization" -> s"Basic $EncodedClientSecretId")
      )
      .text()
      .pipe(
        parse(_)
          .getOrElse(Json.Null)
          .hcursor
          .get[String]("access_token")
          .getOrElse("")
      )
}
