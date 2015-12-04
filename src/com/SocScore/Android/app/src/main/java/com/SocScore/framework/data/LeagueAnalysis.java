package com.SocScore.framework.data;

import com.SocScore.framework.scorekeeper.ScoreKeeper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Static class which stores the lists of {@link Team} and {@link Match} in the league, and performs operations on them.
 * @see com.SocScore.framework.AnalysisViewer
 * @see com.SocScore.framework.scorekeeper.BatchInput
 * @see com.SocScore.framework.scorekeeper.LiveInput
 */
public class LeagueAnalysis {
    private static List<Team> league = new ArrayList<>();
    private static List<Match> matches = new ArrayList<>();

    /**
     * Adds a new team to the league.
     * @param team Team to be added to the league.
     */
    public static void addTeam(Team team) {
        league.add(team);
    }

    /**
     * Removes (and deletes, unless passed to {@link com.SocScore.framework.scorekeeper.BatchInput}) an existing {@link Match} from the league.
     * @param match Match to remove from the league.
     */
    public static void removeMatch(Match match) {
        matches.remove(match);
    }

    /**
     * Adds a new {@link Match} to the league.
     * Matches are generated by {@link com.SocScore.framework.scorekeeper.BatchInput} and {@link com.SocScore.framework.scorekeeper.LiveInput}.
     * @param match Match to be added to the league.
     */
    public static void addMatch(Match match) {
        matches.add(match);
    }

    /**
     * Removes (and deletes) a {@link Team} from the league.
     * @param team Team to be removed from the league.
     */
    public static void removeTeam(Team team) {
        league.remove(team);
    }

    /**
     * Finds a {@link Team} in the league by ID.
     * @param ID ID of the team being searched for.
     * @return Returns the team object if it is found.
     * @throws RuntimeException Throws an exception if no team is found under the providedID.
     */
    public static Team findTeam(int ID) throws RuntimeException {
        for(Team team : league) {
                if(team.getTEAM_ID() == ID) return team;
        }
        throw new NullPointerException("Could not find team under provided ID: " + ID);
    }

    /**
     * Ranks (sorts) {@link Team} in the league by ID (increasing), score (decreasing), or total number of goals (decreasing).
     * @param type Type of rank to apply to the league.
     */
    public static void rankLeague(TeamRankType type) {
        switch(type) {
            case ID: Collections.sort(league, Team.rankByID);
            case TEAM_SCORE: Collections.sort(league, Team.rankByScore);
            case TOTAL_GOALS: Collections.sort(league, Team.rankByTotalGoals);
            default: break;
        }
    }

    /**
     * Sorts {@link Match} in the league by their ID (in increasing order).
     */
    public static void sortMatchesByID() {
        Collections.sort(matches, Match.sortByID);
    }

    /**
     * Sorts {@link Match} in the league by their date (newest to oldest).
     */
    public static void sortMatchesByDate() {
        Collections.sort(matches, Match.sortByTime);
    }

    /**
     * Finds and aggregates all {@link Match} in the league that have been played by the inputted {@link Team}.
     * @param teamID ID of the team to find matches for.
     * @return Returns an ArrayList of all matches played by the team.
     * @throws RuntimeException Throws an exception if no matches could be found for the team.
     */
    public static List getMatchesForTeam(int teamID) throws RuntimeException {
        List<Match> searchList = new ArrayList();
        for(Match match : matches) {
            if(match.getTEAM1_ID() == teamID || match.getTEAM2_ID() == teamID) {
                searchList.add(match);
            }
        }
        if(searchList.isEmpty()) throw new RuntimeException("Could not find any matches for Team ID: " + teamID);
        return searchList;
    }

    /**
     * Finds a {@link Match} in the league by its ID.
     * @param ID ID of the match being looked for.
     * @return Returns the Match object if it is found.
     * @throws RuntimeException Throws an exception if the match with the ID provided can't be found.
     */
    public static Match findMatch(int ID) throws RuntimeException {
        for (Match match : matches) {
            if(match.getMATCH_ID() == ID) return match;
        }
        throw new NullPointerException("Could not find match under provided ID: " + ID);
    }

    /**
     * Saves league (Lists of {@link Team} and {@link Match}) to the disk.
     * @throws RuntimeException Throws an exception if there are still matches being managed by
     * {@link com.SocScore.framework.scorekeeper.BatchInput} or {@link com.SocScore.framework.scorekeeper.LiveInput},
     * as those are not stored to the disk by this method.
     */
    public static void saveLeagueToDisk() throws RuntimeException {
        if(ScoreKeeper.hasUnsavedMatches())
            throw new RuntimeException("Cannot save to disk until all matches have been transferred from Score Keeping instances to League");
        rankLeague(TeamRankType.ID);
        sortMatchesByID();
        DataPersistence.saveToDisk("league.xml", league);
        DataPersistence.saveToDisk("matches.xml", matches);
    }

    /**
     * Loads a saved league (Lists of {@link Team} pr {@link Match}) from disk, and calls the reset methods on the lists' elements.
     * This is done to restore object links in the matches, teams, and players.
     * @see Team#resetPlayers()
     * @see Match#resetTeams()
     */
    public static void loadLeagueFromDisk() {
        league = DataPersistence.loadFromDisk("league.xml");
        matches = DataPersistence.loadFromDisk("matches.xml");
        for(Team team: league) {
            team.resetPlayers();
        };
        for(Match match : matches) {
            match.resetTeams();
        }
    }

    /**
     * @return Returns an ArrayList of every {@link Team} present in the league, in the order it was last sorted in.
     * @see #rankLeague(TeamRankType)
     * @see #sortMatchesByID()
     * @see #sortMatchesByDate()
     */
    public static List getLeague() {
        return league;
    }

    /**
     * @return Returns an ArrayList of every {@link Match} present in the league. Note that this does not include
     * matches currently being edited by {@link com.SocScore.framework.scorekeeper.BatchInput} or {@link com.SocScore.framework.scorekeeper.LiveInput}.
     */
    public static List getMatches() {
        return matches;
    }
}