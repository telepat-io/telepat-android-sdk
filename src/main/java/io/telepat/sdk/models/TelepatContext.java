package io.telepat.sdk.models;

import java.util.HashMap;

/**
 * Created by catalinivan, Andrei Marinescu on 17/03/15.
 * Model class for Telepat Contexts
 */
public class TelepatContext
{
	private String type;
	private long   startTime;
	private long   endTime;
	private int    state;
	private String 	application_id;
	private String	id;
	private String name;
	private HashMap<String, Object> meta;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public long getStartTime()
	{
		return startTime;
	}

	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}

	public long getEndTime()
	{
		return endTime;
	}

	public void setEndTime(long endTime)
	{
		this.endTime = endTime;
	}

	public int getState()
	{
		return state;
	}

	public void setState(int state)
	{
		this.state = state;
	}

	public HashMap<String, Object> getMeta() {
		return meta;
	}

	public void setMeta(HashMap<String, Object> meta)
	{
		this.meta = meta;
	}

	public String getApplication_id() {
		return application_id;
	}

	public void setApplication_id(String application_id) {
		this.application_id = application_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
