import model.{MusicRepository, Playlist, Track}

import scala.util.chaining._

class MigratePlaylistUseCase(
  fromRepository: MusicRepository,
  toRepository: MusicRepository,
  trackComparator: TrackComparator
) {

  def migrate(spotifyPlaylistName: String): Unit =
    getTracks(fromRepository, spotifyPlaylistName).pipe(migrateTracks)


//  def migratePlaylist(spotifyPlaylistName: String): Unit =
//    getTracks(spotifyPlaylistName).pipe(migrateTracks)

  private def getTracks(repository: MusicRepository, spotifyPlaylistName: String): List[Track] =
    repository
      .getPlaylists()
      .pipe(findPlayList(_, spotifyPlaylistName))
      .pipe(playlist => fromRepository.getTracks(playlist.id))

  private def findPlayList(playlists: List[Playlist], playlistName: String): Playlist =
    playlists.find(_.name == playlistName).getOrElse(throw PlayListNotFoundException(s"Play list not found"))

  private def migrateTracks(tracks: List[Track]): Unit =
    tracks.foreach { spotifyTrack =>
      searchTrack(spotifyTrack)
        .pipe(selectAmazonMusicTrack(_, spotifyTrack))
        .pipe(tryDifferentSearchIfNoTrackSelected(_, spotifyTrack))
        .pipe(addTrackToAmazonMusicPlaylist)
    }

  private def searchTrack(spotifyTrack: Track): Option[List[Track]] =
    toRepository.searchTrack(spotifyTrack.name, spotifyTrack.artists.head, spotifyTrack.album)

  private def selectAmazonMusicTrack(
    searchResult: Option[List[Track]],
    spotifyTrack: Track
  ): Option[Track] =
    searchResult.flatMap(amazonMusicTracks => amazonMusicTracks.find(trackComparator.areEquals(_, spotifyTrack)))

  private def tryDifferentSearchIfNoTrackSelected(
    maybeTargetTrack: Option[Track],
    spotifyTrack: Track
  ): Option[Track] =
    maybeTargetTrack.orElse(
      searchTrackWithoutAlbumName(spotifyTrack)
        .tap(printLogIfNoResult(_, spotifyTrack))
        .pipe(selectAmazonMusicTrack(_, spotifyTrack))
        .tap(printLogIfNoTargetTrack(_, spotifyTrack))
    )

  private def searchTrackWithoutAlbumName(spotifyTrack: Track): Option[List[Track]] =
    toRepository.searchTrack(spotifyTrack.name, spotifyTrack.artists.head, "")

  private def printLogIfNoResult(searchResult: Option[List[Track]], spotifyTrack: Track): Unit =
    searchResult.getOrElse(println(s"No result for ${spotifyTrack.name}, ${spotifyTrack.artists.head} in amazon"))

  private def printLogIfNoTargetTrack(maybeAmazonMusicTrack: Option[Track], spotifyTrack: Track): Unit =
    maybeAmazonMusicTrack
      .getOrElse(
        println(s"The searched result does not contain track ${spotifyTrack.name}, ${spotifyTrack.artists.head}")
      )

  private def addTrackToAmazonMusicPlaylist(maybeTrack: Option[Track]): Unit =
    maybeTrack.foreach(amazonMusicTrack =>
      toRepository.addTrackToPlaylist(amazonMusicTrack.id, amazonMusicTrack.name)
    )

  case class PlayListNotFoundException(message: String) extends RuntimeException(message)

}
