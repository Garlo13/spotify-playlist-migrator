package model.selector

import model.Track

trait TrackSelector {

  def select(fromTrack: Track, tracks: List[Track]): Option[Track]
}
