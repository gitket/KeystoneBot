package com.greatmancode.twitchmplusbot;

import org.json.simple.parser.ParseException;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.twitch.TwitchDelaySender;
import org.kitteh.irc.client.library.feature.twitch.TwitchListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TwitchMplusBot {

    public static void main(String[] args) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        Client client = Client.builder()
                .inputListener(line -> System.out.println(sdf.format(new Date()) + ' ' + "[I] " + line))
                .outputListener(line -> System.out.println(sdf.format(new Date()) + ' ' + "[O] " + line))
                .exceptionListener(Throwable::printStackTrace)
                .serverHost("irc.chat.twitch.tv").serverPort(443)
                .serverPassword("AWESOME PASSWORD")
                .nick("perkypugmythicbot")
                .messageSendingQueueSupplier(TwitchDelaySender.getSupplier(false))
                .build();
        client.getEventManager().registerEventListener(new TwitchListener(client));
        client.getEventManager().registerEventListener(new EventListener());
        client.connect();
        client.addChannel("#greatman321","#tankthus", "#logota");
    }
}
