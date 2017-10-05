package com.lheinrich.lotuscloud.builder;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Copyright (c) 2017 Lennart Heinrich (www.lheinrich.com)
 */
public class Builder {

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("Starting LotusCloud Builder...\nCopyright (c) 2017 Lennart Heinrich\nExit with CTRL + C");

        Thread.sleep(3000);

        System.out.println("Cloning Git Repository https://github.com/NexusByte/LotusCloud.git");

        process("", "git", "clone", "https://github.com/NexusByte/LotusCloud.git");

        System.out.println("Git Repository cloned");

        process("LotusCloud/API", "mvn", "clean", "install");

        process("LotusCloud/Master", "mvn", "clean", "package");

        process("LotusCloud/Wrapper", "mvn", "clean", "package");

        File file = new File("");

        System.out.println("\n\n\n\n\n\n\n\n\n\nMaster Jar: " + file.getAbsolutePath() + "/LotusCloud/Master/target\nWrapper Jar: " + file.getAbsolutePath() + "/LotusCloud/Wrapper/target");
        System.out.println("\nFinished!");

        Thread.sleep(1000);
    }

    private static void process(String dir, String... command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command).directory(new File(dir).getAbsoluteFile());

        Process process = processBuilder.start();

        Scanner scanner = new Scanner(process.getInputStream());

        while (process.isAlive()) {
            if (scanner.hasNextLine()) {
                System.out.println(scanner.nextLine());
            }
        }
    }
}
