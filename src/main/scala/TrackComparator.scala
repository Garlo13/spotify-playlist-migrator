import spotify.model.Track
import amazonmusic.model.{Track => AmazonMusicTrack}

import java.text.Normalizer
import java.text.Normalizer.Form

class TrackComparator {

  def areEquals(amazonMusicTrack: AmazonMusicTrack, spotifyTrack: Track): Boolean =
    haveSameArtist(spotifyTrack.artists, amazonMusicTrack.artist) &&
      haveSameTrackName(spotifyTrack.name, amazonMusicTrack.name)

  private def haveSameArtist(spotifyArtistNames: List[String], amazonMusicArtistName: String): Boolean = {
    val normalizedSpotifyArtistName = normalizeArtistName(spotifyArtistNames.head.toLowerCase)
    val normalizedAmazonMusicArtistName = normalizeArtistName(amazonMusicArtistName.toLowerCase)

    normalizedAmazonMusicArtistName.startsWith(normalizedSpotifyArtistName) ||
      normalizedSpotifyArtistName.startsWith(normalizedAmazonMusicArtistName)
  }

  private def haveSameTrackName(spotifyTrackName: String, amazonMusicTrackName: String): Boolean = {
    val sanitizedSpotifyTrackName = sanitizeTrackName(spotifyTrackName).toLowerCase
    val sanitizedAmazonMusicTrackName = sanitizeTrackName(amazonMusicTrackName).toLowerCase

    sanitizedAmazonMusicTrackName.startsWith(sanitizedSpotifyTrackName.toLowerCase) ||
      sanitizedSpotifyTrackName.startsWith(sanitizedAmazonMusicTrackName) ||
      sanitizedSpotifyTrackName.contains(sanitizedAmazonMusicTrackName)
  }

  private def normalizeArtistName(artistName: String): String =
    Normalizer
      .normalize(artistName, Form.NFD)
      .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");

  private def sanitizeTrackName(trackName: String): String =
    trackName.split("-|\\(|\\[").headOption.map(_.trim).getOrElse(trackName)

}
