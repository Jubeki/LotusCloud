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
                    
                    String[] args = new String[rawArgs.length-1];
                    if(args.length > 0) 
		                System.arraycopy(rawArgs, 1, args, 0, args.length);

                    if (commands.containsKey(rawArgs[0]))
                        commands.get(command).process(rawArgs[0], args);
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
