package fileio.input;

public class RepeatStateConverter {

    public static String convertToText(int repeatState, boolean loadedSong, boolean loadedPlaylist, boolean loadedPodcast) {
        if (loadedPlaylist) {
            // Check if playlist is loaded
            switch (repeatState) {
                case 0:
                    return "No Repeat";
                case 1:
                    return "Repeat All";
                case 2:
                    return "Repeat Current Song";
                default:
                    return "Unknown State";
            }
        } else if (loadedSong || loadedPodcast) {
            // Check if a song is loaded
            switch (repeatState) {
                case 0:
                    return "No Repeat";
                case 1:
                    return "Repeat Once";
                case 2:
                    return "Repeat Infinite";
                default:
                    return "Unknown State";
            }
        } else {
            // Neither song nor playlist is loaded
            return "No Media Loaded";
        }
    }
}
