package amazonmusic.converter

import amazonmusic.model.Track

class AmazonMusicConverter {

  def toModel(track: Track): model.Track =
    model.Track(
      id = track.id.split(":").last, name = track.name, artists = List(track.artist), album = "" //TODO: get the album and improve this
    )
}
