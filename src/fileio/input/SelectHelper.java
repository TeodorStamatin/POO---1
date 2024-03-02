package fileio.input;

import java.util.List;

public class SelectHelper {

    /**
     * This method returns the song with the given name from the library.
     * @param library the library from which the song is selected
     * @param songName the name of the song
     * @return the song with the given name
     */
    public static SongInput getSongByName(final LibraryInput library, final String songName) {
        for (SongInput song : library.getSongs()) {
            if (song.getName().equalsIgnoreCase(songName)) {
                return song;
            }
        }
        return null;
    }

    /**
     * This method returns the podcast with the given name from the library.
     * @param library the library from which the artist is selected
     * @param podcastName the name of the podcast
     * @return the podcast with the given name
     */
    public static PodcastInput getPodcastByName(final LibraryInput library,
                                                final String podcastName) {
        for (PodcastInput podcast : library.getPodcasts()) {
            if (podcast.getName().equalsIgnoreCase(podcastName)) {
                return podcast;
            }
        }
        return null;
    }

    /**
     * This method returns the playlist with the given name from the library.
     * @param library the library from which the artist is selected
     * @param playlistName the name of the playlist
     * @param userInformationList the list of users
     * @param username the username of the user
     * @return the playlist with the given name
     */
    public static Playlist getPlaylistByName(final LibraryInput library, final String playlistName,
                                             final List<UserInformation> userInformationList,
                                             final String username) {

        UserInformation user = getUserByUsername(userInformationList, username);

        for (Playlist playlist : user.getPlaylists()) {
            if (playlist.getName().equalsIgnoreCase(playlistName)) {
                return playlist;
            }
        }
        for (UserInformation userInformation : userInformationList) {
            for (Playlist playlist : userInformation.getPlaylists()) {
                if (playlist.getName().equalsIgnoreCase(playlistName)) {
                    return playlist;
                }
            }
        }
        return null;
    }

    /**
     * This method returns the user with the given name from the library.
     * @param userInformationList the list of users
     * @param username the username of the user
     * @return the user with the given name
     */
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
}
