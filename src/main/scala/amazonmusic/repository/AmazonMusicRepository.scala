package amazonmusic.repository

import amazonmusic.model.Track

trait AmazonMusicRepository {

  def searchTrack(trackName: String, artist: String, album: String): Option[List[Track]]

  def addTrackToPlaylist(id: String, name: String): Unit

}
