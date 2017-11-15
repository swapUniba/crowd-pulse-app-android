package com.swapuniba.crowdpulse.business_object;

import org.json.JSONObject;

/**
 * Created by fabio on 05/10/17.
 */

public abstract class AbstractData {

    public abstract String getSource();

    /**
     * please insert the source_type !!!
     * @return
     */
    public abstract JSONObject toJSON();

    public abstract void fromJSON(JSONObject jsonObject);





}
