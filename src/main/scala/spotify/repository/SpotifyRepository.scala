package spotify.repository

import spotify.model.{Playlist, Track}

trait SpotifyRepository {

  def getTracks(playlistId: String): List[Track]

  def getPlaylists(): List[Playlist]

}
