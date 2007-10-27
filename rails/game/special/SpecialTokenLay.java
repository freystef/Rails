/* $Header: /Users/blentz/rails_rcs/cvs/18xx/rails/game/special/SpecialTokenLay.java,v 1.5 2007/10/27 15:26:35 evos Exp $ */
package rails.game.special;


import java.util.ArrayList;
import java.util.List;

import rails.game.*;
import rails.util.Tag;
import rails.util.Util;

public class SpecialTokenLay extends SpecialProperty
{
	String locationCodes = null;
    List<MapHex> locations = null;
	boolean extra = false;
	boolean free = false;
	boolean connected = false;
	Class tokenClass;
	TokenI token = null;
    int numberAvailable = 1;
    int numberUsed = 0;

	public void configureFromXML(Tag tag) throws ConfigurationException
	{
		
		super.configureFromXML (tag);

		Tag tokenLayTag = tag.getChild("SpecialTokenLay");
		if (tokenLayTag == null)
		{
			throw new ConfigurationException("<SpecialTokenLay> tag missing");
		}

		locationCodes = tokenLayTag.getAttributeAsString("location");
		if (!Util.hasValue(locationCodes))
			throw new ConfigurationException("SpecialTokenLay: location missing");
        MapManager mmgr = MapManager.getInstance();
        MapHex hex;
        locations = new ArrayList<MapHex>();
        for (String hexName : locationCodes.split(",")) {
            hex = mmgr.getHex(hexName);
            if (hex == null)
                throw new ConfigurationException("Location " + hexName
                        + " does not exist");
            locations.add (hex);
        }

		extra = tokenLayTag.getAttributeAsBoolean("extra", extra);
		free = tokenLayTag.getAttributeAsBoolean("free", free);
		connected = tokenLayTag.getAttributeAsBoolean("connected", connected);
		closingValue = tokenLayTag.getAttributeAsInteger("closingValue", closingValue);
		
		String tokenClassName = tokenLayTag.getAttributeAsString(
				"class", "rails.game.BaseToken");
		try {
			tokenClass = Class.forName(tokenClassName);
			if (tokenClass == BonusToken.class) {
                BonusToken bToken = (BonusToken) tokenClass.newInstance();
                token = bToken;
                bToken.configureFromXML(tokenLayTag);
                
                numberAvailable = tokenLayTag.getAttributeAsInteger(
                		"number", numberAvailable);
			}
		} catch (ClassNotFoundException e) {
			throw new ConfigurationException ("Unknown class "+tokenClassName, e);
		} catch (Exception e) {
			throw new ConfigurationException ("Cannot instantiate class "+tokenClassName, e);
		}
	}
	
	public boolean isExecutionable() {
		return true;
	}
    
    public int getNumberLeft () {
        return numberAvailable - numberUsed;
    }

	public boolean isExtra()
	{
		return extra;
	}

	public boolean isFree()
	{
		return free;
	}

    /** @deprecated */
	public MapHex getLocation()
	{
        if (locations != null) {
            return locations.get(0);
        } else {
            return null;
        }
	}
    
    public List<MapHex> getLocations () {
        return locations;
    }
    
    public String getLocationCodeString() {
    	return locationCodes;
    }
	
	public Class getTokenClass() {
        return tokenClass;
    }

    public String toString() {
	    return "SpecialTokenLay comp="+privateCompany.getName()
            +" type="+tokenClass.getSimpleName() 
            +": "+ (token != null ? token.toString() : "")
            +" hex="+locationCodes+" extra="+extra+" cost="+free;
	}
}
