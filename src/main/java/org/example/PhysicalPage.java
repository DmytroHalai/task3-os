package org.example;

import lombok.Data;

@Data
public class PhysicalPage {
    int id;
    VirtualPage virtualPage;

    @Override
    public String toString() {
        return "\nPhysicalPage\n\t{" +
                "id=" + id +
                ", virtualPage=" + (virtualPage != null ? virtualPage : "null") +
                '}';
    }
}
