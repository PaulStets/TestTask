package com.testapp.testtask.data;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Created by paul on 04.05.17.
 * Recursive abstract data type to store the regions from xml file.
 */

public class Territory implements Serializable, Comparable {
    private String name;
    private List<Territory> regions;
    private String url;

    public Territory(String name, List<Territory> regions) {
        this.name = name;
        this.regions = regions;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Capitalized representation of the name of the region
     * @return formatted string
     */
    public String getName() {
        String nameParts[] = name.split("-");
        if (name.length() == 1) {
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        else {
            String result = "";
            for (String namePart : nameParts) {
                if(!namePart.isEmpty()) {
                    result += namePart.substring(0, 1).toUpperCase() + namePart.substring(1) + " ";
                }

            }
            return result;
        }
    }

    /**
     * Region inside this region.
     * @return list of sub-regions inside this region, otherwise empty list.
     */
    public List<Territory> getRegions() {
        return Collections.unmodifiableList(regions);
    }

    /**
     * Indicates if the region has sub-regions.
     * @return true if there are sub-regions, otherwise false.
     */
    public boolean hasChildren() {
        if (regions.isEmpty() || regions == null) {
            return false;
        }
        else {
            return true;
        }
    }

    @Override
    public int compareTo(@NonNull Object thatObj) {
        int result = -15;
        if (thatObj instanceof Territory) {
            Territory thatTerritory = (Territory) thatObj;
            result = this.getName().compareTo(thatTerritory.getName());
            return result;
        }
        else {
            return result;
        }
    }
}
