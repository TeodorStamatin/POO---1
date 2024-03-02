package fileio.input;
import java.util.ArrayList;
import java.util.List;

public class UserInformation {
    private String username;
    private int age;
    private String city;
    private List<String> likedSongs;
    private List<Playlist> playlists;
    private List<String> followedPlaylists;

    public UserInformation(final String username, final int age, final String city) {
        this.username = username;
        this.age = age;
        this.city = city;
        this.likedSongs = new ArrayList<>();
        this.playlists = new ArrayList<>();
        this.followedPlaylists = new ArrayList<>();
    }

    /**
     * @return the name of the user
     */
    public String getUsername() {
        return username;
    }
    /**
     * @return the playlist of liked songs
     */
    public List<String> getLikedSongs() {
        return likedSongs;
    }

    /**
     * Method that adds a song to the list of liked songs
     * @param songName the name of the song to be added
     */
    public void likeSong(final String songName) {
        likedSongs.add(songName);
    }
    /**
     * Method that removes a song from the list of liked songs
     * @param songName the name of the song to be removed
     */
    public void unlikeSong(String songName) {
        likedSongs.remove(songName);
    }
    /**
     *  Getter for the playlists
     *  @return the list of playlists
     */
    public List<Playlist> getPlaylists() {
        return playlists;
    }
    /**
     * Method that creates a playlist
     * @param playlistName the name of the playlist to be created
     * @param visibilityStatus the visibility status of the playlist to be created
     * @param owner the owner of the playlist to be created
     */
    public void createPlaylist(final String playlistName, final int visibilityStatus,
                               final String owner) {
        Playlist playlist = new Playlist(playlistName, visibilityStatus, owner);
        playlists.add(playlist);
    }

    /**
    * Getter for the followed playlists
     * @return the list of followed playlists
     */
    public List<String> getFollowedPlaylists() {
        return followedPlaylists;
    }

    /**
     * Method that follows a playlist
     * @param playlistName the name of the playlist
     */
    public void followPlaylist(final String playlistName) {
        followedPlaylists.add(playlistName);
    }
    /**
     * Method that unfollows a playlist
     * @param playlistName the name of the playlist
     */
    public void unfollowPlaylist(final String playlistName) {
        followedPlaylists.remove(playlistName);
    }
    /**
     * Method that checks if a playlist exists
     * @param playlistName the name of the playlist
     * @return true if the playlist exists, false otherwise
     */
    public boolean playlistExists(final String playlistName) {
        for (Playlist playlist : playlists) {
            if (playlist.getName().equals(playlistName)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Method that checks if a song exists in the list of liked songs
     * @param songName the name of the song
     * @return true if the song exists, false otherwise
     */
    public static boolean containsSong(final Playlist playlist, final SongInput songName) {
        return playlist.getSongs().contains(songName);
    }
    /**
     * Method that gets the number of followers of a playlist
     * @param userInformationList the list of users
     * @param playlist the playlist
     * @return the number of followers
     */
    public int getFollowers(final List<UserInformation> userInformationList, final Playlist playlist) {
        int followersCount = 0;

        for (UserInformation user : userInformationList) {
            if (user.isFollowingPlaylist(playlist)) {
                followersCount++;
            }
        }

        return followersCount;
    }
    /**
     * Method that checks if a user is following a playlist
     * @param playlist the playlist
     * @return true if the user is following the playlist, false otherwise
     */
    private boolean isFollowingPlaylist(final Playlist playlist) {
        for (String followedPlaylistName : followedPlaylists) {
            if (followedPlaylistName.equals(playlist.getName())) {
                return true;
            }
        }
        return false;
    }

}
