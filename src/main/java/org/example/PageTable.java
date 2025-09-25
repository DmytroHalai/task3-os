package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class PageTable {
    private final List<VirtualPage> virtualPages;
}
