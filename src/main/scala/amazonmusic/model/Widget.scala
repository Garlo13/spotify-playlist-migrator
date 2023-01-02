package amazonmusic.model

case class Widget(header: String, tracks: List[Track])

case class Track(id: String, name: String, artist: String)

