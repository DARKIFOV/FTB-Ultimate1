package com.feed_the_beast.mods.ftbultimine.shape;

import com.feed_the_beast.mods.ftbultimine.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Shape registry, same structure as the original: a LinkedHashMap of shapes
 * plus next()/prev() cycling and a default shape.
 */
public abstract class Shape
{
	private static final LinkedHashMap<String, Shape> MAP = new LinkedHashMap<String, Shape>();
	private static Shape defaultShape;
	private static List<Shape> list = Collections.emptyList();

	public static void register(Shape shape)
	{
		MAP.put(shape.getName(), shape);

		if (shape.isDefault())
		{
			defaultShape = shape;
		}
	}

	public static Shape get(String id)
	{
		if (id == null || id.isEmpty())
		{
			return defaultShape;
		}

		Shape shape = MAP.get(id);
		return shape == null ? defaultShape : shape;
	}

	public static Shape getDefault()
	{
		return defaultShape;
	}

	public static List<Shape> getList()
	{
		return list;
	}

	/** Called in postInit, exactly like the original Shape.postinit(). */
	public static void postInit()
	{
		list = new ArrayList<Shape>(MAP.values());
	}

	public abstract String getName();

	public boolean isDefault()
	{
		return false;
	}

	public String getTranslationKey()
	{
		return "ftbultimine.shape." + getName();
	}

	public Shape next()
	{
		int i = list.indexOf(this) + 1;
		return list.get(i >= list.size() ? 0 : i);
	}

	public Shape prev()
	{
		int i = list.indexOf(this) - 1;
		return list.get(i < 0 ? list.size() - 1 : i);
	}

	public abstract List<BlockPos> getBlocks(ShapeContext context);
}
