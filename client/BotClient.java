package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class BotClient extends Client{

    public class BotSocketThread extends SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            BotClient.this.sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);

            if (!message.contains(": ")) return;
            String[] splitMsg = message.split(": ");
            String userName = splitMsg[0];
            String data = splitMsg[1];

            Date currentDate = Calendar.getInstance().getTime();

            SimpleDateFormat form;
            String reply = "";

            switch (data) {
                case "дата":
                    form = new SimpleDateFormat("d.MM.YYYY");
                    reply = form.format(currentDate);
                    break;
                case "день":
                    form = new SimpleDateFormat("d");
                    reply = form.format(currentDate);
                    break;
                case "месяц":
                    form = new SimpleDateFormat("MMMM");
                    reply = form.format(currentDate);
                    break;
                case "год":
                    form = new SimpleDateFormat("YYYY");
                    reply = form.format(currentDate);
                    break;
                case "время":
                    form = new SimpleDateFormat("H:mm:ss");
                    reply = form.format(currentDate);
                    break;
                case "час":
                    form = new SimpleDateFormat("H");
                    reply = form.format(currentDate);
                    break;
                case "минуты":
                    form = new SimpleDateFormat("m");
                    reply = form.format(currentDate);
                    break;
                case "секунды":
                    form = new SimpleDateFormat("s");
                    reply = form.format(currentDate);
                    break;
                default:
                    return;
            }

            sendTextMessage("Информация для " + userName + ": " + reply);
        }
    }

    @Override
    protected String getUserName() {
        int random = (int) (Math.random() * 100);
        return "date_bot_" + random;
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }


    public static void main(String[] args) {
        Client botClient = new BotClient();
        botClient.run();
    }
}
