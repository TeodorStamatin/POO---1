package fileio.input;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import fileio.input.UserInformation;

public class SearchHelper {

    public static List<String> performSearch(LibraryInput library, String type, JsonNode filtersNode, List<UserInformation> userInformationList, String username) {
        List<String> results = new ArrayList<>();
        int maxResults = 5;

        // Perform search based on the type and filters
        if ("song".equals(type)) {
            results.addAll(performSongSearch(library, filtersNode, maxResults));
        } else if ("podcast".equals(type)) {
            results.addAll(performPodcastSearch(library, filtersNode, maxResults));
        } else if ("playlist".equals(type)) {
            results.addAll(performPlaylistSearch(library, filtersNode, maxResults, userInformationList, username));
        }

        return results;
    }
    private static UserInformation getUserByUsername(List<UserInformation> userInformationList, String username) {
        for (UserInformation user : userInformationList) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    private static List<String> performPlaylistSearch(LibraryInput library, JsonNode filtersNode, int maxResults, List<UserInformation> userInformationList, String username) {
        List<String> results = new ArrayList<>();

        String ownerFilter = filtersNode.has("owner") ? filtersNode.get("owner").asText() : null;
        String nameFilter = filtersNode.has("name") ? filtersNode.get("name").asText() : null;

        UserInformation user = getUserByUsername(userInformationList, username);

        for(Playlist playlist : user.getPlaylists()) {
            if((nameFilter == null || playlist.getName().equalsIgnoreCase(nameFilter))) {
                if (results.size() < maxResults) {
                    results.add(playlist.getName());
                } else {
                    break; // Break out of the loop if the max results limit is reached
                }
            }
        }

        if(results.size() == 0) {
            for(UserInformation userInformation : userInformationList) {
                if((ownerFilter == null || userInformation.getUsername().equalsIgnoreCase(ownerFilter))) {
                    for(Playlist playlist : userInformation.getPlaylists()) {
                        if((nameFilter == null || playlist.getName().equalsIgnoreCase(nameFilter))) {
                            if (results.size() < maxResults && playlist.getVisibilityStatus() == 1) {
                                results.add(playlist.getName());
                            } else {
                                break; // Break out of the loop if the max results limit is reached
                            }
                        }
                    }
                }
            }
        }
        return results;
    }
    private static List<String> performSongSearch(LibraryInput library, JsonNode filtersNode, int maxResults) {
        List<String> results = new ArrayList<>();

        // Extract filters
        String nameFilter = filtersNode.has("name") ? filtersNode.get("name").asText() : null;
        Integer durationFilter = filtersNode.has("duration") ? filtersNode.get("duration").asInt() : null;
        String albumFilter = filtersNode.has("album") ? filtersNode.get("album").asText() : null;
        List<String> tagsFilter = extractStringList(filtersNode, "tags");
        String lyricsFilter = filtersNode.has("lyrics") ? filtersNode.get("lyrics").asText() : null;
        String genreFilter = filtersNode.has("genre") ? filtersNode.get("genre").asText() : null;
        ReleaseYearFilter releaseYearFilter = extractReleaseYearFilter(filtersNode, "releaseYear");
        String artistFilter = filtersNode.has("artist") ? filtersNode.get("artist").asText() : null;

        // Iterate over each song in the library
        for (SongInput song : library.getSongs()) {
            // Check if the song matches the filters
            if ((nameFilter == null || song.getName().toLowerCase().startsWith(nameFilter.toLowerCase()))
                    && (durationFilter == null || song.getDuration() == durationFilter)
                    && (albumFilter == null || song.getAlbum().equalsIgnoreCase(albumFilter))
                    && (tagsFilter.isEmpty() || song.getTags().containsAll(tagsFilter))
                    && (lyricsFilter == null || song.getLyrics().toLowerCase().contains(lyricsFilter.toLowerCase()))
                    && (genreFilter == null || song.getGenre().equalsIgnoreCase(genreFilter))
                    && (releaseYearFilter == null || compareReleaseYear(song.getReleaseYear(), releaseYearFilter))
                    && (artistFilter == null || song.getArtist().equalsIgnoreCase(artistFilter))) {
                // Add the song to the results, and limit to maxResults
                if (results.size() < maxResults) {
                    results.add(song.getName());
                } else {
                    break; // Break out of the loop if the max results limit is reached
                }
            }
        }

        return results;
    }

    private static List<String> performPodcastSearch(LibraryInput library, JsonNode filtersNode, int maxResults) {
        List<String> results = new ArrayList<>();

        // Podcast search logic
        String nameFilter = filtersNode.has("name") ? filtersNode.get("name").asText() : null;
        String ownerFilter = filtersNode.has("owner") ? filtersNode.get("owner").asText() : null;
        List<String> episodesFilter = extractStringList(filtersNode, "episodes");

        // Iterate over each podcast in the library
        for (PodcastInput podcast : library.getPodcasts()) {
            // Check if the podcast matches the filters
            if ((nameFilter == null || podcast.getName().toLowerCase().startsWith(nameFilter.toLowerCase()))
                    && (ownerFilter == null || podcast.getOwner().equalsIgnoreCase(ownerFilter))
                    && (episodesFilter.isEmpty() || containsAnyEpisode(podcast.getEpisodes(), episodesFilter))) {
                // Add the podcast to the results, and limit to maxResults
                if (results.size() < maxResults) {
                    results.add(podcast.getName());
                } else {
                    break; // Break out of the loop if the max results limit is reached
                }
            }
        }

        return results;
    }

    private static boolean containsAnyEpisode(List<EpisodeInput> episodes, List<String> episodeNames) {
        for (EpisodeInput episode : episodes) {
            if (episodeNames.contains(episode.getName())) {
                return true;
            }
        }
        return false;
    }

    private static ReleaseYearFilter extractReleaseYearFilter(JsonNode filtersNode, String fieldName) {
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

    private static boolean compareReleaseYear(int songReleaseYear, ReleaseYearFilter releaseYearFilter) {
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

    private static List<String> extractStringList(JsonNode node, String fieldName) {
        List<String> result = new ArrayList<>();

        // Check if the specified field exists in the JSON node
        if (node.has(fieldName)) {
            // Retrieve the JSON node associated with the specified field
            JsonNode arrayNode = node.get(fieldName);

            // Check if the node associated with the field is an array
            if (arrayNode.isArray()) {
                // Iterate over each element in the array
                for (JsonNode item : arrayNode) {
                    // Convert each element to a string and add it to the result list
                    result.add(item.asText());
                }
            }
        }

        // Return the list of strings
        return result;
    }

    // Enum to represent the comparison operators
    private static class ReleaseYearFilter {
        private final int year;
        private final int comparison;

        public ReleaseYearFilter(int year, int comparison) {
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
