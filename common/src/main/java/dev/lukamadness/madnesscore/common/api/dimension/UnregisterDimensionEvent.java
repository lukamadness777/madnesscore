package dev.lukamadness.madnesscore.common.api.dimension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.server.level.ServerLevel;

public final class UnregisterDimensionEvent
{
	private static final List<Listener> LISTENERS = new CopyOnWriteArrayList<>();

	private UnregisterDimensionEvent() {}

	public static void register(Listener listener)
	{
		LISTENERS.add(listener);
	}

	public static boolean fire(ServerLevel level)
	{
		for (Listener listener : LISTENERS)
		{
			if (!listener.onUnregisterDimension(level))
			{
				return false;
			}
		}
		return true;
	}

	@FunctionalInterface
	public interface Listener
	{
		boolean onUnregisterDimension(ServerLevel level);
	}
}
