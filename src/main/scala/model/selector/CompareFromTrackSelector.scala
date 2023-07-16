package model.selector
import model.Track

class CompareFromTrackSelector(trackComparator: TrackComparator) extends TrackSelector {

  override def select(fromTrack: Track, tracks: List[Track]): Option[Track] =
    tracks.find(track => trackComparator.areEquals(fromTrack, track))
}
