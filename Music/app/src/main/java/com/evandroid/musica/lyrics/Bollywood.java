/*
 * *
 *  * This file is part of QuickLyric
 *  * Created by geecko
 *  *
 *  * QuickLyric is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * QuickLyric is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  * You should have received a copy of the GNU General Public License
 *  * along with QuickLyric.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.evandroid.musica.lyrics;

import com.evandroid.musica.annotations.Reflection;
import com.evandroid.musica.utils.Net;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

@Reflection
public class Bollywood {

    @Reflection
    public static final String domain = "quicklyric.azurewebsites.net/bollywood/";

    public static ArrayList<Lyrics> search(String query) {
        ArrayList<Lyrics> results = new ArrayList<>();
        String searchUrl = "http://quicklyric.azurewebsites.net/bollywood/search.php?q=%s";
        try {
            String jsonText;
            jsonText = Net.getUrlAsString(String.format(searchUrl, URLEncoder.encode(query, "utf-8")));
            JsonObject jsonResponse = new JsonParser().parse(jsonText).getAsJsonObject();
            JsonArray lyricsResults = jsonResponse.getAsJsonArray("lyrics");
            for (int i = 0; i < lyricsResults.size(); ++i) {
                JsonObject lyricsResult = lyricsResults.get(i).getAsJsonObject();
                JsonArray tags = lyricsResult.get("tags").getAsJsonArray();
                Lyrics lyrics = new Lyrics(Lyrics.SEARCH_ITEM);
                lyrics.setTitle(lyricsResult.get("name").getAsString());
                for (int j = 0; i < tags.size(); ++j) {
                    JsonObject tag = tags.get(j).getAsJsonObject();
                    if (tag.get("tag_type").getAsString().equals("Singer")) {
                        lyrics.setArtist(tag.get("name").getAsString().trim());
                        break;
                    }
                }
                lyrics.setURL("http://quicklyric.azurewebsites.net/bollywood/get.php?id=" + lyricsResult.get("id").getAsInt());
                results.add(lyrics);
            }
        } catch (IOException | JsonParseException e) {
            e.printStackTrace();
        }
        return results;
    }

    @Reflection
    public static Lyrics fromMetaData(String artist, String title) {
        ArrayList<Lyrics> searchResults = search(artist + " " + title);
        for (Lyrics result : searchResults) {
            if (artist.contains(result.getArtist()) && title.equalsIgnoreCase(result.getTrack()))
                return fromAPI(result.getURL(), artist, result.getTrack());
        }
        return new Lyrics(Lyrics.NO_RESULT);
    }

    // TODO handle urls
    @Reflection
    public static Lyrics fromURL(String url, String artist, String title) {
        return fromAPI(url, artist, title);
    }

    public static Lyrics fromAPI(String url, String artist, String title) {
        Lyrics lyrics = new Lyrics(Lyrics.POSITIVE_RESULT);
        lyrics.setArtist(artist);
        lyrics.setTitle(title);
        // fixme no public url
        try {
            String jsonText = Net.getUrlAsString(url);
            JsonObject lyricsJSON = new JsonParser().parse(jsonText).getAsJsonObject();
            lyrics.setText(lyricsJSON.get("body").getAsString().trim());
        } catch (IOException | JsonParseException e) {
            e.printStackTrace();
            return new Lyrics(Lyrics.ERROR);
        }
        lyrics.setSource(domain);
        return lyrics;
    }
}
