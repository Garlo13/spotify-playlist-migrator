package model.selector

import model.Track

import java.text.Normalizer
import java.text.Normalizer.Form

class TrackComparator {

  def areEquals(amazonMusicTrack: Track, spotifyTrack: Track): Boolean =
    haveSameArtist(spotifyTrack.artists.head, amazonMusicTrack.artists.head) && //TODO: Fix artist.head
      haveSameTrackName(spotifyTrack.name, amazonMusicTrack.name)

  private def haveSameArtist(spotifyArtistName: String, amazonMusicArtistName: String): Boolean = {
    val normalizedSpotifyArtistName = normalizeArtistName(sanitizeTrackName(spotifyArtistName.toLowerCase))
    val normalizedAmazonMusicArtistName = normalizeArtistName(sanitizeTrackName(amazonMusicArtistName.toLowerCase))

    normalizedAmazonMusicArtistName.startsWith(normalizedSpotifyArtistName) ||
      normalizedSpotifyArtistName.startsWith(normalizedAmazonMusicArtistName)
  }

  private def haveSameTrackName(spotifyTrackName: String, amazonMusicTrackName: String): Boolean = {
    val sanitizedSpotifyTrackName = sanitizeTrackName(normalizeArtistName(spotifyTrackName)).toLowerCase
    val sanitizedAmazonMusicTrackName = sanitizeTrackName(normalizeArtistName(amazonMusicTrackName)).toLowerCase

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
