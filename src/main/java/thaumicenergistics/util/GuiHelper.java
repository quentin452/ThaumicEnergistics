package thaumicenergistics.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiHelper
{
	/**
	 * Pixel area represented by the top-left and bottom-right corners of a
	 * rectangle.
	 * 
	 * @author Nividica
	 * 
	 */
	private static class Bounds
	{
		/**
		 * Top Y position.
		 */
		public int T;

		/**
		 * Left X position.
		 */
		public int L;

		/**
		 * Bottom Y position.
		 */
		public int B;

		/**
		 * Right X position.
		 */
		public int R;

		/**
		 * Creates the boundary
		 * 
		 * @param t
		 * Top Y position.
		 * @param l
		 * Left X position.
		 * @param b
		 * Bottom Y position.
		 * @param r
		 * Right X position.
		 */
		public Bounds( final int t, final int l, final int b, final int r )
		{
			this.T = t;
			this.L = l;
			this.B = b;
			this.R = r;
		}
	}

	/**
	 * Maps int -> mouse button
	 */
	public static final int MOUSE_BUTTON_LEFT = 0;

	public static final int MOUSE_BUTTON_RIGHT = 1;

	public static final int MOUSE_BUTTON_WHEEL = 2;

	/**
	 * Character MC uses to denote the next character is a formating character
	 */
	public static final String CHAT_COLOR_HEADER = "�";

	/**
	 * Length of color arrays.
	 */
	private static final int COLOR_ARRAY_SIZE = 4;

	/**
	 * Tooltip offset from the mouse.
	 */
	private static final int TOOLTIP_OFFSET = 12;

	/**
	 * Height of a tooltip with no text.
	 */
	private static final int TOOLTIP_EMPTY_HEIGHT = 8;

	/**
	 * (Top) Margin from the borders to start drawing text.
	 */
	private static final int TOOLTIP_HEIGHT_MARGIN = 2;

	/**
	 * Height of each line of text.
	 */
	private static final int TOOLTIP_LINE_HEIGHT = 10;

	/**
	 * Color of the tooltip's outer borders.
	 */
	private static final int TOOLTIP_COLOR_OUTER = 0xFF000000;

	/**
	 * Color of the tooltip background.
	 */
	private static final int TOOLTIP_COLOR_BACKGROUND = 0xF0100010;

	/**
	 * Starting color of the tooltip's inner borders. 
	 */
	private static final int TOOLTIP_COLOR_INNER_BEGIN = 0xC05000FF;

	/**
	 * Ending color of the tooltip's inner borders. 
	 */
	private static final int TOOLTIP_COLOR_INNER_END = 0xC05000FF;

	/**
	 * Thickness of the tooltip's borders.
	 */
	private static final int TOOLTIP_BORDER_SIZE = 3;

	/**
	 * Bitshift amounts based on byte position
	 */
	private static final int[] COLOR_SHIFT_AMOUNT = new int[] { 0, 8, 16, 24 };

	/**
	 * Draws the background, outer borders, and inner borders for a tooltip.
	 * 
	 * @param guiObject
	 * @param drawGradientRect
	 * @param bounds
	 * @throws Exception
	 */
	private static final void drawTooltipBackground( final Gui guiObject, final Method drawGradientRect, final Bounds bounds ) throws Exception
	{
		// Background
		drawGradientRect.invoke( guiObject, bounds.L, bounds.T, bounds.R, bounds.B, GuiHelper.TOOLTIP_COLOR_BACKGROUND,
			GuiHelper.TOOLTIP_COLOR_BACKGROUND );

		// Draw outer borders
		GuiHelper.drawTooltipBorders( guiObject, drawGradientRect, bounds, GuiHelper.TOOLTIP_COLOR_OUTER, GuiHelper.TOOLTIP_COLOR_OUTER, 0 );

		// Adjust bounds for inner borders
		bounds.T++ ;
		bounds.L++ ;
		bounds.B-- ;
		bounds.R-- ;

		// Draw innder borders
		GuiHelper.drawTooltipBorders( guiObject, drawGradientRect, bounds, GuiHelper.TOOLTIP_COLOR_INNER_BEGIN, GuiHelper.TOOLTIP_COLOR_INNER_END, 1 );
	}

	/**
	 * Draws the vertical and horizontal borders for a tooltip
	 * 
	 * @param guiObject
	 * @param drawGradientRect
	 * @param bounds
	 * @param colorStart
	 * @param colorEnd
	 * @param cornerExpansion 1 to connect corners, 0 to leave notches.
	 * @throws Exception
	 */
	private static final void drawTooltipBorders( final Gui guiObject, final Method drawGradientRect, final Bounds bounds, final int colorStart,
													final int colorEnd, final int cornerExpansion ) throws Exception
	{
		// Left
		drawGradientRect.invoke( guiObject, bounds.L - 1, bounds.T - cornerExpansion, bounds.L, bounds.B + cornerExpansion, colorStart, colorEnd );

		// Top
		drawGradientRect.invoke( guiObject, bounds.L, bounds.T - 1, bounds.R, bounds.T, colorStart, colorEnd );

		// Right
		drawGradientRect.invoke( guiObject, bounds.R, bounds.T - cornerExpansion, bounds.R + 1, bounds.B + cornerExpansion, colorStart, colorEnd );

		// Bottom
		drawGradientRect.invoke( guiObject, bounds.L, bounds.B, bounds.R, bounds.B + 1, colorStart, colorEnd );
	}

	public static final byte[] convertPackedColorToARGB( final int color )
	{
		byte[] colorBytes = new byte[COLOR_ARRAY_SIZE];

		// Extract bytes
		for( int i = 0; i < COLOR_ARRAY_SIZE; i++ )
		{
			// Get byte
			colorBytes[COLOR_ARRAY_SIZE - 1 - i] = (byte)( ( color >> COLOR_SHIFT_AMOUNT[i] ) & 0xFF );
		}

		return colorBytes;
	}

	public static final int[] createColorGradient( final int fromColor, final int toColor, final int iterations )
	{
		// Is there enough iterations to create a gradient?
		if( iterations < 3 )
		{
			return new int[] { fromColor, toColor };
		}

		// Holds the A,R,G,B bytes of each color
		int[] fromColorBytes = new int[COLOR_ARRAY_SIZE];
		int[] toColorBytes = new int[COLOR_ARRAY_SIZE];

		// Holds how much to change the color amount by for each iteration
		float[] stepAmount = new float[COLOR_ARRAY_SIZE];

		// Holds the color 'bytes' as they change
		float[] currentColor = new float[COLOR_ARRAY_SIZE];

		// Holds the final list of colors
		int[] gradient = new int[iterations];

		// Extract bytes
		for( int i = 0; i < COLOR_ARRAY_SIZE; i++ )
		{
			// Get fromColor byte
			fromColorBytes[i] = ( fromColor >> COLOR_SHIFT_AMOUNT[i] ) & 0xFF;

			// Get toColor byte
			toColorBytes[i] = ( ( toColor >> COLOR_SHIFT_AMOUNT[i] ) & 0xFF );

			// Calculate step amount
			stepAmount[i] = ( toColorBytes[i] - fromColorBytes[i] ) / (float)iterations;

			// Init the current color
			currentColor[i] = fromColorBytes[i];
		}

		// Set the first color
		gradient[0] = fromColor;

		for( int iteration = 1; iteration < iterations; iteration++ )
		{
			int result = 0;

			// Add the step amounts to the current color and incorporate into the result color
			for( int i = 0; i < COLOR_ARRAY_SIZE; i++ )
			{
				// Add the step amount
				currentColor[i] += stepAmount[i];

				// Add to result color
				result += ( ( Math.round( currentColor[i] ) & 0xFF ) << COLOR_SHIFT_AMOUNT[i] );

			}

			// Set gradient
			gradient[iteration] = result;

		}

		// Set the last color
		gradient[iterations - 1] = toColor;

		return gradient;
	}

	/**
	 * Draws an on-screen tooltip.
	 * @param guiObject The parent of this tooltip.
	 * @param descriptionLines Lines shown in the tooltip. Can be empty, can not be null.
	 * @param posX X anchor position to draw the tooltip. Generally the mouse's X position.
	 * @param posY Y anchor position to draw the tooltip. Generally the mouse's Y position.
	 * @param fontrenderer The renderer used to draw the text with.
	 */
	public static final void drawTooltip( final Gui guiObject, final List<String> descriptionLines, int posX, int posY, final FontRenderer fontrenderer )
	{
		if( !descriptionLines.isEmpty() )
		{
			// Disable rescaling
			GL11.glDisable( GL12.GL_RESCALE_NORMAL );

			// Disable lighting
			GL11.glDisable( GL11.GL_LIGHTING );

			// Disable depth testing
			GL11.glDisable( GL11.GL_DEPTH_TEST );

			try
			{
				// Use reflection to access zLevel
				Field zLevel = Gui.class.getDeclaredField( "zLevel" );

				// Use reflection to access drawGradientRect
				Method drawGradientRect = Gui.class.getDeclaredMethod( "drawGradientRect", int.class, int.class, int.class, int.class, int.class,
					int.class );

				// Set reflected fields to public
				zLevel.setAccessible( true );
				drawGradientRect.setAccessible( true );

				// Assume string length is zero
				int maxStringLength_px = 0;

				// Get max string length from lines in the list
				for( String string : descriptionLines )
				{
					// Get the length of the string
					int stringLen = fontrenderer.getStringWidth( string );

					// Is it larger than the previous length?
					if( stringLen > maxStringLength_px )
					{
						// Set it to maximum
						maxStringLength_px = stringLen;
					}
				}

				// Offset the tooltip slightly
				posX = posX + GuiHelper.TOOLTIP_OFFSET;
				posY = posY - GuiHelper.TOOLTIP_OFFSET;

				// Base height of 8
				int tooltipHeight = GuiHelper.TOOLTIP_EMPTY_HEIGHT;

				// Adjust height based on the number of lines
				if( descriptionLines.size() > 1 )
				{
					// Calculate the line height
					int lineHeight = ( descriptionLines.size() - 1 ) * GuiHelper.TOOLTIP_LINE_HEIGHT;

					// Adjust the height
					tooltipHeight += ( GuiHelper.TOOLTIP_HEIGHT_MARGIN + lineHeight );
				}

				// Get the current z level
				float prevZlevel = (Float)zLevel.get( guiObject );

				// Set the new level to some high number
				zLevel.set( guiObject, 300.0F );

				// Tooltip boundary
				Bounds bounds = new Bounds( posY - GuiHelper.TOOLTIP_BORDER_SIZE, posX - GuiHelper.TOOLTIP_BORDER_SIZE, posY + tooltipHeight +
								GuiHelper.TOOLTIP_BORDER_SIZE, posX + maxStringLength_px + GuiHelper.TOOLTIP_BORDER_SIZE );

				// Draw the background and borders
				GuiHelper.drawTooltipBackground( guiObject, drawGradientRect, bounds );

				// Draw each line
				for( int index = 0; index < descriptionLines.size(); index++ )
				{
					// Get the line
					String line = descriptionLines.get( index );

					// Draw the line
					fontrenderer.drawStringWithShadow( line, posX, posY, -1 );

					// Is this the first line?
					if( index == 0 )
					{
						// Add the margin
						posY += GuiHelper.TOOLTIP_HEIGHT_MARGIN;
					}

					// Add the line height
					posY += GuiHelper.TOOLTIP_LINE_HEIGHT;
				}

				// Return the z level to what it was before
				zLevel.set( guiObject, prevZlevel );
			}
			catch( Throwable e )
			{
				// Something went wrong, abort.
			}

			// Reenable lighting
			GL11.glEnable( GL11.GL_LIGHTING );

			// Reenable depth testing
			GL11.glEnable( GL11.GL_DEPTH_TEST );

			// Reenable scaling
			GL11.glEnable( GL12.GL_RESCALE_NORMAL );
		}
	}

	/**
	 * Checks if the specified point is within or on the bounds of a rectangle.
	 * This version localizes the rectangle to the confounds of the current gui.
	 * 
	 * @param top
	 * @param left
	 * @param height
	 * @param width
	 * @param pointX
	 * @param pointY
	 * @param guiLeft
	 * @param guiTop
	 * @return
	 */
	public static final boolean isPointInGuiRegion( final int top, final int left, final int height, final int width, final int pointX,
													final int pointY, final int guiLeft, final int guiTop )
	{
		return isPointInRegion( top, left, height, width, pointX - guiLeft, pointY - guiTop );
	}

	/**
	 * Checks if the specified point is within or on the bounds of a rectangle
	 * 
	 * @param top
	 * @param left
	 * @param height
	 * @param width
	 * @param pointX
	 * @param pointY
	 * @return
	 */
	public static final boolean isPointInRegion( final int top, final int left, final int height, final int width, final int pointX, final int pointY )
	{
		return ( pointX >= top ) && ( pointX <= ( top + width ) ) && ( pointY >= left ) && ( pointY <= ( left + height ) );
	}
}
