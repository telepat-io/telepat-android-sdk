package com.appscend.library;

/**
 * Created by catalinivan on 20/04/15.
 */
public class SimpleTileObject
{
	private String  mTitle;
	private String  mDescription;
	private String  mHelpText;
	private boolean isShareable;
	private boolean shouldShowImage;
	private int     mTitleColor;
	private int     mImageBackground;

	public String getTitle()
	{
		return mTitle;
	}

	public String getDescription()
	{
		return mDescription;
	}

	public String getHelpText()
	{
		return mHelpText;
	}

	public boolean isShareable()
	{
		return isShareable;
	}

	public boolean shouldShowImage()
	{
		return shouldShowImage;
	}

	public int getTitleColor()
	{
		return mTitleColor;
	}

	public int getImageBackground()
	{
		return mImageBackground;
	}
}
