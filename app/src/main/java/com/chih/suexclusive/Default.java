package com.chih.suexclusive;

import java.util.ArrayList;

/**
 * Created by parallels on 6/6/15.
 */
public class Default {
    public final ArrayList<String> lines = new ArrayList<String>();

    public Default() {
        lines.add("[default]");
        lines.add("notify=0");
        lines.add("log=1");
        lines.add("wait=10");
        lines.add("access=0");
        lines.add("respectcm=0");
        lines.add("trustsystem=0");
        lines.add("enablemultiuser=0");
        lines.add("enableduringboot=1");
        lines.add("enablemountnamespaceseparation=1\n");
    }
}
