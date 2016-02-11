package org.projectbuendia.client.ui.chart;

import org.projectbuendia.client.App;
import org.projectbuendia.client.R;
import org.projectbuendia.client.models.ChartItem;
import org.projectbuendia.client.models.ObsPoint;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

/** Descriptor for a tile (latest observed value) in the patient chart. */
public class Tile {
    public final ChartItem item;
    public final ObsPoint[] points;

    static Map<String, ChartItem> DEFAULTS = new HashMap<>();
    static {
        DEFAULTS.put("select_one", new ChartItem("", "", false, null, "{1,abbr}", "{1,name}", "", "", ""));
        // DO NOT MERGE. This is a hack, we shouldn't be statically accessing the App here in the
        // hope that this class isn't referenced as part of the App class. Instead store the
        // resource ID.
        DEFAULTS.put("yes_no", new ChartItem("", "", false, null, App.getInstance().getResources().getString(R.string.default_format_yes_no), "", "", "", ""));
        DEFAULTS.put("yes_no_unknown", new ChartItem("", "", false, null, App.getInstance().getResources().getString(R.string.default_format_yes_no_unknown), "", "", "", ""));
        DEFAULTS.put("number", new ChartItem("", "", false, null, "0", "", "", "", ""));
        DEFAULTS.put("text", new ChartItem("", "", false, null, "{1,text,60}", "", "", "", ""));
        DEFAULTS.put("date", new ChartItem("", "", false, null, "{1,date,YYYY-MM-dd}", "", "", "", ""));
        DEFAULTS.put("time", new ChartItem("", "", false, null, "{1,time,HH:mm}", "", "", "", ""));
    }
    
    public Tile(@Nonnull ChartItem item, @Nonnull ObsPoint[] points) {
        this.item = item.withDefaults(DEFAULTS.get(item.type));
        this.points = points;
    }
}
