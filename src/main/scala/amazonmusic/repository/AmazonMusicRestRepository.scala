package amazonmusic.repository

import amazonmusic.decoders.WidgetDecoder.WidgetsDecoder
import amazonmusic.model.{Track, Widget}
import io.circe.parser.parse

import scala.util.chaining._

class AmazonMusicRestRepository(
  amazonAuthenticationHeader: String,
  amazonPlayListId: String
) extends AmazonMusicRepository {

  val amazonFindTrackEndpoint: String = "https://eu.mesk.skill.music.a2z.com/api/showSearch"
  val amazonFindTrackPayload: Map[String, String] =
    Map(
      "filter" -> """{\"IsLibrary\":[\"false\"]}""",
      "headers" -> amazonAuthenticationHeader,
      "keyword" -> """{\"interface\":\"Web.TemplatesInterface.v1_0.Touch.SearchTemplateInterface.SearchKeywordClientInformation\",\"keyword\":\"\"}""",
      "userHash" -> """{\"level\":\"SONIC_RUSH_MEMBER\"}"""
    )

  val amazonAddTrackToPlaylistEndpoint: String = "https://eu.mesk.skill.music.a2z.com/api/addTrackToPlaylist"
  val amazonAddTrackToPlaylistPayload: Map[String, String] =
    Map(
      "headers" -> amazonAuthenticationHeader,
      "isTrackInLibrary" -> "false",
      "playlistId" -> amazonPlayListId,
      "playlistTitle" -> "Test",
      "rejectDuplicate" -> "true",
      "shouldReplaceAddedTrack" -> "false",
      "userHash" -> """{\"level\":\"SONIC_RUSH_MEMBER\"}""",
      "version" -> "18"
    )

  override def searchTrack(trackName: String, artist: String, album: String): Option[List[Track]] = {
    val payload = amazonFindTrackPayload ++ List(
      "suggestedKeyword" ->
        s"${sanitizeName(trackName)} $artist ${sanitizeName(album)}",
    )

    requests
      .post(url = amazonFindTrackEndpoint, data = toJsonString(payload))
      .text
      .pipe(
        parse(_)
          .flatMap(_.as[List[Widget]])
          .map(_.find(_.header == "Canciones"))
          .map(_.map(_.tracks))
          .toOption
          .flatten
      )
  }

  override def addTrackToPlaylist(id: String, name: String): Unit = {
    val payload = amazonAddTrackToPlaylistPayload ++ List(
      "trackId" -> id,
      "trackTitle" -> name.replace("\"", "\\\"")
    )
    requests
      .post(url = amazonAddTrackToPlaylistEndpoint, data = toJsonString(payload))
  }

  private def sanitizeName(trackName: String): String =
    trackName
      .replace("\\", "\\\\")
      .replace("\"", "\\\"")

  private def toJsonString(map: Map[String, String]): String = {
    val values = map
      .map(tuple => s"""\"${tuple._1}\":\"${tuple._2}\"""")
      .toList
      .mkString(",")
    s"{$values}"
  }
}
