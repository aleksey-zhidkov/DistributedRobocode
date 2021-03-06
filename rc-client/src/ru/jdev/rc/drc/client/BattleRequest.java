/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.client;

import ru.jdev.rc.drc.server.*;

import java.util.List;

/**
 * User: jdev
 * Date: 20.08.11
 */
public class BattleRequest {

    private static final int MAX_SCORE = 6300;

    public final List<Competitor> competitors;
    public final BfSpec bfSpec;
    public final int rounds;
    public final String groupName;
    public final String botAlias;

    public int localId;
    public int remoteId;
    public RsBattleResults battleResults;
    public BattleRequestState state;
    public long requestStartExecutingTime;
    public int currentRound;

    public BattleRequest(List<Competitor> competitors, BfSpec bfSpec, int rounds, String groupName, String botAlias) {
        this.competitors = competitors;
        this.bfSpec = bfSpec;
        this.rounds = rounds;
        this.groupName = groupName;
        this.botAlias = botAlias;
    }

    public double getChallengerAPS() {
        // todo(zhidkov): workaround for strange bug, fix it
        if (battleResults == null || battleResults.getCompetitorResults() == null ||
                battleResults.getCompetitorResults().size() < 2) {
            return Double.NaN;
        }
        final CompetitorResults cr = battleResults.getCompetitorResults().get(0);
        final CompetitorResults rr = battleResults.getCompetitorResults().get(1);

        if (cr == null || rr == null) {
            return Double.NaN;
        }

        return ((double) cr.getScore() / (double) (cr.getScore() + rr.getScore())) * 100;
    }

    public int getChallengerScore() {
        // todo(zhidkov): workaround for strange bug, fix it
        if (battleResults == null || battleResults.getCompetitorResults() == null ||
                battleResults.getCompetitorResults().size() < 2) {
            return 0;
        }
        final CompetitorResults cr = battleResults.getCompetitorResults().get(0);

        if (cr == null) {
            return -1;
        }

        return cr.getScore();
    }

    public int getChallengerBulletDamage() {
        // todo(zhidkov): workaround for strange bug, fix it
        if (battleResults == null || battleResults.getCompetitorResults() == null ||
                battleResults.getCompetitorResults().size() < 2) {
            return 0;
        }
        final CompetitorResults cr = battleResults.getCompetitorResults().get(0);

        if (cr == null) {
            return -1;
        }

        return cr.getBulletDamage();
    }

    public int getReferenceScore() {
        // todo(zhidkov): workaround for strange bug, fix it
        if (battleResults == null || battleResults.getCompetitorResults() == null ||
                battleResults.getCompetitorResults().size() < 2) {
            return 0;
        }
        final CompetitorResults cr = battleResults.getCompetitorResults().get(1);

        if (cr == null) {
            return -1;
        }

        return cr.getScore();
    }

    public int getReferenceBulletDamage() {
        // todo(zhidkov): workaround for strange bug, fix it
        if (battleResults == null || battleResults.getCompetitorResults() == null ||
                battleResults.getCompetitorResults().size() < 2) {
            return 0;
        }
        final CompetitorResults cr = battleResults.getCompetitorResults().get(1);

        if (cr == null) {
            return -1;
        }

        return cr.getBulletDamage();
    }

    public int getTotalScore() {
        // todo(zhidkov): workaround for strange bug, fix it
        if (battleResults == null || battleResults.getCompetitorResults() == null ||
                battleResults.getCompetitorResults().size() < 2) {
            return 0;
        }
        final CompetitorResults cr = battleResults.getCompetitorResults().get(0);
        final CompetitorResults rr = battleResults.getCompetitorResults().get(1);

        if (cr == null || rr == null) {
            return -1;
        }

        return cr.getScore() + rr.getScore();
    }

    public double getChallengerScoreGainRate() {
        // todo(zhidkov): workaround for strange bug, fix it
        if (battleResults == null || battleResults.getCompetitorResults() == null ||
                battleResults.getCompetitorResults().size() < 2) {
            return Double.NaN;
        }
        final CompetitorResults cr = battleResults.getCompetitorResults().get(0);

        if (cr == null) {
            return Double.NaN;
        }

        return ((double) cr.getScore() / (MAX_SCORE)) * 100;
    }

    public double getChallengerEnergyConserved() {
        // todo(zhidkov): workaround for strange bug, fix it
        if (battleResults == null || battleResults.getCompetitorResults() == null ||
                battleResults.getCompetitorResults().size() < 2) {
            return Double.NaN;
        }
        final CompetitorResults rr = battleResults.getCompetitorResults().get(1);

        if (rr == null) {
            return Double.NaN;
        }
        return 100 - ((double) rr.getBulletDamage() / rounds);
    }

    @Override
    public String toString() {
        return String.valueOf(localId);
    }

    public String getChallengerNameAndVersion() {
        return competitors.get(0).getName() + " " + competitors.get(0).getVersion();
    }

    public String getReferenceNameAndVersion() {
        return competitors.get(1).getName() + " " + competitors.get(1).getVersion();
    }
}
