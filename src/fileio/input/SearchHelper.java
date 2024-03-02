package fileio.input;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class SearchHelper {

    /**
     * Performs a search in the library for the specified type and filters
     * @param library The library to search in
     * @param type The type of the search (song, podcast, playlist)
     * @param filtersNode The JSON node containing the filters
     * @return A list of strings representing the names of the results
     */
    public static List<String> performSearch(final LibraryInput library, final String type,
                                             final JsonNode filtersNode,
                                             final List<UserInformation> userInformationList,
                                             final String username) {
        List<String> results = new ArrayList<>();
        int maxResults = 5;

        if ("song".equals(type)) {
            results.addAll(performSongSearch(library, filtersNode, maxResults));
        } else if ("podcast".equals(type)) {
            results.addAll(performPodcastSearch(library, filtersNode, maxResults));
        } else if ("playlist".equals(type)) {
            results.addAll(performPlaylistSearch(library, filtersNode, maxResults,
                    userInformationList, username));
        }

        return results;
    }

    private static UserInformation getUserByUsername(final List<UserInformation>
                                                             userInformationList,
                                                     final String username) {
        for (UserInformation user : userInformationList) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    private static List<String> performPlaylistSearch(final LibraryInput library,
                                                      final JsonNode filtersNode,
                                                      final int maxResults,
                                                      final List<UserInformation>
                                                              userInformationList,
                                                      final String username) {
        List<String> results = new ArrayList<>();

        String ownerFilter = filtersNode.has("owner") ? filtersNode.get("owner").asText() : null;
        String nameFilter = filtersNode.has("name") ? filtersNode.get("name").asText() : null;

        UserInformation user = getUserByUsername(userInformationList, username);

        for (Playlist playlist : user.getPlaylists()) {
            if ((nameFilter == null || playlist.getName().equalsIgnoreCase(nameFilter))) {
                if (results.size() < maxResults) {
                    results.add(playlist.getName());
                } else {
                    break;
                }
            }
        }

        if (results.size() == 0) {
            for (UserInformation userInformation : userInformationList) {
                if ((ownerFilter == null || userInformation.getUsername().
                        equalsIgnoreCase(ownerFilter))) {
                    for (Playlist playlist : userInformation.getPlaylists()) {
                        if ((nameFilter == null || playlist.getName().
                                equalsIgnoreCase(nameFilter))) {
                            if (results.size() < maxResults && playlist.
                                    getVisibilityStatus() == 1) {
                                results.add(playlist.getName());
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        }
        return results;
    }
    private static List<String> performSongSearch(final LibraryInput library,
                                                  final JsonNode filtersNode,
                                                  final int maxResults) {
        List<String> results = new ArrayList<>();

        String nameFilter = filtersNode.has("name") ? filtersNode.get("name").asText() : null;
        Integer durationFilter = filtersNode.has("duration") ? filtersNode.get("duration").
                asInt() : null;
        String albumFilter = filtersNode.has("album") ? filtersNode.get("album").asText() : null;
        List<String> tagsFilter = extractStringList(filtersNode, "tags");
        String lyricsFilter = filtersNode.has("lyrics") ? filtersNode.get("lyrics").
                asText() : null;
        String genreFilter = filtersNode.has("genre") ? filtersNode.get("genre").asText() : null;
        ReleaseYearFilter releaseYearFilter = extractReleaseYearFilter(filtersNode, "releaseYear");
        String artistFilter = filtersNode.has("artist") ? filtersNode.get("artist").
                asText() : null;

        for (SongInput song : library.getSongs()) {
            if ((nameFilter == null || song.getName().toLowerCase().startsWith(nameFilter.
                    toLowerCase()))
                    && (durationFilter == null || song.getDuration() == durationFilter)
                    && (albumFilter == null || song.getAlbum().equalsIgnoreCase(albumFilter))
                    && (tagsFilter.isEmpty() || song.getTags().containsAll(tagsFilter))
                    && (lyricsFilter == null || song.getLyrics().toLowerCase().
                    contains(lyricsFilter.toLowerCase()))
                    && (genreFilter == null || song.getGenre().equalsIgnoreCase(genreFilter))
                    && (releaseYearFilter == null || compareReleaseYear(song.
                    getReleaseYear(), releaseYearFilter))
                    && (artistFilter == null || song.getArtist().equalsIgnoreCase(artistFilter))) {
                if (results.size() < maxResults) {
                    results.add(song.getName());
                } else {
                    break;
                }
            }
        }

        return results;
    }

    private static List<String> performPodcastSearch(final LibraryInput library,
                                                     final JsonNode filtersNode,
                                                     final int maxResults) {
        List<String> results = new ArrayList<>();

        // Podcast search logic
        String nameFilter = filtersNode.has("name") ? filtersNode.get("name").asText() : null;
        String ownerFilter = filtersNode.has("owner") ? filtersNode.get("owner").asText() : null;
        List<String> episodesFilter = extractStringList(filtersNode, "episodes");

        for (PodcastInput podcast : library.getPodcasts()) {
            if ((nameFilter == null || podcast.getName().toLowerCase().startsWith(nameFilter.
                    toLowerCase()))
                    && (ownerFilter == null || podcast.getOwner().equalsIgnoreCase(ownerFilter))
                    && (episodesFilter.isEmpty() || containsAnyEpisode(podcast.
                    getEpisodes(), episodesFilter))) {
                if (results.size() < maxResults) {
                    results.add(podcast.getName());
                } else {
                    break;
                }
            }
        }

        return results;
    }

    private static boolean containsAnyEpisode(final List<EpisodeInput> episodes,
                                              final List<String> episodeNames) {
        for (EpisodeInput episode : episodes) {
            if (episodeNames.contains(episode.getName())) {
                return true;
            }
        }
        return false;
    }

    private static ReleaseYearFilter extractReleaseYearFilter(final JsonNode filtersNode,
                                                              final String fieldName) {
        if (filtersNode.has(fieldName)) {
            String filterValue = filtersNode.get(fieldName).asText();
            int comparison = 0;

            if (filterValue.startsWith(">")) {
                comparison = 1;
                filterValue = filterValue.substring(1);
            } else if (filterValue.startsWith("<")) {
                comparison = -1;
                filterValue = filterValue.substring(1);
            }

            int year = Integer.parseInt(filterValue);
            return new ReleaseYearFilter(year, comparison);
        }

        return null;
    }

    private static boolean compareReleaseYear(final int songReleaseYear,
                                              final ReleaseYearFilter releaseYearFilter) {
        int filterReleaseYear = releaseYearFilter.getYear();
        int comparison = releaseYearFilter.getComparison();

        if (comparison == 1) {
            return songReleaseYear > filterReleaseYear;
        } else if (comparison == -1) {
            return songReleaseYear < filterReleaseYear;
        } else {
            return songReleaseYear == filterReleaseYear;
        }
    }


    private static List<String> extractStringList(final JsonNode node, final String fieldName) {
        List<String> result = new ArrayList<>();

        if (node.has(fieldName)) {
            JsonNode arrayNode = node.get(fieldName);

            if (arrayNode.isArray()) {
                for (JsonNode item : arrayNode) {
                    result.add(item.asText());
                }
            }
        }

        return result;
    }
    private static class ReleaseYearFilter {
        private final int year;
        private final int comparison;

        ReleaseYearFilter(final int year, final int comparison) {
            this.year = year;
            this.comparison = comparison;
        }

        public int getYear() {
            return year;
        }

        public int getComparison() {
            return comparison;
        }
    }
}
