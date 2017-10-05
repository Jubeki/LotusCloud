package com.lheinrich.lotuscloud.api.console;

import java.util.HashMap;
import java.util.Scanner;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class ConsoleReader {

    private HashMap<String, ConsoleCommand> commands = new HashMap<>();

    public ConsoleReader() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String[] rawArgs = scanner.nextLine().split(" ");

                if (rawArgs.length > 0 && !rawArgs[0].equals("")) {
                    String command = rawArgs[0];
                    String args = "";

                    for (int i = 1; i < rawArgs.length; i++)
                        args += rawArgs[i] + " ";

                    if (commands.containsKey(command))
                        commands.get(command).process(command, (args.equals("") ? new String[]{} : args.split(" ")));
                    else
                        System.out.println("Not found");
                }
            }
        }).start();
    }

    public void register(String name, ConsoleCommand command) {
        if (commands.containsKey(name))
            commands.remove(name);
        commands.put(name, command);
    }

    public void unregister(String name) {
        if (commands.containsKey(name))
            commands.remove(name);
    }
}