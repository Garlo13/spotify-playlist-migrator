package model

trait MusicRepository {

  def getPlaylists(): List[Playlist]

  def getTracks(playlistId: String): List[Track]

  def searchTrack(trackName: String, artist: String, album: String): Option[List[Track]]

  def addTrackToPlaylist(id: String, name: String): Unit

}
