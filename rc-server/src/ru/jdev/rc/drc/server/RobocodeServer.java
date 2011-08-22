/*
 * Copyright (c) 2011 Alexey Zhidkov (Jdev). All Rights Reserved.
 */

package ru.jdev.rc.drc.server;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.Endpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * User: jdev
 * Date: 13.08.11
 */
@WebService
public class RobocodeServer {

    private final Map<Integer, BattleRequest> battleRequests = new HashMap<>();

    private final CodeManager codeManager = new CodeManager();

    private BattleRequestsQueue battleRequestsQueue;
    private BattleResultsBuffer battleResultsBuffer;
    private BattleRequestQueueProcessor processor;

    private final Object brsLock = new Object();
    private int battleRequestsSequence = 0;

    public RobocodeServer() {
    }

    private void init(String authToken) {
        battleRequestsQueue = new BattleRequestsQueue(authToken);
        battleResultsBuffer = new BattleResultsBuffer();
        final RCBattlesExecutor executor = new RCBattlesExecutor();
        processor = new BattleRequestQueueProcessor(battleRequestsQueue, executor, codeManager, battleResultsBuffer);
        Executors.newSingleThreadExecutor().execute(processor);
    }

    @WebMethod
    public boolean hasCompetitor(Competitor competitor) {
        return codeManager.hasCompetitor(competitor);
    }

    @WebMethod
    public List<Competitor> getMissedCompetitors(List<Competitor> competitors) {
        final List<Competitor> missedCompetitors = new ArrayList<>();

        for (Competitor competitor : competitors) {
            if (!codeManager.hasCompetitor(competitor)) {
                missedCompetitors.add(competitor);
            }
        }

        return missedCompetitors;
    }

    @WebMethod
    public boolean registerCode(Competitor competitor, byte[] code) {
        try {
            codeManager.storeCompetitor(competitor, code);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @WebMethod
    public Integer executeBattle(Competitor[] competitors, BFSpec bfSpec, int rounds, String authToken) {
        final int battleRequestId;
        synchronized (brsLock) {
            battleRequestId = battleRequestsSequence++;
        }
        final BattleRequest battleRequest = new BattleRequest(battleRequestId, authToken, competitors, rounds, bfSpec);
        battleRequestsQueue.addBattleRequest(battleRequest);
        battleRequests.put(battleRequestId, battleRequest);
        return battleRequestId;
    }

    @WebMethod
    public BattleRequestState getState(Integer battleRequestId) {
        return battleRequests.get(battleRequestId).state;
    }

    @WebMethod
    public RSBattleResults getBattleResults(Integer battleRequestId) {
        return battleResultsBuffer.getResults(battleRequestId);
    }

    public static void main(String[] args) throws IOException {
        final RobocodeServer server = new RobocodeServer();
        server.init(args[2]);
        final Endpoint endpoint = Endpoint.publish(String.format("http://%s:%s/RS", args[0], args[1]), server);
        System.out.println("Endpoint published");
        System.out.println("Type \"exit\" to exit");

        byte[] buffer = new byte[256];
        int len;

        int lineSeparatorLength = System.getProperty("line.separator").length();
        do {
            len = System.in.read(buffer);
            if (len > lineSeparatorLength && new String(buffer, 0, len - lineSeparatorLength).equalsIgnoreCase("exit")) {
                System.out.println("Stopping queue processor");
                server.processor.stop();
                System.out.println("Stopping end point");
                endpoint.stop();
                break;
            }
        } while (true);
    }

}
