package org.example;

import lombok.Data;

@Data
public class VirtualPage {
    private int ppn;
    private boolean present;
    private boolean referenced;
    private boolean modified;
    private int pid;
    private int vpn;
}
