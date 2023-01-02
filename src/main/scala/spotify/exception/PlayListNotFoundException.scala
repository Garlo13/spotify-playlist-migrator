package spotify.exception

case class PlayListNotFoundException(message: String) extends RuntimeException(message)
