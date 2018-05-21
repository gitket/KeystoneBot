package com.greatmancode.twitchmplusbot;

import net.engio.mbassy.listener.Handler;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.kitteh.irc.client.library.defaults.element.mode.DefaultChannelUserMode;
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventListener {

    Map<String,String> waitingList = new HashMap<>();
    boolean raffleOpen = false;
    List<String> admins = new ArrayList<>();


    public EventListener() {
        admins.add("greatman321");
        admins.add("tankthus");
        admins.add("logota");
    }

    @Handler
    public void onMessageReceived(ChannelMessageEvent event) {
        System.out.println("Received" + event);
        if (event.getActor().equals(event.getClient().getUser().get())) {
            return;
        }
        if (event.getMessage().startsWith("!mplus")) {
            String[] command = event.getMessage().split(" ");
            if (command.length == 2) {
                if (command[1].equals("winner") && raffleOpen) {
                    if (!admins.contains(event.getActor().getNick())) {
                        return;
                    }
                    if (raffleOpen && waitingList.size() == 0) {
                        event.sendReply("Nobody in the raffle!");
                        return;
                    }
                    int id = randomWithRange(0,waitingList.size() - 1);

                    String user = (String) waitingList.keySet().toArray()[id];
                    String winner = (String) waitingList.values().toArray()[id];
                    final String[] winnerString = {""};
                    admins.forEach(admin -> winnerString[0] += ""+admin + " ");
                    winnerString[0] +=  user + " won! /invite " + winner;
                    event.sendReply(winnerString[0]);
                    waitingList.remove(user);
                    return;
                }
                if (command[1].equals("reset") && raffleOpen) {
                    if (!admins.contains(event.getActor().getNick())) {
                        return;
                    }
                    waitingList.clear();
                    raffleOpen = false;
                    event.sendReply("Raffle reset and closed.");
                    return;
                }
                if (command[1].equals("open") && !raffleOpen) {
                    if (!admins.contains(event.getActor().getNick())) {
                        return;
                    }
                    waitingList.clear();
                    raffleOpen = true;
                    event.sendReply("Raffle opened! Type \"!mplus Character-Realm\" to enter!");
                    return;
                }

                if (raffleOpen) {
                    String[] character = command[1].split("-");
                    OkHttpClient client = new OkHttpClient.Builder().build();
                    HttpUrl url = new HttpUrl.Builder()
                            .scheme("https")
                            .host("us.api.battle.net")
                            .addPathSegments("wow/character/" + character[1] + "/" + character[0])
                            .addQueryParameter("fields", "achievements")
                            .addQueryParameter("locale", "en-US")
                            .addQueryParameter("apikey", "MY AWESOME API KEY")
                            .build();
                    Request request = new Request.Builder().url(url).build();
                    try {
                        Response response = client.newCall(request).execute();
                        if (didM15(response.body().string())) {
                            event.sendReply(event.getActor().getNick() + " shame! You already did at least one +15!");
                            return;
                        } else {
                            waitingList.put(event.getActor().getNick(),command[1]);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    public boolean didM15(String json) throws ParseException {
        boolean result = false;
        if (json != null) {
            JSONParser parser = new JSONParser();
            JSONObject battleNetCharacter = (JSONObject) parser.parse(json);
            JSONObject achivements = (JSONObject) battleNetCharacter.get("achievements");
            JSONArray achivementsCompleted = (JSONArray) achivements.get("achievementsCompleted");
            for (int i = 0; i < achivementsCompleted.size(); i++) {
                if ((long) achivementsCompleted.get(i) == 11162) {
                    result = true;
                }
            }
        }
        return result;
    }

    int randomWithRange(int min, int max)
    {
        int range = (max - min) + 1;
        return (int)(Math.random() * range) + min;
    }
}
