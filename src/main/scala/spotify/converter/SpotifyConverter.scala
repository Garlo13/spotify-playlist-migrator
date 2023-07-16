package spotify.converter

import spotify.model.{Playlist, Track}

class SpotifyConverter {

  def toModel(playlist: Playlist): model.Playlist =
    model.Playlist(
      id = playlist.id,
      name = playlist.name
    )
    
  def toModel(track: Track): model.Track =
    model.Track(
      id = track.id, name = track.name, artists = track.artists, album = track.album
    )
}
