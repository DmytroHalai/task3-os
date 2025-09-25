package org.example;

import lombok.Data;

@Data
public class VirtualPage {
    int ppn;
    boolean present;
    boolean referenced;
    boolean modified;
}
