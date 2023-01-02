import amazonmusic.repository.AmazonMusicRepository
import amazonmusic.model.{Track => AmazonMusicTrack}
import spotify.model.{Playlist, Track}
import spotify.repository.SpotifyRepository

import scala.util.chaining._

class MigratePlaylistUseCase(
  spotifyRepository: SpotifyRepository,
  amazonMusicRepository: AmazonMusicRepository
) {

  def migratePlaylist(spotifyPlaylistName: String): Unit =
    getSpotifyTracks(spotifyPlaylistName).pipe(migrateTracksToAmazonMusic)

  private def getSpotifyTracks(spotifyPlaylistName: String): List[Track] =
    spotifyRepository
      .getPlaylists()
      .pipe(findPlayList(_, spotifyPlaylistName))
      .pipe(playlist => spotifyRepository.getTracks(playlist.id))

  private def findPlayList(playlists: List[Playlist], playlistName: String): Playlist =
    playlists.find(_.name == playlistName).getOrElse(throw PlayListNotFoundException(s"Play list not found"))

  private def migrateTracksToAmazonMusic(tracks: List[Track]): Unit =
    tracks.foreach { spotifyTrack =>
      searchTrack(spotifyTrack)
        .tap(printLogIfNoResult(_, spotifyTrack))
        .pipe(selectAmazonMusicTrack(_, spotifyTrack))
        .tap(printLogIfNoTargetTrack(_, spotifyTrack))
        .pipe(addTrackToAmazonMusicPlaylist(_))
    }

  private def searchTrack(spotifyTrack: Track): Option[List[AmazonMusicTrack]] =
    amazonMusicRepository.searchTrack(spotifyTrack.name, spotifyTrack.artists.head, spotifyTrack.album)

  private def printLogIfNoResult(searchResult: Option[List[AmazonMusicTrack]], spotifyTrack: Track): Unit =
    searchResult.getOrElse(println(s"No result for ${spotifyTrack.name}, ${spotifyTrack.artists.head} in amazon"))

  private def printLogIfNoTargetTrack(maybeAmazonMusicTrack: Option[AmazonMusicTrack], spotifyTrack: Track): Unit =
    maybeAmazonMusicTrack
      .getOrElse(
        println(s"The searched result does not contain track ${spotifyTrack.name}, ${spotifyTrack.artists.head}")
      )
  
  private def selectAmazonMusicTrack(
    searchResult: Option[List[AmazonMusicTrack]], 
    spotifyTrack: Track
  ): Option[AmazonMusicTrack] =
    searchResult.flatMap { amazonMusicTracks =>
      amazonMusicTracks.find(selectTargetTrack(_, spotifyTrack))
    }

  private def selectTargetTrack(amazonMusicTrack: AmazonMusicTrack, spotifyTrack: Track): Boolean = {
    val sanitizedTrackName = sanitizeTrackName(spotifyTrack.name)

    (amazonMusicTrack.name.toLowerCase.startsWith(sanitizedTrackName.toLowerCase) ||
      sanitizedTrackName.toLowerCase.startsWith(amazonMusicTrack.name.toLowerCase)) &&
      (amazonMusicTrack.artist.toLowerCase.startsWith(spotifyTrack.artists.head.toLowerCase) ||
        spotifyTrack.artists.head.toLowerCase.startsWith(amazonMusicTrack.artist.toLowerCase))
  }

  private def sanitizeTrackName(trackName: String): String =
    trackName.split("-|\\(").headOption.getOrElse(trackName)

  private def addTrackToAmazonMusicPlaylist(maybeTrack: Option[AmazonMusicTrack]): Unit =
    maybeTrack.foreach(amazonMusicTrack =>
      amazonMusicRepository.addTrackToPlaylist(amazonMusicTrack.id.split(":").last, amazonMusicTrack.name)
    )

  case class PlayListNotFoundException(message: String) extends RuntimeException(message)

}
