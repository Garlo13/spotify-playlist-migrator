package deezer.converter

import deezer.model.Track

class DeezerConverter {

  def toModel(track: Track): model.Track =
    model.Track(
      id = track.id,
      name = track.title,
      artists = List(track.artist),
      album = track.album
    )

}
