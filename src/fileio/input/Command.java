package fileio.input;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Collections;
import java.lang.management.PlatformLoggingMXBean;
import java.util.List;
import fileio.input.PlayerState;
import fileio.input.LibraryInput;
import fileio.input.SearchHelper;
import fileio.input.UserInput;
import fileio.input.SongInput;
import fileio.input.EpisodeInput;
import fileio.input.PodcastInput;
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
    public static void handleSearchCommand(LibraryInput library, JsonNode commandNode, ArrayNode outputs, List<UserInformation> userInformationList) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();
        String type = commandNode.get("type").asText();
        JsonNode filtersNode = commandNode.get("filters");
        if(loadedPlaylist) {
            currentPlaylistState.setLastTimestamp(timestamp);
            currentPlaylistState.setTimeRemaining(currentPlaylistState.calculateTimeRemaining());
        } else if(loadedPodcast) {
            currentPodcastState.setLastTimestamp(timestamp);
            currentPodcastState.setTimeRemaining(currentPodcastState.calculateTimeRemaining());

        } else if(loadedSong) {
            currentPlayerState.setLastTimestamp(timestamp);
            currentPlayerState.setTimeRemaining(currentPlayerState.calculateTimeRemaining());
        }
        selectedType = "";
        searchType = type;
        loadedPlaylist = false;
        loadedSong = false;
        loadedPodcast = false;
        isPlaying = false;

        // Perform search based on type and filters
        lastSearchResults = SearchHelper.performSearch(library, type, filtersNode, userInformationList, username);

        // Create output JSON
        ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
        outputNode.put("command", "search");
        outputNode.put("user", username);
        outputNode.put("timestamp", timestamp);
        outputNode.put("message", "Search returned " + lastSearchResults.size() + " results");

        // Add results to the output JSON
        ArrayNode resultsArrayNode = outputNode.putArray("results");
        for (String result : lastSearchResults) {
            resultsArrayNode.add(result);
        }

        // Add the output to the outputs array
        outputs.add(outputNode);
    }

    public static void handleSelectCommand(LibraryInput library, JsonNode commandNode, ArrayNode outputs, List<UserInformation> userInformationList) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();
        int itemNumber = commandNode.get("itemNumber").asInt();

        // Assuming itemNumber is 1-indexed
        int selectedIndex = itemNumber - 1;

        // Check if lastSearchResults is not null and selectedIndex is within the valid range
        if (lastSearchResults != null && selectedIndex >= 0 && selectedIndex < lastSearchResults.size()) {
            selectedType = searchType;
            String selectedCommand = lastSearchResults.get(selectedIndex);
            switch (selectedType) {
                case "song":
                    currentSong = SelectHelper.getSongByName(library, selectedCommand);
                    break;
                case "podcast":
                    currentPodcast = SelectHelper.getPodcastByName(library, selectedCommand);
                    break;
                case "playlist":
                    currentPlaylist = SelectHelper.getPlaylistByName(library, selectedCommand, userInformationList, username);
                    break;

            }

            // Create output JSON for successful selection
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "select");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Successfully selected " + selectedCommand + ".");

            // Add the output to the outputs array
            outputs.add(outputNode);
            selected = 1;
        } else {

            // Create output JSON for invalid selectedIndex
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "select");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "The selected ID is too high.");

            // Add the output to the outputs array
            outputs.add(outputNode);
            selected = 0;
        }
    }

    public static void handleLoadCommand(LibraryInput library, JsonNode commandNode, ArrayNode outputs, List<UserInformation> userInformationList) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();
        if(selected == 0) {
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "load");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please select a source before attempting to load.");
            // Adăugați rezultatul la lista de ieșiri
            outputs.add(outputNode);
            return;
        }
        // Verificați dacă a fost selectată o melodie
        if (selectedType.equals("song")) {
            // Creare nod de ieșire JSON pentru încărcare reușită
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "load");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Playback loaded successfully.");
            // Adăugați rezultatul la lista de ieșiri
            outputs.add(outputNode);

            isPlaying = true;
            loadedSong = true;
            loadedPodcast = false;
            loadedPlaylist = false;
            if(currentPlayerState != null && currentSong.getName().equals(currentPlayerState.getSelectedSongName())) {
                currentPlayerState.setInitialTimestamp(timestamp);
                currentPlayerState.setLastTimestamp(timestamp);
                currentPlayerState.setPaused(false);
            } else {
                //String repeatStateText = RepeatStateConverter.convertToText(repeatState, loadedSong, loadedPlaylist, loadedPodcast);
                currentPlayerState = new PlayerState(currentSong.getName(), shuffleState, !isPlaying, "No Repeat", timestamp, currentSong.getDuration(), currentSong.getDuration(), timestamp);
            }
            if(currentPlaylistState != null) {
                currentPlaylistState.setPaused(true);
            }
            if(currentPodcastState != null) {
                currentPodcastState.setPaused(true);
            }
        } else if(selectedType.equals("podcast")) {
            // Creare nod de ieșire JSON pentru încărcare reușită
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "load");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Playback loaded successfully.");
            // Adăugați rezultatul la lista de ieșiri
            outputs.add(outputNode);

            isPlaying = true;
            loadedSong = false;
            loadedPodcast = true;
            loadedPlaylist = false;
            if(currentPodcastState != null && currentPodcast.getName().equals(currentPodcastState.getSelectedPodcastName())) {
                currentPodcastState.setInitialTimestamp(timestamp);
                currentPodcastState.setLastTimestamp(timestamp);
                currentPodcastState.setPaused(false);
            } else {
                //String repeatStateText = RepeatStateConverter.convertToText(repeatState, loadedSong, loadedPlaylist, loadedPodcast);
                currentPodcastState = new PodcastState(currentPodcast.getName(), !isPlaying, "No Repeat", timestamp, timestamp, currentPodcast.getEpisodes());
            }
            if(currentPlayerState != null) {
                currentPlayerState.setPaused(true);
            }
            if(currentPlaylistState != null) {
                currentPlaylistState.setPaused(true);
            }
        } else if(selectedType.equals("playlist")) {
            // Creare nod de ieșire JSON pentru încărcare reușită
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "load");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Playback loaded successfully.");
            // Adăugați rezultatul la lista de ieșiri
            outputs.add(outputNode);

            isPlaying = true;
            loadedSong = false;
            loadedPodcast = false;
            loadedPlaylist = true;
            if(currentPlaylistState != null && currentPlaylist.getName().equals(currentPlaylistState.getSelectedPlaylistName())) {
                currentPlaylistState.setInitialTimestamp(timestamp);
                currentPlaylistState.setLastTimestamp(timestamp);
                currentPlaylistState.setPaused(false);
            } else {
                //String repeatStateText = RepeatStateConverter.convertToText(repeatState, loadedSong, loadedPlaylist, loadedPodcast);
                currentPlaylistState = new PlaylistState(currentPlaylist.getName(), shuffleState, !isPlaying, "No Repeat", timestamp, timestamp, currentPlaylist.getSongs());
            }
            if(currentPlayerState != null) {
                currentPlayerState.setPaused(true);
            }
            if(currentPodcastState != null) {
                currentPodcastState.setPaused(true);
            }
        } else {
            loadedSong = false;
            loadedPodcast = false;
            loadedPlaylist = false;

            // Creare nod de ieșire JSON pentru eroare
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "load");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please select a source before attempting to load.");
            // Adăugați rezultatul la lista de ieșiri
            outputs.add(outputNode);
        }
        selected = 0;
    }

    public static void handlePlayPauseCommand(LibraryInput library, JsonNode commandNode, ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();
        // Verificați dacă a fost încărcată o sursă
        if (loadedSong) {
            currentPlayerState.setLastTimestamp(timestamp);
            // Verificați starea curentă a playback-ului și faceți tranziția
            if (currentPlayerState.isPaused() == false) {
                // Player-ul este în starea de play, faceți tranziția la pause
                // Logică pentru a pune playback-ul în starea de pause
                // Creare nod de ieșire JSON pentru pauzare reușită
                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "playPause");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "Playback paused successfully.");
                // Adăugați rezultatul la lista de ieșiri
                outputs.add(outputNode);

                currentPlayerState.setTimeRemaining(currentPlayerState.calculateTimeRemaining());
                isPlaying = false;
                currentPlayerState.setPaused(true);
            } else {
                // Player-ul este în starea de pauză, faceți tranziția la play
                // Logică pentru a pune playback-ul în starea de play
                // Creare nod de ieșire JSON pentru repornire reușită
                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "playPause");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "Playback resumed successfully.");
                // Adăugați rezultatul la lista de ieșiri
                outputs.add(outputNode);

                isPlaying = true;
                currentPlayerState.setPaused(false);
                currentPlayerState.setInitialTimestamp(timestamp);
            }
        } else if(loadedPlaylist) {
            currentPlaylistState.setLastTimestamp(timestamp);
            if(currentPlaylistState.isPaused() == false) {
                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "playPause");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "Playback paused successfully.");
                // Adăugați rezultatul la lista de ieșiri
                outputs.add(outputNode);

                currentPlaylistState.setTimeRemaining(currentPlaylistState.calculateTimeRemaining());
                isPlaying = false;
                currentPlaylistState.setPaused(true);
            } else {
                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "playPause");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "Playback resumed successfully.");
                // Adăugați rezultatul la lista de ieșiri
                outputs.add(outputNode);

                isPlaying = true;
                currentPlaylistState.setPaused(false);
                currentPlaylistState.setInitialTimestamp(timestamp);
            }
        } else if(loadedPodcast) {
            currentPodcastState.setLastTimestamp(timestamp);
            if(currentPodcastState.isPaused() == false) {
                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "playPause");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "Playback paused successfully.");
                // Adăugați rezultatul la lista de ieșiri
                outputs.add(outputNode);

                currentPodcastState.setTimeRemaining(currentPodcastState.calculateTimeRemaining());
                isPlaying = false;
                currentPodcastState.setPaused(true);
            } else {
                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "playPause");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "Playback resumed successfully.");
                // Adăugați rezultatul la lista de ieșiri
                outputs.add(outputNode);

                isPlaying = true;
                currentPodcastState.setPaused(false);
                currentPodcastState.setInitialTimestamp(timestamp);
            }
        } else {
            // Nu a fost încărcată nicio sursă, returnați un mesaj de eroare
            // Creare nod de ieșire JSON pentru eroare
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "playPause");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please load a source before attempting to pause or resume playback.");
            // Adăugați rezultatul la lista de ieșiri
            outputs.add(outputNode);
        }
    }

    public static void handleRepeatCommand(LibraryInput library, JsonNode commandNode, ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();

        if (loadedSong) {
            // print time remaining
            if(currentPlayerState.getRepeatState().equalsIgnoreCase("No Repeat")) {
                repeatState = 1;
            } else if(currentPlayerState.getRepeatState().equalsIgnoreCase("Repeat Once")) {
                repeatState = 2;
            } else if(currentPlayerState.getRepeatState().equalsIgnoreCase("Repeat Infinite")) {
                repeatState = 0;
            }
            currentPlayerState.setLastTimestamp(timestamp);
            currentPlayerState.setTimeRemaining(currentPlayerState.calculateTimeRemaining());
            currentPlayerState.setInitialTimestamp(timestamp);
            // Map repeat state to corresponding text
            String repeatStateText = RepeatStateConverter.convertToText(repeatState, loadedSong, loadedPlaylist, loadedPodcast);
            currentPlayerState.setRepeatState(repeatStateText);
            // Create output JSON
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "repeat");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Repeat mode changed to " + repeatStateText.toLowerCase() + ".");

            // Add the output to the outputs array
            outputs.add(outputNode);
        } else if(loadedPodcast) {
            // Process repeat command only if playback is active
            if(currentPodcastState.getRepeatState().equalsIgnoreCase("No Repeat")) {
                repeatState = 1;
            } else if(currentPodcastState.getRepeatState().equalsIgnoreCase("Repeat Once")) {
                repeatState = 2;
            } else if(currentPodcastState.getRepeatState().equalsIgnoreCase("Repeat Infinite")) {
                repeatState = 0;
            }
            currentPodcastState.setLastTimestamp(timestamp);
            currentPodcastState.setTimeRemaining(currentPodcastState.calculateTimeRemaining());
            currentPodcastState.setInitialTimestamp(timestamp);
            // Map repeat state to corresponding text
            String repeatStateText = RepeatStateConverter.convertToText(repeatState, loadedSong, loadedPlaylist, loadedPodcast);
            currentPodcastState.setRepeatState(repeatStateText);
            // Create output JSON
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "repeat");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Repeat mode changed to " + repeatStateText.toLowerCase() + ".");

            // Add the output to the outputs array
            outputs.add(outputNode);
        } else if(loadedPlaylist) {
            // Process repeat command only if playback is active
            if(currentPlaylistState.getRepeatState().equalsIgnoreCase("No Repeat")) {
                repeatState = 1;
            } else if(currentPlaylistState.getRepeatState().equalsIgnoreCase("Repeat All")) {
                repeatState = 2;
            } else if(currentPlaylistState.getRepeatState().equalsIgnoreCase("Repeat Current Song")) {
                repeatState = 0;
            }
            currentPlaylistState.setLastTimestamp(timestamp);
            currentPlaylistState.setTimeRemaining(currentPlaylistState.calculateTimeRemaining());
            currentPlaylistState.setInitialTimestamp(timestamp);
            // Map repeat state to corresponding text
            String repeatStateText = RepeatStateConverter.convertToText(repeatState, loadedSong, loadedPlaylist, loadedPodcast);
            currentPlaylistState.setRepeatState(repeatStateText);
            // Create output JSON
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "repeat");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Repeat mode changed to " + repeatStateText.toLowerCase() + ".");

            // Add the output to the outputs array
            outputs.add(outputNode);
        } else {
            // Output error message if playback is not active
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "repeat");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please load a source before setting the repeat status.");

            // Add the output to the outputs array
            outputs.add(outputNode);
        }
    }

    public static void handleStatusCommand(LibraryInput library, JsonNode commandNode, ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();

        // Check if player is in a valid state (i.e., loaded with a source)
        if (loadedSong) {
            currentPlayerState.setLastTimestamp(timestamp);
            // Create output JSON
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "status");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);

            currentPlayerState.setTimeRemaining(currentPlayerState.calculateTimeRemaining());
            if (currentPlayerState.getTimeRemaining() == 0) {
                currentPlayerState.setPaused(true);
                currentPlayerState.setSelectedSongName("");
            }
            // Create stats JSON
            ObjectNode statsNode = JsonNodeFactory.instance.objectNode();
            statsNode.put("name", currentPlayerState.getSelectedSongName());
            statsNode.put("remainedTime", currentPlayerState.getTimeRemaining());
            statsNode.put("repeat", currentPlayerState.getRepeatState());
            statsNode.put("shuffle", currentPlayerState.isShuffleEnabled());
            statsNode.put("paused", currentPlayerState.isPaused());

            // Add stats to the output JSON
            outputNode.set("stats", statsNode);

            // Add the output to the outputs array
            outputs.add(outputNode);
            currentPlayerState.setInitialTimestamp(timestamp);
        } else if (loadedPlaylist){
            currentPlaylistState.setLastTimestamp(timestamp);

            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "status");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);

            ObjectNode statsNode = JsonNodeFactory.instance.objectNode();
            currentPlaylistState.setTimeRemaining(currentPlaylistState.calculateTimeRemaining());
            if(currentPlaylistState.getTimeRemaining() == 0) {
                currentPlaylistState.setPaused(true);
                //currentPlaylistState.setSelectedPlaylistName("");
                statsNode.put("name", "");
            } else {
                statsNode.put("name", currentPlaylistState.getSelectedTrackName());
            }

            statsNode.put("remainedTime", currentPlaylistState.getTimeRemaining());
            statsNode.put("repeat", currentPlaylistState.getRepeatState());
            statsNode.put("shuffle", currentPlaylistState.isShuffleEnabled());
            statsNode.put("paused", currentPlaylistState.isPaused());

            // Add stats to the output JSON
            outputNode.set("stats", statsNode);

            // Add the output to the outputs array
            outputs.add(outputNode);
            currentPlaylistState.setInitialTimestamp(timestamp);
        } else if (loadedPodcast){
            currentPodcastState.setLastTimestamp(timestamp);

            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "status");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);

            ObjectNode statsNode = JsonNodeFactory.instance.objectNode();
            currentPodcastState.setTimeRemaining(currentPodcastState.calculateTimeRemaining());
            if(currentPodcastState.getTimeRemaining() == 0) {
                currentPodcastState.setPaused(true);
                //currentPodcastState.setSelectedPodcastName("");
                statsNode.put("name", "");
            } else {
                statsNode.put("name", currentPodcastState.getSelectedEpisodeName());
            }

            statsNode.put("remainedTime", currentPodcastState.getTimeRemaining());
            statsNode.put("repeat", currentPodcastState.getRepeatState());
            statsNode.put("shuffle", false);
            statsNode.put("paused", currentPodcastState.isPaused());

            // Add stats to the output JSON
            outputNode.set("stats", statsNode);

            // Add the output to the outputs array
            outputs.add(outputNode);
            currentPodcastState.setInitialTimestamp(timestamp);
        } else{
            // Output error message if player is not in a valid state
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "status");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);

            // Create stats JSON
            ObjectNode statsNode = JsonNodeFactory.instance.objectNode();
            statsNode.put("name","");
            statsNode.put("remainedTime", 0);
            statsNode.put("repeat", "No Repeat");
            statsNode.put("shuffle", false);
            statsNode.put("paused", true);

            // Add stats to the output JSON
            outputNode.set("stats", statsNode);

            // Add the output to the outputs array
            outputs.add(outputNode);
        }
    }

    public static void handleShuffleCommand(LibraryInput library, JsonNode commandNode, ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();

        if (loadedPlaylist) {
            if(commandNode.has("seed")) {
                long seed = commandNode.get("seed").asLong();

                currentPodcastState.setLastTimestamp(timestamp);
                currentPodcastState.setTimeRemaining(currentPodcastState.calculateTimeRemaining());
                currentPodcastState.setInitialTimestamp(timestamp);

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
                currentPlaylistState.setInitialTimestamp(timestamp);
                currentPlaylistState.setLastTimestamp(timestamp);
            }
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "shuffle");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            String message = shuffleState ? "Shuffle function activated successfully." : "Shuffle function deactivated successfully.";
            outputNode.put("message", message);
            // Add the output to the outputs array
            outputs.add(outputNode);
        } else {
            // Output error message if player is not in a valid state
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "shuffle");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please load a playlist before shuffling.");

            // Add the output to the outputs array
            outputs.add(outputNode);
        }
    }

    public static void handleCreatePlaylistCommand(LibraryInput library, List<UserInformation> userInformationList, JsonNode commandNode, ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();
        String playlistName = commandNode.get("playlistName").asText();

        UserInformation user = getUserByUsername(userInformationList, username);

        if (user != null) {
            // check if playlist already exists
            boolean playlistExists = user.playlistExists(playlistName);

            if (!playlistExists) {
                user.createPlaylist(playlistName, 1);

                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "createPlaylist");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "Playlist created successfully.");

                // Add the output to the outputs array
                outputs.add(outputNode);
            } else {
                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "createPlaylist");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "A playlist with the same name already exists.");

                // Add the output to the outputs array
                outputs.add(outputNode);
            }
        }

    }

    public static void handleAddRemoveInPlaylistCommand(LibraryInput library, List<UserInformation> userInformationList, JsonNode commandNode, ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();
        int playlistId = commandNode.get("playlistId").asInt();

        UserInformation user = getUserByUsername(userInformationList, username);
        if ("".equals(selectedType)) {
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "addRemoveInPlaylist");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please load a source before adding to or removing from the playlist.");

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
            // check if playlist exists
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

                SongInput song = SelectHelper.getSongByName(library, currentPlayerState.getSelectedSongName());
                boolean songExists = UserInformation.containsSong(playlist, song);

                if (songExists) {
                    // Remove the song from the playlist
                    playlist.removeSong(song);
                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "addRemoveInPlaylist");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Successfully removed from playlist.");

                    outputs.add(outputNode);
                    //System.out.println("Removed\n");
                } else {
                    // Add the song to the playlist
                    playlist.addSong(song);
                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "addRemoveInPlaylist");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Successfully added to playlist.");

                    outputs.add(outputNode);
                    //System.out.println("Added\n");
                }

            } else {
                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "addRemoveInPlaylist");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "Please load a source before adding to or removing from the playlist.");

                outputs.add(outputNode);
            }

        }
    }

    private static UserInformation getUserByUsername(List<UserInformation> userInformationList, String username) {
        for (UserInformation user : userInformationList) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public static void handleLikeCommand(LibraryInput library, List<UserInformation> userInformationList, JsonNode commandNode, ArrayNode outputs) {
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
                    // Remove the song from the playlist
                    user.unlikeSong(currentPlayerState.getSelectedSongName());

                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "like");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Unlike registered successfully.");

                    outputs.add(outputNode);
                } else {
                    // Add the song to the playlist
                    user.likeSong(currentPlayerState.getSelectedSongName());

                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "like");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Like registered successfully.");

                    outputs.add(outputNode);
                }

            } else if(loadedPlaylist) {
                boolean songExists = false;
                List<String> likedSongs = user.getLikedSongs();
                for(String songName : likedSongs) {
                    if(songName.equals(currentPlaylistState.getSelectedTrackName())) {
                        songExists = true;
                        break;
                    }
                }
                if(songExists) {
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

    public static void handlePrefferedSongs(LibraryInput library, List<UserInformation> userInformationList, JsonNode commandNode, ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();

        UserInformation user = getUserByUsername(userInformationList, username);

        List<String> likedSongs = user.getLikedSongs();

        ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
        outputNode.put("command", "showPreferredSongs");
        outputNode.put("user", username);
        outputNode.put("timestamp", timestamp);

        // Add results to the output JSON
        ArrayNode resultsArrayNode = outputNode.putArray("result");
        for (String result : likedSongs) {
            resultsArrayNode.add(result);
        }

        // Add the output to the outputs array
        outputs.add(outputNode);
    }

    public static void handleShowPlaylistsCommand(LibraryInput library, List<UserInformation> userInformationList, JsonNode commandNode, ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();

        UserInformation user = getUserByUsername(userInformationList, username);

        List<Playlist> playlists = user.getPlaylists();
        ArrayNode resultsArrayNode = JsonNodeFactory.instance.arrayNode();

        for(Playlist playlist : playlists) {
            ObjectNode playlistNode = JsonNodeFactory.instance.objectNode();
            playlistNode.put("name", playlist.getName());
            playlistNode.set("songs", convertListToJsonArray(playlist.getSongs()));
            playlistNode.put("visibility", playlist.getVisibilityStatus() == 1 ? "public" : "private");
            playlistNode.put("followers", user.getFollowers(userInformationList, playlist));

            resultsArrayNode.add(playlistNode);
        }

        ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
        outputNode.put("command", "showPlaylists");
        outputNode.put("user", username);
        outputNode.put("timestamp", timestamp);
        outputNode.set("result", resultsArrayNode);

        // Add the output to the outputs array
        outputs.add(outputNode);
    }

    private static ArrayNode convertListToJsonArray(List<SongInput> list) {
        ArrayNode jsonArray = JsonNodeFactory.instance.arrayNode();
        for (SongInput item : list) {
            jsonArray.add(item.getName());
        }
        return jsonArray;
    }
}
