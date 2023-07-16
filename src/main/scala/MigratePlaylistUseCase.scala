import model.selector.TrackSelector
import model.{MusicRepository, Playlist, Track}

import scala.util.chaining._

class MigratePlaylistUseCase(
  fromRepository: MusicRepository,
  toRepository: MusicRepository,
  trackSelector: TrackSelector
) {

  type NonMigratedTracks = List[Track]
  type MigratedTracks = List[Track]

  def migrate(spotifyPlaylistName: String): Unit =
    getTracks(fromRepository, spotifyPlaylistName)
      .pipe(migrateTracks)
      .tap { case (nonMigratedTracks, _) => printNotMigratedTracks(nonMigratedTracks) }

  private def getTracks(repository: MusicRepository, spotifyPlaylistName: String): List[Track] =
    repository
      .getPlaylists()
      .pipe(findPlayList(_, spotifyPlaylistName))
      .pipe(playlist => fromRepository.getTracks(playlist.id))

  private def findPlayList(playlists: List[Playlist], playlistName: String): Playlist =
    playlists.find(_.name == playlistName).getOrElse(throw PlayListNotFoundException(s"Play list not found"))

  private def migrateTracks(tracks: List[Track]): (NonMigratedTracks, MigratedTracks) =
    tracks.map(spotifyTrack =>
      searchTrack(spotifyTrack)
        .pipe(selectTrack(_, spotifyTrack))
        .tap(addTrackToAmazonMusicPlaylist)
        .pipe(_.toRight(spotifyTrack))
    ).partitionMap(identity)

  private def searchTrack(spotifyTrack: Track): Option[List[Track]] =
    toRepository
      .searchTrack(spotifyTrack.name, spotifyTrack.artists.head, spotifyTrack.album)
      .flatMap(searchIfEmptyResult(_, spotifyTrack, searchTrackWithoutAlbumName))
      .flatMap(searchIfEmptyResult(_, spotifyTrack, searchTrackWithoutArtistAndAlbumName))
      .tap(printLogIfNoResult(_, spotifyTrack))

  private def searchIfEmptyResult(
    searchResult: List[Track],
    trackToSearch: Track,
    searchFn: Track => Option[List[Track]]
  ): Option[List[Track]] =
    searchResult match {
      case Nil => searchFn(trackToSearch)
      case nonEmpty => Some(nonEmpty)
    }

  private def selectTrack(
    searchResult: Option[List[Track]],
    fromTrack: Track
  ): Option[Track] =
    searchResult.flatMap(trackSelector.select(fromTrack, _))

  private def searchTrackWithoutAlbumName(track: Track): Option[List[Track]] =
    toRepository.searchTrack(track.name, track.artists.head, "")

  private def searchTrackWithoutArtistAndAlbumName(track: Track): Option[List[Track]] =
    toRepository.searchTrack(track.name, "", "")

  private def printLogIfNoResult(searchResult: Option[List[Track]], track: Track): Unit =
    searchResult.getOrElse(println(s"No result for ${track.name}, ${track.artists.head}"))

  private def addTrackToAmazonMusicPlaylist(maybeTrack: Option[Track]): Unit =
    maybeTrack.foreach(amazonMusicTrack =>
      toRepository.addTrackToPlaylist(amazonMusicTrack.id, amazonMusicTrack.name)
    )

  private def printNotMigratedTracks(tracks: NonMigratedTracks): Unit = {
    println("Non Migrated tracks:")
    tracks.foreach(track =>
      println(s"${track.name}, ${track.artists.mkString(",")}")
    )
  }

  case class PlayListNotFoundException(message: String) extends RuntimeException(message)

}
