import amazonmusic.converter.AmazonMusicConverter
import amazonmusic.repository.AmazonMusicRestRepository
import deezer.converter.DeezerConverter
import deezer.repository.DeezerRestRepository
import model.selector.{CompareFromTrackSelector, FirstTrackSelector, TrackComparator}
import spotify.converter.SpotifyConverter
import spotify.repository.SpotifyRestRepository

import scala.io.StdIn.readLine

object Main extends App {

  val RefreshToken: String =  System.getenv("SPOTIFY_REFRESH_TOKEN")
  val ClientId: String =  System.getenv("SPOTIFY_CLIENT_ID")
  val SecretId: String =  System.getenv("SPOTIFY_SECRET_ID")

  val DeezerAccessToken: String = System.getenv("DEEZER_ACCESS_TOKEN")
  val DeezerPlaylistId: String = System.getenv("DEEZER_PLAYLIST_ID")

  val AmazonMusicAuthenticationHeader: String = System.getenv("AMAZONMUSIC_AUTHENTICATION_HEADER")
  val AmazonMusicPlaylistId: String = System.getenv("AMAZONMUSIC_PLAYLIST_ID")

  val spotifyConverter = new SpotifyConverter
  val amazonMusicConverter = new AmazonMusicConverter
  val deezerConverter = new DeezerConverter

  val spotifyRepository = new SpotifyRestRepository(spotifyConverter, ClientId, SecretId, RefreshToken)
  val amazonMusicRepository = new AmazonMusicRestRepository(amazonMusicConverter, AmazonMusicAuthenticationHeader, AmazonMusicPlaylistId)
  val deezerRepository = new DeezerRestRepository(deezerConverter, DeezerPlaylistId, DeezerAccessToken)

  val trackComparator = new TrackComparator
  val compareFromTrackSelector = new CompareFromTrackSelector(trackComparator)
  val firstTrackSelector = new FirstTrackSelector()

  val useCase = new MigratePlaylistUseCase(spotifyRepository, deezerRepository, firstTrackSelector)

  print("Enter the Spotify playlist name to migrate: ")
  val spotifyPlaylistName = readLine()

  useCase.migrate(spotifyPlaylistName)
}
