/* $Header: /Users/blentz/rails_rcs/cvs/18xx/game/Attic/MapHex.java,v 1.14 2005/10/22 14:26:46 evos Exp $
 * 
 * Created on 10-Aug-2005
 * Change Log:
 */
package game;

import java.util.regex.*;

import org.w3c.dom.*;

import util.XmlUtils;

/**
 * Represents a Hex on the Map from the Model side.
 * 
 * <p><b>Tile orientations</b>. Tiles can be oriented NS or EW;
 * the directions refer to the "flat" hex sides.
 * <p>The term "orientation" is also used to indicate the amount
 * of rotation (in 60 degree units) from the standard orientation
 * of the tile. The orientation numbers are indicated in the below
 * picture for an NS-oriented tile:<p><code>
 * 
 *       ____3____            
 *      /         \
 *     2           4
 *    /     NS      \
 *    \             /
 *     1           5
 *      \____0____/
 * </code><p>
 * For EW-oriented tiles the above picture should be rotated 30 degrees clockwise.
 * 
 * @author Erik Vos
 */
public class MapHex implements ConfigurableComponentI
{

	public static final int EW = 0;
	public static final int NS = 1;
	protected static int tileOrientation;
	protected static boolean lettersGoHorizontal;
	protected static boolean letterAHasEvenNumbers;

	// Coordinates as used in the ui.hexmap package
	protected int x;
	protected int y;

	// Map coordinates as printed on the game board
	protected String name;
	protected int row;
	protected int column;
	protected int letter;
	protected int number;
	protected String tileFileName;
	protected int preprintedTileId;
	protected int preprintedTileOrientation;
	
	/** Neighbouring hexes <i>to which track may be laid</i>. */
	protected MapHex[] neighbours = new MapHex[6];
	
	/* Temporary storage for impassable hexsides. 
	 * Once neighbours has been set up, this attribute is no longer used.
	 * Only the black or blue bars on the map need be specified,
	 * and each one only once. Impassable non-track sides of "offboard"
	 * (red) and "fixed" (grey or brown) preprinted tiles will be derived
	 * and need not be specified.
	 */
	protected String impassable = null;

	public MapHex()
	{
	}

	/**
	 * @see game.ConfigurableComponentI#configureFromXML(org.w3c.dom.Element)
	 */
	public void configureFromXML(Element el) throws ConfigurationException
	{
		NamedNodeMap nnp = el.getAttributes();
		Pattern namePattern = Pattern.compile("(\\D)(\\d+)");

		name = XmlUtils.extractStringAttribute(nnp, "name");
		Matcher m = namePattern.matcher(name);
		if (!m.matches())
		{
			throw new ConfigurationException("Invalid name format: " + name);
		}
		letter = m.group(1).charAt(0);
		try
		{
			number = Integer.parseInt(m.group(2));
		}
		catch (NumberFormatException e)
		{
			// Cannot occur!
		}

		/*
		 * Translate hex names (as on the board) to coordinates used for
		 * drawing.
		 */
		if (lettersGoHorizontal)
		{
			row = number;
			column = letter - '@';
			if (tileOrientation == MapHex.EW)
			{
				// Tiles with flat EW sides, letters go horizontally.
				// Example: 1841 (NOT TESTED, PROBABLY WRONG).
				x = column;
				y = row / 2;
			}
			else
			{
				// Tiles with flat NS sides, letters go horizontally.
				// Tested for 1856.
				x = column;
				y = (row + 1) / 2;
			}
		}
		else
		// letters go vertical (normal case)
		{
			row = letter - '@';
			column = number;
			if (tileOrientation == MapHex.EW)
			{
				// Tiles with flat EW sides, letters go vertically.
				// Most common case.
				// Tested for 1830 and 1870.
				x = (column + (letterAHasEvenNumbers ? 1 : 0)) / 2;
				y = row;
			}
			else
			{
				// Tiles with flat NS sides, letters go vertically.
				// Tested for 18AL.
				x = column;
				y = (row + 1) / 2;
			}
		}

		String sTileId = XmlUtils.extractStringAttribute(nnp, "tile");
		if (sTileId != null)
		{
			try
			{
				preprintedTileId = Integer.parseInt(sTileId);
			}
			catch (NumberFormatException e)
			{
				throw new ConfigurationException("Invalid tile ID: " + sTileId);
			}
		}
		else
		{
			preprintedTileId = -999;
		}

		preprintedTileOrientation = XmlUtils.extractIntegerAttribute(nnp,
				"orientation",
				0);
		
		impassable = XmlUtils.extractStringAttribute(nnp, "impassable");

	}
	
