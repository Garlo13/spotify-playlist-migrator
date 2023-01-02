package spotify.model


import java.time.LocalDateTime

case class Track(name: String, artists: List[String], album: String, addedAt: LocalDateTime)

case class TrackPage(total: Int, tracks: List[Track], nextPage: Option[String])
