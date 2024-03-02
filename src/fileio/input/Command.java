package fileio.input;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Command {

    private static List<String> lastSearchResults;
    private static int repeatState = 0;
    private static boolean shuffleState = false;
    private static PlayerState currentPlayerState;
    private static PodcastState currentPodcastState;
    private static PlaylistState currentPlaylistState;
    private static Playlist currentPlaylist;
    private static SongInput currentSong;
    private static boolean loadedSong = false;
    private static boolean loadedPodcast = false;
    private static boolean loadedPlaylist = false;
    private static boolean isPlaying;
    private static String searchType = "";
    private static String selectedType = "";
    private static PodcastInput currentPodcast;
    private static int selected = 0;
    private static int loaded = 0;
    private static boolean selectedForFollow = false;

    /**
     *
     * @param library
     * @param commandNode
     * @param outputs
     * @param userInformationList
     */
    public static void handleSearchCommand(final LibraryInput library, final JsonNode commandNode,
                                           final ArrayNode outputs, final List<UserInformation>
                                                   userInformationList) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();
        String type = commandNode.get("type").asText();
        JsonNode filtersNode = commandNode.get("filters");
        if (loadedPlaylist) {
            currentPlaylistState.setLastTimestamp(timestamp);
            currentPlaylistState.setTimeRemaining(currentPlaylistState.calculateTimeRemaining());
        } else if (loadedPodcast) {
            currentPodcastState.setLastTimestamp(timestamp);
            currentPodcastState.setTimeRemaining(currentPodcastState.calculateTimeRemaining());

        } else if (loadedSong) {
            currentPlayerState.setLastTimestamp(timestamp);
            currentPlayerState.setTimeRemaining(currentPlayerState.calculateTimeRemaining());
        }
        selectedType = "";
        searchType = type;
        loadedPlaylist = false;
        loadedSong = false;
        loadedPodcast = false;
        isPlaying = false;
        loaded = 0;


        lastSearchResults = SearchHelper.performSearch(library, type, filtersNode,
                userInformationList, username);


        ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
        outputNode.put("command", "search");
        outputNode.put("user", username);
        outputNode.put("timestamp", timestamp);
        outputNode.put("message", "Search returned " + lastSearchResults.size() + " results");


        ArrayNode resultsArrayNode = outputNode.putArray("results");
        for (String result : lastSearchResults) {
            resultsArrayNode.add(result);
        }


        outputs.add(outputNode);
    }

    /**
     *
     * @param library
     * @param commandNode
     * @param outputs
     * @param userInformationList
     */
    public static void handleSelectCommand(final LibraryInput library, final JsonNode commandNode,
                                           final ArrayNode outputs, final List<UserInformation>
                                                   userInformationList) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();
        int itemNumber = commandNode.get("itemNumber").asInt();


        int selectedIndex = itemNumber - 1;

        if (searchType.equals("")) {
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "select");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please conduct a search before making a selection.");

            outputs.add(outputNode);
            return;
        }

        if (lastSearchResults != null && selectedIndex >= 0 && selectedIndex
                < lastSearchResults.size()) {
            selectedType = searchType;
            String selectedCommand = lastSearchResults.get(selectedIndex);
            switch (selectedType) {
                case "song":
                    currentSong = SelectHelper.getSongByName(library, selectedCommand);
                    currentPlaylist = null;
                    currentPodcast = null;
                    break;
                case "podcast":
                    currentPodcast = SelectHelper.getPodcastByName(library, selectedCommand);
                    currentPlaylist = null;
                    currentSong = null;
                    break;
                case "playlist":
                    currentPlaylist = SelectHelper.getPlaylistByName(library, selectedCommand,
                            userInformationList, username);
                    currentSong = null;
                    currentPodcast = null;
                    selectedForFollow = true;
                    break;
                default:
                    break;

            }


            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "select");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Successfully selected " + selectedCommand + ".");


            outputs.add(outputNode);
            selected = 1;
        } else {


            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "select");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "The selected ID is too high.");


            outputs.add(outputNode);
            selected = 0;
        }
    }

    /**
     *
     * @param library
     * @param commandNode
     * @param outputs
     * @param userInformationList
     */
    public static void handleLoadCommand(final LibraryInput library, final JsonNode commandNode,
                                         final ArrayNode outputs, final List<UserInformation>
                                                 userInformationList) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();
        if (selected == 0) {
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "load");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please select a source before attempting to load.");

            outputs.add(outputNode);
            return;
        }

        if (selectedType.equals("song")) {

            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "load");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Playback loaded successfully.");

            outputs.add(outputNode);

            isPlaying = true;
            loadedSong = true;
            loadedPodcast = false;
            loadedPlaylist = false;
            if (currentPlayerState != null
                    && currentSong.getName().equals(currentPlayerState.getSelectedSongName())) {
                currentPlayerState.setInitialTimestamp(timestamp);
                currentPlayerState.setLastTimestamp(timestamp);
                currentPlayerState.setPaused(false);
            } else {
                currentPlayerState = new PlayerState(currentSong.getName(), shuffleState,
                        !isPlaying, "No Repeat", timestamp, currentSong.getDuration(),
                        currentSong.getDuration(), timestamp);
            }
            if (currentPlaylistState != null) {
                currentPlaylistState.setPaused(true);
            }
            if (currentPodcastState != null) {
                currentPodcastState.setPaused(true);
            }
        } else if (selectedType.equals("podcast")) {

            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "load");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Playback loaded successfully.");

            outputs.add(outputNode);

            isPlaying = true;
            loadedSong = false;
            loadedPodcast = true;
            loadedPlaylist = false;
            if (currentPodcastState != null
                    && currentPodcast.getName().
                    equals(currentPodcastState.getSelectedPodcastName())) {
                currentPodcastState.setInitialTimestamp(timestamp);
                currentPodcastState.setLastTimestamp(timestamp);
                currentPodcastState.setPaused(false);
            } else {
                currentPodcastState = new PodcastState(currentPodcast.getName(),
                        !isPlaying, "No Repeat", timestamp, timestamp,
                        currentPodcast.getEpisodes());
            }
            if (currentPlayerState != null) {
                currentPlayerState.setPaused(true);
            }
            if (currentPlaylistState != null) {
                currentPlaylistState.setPaused(true);
            }
        } else if (selectedType.equals("playlist")) {

            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "load");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Playback loaded successfully.");

            outputs.add(outputNode);

            isPlaying = true;
            loadedSong = false;
            loadedPodcast = false;
            loadedPlaylist = true;
            if (currentPlaylistState != null && currentPlaylist.getName().
                    equals(currentPlaylistState.getSelectedPlaylistName())
                    && currentPlaylistState.getOwner().equalsIgnoreCase(username)) {
                currentPlaylistState.setInitialTimestamp(timestamp);
                currentPlaylistState.setLastTimestamp(timestamp);
                currentPlaylistState.setPaused(false);
                currentPlaylistState.reset();
            } else {
                currentPlaylistState = new PlaylistState(currentPlaylist.getName(),
                        shuffleState, !isPlaying, "No Repeat", timestamp, timestamp,
                        currentPlaylist.getSongs(), username);
                currentPlaylistState.reset();
            }
            if (currentPlayerState != null) {
                currentPlayerState.setPaused(true);
            }
            if (currentPodcastState != null) {
                currentPodcastState.setPaused(true);
            }
        } else {
            loadedSong = false;
            loadedPodcast = false;
            loadedPlaylist = false;


            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "load");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please select a source before attempting to load.");

            outputs.add(outputNode);
        }
        selected = 0;
        searchType = "";
        loaded = 1;
    }

    /**
     *
     * @param library
     * @param commandNode
     * @param outputs
     */
    public static void handlePlayPauseCommand(final LibraryInput library, final JsonNode
            commandNode, final ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();

        if (loadedSong) {
            currentPlayerState.setLastTimestamp(timestamp);
            if (!currentPlayerState.isPaused()) {



                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "playPause");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "Playback paused successfully.");

                outputs.add(outputNode);

                currentPlayerState.setTimeRemaining(currentPlayerState.calculateTimeRemaining());
                isPlaying = false;
                currentPlayerState.setPaused(true);
            } else {



                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "playPause");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "Playback resumed successfully.");

                outputs.add(outputNode);

                isPlaying = true;
                currentPlayerState.setPaused(false);
                currentPlayerState.setInitialTimestamp(timestamp);
            }
        } else if (loadedPlaylist) {
            currentPlaylistState.setLastTimestamp(timestamp);
            if (!currentPlaylistState.isPaused()) {
                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "playPause");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "Playback paused successfully.");

                outputs.add(outputNode);

                currentPlaylistState.setTimeRemaining(currentPlaylistState.
                        calculateTimeRemaining());
                isPlaying = false;
                currentPlaylistState.setPaused(true);
            } else {
                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "playPause");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "Playback resumed successfully.");

                outputs.add(outputNode);

                isPlaying = true;
                currentPlaylistState.setPaused(false);
                currentPlaylistState.setInitialTimestamp(timestamp);
            }
        } else if (loadedPodcast) {
            currentPodcastState.setLastTimestamp(timestamp);
            if (!currentPodcastState.isPaused()) {
                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "playPause");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "Playback paused successfully.");

                outputs.add(outputNode);

                currentPodcastState.setTimeRemaining(currentPodcastState.
                        calculateTimeRemaining());
                isPlaying = false;
                currentPodcastState.setPaused(true);
            } else {
                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "playPause");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "Playback resumed successfully.");

                outputs.add(outputNode);

                isPlaying = true;
                currentPodcastState.setPaused(false);
                currentPodcastState.setInitialTimestamp(timestamp);
            }
        } else {


            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "playPause");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please load a source before attempting to pause or resume "
                    + "playback.");

            outputs.add(outputNode);
        }
    }

    /**
     *
     * @param library
     * @param commandNode
     * @param outputs
     */
    public static void handleRepeatCommand(final LibraryInput library, final JsonNode
            commandNode, final ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();

        if (loadedSong) {

            if (currentPlayerState.getRepeatState().equalsIgnoreCase("No Repeat")) {
                repeatState = 1;
            } else if (currentPlayerState.getRepeatState().equalsIgnoreCase("Repeat Once")) {
                repeatState = 2;
            } else if (currentPlayerState.getRepeatState().equalsIgnoreCase("Repeat Infinite")) {
                repeatState = 0;
            }
            currentPlayerState.setLastTimestamp(timestamp);
            currentPlayerState.setTimeRemaining(currentPlayerState.calculateTimeRemaining());
            currentPlayerState.setInitialTimestamp(timestamp);

            String repeatStateText = RepeatStateConverter.convertToText(repeatState, loadedSong,
                    loadedPlaylist, loadedPodcast);
            currentPlayerState.setRepeatState(repeatStateText);

            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "repeat");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Repeat mode changed to " + repeatStateText.toLowerCase()
                    + ".");


            outputs.add(outputNode);
        } else if (loadedPodcast) {

            if (currentPodcastState.getRepeatState().equalsIgnoreCase("No Repeat")) {
                repeatState = 1;
            } else if (currentPodcastState.getRepeatState().equalsIgnoreCase("Repeat Once")) {
                repeatState = 2;
            } else if (currentPodcastState.getRepeatState().equalsIgnoreCase("Repeat Infinite")) {
                repeatState = 0;
            }
            currentPodcastState.setLastTimestamp(timestamp);
            currentPodcastState.setTimeRemaining(currentPodcastState.calculateTimeRemaining());
            currentPodcastState.setInitialTimestamp(timestamp);

            String repeatStateText = RepeatStateConverter.convertToText(repeatState, loadedSong,
                    loadedPlaylist, loadedPodcast);
            currentPodcastState.setRepeatState(repeatStateText);

            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "repeat");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Repeat mode changed to " + repeatStateText.toLowerCase()
                    + ".");


            outputs.add(outputNode);
        } else if (loadedPlaylist) {

            if (currentPlaylistState.getRepeatState().equalsIgnoreCase("No Repeat")) {
                repeatState = 1;
            } else if (currentPlaylistState.getRepeatState().equalsIgnoreCase("Repeat All")) {
                repeatState = 2;
            } else if (currentPlaylistState.getRepeatState().
                    equalsIgnoreCase("Repeat Current Song")) {
                repeatState = 0;
            }
            currentPlaylistState.setLastTimestamp(timestamp);
            currentPlaylistState.setTimeRemaining(currentPlaylistState.calculateTimeRemaining());
            currentPlaylistState.setInitialTimestamp(timestamp);

            String repeatStateText = RepeatStateConverter.convertToText(repeatState, loadedSong,
                    loadedPlaylist, loadedPodcast);
            currentPlaylistState.setRepeatState(repeatStateText);

            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "repeat");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Repeat mode changed to " + repeatStateText.toLowerCase()
                    + ".");


            outputs.add(outputNode);
        } else {

            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "repeat");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please load a source before setting the repeat status.");


            outputs.add(outputNode);
        }
    }

    /**
     *
     * @param library
     * @param commandNode
     * @param outputs
     */
    public static void handleStatusCommand(final LibraryInput library, final JsonNode
            commandNode, final ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();

        if (loadedSong) {
            currentPlayerState.setLastTimestamp(timestamp);

            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "status");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);

            currentPlayerState.setTimeRemaining(currentPlayerState.calculateTimeRemaining());
            if (currentPlayerState.getTimeRemaining() == 0) {
                currentPlayerState.setPaused(true);
                currentPlayerState.setSelectedSongName("");
            }

            ObjectNode statsNode = JsonNodeFactory.instance.objectNode();
            statsNode.put("name", currentPlayerState.getSelectedSongName());
            statsNode.put("remainedTime", currentPlayerState.getTimeRemaining());
            statsNode.put("repeat", currentPlayerState.getRepeatState());
            statsNode.put("shuffle", false);
            statsNode.put("paused", currentPlayerState.isPaused());


            outputNode.set("stats", statsNode);


            outputs.add(outputNode);
            currentPlayerState.setInitialTimestamp(timestamp);
        } else if (loadedPlaylist) {
            currentPlaylistState.setLastTimestamp(timestamp);

            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "status");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);

            ObjectNode statsNode = JsonNodeFactory.instance.objectNode();
            currentPlaylistState.setTimeRemaining(currentPlaylistState.calculateTimeRemaining());
            if (currentPlaylistState.getTimeRemaining() == 0) {
                currentPlaylistState.setPaused(true);

                statsNode.put("name", "");
            } else {
                statsNode.put("name", currentPlaylistState.getSelectedTrackName());
            }

            statsNode.put("remainedTime", currentPlaylistState.getTimeRemaining());
            statsNode.put("repeat", currentPlaylistState.getRepeatState());
            statsNode.put("shuffle", currentPlaylistState.isShuffleEnabled());
            statsNode.put("paused", currentPlaylistState.isPaused());


            outputNode.set("stats", statsNode);


            outputs.add(outputNode);
            currentPlaylistState.setInitialTimestamp(timestamp);
        } else if (loadedPodcast) {
            currentPodcastState.setLastTimestamp(timestamp);

            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "status");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);

            ObjectNode statsNode = JsonNodeFactory.instance.objectNode();
            currentPodcastState.setTimeRemaining(currentPodcastState.calculateTimeRemaining());
            if (currentPodcastState.getTimeRemaining() == 0) {
                currentPodcastState.setPaused(true);

                statsNode.put("name", "");
            } else {
                statsNode.put("name", currentPodcastState.getSelectedEpisodeName());
            }

            statsNode.put("remainedTime", currentPodcastState.getTimeRemaining());
            statsNode.put("repeat", currentPodcastState.getRepeatState());
            statsNode.put("shuffle", false);
            statsNode.put("paused", currentPodcastState.isPaused());


            outputNode.set("stats", statsNode);


            outputs.add(outputNode);
            currentPodcastState.setInitialTimestamp(timestamp);
        } else {

            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "status");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);


            ObjectNode statsNode = JsonNodeFactory.instance.objectNode();
            statsNode.put("name", "");
            statsNode.put("remainedTime", 0);
            statsNode.put("repeat", "No Repeat");
            statsNode.put("shuffle", false);
            statsNode.put("paused", true);


            outputNode.set("stats", statsNode);


            outputs.add(outputNode);
        }
    }

    /**
     *
     * @param library
     * @param commandNode
     * @param outputs
     */
    public static void handleShuffleCommand(final LibraryInput library, final JsonNode
            commandNode, final ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();

        if (loaded == 0) {
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "shuffle");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please load a source before using the shuffle function.");

            outputs.add(outputNode);
            return;
        }
        if (loadedPlaylist) {
            if (commandNode.has("seed")) {
                long seed = commandNode.get("seed").asLong();

                currentPlaylistState.setLastTimestamp(timestamp);
                currentPlaylistState.setTimeRemaining(currentPlaylistState.
                        calculateTimeRemaining());
                currentPlaylistState.setInitialTimestamp(timestamp);

                List<SongInput> playlistSongs = currentPlaylistState.gettracks();
                List<Integer> indices = new ArrayList<>();

                for (int i = 0; i < playlistSongs.size(); i++) {
                    indices.add(i);
                }

                Collections.shuffle(indices, new java.util.Random(seed));

                currentPlaylistState.shuffleOn(indices);

                shuffleState = true;
                currentPlaylistState.setShuffleEnabled(true);
            } else {
                shuffleState = false;
                currentPlaylistState.setShuffleEnabled(false);

                currentPlaylistState.shuffleOff();

                currentPlaylistState.setLastTimestamp(timestamp);
                currentPlaylistState.setTimeRemaining(currentPlaylistState.
                        calculateTimeRemaining());
                currentPlaylistState.setInitialTimestamp(timestamp);

                if (currentPlaylistState.getTimeRemaining() == 0) {
                    currentPlaylistState.setPaused(true);
                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "shuffle");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Please load a source before using the shuffle "
                            + "function.");

                    outputs.add(outputNode);
                    return;
                }
            }
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "shuffle");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            String message = shuffleState ? "Shuffle function activated successfully." : "Shuffle "
                    + "function deactivated successfully.";
            outputNode.put("message", message);

            outputs.add(outputNode);
        } else {

            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "shuffle");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "The loaded source is not a playlist.");


            outputs.add(outputNode);
        }
    }

    /**
     *
     * @param library
     * @param userInformationList
     * @param commandNode
     * @param outputs
     */
    public static void handleCreatePlaylistCommand(final LibraryInput library, final
    List<UserInformation> userInformationList, final JsonNode commandNode, final
    ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();
        String playlistName = commandNode.get("playlistName").asText();

        UserInformation user = getUserByUsername(userInformationList, username);

        if (user != null) {

            boolean playlistExists = user.playlistExists(playlistName);

            if (!playlistExists) {
                user.createPlaylist(playlistName, 1, username);

                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "createPlaylist");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "Playlist created successfully.");


                outputs.add(outputNode);
            } else {
                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "createPlaylist");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "A playlist with the same name already exists.");


                outputs.add(outputNode);
            }
        }

    }

    /**
     *
     * @param library
     * @param userInformationList
     * @param commandNode
     * @param outputs
     */
    public static void handleAddRemoveInPlaylistCommand(final LibraryInput library, final
    List<UserInformation> userInformationList, final JsonNode commandNode, final
    ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();
        int playlistId = commandNode.get("playlistId").asInt();

        UserInformation user = getUserByUsername(userInformationList, username);
        if ("".equals(selectedType)) {
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "addRemoveInPlaylist");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please load a source before adding to or removing "
                    + "from the playlist.");

            outputs.add(outputNode);
            return;
        }

        if (!("song".equals(selectedType))) {
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "addRemoveInPlaylist");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "The loaded source is not a song.");

            outputs.add(outputNode);
            return;
        }
        if (user != null) {

            if (playlistId > user.getPlaylists().size()) {
                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "addRemoveInPlaylist");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "The specified playlist does not exist.");

                outputs.add(outputNode);
                return;
            }
            Playlist playlist = user.getPlaylists().get(playlistId - 1);
            if (playlist == null) {
                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "addRemoveInPlaylist");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "The specified playlist does not exist.");

                outputs.add(outputNode);
                return;
            }

            if (loadedSong) {

                SongInput song = SelectHelper.getSongByName(library, currentPlayerState.
                        getSelectedSongName());
                boolean songExists = UserInformation.containsSong(playlist, song);

                if (songExists) {

                    playlist.removeSong(song);
                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "addRemoveInPlaylist");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Successfully removed from playlist.");

                    outputs.add(outputNode);

                } else {

                    playlist.addSong(song);
                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "addRemoveInPlaylist");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Successfully added to playlist.");

                    outputs.add(outputNode);

                }

            } else {
                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "addRemoveInPlaylist");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "Please load a source before adding to or removing "
                        + "from the playlist.");

                outputs.add(outputNode);
            }

        }
    }

    /**
     *
     * @param userInformationList
     * @param username
     * @return
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

    /**
     *
     * @param library
     * @param userInformationList
     * @param commandNode
     * @param outputs
     */
    public static void handleLikeCommand(final LibraryInput library, final List<UserInformation>
            userInformationList, final JsonNode commandNode, final ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();

        UserInformation user = getUserByUsername(userInformationList, username);

        if (user != null) {
            if (loadedSong) {
                boolean songExists = false;
                List<String> likedSongs = user.getLikedSongs();
                for (String songName : likedSongs) {
                    if (songName.equals(currentPlayerState.getSelectedSongName())) {
                        songExists = true;
                        break;
                    }
                }

                if (songExists) {

                    user.unlikeSong(currentPlayerState.getSelectedSongName());

                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "like");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Unlike registered successfully.");

                    outputs.add(outputNode);
                } else {

                    user.likeSong(currentPlayerState.getSelectedSongName());

                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "like");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Like registered successfully.");

                    outputs.add(outputNode);
                }

            } else if (loadedPlaylist) {
                boolean songExists = false;
                List<String> likedSongs = user.getLikedSongs();
                for (String songName : likedSongs) {
                    if (songName.equals(currentPlaylistState.getSelectedTrackName())) {
                        songExists = true;
                        break;
                    }
                }
                if (songExists) {
                    user.unlikeSong(currentPlaylistState.getSelectedTrackName());

                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "like");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Unlike registered successfully.");

                    outputs.add(outputNode);
                } else {
                    user.likeSong(currentPlaylistState.getSelectedTrackName());

                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "like");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Like registered successfully.");

                    outputs.add(outputNode);
                }
            } else {
                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "like");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "Please load a source before liking or unliking.");

                outputs.add(outputNode);
            }
        }
    }

    /**
     *
     * @param library
     * @param userInformationList
     * @param commandNode
     * @param outputs
     */
    public static void handlePrefferedSongs(final LibraryInput library, final List<UserInformation>
            userInformationList, final JsonNode commandNode, final ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();

        UserInformation user = getUserByUsername(userInformationList, username);

        List<String> likedSongs = user.getLikedSongs();

        ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
        outputNode.put("command", "showPreferredSongs");
        outputNode.put("user", username);
        outputNode.put("timestamp", timestamp);


        ArrayNode resultsArrayNode = outputNode.putArray("result");
        for (String result : likedSongs) {
            resultsArrayNode.add(result);
        }


        outputs.add(outputNode);
    }

    /**
     *
     * @param library
     * @param userInformationList
     * @param commandNode
     * @param outputs
     */
    public static void handleShowPlaylistsCommand(final LibraryInput library, final
    List<UserInformation> userInformationList, final JsonNode commandNode, final
    ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();

        UserInformation user = getUserByUsername(userInformationList, username);

        List<Playlist> playlists = user.getPlaylists();
        ArrayNode resultsArrayNode = JsonNodeFactory.instance.arrayNode();

        for (Playlist playlist : playlists) {
            ObjectNode playlistNode = JsonNodeFactory.instance.objectNode();
            playlistNode.put("name", playlist.getName());
            playlistNode.set("songs", convertListToJsonArray(playlist.getSongs()));
            playlistNode.put("visibility", playlist.getVisibilityStatus()
                    == 1 ? "public" : "private");
            playlistNode.put("followers", user.getFollowers(userInformationList, playlist));

            resultsArrayNode.add(playlistNode);
        }

        ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
        outputNode.put("command", "showPlaylists");
        outputNode.put("user", username);
        outputNode.put("timestamp", timestamp);
        outputNode.set("result", resultsArrayNode);

        outputs.add(outputNode);
    }

    /**
     *
     * @param list
     * @return
     */
    private static ArrayNode convertListToJsonArray(final List<SongInput> list) {
        ArrayNode jsonArray = JsonNodeFactory.instance.arrayNode();
        for (SongInput item : list) {
            jsonArray.add(item.getName());
        }
        return jsonArray;
    }

    /**
     *
     * @param library
     * @param commandNode
     * @param outputs
     */
    public static void handleNextCommand(final LibraryInput library, final JsonNode commandNode,
                                         final ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();

        if (loadedSong || loadedPodcast || loadedPlaylist) {
            if (loadedSong) {
                boolean stopped = currentPlayerState.next(timestamp);
                if (stopped) {
                    loadedSong = false;
                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "next");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Please load a source before skipping to the next "
                            + "track.");

                    outputs.add(outputNode);
                } else {
                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "next");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Skipped to next track successfully. The current "
                            + "track is " + currentPlayerState.getSelectedSongName() + ".");

                    outputs.add(outputNode);
                }
            } else if (loadedPodcast) {
                boolean stopped = currentPodcastState.next(timestamp);
                if (stopped) {
                    loadedPodcast = false;
                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "next");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Please load a source before skipping to the next "
                            + "track.");

                    outputs.add(outputNode);
                } else {
                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "next");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Skipped to next track successfully. The current "
                            + "track is " + currentPodcastState.getSelectedEpisodeName() + ".");

                    outputs.add(outputNode);
                }
            } else if (loadedPlaylist) {
                boolean stopped = currentPlaylistState.next(timestamp);
                if (stopped) {
                    loadedPlaylist = false;
                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "next");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Please load a source before skipping to the next "
                            + "track.");

                    outputs.add(outputNode);
                } else {
                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "next");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Skipped to next track successfully. The current "
                            + "track is " + currentPlaylistState.getSelectedTrackName() + ".");

                    outputs.add(outputNode);
                }
            }
        } else {
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "next");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please load a source before skipping to the next "
                    + "track.");

            outputs.add(outputNode);
        }
    }

    /**
     *
     * @param library
     * @param commandNode
     * @param outputs
     */
    public static void handlePrevCommand(final LibraryInput library, final JsonNode commandNode,
                                         final ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();


        if (loadedSong || loadedPodcast || loadedPlaylist) {
            if (loadedSong) {
                currentPlayerState.prev(timestamp);
                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "prev");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "Returned to previous track successfully. The current "
                        + "track is " + currentPlayerState.getSelectedSongName() + ".");


                outputs.add(outputNode);
            } else if (loadedPodcast) {
                currentPodcastState.prev(timestamp);
                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "prev");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "Returned to previous track successfully. The current "
                        + "track is " + currentPodcastState.getSelectedEpisodeName() + ".");


                outputs.add(outputNode);
            } else if (loadedPlaylist) {
                currentPlaylistState.prev(timestamp);
                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "prev");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "Returned to previous track successfully. The current "
                        + "track is " + currentPlaylistState.getSelectedTrackName() + ".");


                outputs.add(outputNode);
            }
        } else {

            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "prev");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please load a source before returning to the previous "
                    + "track.");


            outputs.add(outputNode);
        }
    }

    /**
     *
     * @param library
     * @param commandNode
     * @param outputs
     */
    public static void handleForwardCommand(final LibraryInput library, final JsonNode commandNode,
                                            final ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();

        if (!loadedSong && !loadedPodcast && !loadedPlaylist) {
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "forward");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please load a source before attempting to forward.");

            outputs.add(outputNode);
            return;
        }

        if (loadedPodcast) {
            boolean stopped = currentPodcastState.forward(timestamp);
            if (stopped) {
                loadedPodcast = false;
            }
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "forward");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Skipped forward successfully.");

            outputs.add(outputNode);
        } else {

            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "forward");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "The loaded source is not a podcast.");


            outputs.add(outputNode);
        }
    }

    /**
     *
     * @param library
     * @param commandNode
     * @param outputs
     */
    public static void handleBackwardCommand(final LibraryInput library, final JsonNode commandNode,
                                             final ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();


        if (loadedPodcast) {
            currentPodcastState.backward(timestamp);

            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "backward");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Rewound successfully.");

            outputs.add(outputNode);
        } else {

            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "backward");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "The loaded source is not a podcast.");


            outputs.add(outputNode);
        }
    }

    /**
     *
     * @param library
     * @param userInformationList
     * @param commandNode
     * @param outputs
     */
    public static void handleFollowCommand(final LibraryInput library, final List<UserInformation>
            userInformationList, final JsonNode commandNode, final ArrayNode outputs) {

        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();

        if (currentSong != null || currentPodcast != null) {
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "follow");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "The selected source is not a playlist.");
            outputs.add(outputNode);
            return;
        }
        if (selected == 0 || !selectedForFollow) {
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "follow");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please select a source before following or "
                    + "unfollowing.");
            outputs.add(outputNode);
            return;
        }
        if (selectedForFollow) {
            selectedForFollow = false;
        }

        if (currentPlaylist == null) {
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "follow");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please load a playlist before attempting to follow it.");
            outputs.add(outputNode);
            return;
        }

        UserInformation user = getUserByUsername(userInformationList, username);
        UserInformation userToFollow = getUserByUsername(userInformationList,
                currentPlaylist.getOwner());

        Playlist playlistToFollow = null;
        List<Playlist> playlists = userToFollow.getPlaylists();
        for (Playlist playlist : playlists) {
            if (playlist.getName().equals(currentPlaylist.getName())) {
                playlistToFollow = playlist;
                break;
            }
        }
        List<String> followedPlaylist = user.getFollowedPlaylists();
        boolean playlistExists = false;
        for (String playlistName : followedPlaylist) {
            if (playlistName.equals(playlistToFollow.getName())) {
                playlistExists = true;
                break;
            }
        }
        if (playlistExists) {
            playlistToFollow.decreaseFollowers();
            user.unfollowPlaylist(playlistToFollow.getName());
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "follow");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Playlist unfollowed successfully.");

            outputs.add(outputNode);
        } else {
            playlistToFollow.increaseFollowers();
            user.followPlaylist(playlistToFollow.getName());
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "follow");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Playlist followed successfully.");
            outputs.add(outputNode);
        }

    }

    /**
     *
     * @param library
     * @param userInformationList
     * @param commandNode
     * @param outputs
     */
    public static void handleSwitchVisibilityCommand(final LibraryInput library, final
    List<UserInformation> userInformationList, final JsonNode commandNode, final
    ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();

        if (currentPlaylist == null) {
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "follow");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please load a playlist before attempting to follow it.");
            outputs.add(outputNode);
            return;
        }

        UserInformation user = getUserByUsername(userInformationList, username);
        UserInformation userVisibility = getUserByUsername(userInformationList,
                currentPlaylist.getOwner());
        List<Playlist> playlists = userVisibility.getPlaylists();
        for (Playlist playlist : playlists) {
            if (playlist.getName().equals(currentPlaylist.getName())) {
                if (playlist.getVisibilityStatus() == 1) {
                    playlist.setVisibilityStatus(0);
                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "switchVisibility");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Visibility status updated successfully to "
                            + "private.");
                    outputs.add(outputNode);
                } else {
                    playlist.setVisibilityStatus(1);
                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "switchVisibility");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Visibility status updated successfully to "
                            + "public.");
                    outputs.add(outputNode);
                }
                break;
            }
        }
    }

}