	public boolean isNeighbour (MapHex neighbour) {
	    
	    /* Various reasons why a bordering hex may not be a neighbour
	     * in the sense that track may be laid to that border:
	     */
	    /* 1. The hex side is marked "impassable" */
	    if (impassable != null && impassable.indexOf(neighbour.getName()) > -1) return false;
	    /* 2. The preprinted tile on this hex is offmap or fixed and has no track to this side. */
	    // We can't handle this as we don't yet read Tiles.xml.
	    
	    return true;
	}

	public static void setTileOrientation(int orientation)
	{
		tileOrientation = orientation;
	}

	public static int getTileOrientation()
	{
		return tileOrientation;
	}

	public static void setLettersGoHorizontal(boolean b)
	{
		lettersGoHorizontal = b;
	}

	/**
	 * @return Returns the letterAHasEvenNumbers.
	 */
	public static boolean hasLetterAEvenNumbers()
	{
		return letterAHasEvenNumbers;
	}

	/**
	 * @param letterAHasEvenNumbers
	 *            The letterAHasEvenNumbers to set.
	 */
	public static void setLetterAHasEvenNumbers(boolean letterAHasEvenNumbers)
	{
		MapHex.letterAHasEvenNumbers = letterAHasEvenNumbers;
	}

	/**
	 * @return Returns the lettersGoHorizontal.
	 */
	public static boolean isLettersGoHorizontal()
	{
		return lettersGoHorizontal;
	}

	/**
	 * @return Returns the column.
	 */
	public int getColumn()
	{
		return column;
	}

	/**
	 * @return Returns the row.
	 */
	public int getRow()
	{
		return row;
	}

	public String getName()
	{
		return name;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	/**
	 * @return Returns the preprintedTileId.
	 */
	public int getPreprintedTileId()
	{
		return preprintedTileId;
	}

	/**
	 * @return Returns the image file name for the tile.
	 */
	public String getTileFileName()
	{
		return tileFileName;
	}

	/**
	 * @return Returns the preprintedTileOrientation.
	 */
	public int getPreprintedTileOrientation()
	{
		return preprintedTileOrientation;
	}

	public void setNeighbor(int orientation, MapHex neighbour)
	{
		orientation %= 6;
		neighbours[orientation] = neighbour;
	}

	public MapHex getNeighbor(int orientation)
	{
		return neighbours[orientation % 6];
	}

	public MapHex[] getNeighbors()
	{
		return neighbours;
	}
	
	/** Look for the Hex matching the Label in the terrain static map */
	/* EV: useful, but needs to be rewritten */
	public static MapHex getHexByLabel(String terrain, String label)
	{
	    /*
		int x = 0;
		int y = Integer.parseInt(new String(label.substring(1)));
		switch (label.charAt(0))
		{
			case 'A':
			case 'a':
				x = 0;
				break;

			case 'B':
			case 'b':
				x = 1;
				break;

			case 'C':
			case 'c':
				x = 2;
				break;

			case 'D':
			case 'd':
				x = 3;
				break;

			case 'E':
			case 'e':
				x = 4;
				break;

			case 'F':
			case 'f':
				x = 5;
				break;

			case 'X':
			case 'x':

				// entrances
				GUIHex[] gameEntrances = (GUIHex[]) entranceHexes.get(terrain);
				return gameEntrances[y].getMapHexModel();

			default:
				Log.error("Label " + label + " is invalid");
		}
		y = 6 - y - (int) Math.abs(((x - 3) / 2));
		GUIHex[][] correctHexes = (GUIHex[][]) terrainH.get(terrain);
		return correctHexes[x][y].getMapHexModel();
		*/
		return null;
	}

}
