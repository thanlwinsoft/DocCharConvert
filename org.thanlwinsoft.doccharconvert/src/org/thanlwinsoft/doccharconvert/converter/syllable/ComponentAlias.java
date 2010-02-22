package org.thanlwinsoft.doccharconvert.converter.syllable;

import java.util.Iterator;

/**
 * @author keith
 * A Component which has the same members as another Component which it references.
 */
public class ComponentAlias implements IComponent
{
	private Script mScript = null;
	private String mRefId = null;
	
	protected ComponentAlias(Script script, String refId)
	{
		mRefId = refId;
		mScript = script;
	}
	public Component getComponent()
	{
		return mScript.getSyllableComponent(mRefId);
	}
	
	@Override
	public ComponentClass getClass(String id)
	{
		return getComponent().getClass(id);
	}

	@Override
	public Iterator<String> getClassIdIterator()
	{
		return getComponent().getClassIdIterator();
	}

	@Override
	public String getDescription()
	{
		return getComponent().getDescription();
	}

	@Override
	public String getId()
	{
		return mRefId;
	}

	@Override
	public int getIndex(String entry)
	{
		return getComponent().getIndex(entry);
	}

	@Override
	public int getMaxLength()
	{
		return getComponent().getMaxLength();
	}

	@Override
	public int getPriority()
	{
		return getComponent().getPriority();
	}

	@Override
	public Script getScript()
	{
		return getComponent().getScript();
	}

	@Override
	public int size()
	{
		return getComponent().size();
	}

}
