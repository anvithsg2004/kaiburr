package com.example.kaiburr.service;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CommandValidator {
    private static final Set<String> DENY_TOKENS = Set.of(";", "&&", "||", "|", "`", "$(", ">", ">>", "<", "<<", "&", "sudo", "rm", "mkfs", "dd", "kill", "reboot", "shutdown", "init", "halt", "chown", "chmod", "kubectl", "docker", "curl", "wget", "nc", "telnet", "scp", "ssh", ":(){:|:&};:");
    private static final Set<String> ALLOW_PREFIXES = Set.of("echo", "ls", "pwd", "whoami", "date", "cat");

    public boolean isSafe(String cmd) {
        if (cmd == null) return false;
        String c = cmd.trim();
        boolean allowed = ALLOW_PREFIXES.stream().anyMatch(p -> c.startsWith(p + " ") || c.equals(p));
        if (!allowed) return false;
        String lowered = c.toLowerCase();
        for (String t : DENY_TOKENS) if (lowered.contains(t)) return false;
        return true;
    }
}
