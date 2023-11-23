package fileio.input;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;
import java.util.Map.Entry;
import java.util.Map;
import java.util.HashMap;
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
        loaded = 0;

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

        if(searchType.equals("")) {
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "select");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please conduct a search before making a selection.");
            // Adăugați rezultatul la lista de ieșiri
            outputs.add(outputNode);
            return;
        }
        // Check if lastSearchResults is not null and selectedIndex is within the valid range
        if (lastSearchResults != null && selectedIndex >= 0 && selectedIndex < lastSearchResults.size()) {
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
                    currentPlaylist = SelectHelper.getPlaylistByName(library, selectedCommand, userInformationList, username);
                    currentSong = null;
                    currentPodcast = null;
                    selectedForFollow = true;
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
            if(currentPlaylistState != null && currentPlaylist.getName().equals(currentPlaylistState.getSelectedPlaylistName()) && currentPlaylistState.getOwner().equalsIgnoreCase(username)) {
                currentPlaylistState.setInitialTimestamp(timestamp);
                currentPlaylistState.setLastTimestamp(timestamp);
                currentPlaylistState.setPaused(false);
                currentPlaylistState.reset();
            } else {
                //String repeatStateText = RepeatStateConverter.convertToText(repeatState, loadedSong, loadedPlaylist, loadedPodcast);
                currentPlaylistState = new PlaylistState(currentPlaylist.getName(), shuffleState, !isPlaying, "No Repeat", timestamp, timestamp, currentPlaylist.getSongs(), username);
                currentPlaylistState.reset();
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
        searchType = "";
        loaded = 1;
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
            statsNode.put("shuffle", false);
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

        if(loaded == 0) {
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "shuffle");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please load a source before using the shuffle function.");
            // Adăugați rezultatul la lista de ieșiri
            outputs.add(outputNode);
            return;
        }
        if (loadedPlaylist) {
            if(commandNode.has("seed")) {
                long seed = commandNode.get("seed").asLong();

                currentPlaylistState.setLastTimestamp(timestamp);
                currentPlaylistState.setTimeRemaining(currentPlaylistState.calculateTimeRemaining());
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
                // print the track names and shuffle order
                currentPlaylistState.setLastTimestamp(timestamp);
                currentPlaylistState.setTimeRemaining(currentPlaylistState.calculateTimeRemaining());
                currentPlaylistState.setInitialTimestamp(timestamp);

                if(currentPlaylistState.getTimeRemaining() == 0) {
                    currentPlaylistState.setPaused(true);
                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "shuffle");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Please load a source before using the shuffle function.");
                    // Add the output to the outputs array
                    outputs.add(outputNode);
                    return;
                }
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
            outputNode.put("message", "The loaded source is not a playlist.");

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
                user.createPlaylist(playlistName, 1, username, timestamp);

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

    public static void handleNextCommand(LibraryInput library, JsonNode commandNode, ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();

        if (loadedSong || loadedPodcast || loadedPlaylist) {
            if (loadedSong) {
                boolean stopped = currentPlayerState.next(timestamp);
                if(stopped) {
                    loadedSong = false;
                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "next");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Please load a source before skipping to the next track.");

                    // Add the output to the outputs array
                    outputs.add(outputNode);
                } else {
                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "next");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Skipped to next track successfully. The current track is " + currentPlayerState.getSelectedSongName() + ".");

                    // Add the output to the outputs array
                    outputs.add(outputNode);
                }
            } else if (loadedPodcast) {
                boolean stopped = currentPodcastState.next(timestamp);
                if(stopped) {
                    loadedPodcast = false;
                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "next");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Please load a source before skipping to the next track.");

                    // Add the output to the outputs array
                    outputs.add(outputNode);
                } else {
                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "next");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Skipped to next track successfully. The current track is " + currentPodcastState.getSelectedEpisodeName() + ".");

                    // Add the output to the outputs array
                    outputs.add(outputNode);
                }
            } else if (loadedPlaylist) {
                boolean stopped = currentPlaylistState.next(timestamp);
                if(stopped) {
                    loadedPlaylist = false;
                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "next");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Please load a source before skipping to the next track.");

                    // Add the output to the outputs array
                    outputs.add(outputNode);
                } else {
                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "next");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Skipped to next track successfully. The current track is " + currentPlaylistState.getSelectedTrackName() + ".");

                    // Add the output to the outputs array
                    outputs.add(outputNode);
                }
            }
        } else {
            // Output error message if no source is loaded
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "next");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please load a source before skipping to the next track.");

            // Add the output to the outputs array
            outputs.add(outputNode);
        }
    }

    public static void handlePrevCommand(LibraryInput library, JsonNode commandNode, ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();

        // Check if a source is loaded
        if (loadedSong || loadedPodcast || loadedPlaylist) {
            if (loadedSong) {
                currentPlayerState.prev(timestamp);
                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "prev");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "Returned to previous track successfully. The current track is " + currentPlayerState.getSelectedSongName() + ".");

                // Add the output to the outputs array
                outputs.add(outputNode);
            } else if (loadedPodcast) {
                currentPodcastState.prev(timestamp);
                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "prev");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "Returned to previous track successfully. The current track is " + currentPodcastState.getSelectedEpisodeName() + ".");

                // Add the output to the outputs array
                outputs.add(outputNode);
            } else if (loadedPlaylist) {
                currentPlaylistState.prev(timestamp);
                ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                outputNode.put("command", "prev");
                outputNode.put("user", username);
                outputNode.put("timestamp", timestamp);
                outputNode.put("message", "Returned to previous track successfully. The current track is " + currentPlaylistState.getSelectedTrackName() + ".");

                // Add the output to the outputs array
                outputs.add(outputNode);
            }
        } else {
            // Output error message if no source is loaded
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "prev");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please load a source before returning to the previous track.");

            // Add the output to the outputs array
            outputs.add(outputNode);
        }
    }

    public static void handleForwardCommand(LibraryInput library, JsonNode commandNode, ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();

        if(loadedSong == false && loadedPodcast == false && loadedPlaylist == false) {
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "forward");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please load a source before attempting to forward.");

            outputs.add(outputNode);
            return;
        }
        // Check if a source is loaded
        if (loadedPodcast) {
            boolean stopped = currentPodcastState.forward(timestamp);
            if(stopped) {
                loadedPodcast = false;
            }
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "forward");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Skipped forward successfully.");

            outputs.add(outputNode);
        } else {
            // Output error message if no podcast is loaded
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "forward");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "The loaded source is not a podcast.");

            // Add the output to the outputs array
            outputs.add(outputNode);
        }
    }

    public static void handleBackwardCommand(LibraryInput library, JsonNode commandNode, ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();

        // Check if a source is loaded
        if (loadedPodcast) {
            currentPodcastState.backward(timestamp);

            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "backward");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Rewound successfully.");

            outputs.add(outputNode);
        } else {
            // Output error message if no podcast is loaded
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "backward");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "The loaded source is not a podcast.");

            // Add the output to the outputs array
            outputs.add(outputNode);
        }
    }

    public static void handleFollowCommand(LibraryInput library, List<UserInformation> userInformationList, JsonNode commandNode, ArrayNode outputs) {

        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();

        if(currentSong != null || currentPodcast != null) {
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "follow");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "The selected source is not a playlist.");
            outputs.add(outputNode);
            return;
        }
        if(selected == 0 || selectedForFollow == false) {
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "follow");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please select a source before following or unfollowing.");
            outputs.add(outputNode);
            return;
        }
        if(selectedForFollow) {
            selectedForFollow = false;
        }

        if(currentPlaylist == null) {
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "follow");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please load a playlist before attempting to follow it.");
            outputs.add(outputNode);
            return;
        }

        UserInformation user = getUserByUsername(userInformationList, username);
        UserInformation userToFollow = getUserByUsername(userInformationList, currentPlaylist.getOwner());

        Playlist playlistToFollow = null;
        List<Playlist> playlists = userToFollow.getPlaylists();
        for(Playlist playlist : playlists) {
            if(playlist.getName().equals(currentPlaylist.getName())) {
                playlistToFollow = playlist;
                break;
            }
        }
        List<String> followedPlaylist = user.getFollowedPlaylists();
        boolean playlistExists = false;
        for(String playlistName : followedPlaylist) {
            if(playlistName.equals(playlistToFollow.getName())) {
                playlistExists = true;
                break;
            }
        }
        if(playlistExists) {
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

    public static void handleSwitchVisibilityCommand(LibraryInput library, List<UserInformation> userInformationList, JsonNode commandNode, ArrayNode outputs) {
        String username = commandNode.get("username").asText();
        int timestamp = commandNode.get("timestamp").asInt();

        if(currentPlaylist == null) {
            ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
            outputNode.put("command", "follow");
            outputNode.put("user", username);
            outputNode.put("timestamp", timestamp);
            outputNode.put("message", "Please load a playlist before attempting to follow it.");
            outputs.add(outputNode);
            return;
        }

        UserInformation user = getUserByUsername(userInformationList, username);
        UserInformation userVisibility = getUserByUsername(userInformationList, currentPlaylist.getOwner());
        List<Playlist> playlists = userVisibility.getPlaylists();
        for(Playlist playlist : playlists) {
            if(playlist.getName().equals(currentPlaylist.getName())) {
                if(playlist.getVisibilityStatus() == 1) {
                    playlist.setVisibilityStatus(0);
                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "switchVisibility");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Visibility status updated successfully to private.");
                    outputs.add(outputNode);
                } else {
                    playlist.setVisibilityStatus(1);
                    ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
                    outputNode.put("command", "switchVisibility");
                    outputNode.put("user", username);
                    outputNode.put("timestamp", timestamp);
                    outputNode.put("message", "Visibility status updated successfully to public.");
                    outputs.add(outputNode);
                }
                break;
            }
        }
    }

    public static void handleGetTop5PlaylistsCommand(LibraryInput library, List<UserInformation> userInformationList, JsonNode commandNode, ArrayNode outputs) {

        int timestamp = commandNode.get("timestamp").asInt();
        String username = "null";

        Map<String, Integer> playlistFollowers = new HashMap<>();

        for(UserInformation user : userInformationList) {
            List<Playlist> playlists = user.getPlaylists();
            for(Playlist playlist : playlists) {
                if(playlistFollowers.containsKey(playlist.getName())) {
                    playlistFollowers.put(playlist.getName(), playlistFollowers.get(playlist.getName()) + playlist.getFollowers());
                } else {
                    playlistFollowers.put(playlist.getName(), playlist.getFollowers());
                }
            }
        }

        List<Entry<String, Integer>> sortedPlaylists = new ArrayList<>(playlistFollowers.entrySet());
        sortedPlaylists.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        List<String> top5Playlists = new ArrayList<>();
        int count = 0;
        for (Entry<String, Integer> entry : sortedPlaylists) {
            if (count < 5) {
                top5Playlists.add(entry.getKey());
                count++;
            } else {
                break;
            }
        }

        ArrayNode results = JsonNodeFactory.instance.arrayNode();
        for(String item : top5Playlists) {
            results.add(item);
        }

        ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
        outputNode.put("command", "getTop5Playlists");
        outputNode.put("user", "null");
        outputNode.put("timestamp", timestamp);
        outputNode.set("result", results);

        // Add the output to the outputs array
        outputs.add(outputNode);
    }

    public static void handleGetTop5SongsCommand(LibraryInput library, List<UserInformation> userInformationList, JsonNode commandNode, ArrayNode outputs) {
        int timestamp = commandNode.get("timestamp").asInt();

        Map<String, Integer> songFrequencies = new HashMap<>();

        for(UserInformation user : userInformationList) {
            List<String> likedSongs = user.getLikedSongs();
            for(String songName : likedSongs) {
                if(songFrequencies.containsKey(songName)) {
                    songFrequencies.put(songName, songFrequencies.get(songName) + 1);
                } else {
                    songFrequencies.put(songName, 1);
                }
            }
        }

        List<Entry<String, Integer>> sortedEntries = new ArrayList<>(songFrequencies.entrySet());
        sortedEntries.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        List<String> top5Songs = new ArrayList<>();
        int count = 0;
        for (Entry<String, Integer> entry : sortedEntries) {
            if (count < 5) {
                top5Songs.add(entry.getKey());
                count++;
            } else {
                break;
            }
        }

        ArrayNode results = JsonNodeFactory.instance.arrayNode();
        for (String item : top5Songs) {
            results.add(item);
        }

        ObjectNode outputNode = JsonNodeFactory.instance.objectNode();
        outputNode.put("command", "getTop5Playlists");
        outputNode.put("user", "null");
        outputNode.put("timestamp", timestamp);
        outputNode.set("result", results);

        // Add the output to the outputs array
        outputs.add(outputNode);
    }
}
