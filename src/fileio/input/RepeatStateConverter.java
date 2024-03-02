package fileio.input;

public class RepeatStateConverter {

    /**
     * Converts the repeat state to text
     * @param repeatState
     * @param loadedSong
     * @param loadedPlaylist
     * @param loadedPodcast
     * @return
     */
    public static String convertToText(final int repeatState, final boolean loadedSong,
                                       final boolean loadedPlaylist,
                                       final boolean loadedPodcast) {
        if (loadedPlaylist) {
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
            return "No Media Loaded";
        }
    }
}
