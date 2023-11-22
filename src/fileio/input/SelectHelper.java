package fileio.input;

import java.util.List;

public class SelectHelper {

    public static SongInput getSongByName(LibraryInput library, String songName) {
        for (SongInput song : library.getSongs()) {
            if (song.getName().equalsIgnoreCase(songName)) {
                return song;
            }
        }
        return null;
    }

    public static PodcastInput getPodcastByName(LibraryInput library, String podcastName) {
        for (PodcastInput podcast : library.getPodcasts()) {
            if (podcast.getName().equalsIgnoreCase(podcastName)) {
                return podcast;
            }
        }
        return null;
    }

    public static Playlist getPlaylistByName(LibraryInput library, String playlistName, List<UserInformation> userInformationList, String username) {

        UserInformation user = getUserByUsername(userInformationList, username);

        for (Playlist playlist : user.getPlaylists()) {
            if (playlist.getName().equalsIgnoreCase(playlistName)) {
                return playlist;
            }
        }
        return null;
    }

    private static UserInformation getUserByUsername(List<UserInformation> userInformationList, String username) {
        for (UserInformation user : userInformationList) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }
}
