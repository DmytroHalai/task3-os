package org.example;

import lombok.Data;

@Data
public class Stats {
    private long accesses;
    private long reads;
    private long writes;
    private long pageFaults;
    private long evictions;

    private long nruClass0;
    private long nruClass1;
    private long nruClass2;
    private long nruClass3;

    public void incAccess(boolean write) {
        accesses++;
        if (write) writes++; else reads++;
    }
}
