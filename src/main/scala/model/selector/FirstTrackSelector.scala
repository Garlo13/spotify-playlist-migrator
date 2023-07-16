package model.selector
import model.Track

class FirstTrackSelector() extends TrackSelector {

  override def select(fromTrack: Track, tracks: List[Track]): Option[Track] =
    tracks.headOption
}
