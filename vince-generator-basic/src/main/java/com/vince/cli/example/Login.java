package com.vince.cli.example;

import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

public class Login implements Callable<Integer> {
    @Option(names = {"-u", "--user"}, description = "User name")
    String user;

    @Option(names = {"-p", "--password"}, arity = "0..1", description = "Passphrase", interactive = true, echo = true, prompt = "请输入密码：")
    String password;

    @Option(names = {"-cp", "--checkPassword"}, arity = "0..1", description = "Check Password", interactive = true, prompt = "请二次输入密码：")
    String checkPassword;

    public Integer call() throws Exception {
        System.out.println("password = " + password);
        System.out.println("checkPassword = " + checkPassword);
        return 0;
    }

    public static void main(String[] args) {
        args = new String[]{"-u", "user123", "-p", "-cp", "xx"};
        new CommandLine(new Login()).execute(args);
    }
}

