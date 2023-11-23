package main;

import checker.Checker;
import checker.CheckerConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import fileio.input.LibraryInput;
import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import fileio.input.Command;
import fileio.input.UserInformation;
import fileio.input.UserInput;

/**
 * The entry point to this homework. It runs the checker that tests your implentation.
 */
public final class Main {
    static final String LIBRARY_PATH = CheckerConstants.TESTS_PATH + "library/library.json";

    /**
     * for coding style
     */
    private Main() {
    }

    /**
     * DO NOT MODIFY MAIN METHOD
     * Call the checker
     * @param args from command line
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void main(final String[] args) throws IOException {
        File directory = new File(CheckerConstants.TESTS_PATH);
        Path path = Paths.get(CheckerConstants.RESULT_PATH);

        if (Files.exists(path)) {
            File resultFile = new File(String.valueOf(path));
            for (File file : Objects.requireNonNull(resultFile.listFiles())) {
                file.delete();
            }
            resultFile.delete();
        }
        Files.createDirectories(path);

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.getName().startsWith("library")) {
                continue;
            }

            String filepath = CheckerConstants.OUT_PATH + file.getName();
            File out = new File(filepath);
            boolean isCreated = out.createNewFile();
            if (isCreated) {
                action(file.getName(), filepath);
            }
        }

        Checker.calculateScore();
    }

    /**
     * @param filePathInput for input file
     * @param filePathOutput for output file
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void action(final String filePathInput,
                              final String filePathOutput) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        LibraryInput library = objectMapper.readValue(new File(LIBRARY_PATH), LibraryInput.class);

        List<UserInput> users = library.getUsers();
        List<UserInformation> userInformationList = new ArrayList<>();

        for (UserInput userInput : users) {
            // Extract user data from UserInput
            String username = userInput.getUsername();
            int age = userInput.getAge();
            String city = userInput.getCity();

            // Create UserInformation object
            UserInformation userInformation = new UserInformation(username, age, city);

            // Add UserInformation to the list
            userInformationList.add(userInformation);
        }

        ArrayNode outputs = objectMapper.createArrayNode();

        JsonNode input = objectMapper.readTree(new File(CheckerConstants.TESTS_PATH + filePathInput));

        for (JsonNode commandNode : input) {

            String commandType = commandNode.get("command").asText();

            switch (commandType) {
                case "search":
                    Command.handleSearchCommand(library, commandNode, outputs, userInformationList);
                    break;
                case "select":
                    Command.handleSelectCommand(library, commandNode, outputs, userInformationList);
                    break;
                case "load":
                    Command.handleLoadCommand(library, commandNode, outputs, userInformationList);
                    break;
                case "playPause":
                    Command.handlePlayPauseCommand(library, commandNode, outputs);
                    break;
                case "repeat":
                    Command.handleRepeatCommand(library, commandNode, outputs);
                    break;
                case "shuffle":
                    Command.handleShuffleCommand(library, commandNode, outputs);
                    break;
                case "status":
                    Command.handleStatusCommand(library, commandNode, outputs);
                    break;
                case "createPlaylist":
                    Command.handleCreatePlaylistCommand(library, userInformationList, commandNode, outputs);
                    break;
                case "like":
                    Command.handleLikeCommand(library, userInformationList, commandNode, outputs);
                    break;
                case "addRemoveInPlaylist":
                    Command.handleAddRemoveInPlaylistCommand(library, userInformationList, commandNode, outputs);
                    break;
                case "showPreferredSongs":
                    Command.handlePrefferedSongs(library, userInformationList, commandNode, outputs);
                    break;
                case "showPlaylists":
                    Command.handleShowPlaylistsCommand(library, userInformationList, commandNode, outputs);
                    break;
                case "next":
                    Command.handleNextCommand(library, commandNode, outputs);
                    break;
                case "prev":
                    Command.handlePrevCommand(library, commandNode, outputs);
                    break;
                case "forward":
                    Command.handleForwardCommand(library, commandNode, outputs);
                    break;
                case "backward":
                    Command.handleBackwardCommand(library, commandNode, outputs);
                    break;
                case "follow":
                    Command.handleFollowCommand(library, userInformationList, commandNode, outputs);
                    break;
                case "switchVisibility":
                    Command.handleSwitchVisibilityCommand(library, userInformationList, commandNode, outputs);
                    break;
                case "getTop5Playlists":
                    Command.handleGetTop5PlaylistsCommand(library, userInformationList, commandNode, outputs);
                    break;
                case "getTop5Songs":
                    Command.handleGetTop5SongsCommand(library, userInformationList, commandNode, outputs);
                    break;
            }
        }


        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(new File(filePathOutput), outputs);
    }
}