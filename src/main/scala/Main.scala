import amazonmusic.repository.AmazonMusicRestRepository
import spotify.repository.SpotifyRestRepository

import scala.io.StdIn.readLine

object Main extends App {

  val RefreshToken: String =  System.getenv("SPOTIFY_REFRESH_TOKEN")
  val ClientId: String =  System.getenv("SPOTIFY_CLIENT_ID")
  val SecretId: String =  System.getenv("SPOTIFY_SECRET_ID")

  val AmazonMusicAuthenticationHeader: String = System.getenv("AMAZONMUSIC_AUTHENTICATION_HEADER")
  val AmazonMusicPlaylistId: String = System.getenv("AMAZONMUSIC_PLAYLIST_ID")

  val spotifyRepository = new SpotifyRestRepository(ClientId, SecretId, RefreshToken)
  val amazonMusicRepository = new AmazonMusicRestRepository(AmazonMusicAuthenticationHeader, AmazonMusicPlaylistId)

  val useCase = new MigratePlaylistUseCase(spotifyRepository, amazonMusicRepository)

  print("Enter the Spotify playlist name to migrate: ")
  val spotifyPlaylistName = readLine()

  useCase.migratePlaylist(spotifyPlaylistName)
}
