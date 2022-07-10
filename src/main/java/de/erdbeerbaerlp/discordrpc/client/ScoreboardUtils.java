package de.erdbeerbaerlp.discordrpc.client;

import net.minecraft.ChatFormatting;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Written by Aaron1998ish on 11/12/2017.
 * https://gist.github.com/aaron1998ish/c25e90f367d76f663a407f6907294d34
 * <p>
 * Useful methods for retrieving data on scoreboards on Hypixel
 */
public class ScoreboardUtils {

    /**
     * Filters scores from a scoreboards objective and returns the values in a list
     * <p>
     * Output was designed around Hypixels scoreboard setup.
     *
     * @param scoreboard the target scoreboard
     * @return returns an empty list if no scores were found.
     */
    public static List<String> getSidebarScores(Scoreboard scoreboard) {

        List<String> found = new ArrayList<>();

        Objective sidebar = scoreboard.getDisplayObjective(1);

        if (sidebar != null) {
            List<Score> scores = new ArrayList<>(scoreboard.getPlayerScores(sidebar));

            /*Scores retrieved here do not care for ordering, this is done by the Scoreboard its self.
              We'll need to do this our selves in this case.
              This will appear backwars in chat, but remember that the scoreboard reverses this order
              to ensure highest scores go first.
             */
            scores.sort(Comparator.comparingInt(Score::getScore));
            found = scores.stream()
                    .filter(score -> {
                        //noinspection ConstantConditions
                        if (score.getObjective() == null || score.getObjective().getName() == null) return false;
                        return score.getObjective().getName().equals(sidebar.getName());
                    })
                    .map(score -> (getPrefixFromContainingTeam(scoreboard, score.getOwner()) + getSuffixFromContainingTeam(scoreboard, score.getOwner())))
                    .collect(Collectors.toList());
        }
        return found;
    }

    /**
     * Filters through Scoreboard teams searching for a team
     * that contains the last part of our scoreboard message.
     *
     * @param scoreboard The target scoreboard
     * @param member     The message we're searching for inside a teams member collection.
     * @return If no team was found, an empty suffix is returned
     */
    private static String getSuffixFromContainingTeam(Scoreboard scoreboard, String member) {
        String suffix = null;
        for (PlayerTeam team : scoreboard.getPlayerTeams()) {
            if (team.getPlayers().contains(member)) {
                suffix = ChatFormatting.stripFormatting(team.getPlayerSuffix().getString());
                break;
            }
        }

        return (suffix == null ? "" : suffix);
    }

    private static String getPrefixFromContainingTeam(Scoreboard scoreboard, String member) {
        String suffix = null;
        for (PlayerTeam team : scoreboard.getPlayerTeams()) {
            if (team.getPlayers().contains(member)) {
                suffix = ChatFormatting.stripFormatting(team.getPlayerPrefix().getString());
                break;
            }
        }
        return (suffix == null ? "" : suffix);
    }
}