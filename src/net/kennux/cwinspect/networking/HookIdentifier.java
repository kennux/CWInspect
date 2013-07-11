package net.kennux.cwinspect.networking;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import net.kennux.cwinspect.hooks.AHook;

public class HookIdentifier
{
	public static AHook identifyServerHook(int packetId)
	{
		return identifyHook(packetId, "net.kennux.cwinspect.hooks.server");
	}
	
	public static AHook identifyClientHook(int packetId)
	{
		return identifyHook(packetId, "net.kennux.cwinspect.hooks.client");
	}
	
	@SuppressWarnings("rawtypes")
	private static AHook identifyHook(int packetId, String packageName)
	{
		// Get all packets in the package
		ArrayList<Class> hookClasses = getClasses(packageName);
		Iterator<Class> iterator = hookClasses.iterator();
		
		// Iterate through all packets in package
		while(iterator.hasNext())
		{
			// Get current packet
			try
			{
				AHook hook = (AHook) iterator.next().newInstance();
				
				if (hook.getPacketId() == packetId)
				{
					// Success!
					return hook;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return null;
	}

	/**
	 * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
	 *
	 * @param packageName The base package
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	private static ArrayList<Class> getClasses(String packageName)
	{
		try
		{
		    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		    assert classLoader != null;
		    
		    String path = packageName.replace('.', '/');
		    Enumeration<URL> resources = classLoader.getResources(path);
		    
		    List<File> dirs = new ArrayList<File>();
		    
		    while (resources.hasMoreElements())
		    {
		        URL resource = resources.nextElement();
		        dirs.add(new File(resource.getFile()));
		    }
		    
		    ArrayList<Class> classes = new ArrayList<Class>();
		    for (File directory : dirs)
		    {
		        classes.addAll(findClasses(directory, packageName));
		    }
		    return classes;
		}
		catch (Exception e)
		{
			return new ArrayList<Class>();
		}
	}

	/**
	 * Recursive method used to find all classes in a given directory and subdirs.
	 *
	 * @param directory   The base directory
	 * @param packageName The package name for classes found inside the base directory
	 * @return The classes
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("rawtypes")
	private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException
	{
	    List<Class> classes = new ArrayList<Class>();
	    
	    if ( ! directory.exists())
	    {
	        return classes;
	    }
	    
	    File[] files = directory.listFiles();
	    
	    for (File file : files)
	    {
	        if (file.isDirectory())
	        {
	            assert ! file.getName().contains(".");
	            classes.addAll(findClasses(file, packageName + "." + file.getName()));
	        }
	        else if (file.getName().endsWith(".class"))
	        {
	            classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
	        }
	    }
	    return classes;
	}

}
