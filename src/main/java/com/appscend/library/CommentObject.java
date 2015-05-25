package com.appscend.library;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by catalinivan on 21/04/15.
 */
public class CommentObject implements Parcelable
{
	private String mComment;

	public CommentObject(String comment)
	{
		mComment = comment;
	}

	public String getUsername()
	{
		return "";
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{

	}
}
