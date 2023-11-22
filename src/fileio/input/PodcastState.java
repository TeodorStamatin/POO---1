package fileio.input;

import java.util.ArrayList;
import java.util.List;

public class PodcastState {
    private String selectedPodcastName;
    private boolean isPaused;
    private String repeatState;
    private int lastTimestamp;
    private int initialTimestamp;
    private ArrayList<EpisodeInput> episodes;
    private int currentEpisodeIndex = 0;
    private int timeRemaining;

    // Constructor
    public PodcastState(String selectedPodcastName, boolean isPaused, String repeatState, int lastTimestamp, int initialTimestamp, ArrayList<EpisodeInput> episodeNames) {
        this.selectedPodcastName = selectedPodcastName;
        this.isPaused = isPaused;
        this.repeatState = repeatState;
        this.lastTimestamp = lastTimestamp;
        this.initialTimestamp = initialTimestamp;
        this.episodes = episodeNames;
        updateCurrentEpisodeInfo();
    }

    public String getSelectedPodcastName() {
        return selectedPodcastName;
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public String getRepeatState() {
        return repeatState;
    }


    public boolean isPaused() {
        return isPaused;
    }

    // Additional episode getters
    public List<EpisodeInput> getEpisodeNames() {
        return episodes;
    }

    public int getCurrentEpisodeIndex() {
        return currentEpisodeIndex;
    }

    // Setters
    public void setSelectedPodcastName(String selectedPodcastName) {
        this.selectedPodcastName = selectedPodcastName;
    }


    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    public void setRepeatState(String repeatState) {
        this.repeatState = repeatState;
    }

    public void setLastTimestamp(int lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    public void setInitialTimestamp(int initialTimestamp) {
        this.initialTimestamp = initialTimestamp;
    }


    private void updateCurrentEpisodeInfo() {
        timeRemaining = episodes.get(currentEpisodeIndex).getDuration();
    }

    public int calculateTimeRemaining() {
        if (isPaused) {
            return timeRemaining;
        } else {
            int elapsedTime = lastTimestamp - initialTimestamp;
            // check if you need to go to the next episode
            while(elapsedTime >= timeRemaining) {
                elapsedTime -= timeRemaining;
                currentEpisodeIndex++;
                if (repeatState.equals("Repeat Once")) {
                    currentEpisodeIndex--;
                    repeatState = "No Repeat";
                }
                if (currentEpisodeIndex >= episodes.size()) {
                    if(repeatState.equals("No Repeat")) {
                        currentEpisodeIndex = 0;
                        return 0;
                    } else if (repeatState.equals("Repeat Infinite")) {
                        currentEpisodeIndex = 0;
                    }
                }
                updateCurrentEpisodeInfo();
            }
            return Math.max(0, timeRemaining - elapsedTime);
        }
    }
    public void setTimeRemaining(int timeRemaining) {
        this.timeRemaining = timeRemaining;
    }
    public String getSelectedEpisodeName() {
        return episodes.get(currentEpisodeIndex).getName();
    }
}
