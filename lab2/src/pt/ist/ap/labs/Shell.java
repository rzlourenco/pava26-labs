package pt.ist.ap.labs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Shell {
	Object lastResult;
	Map<String, Object> saved;
	Scanner in;
	
	public Shell(Scanner s) {
		in = s;
		saved = new HashMap<>();
	}
	
	public static void main(String[] args) {
		new Shell(new Scanner(System.in)).run();
	}
	
	void runClassCommand(String className) {
		Class<?> theClass;
		
		try {
			theClass = Class.forName(className);
		} catch (ClassNotFoundException cnfex) {
			System.out.println("Class not found.");
			return;
		}

		lastResult = theClass;
	}
	
	void runGetCommand(String name) {
		lastResult = saved.get(name);		
	}
	
	void runSetCommand(String name) {
		if (lastResult == null) {
			System.out.println("No object to save.");
			return;
		}
		
		saved.put(name, lastResult);
		System.out.println("Saved name for object of type: " + lastResult.getClass());
	}
	
	void runIndexCommand(String stringIndex) {
		if (lastResult == null)
			return;
		
		Class<?> theClass = lastResult.getClass();
		if (!theClass.isArray())
			return;
		
		int index;
		try {
			index = Integer.parseInt(stringIndex);
		} catch (NumberFormatException nfe) {
			return;
		}
		
		Object[] array = (Object[])lastResult;
		if (index < 0 || index >= array.length)
			return;
		
		lastResult = array[index];
	}
	
	void runMethodName(String[] parts) {
		if (lastResult == null)
			return;
		
		System.out.println("Trying generic command: " + parts[0]);
		Class<?> lastResultClass = lastResult.getClass();
		Method method;
		
		try {
			method = lastResultClass.getMethod(parts[0]);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return;
		}
		
		try {
			lastResult = method.invoke(lastResult);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return;
		}
	}
	
	void printLastResult() {
		if (lastResult == null)
			return;
		
		if (lastResult.getClass().isArray()) {
			Object[] array = (Object[])lastResult;
			
			for (int i = 0; i < array.length; ++i) {
				System.out.printf("%3d: %s\n", i, array[i] == null ? "" : array[i].toString());
			}
		} else {
			System.out.println(lastResult.toString());
		}
	}
	
	public void run() {
		do {
			String[] parts;
			boolean ranCommand = false;
			
			System.out.print("Command:> ");
			try {
				parts = Arrays
						.stream(in.nextLine().split("\\p{Space}"))
						.filter(s -> !s.isEmpty())
						.collect(Collectors.toList())
						.toArray(new String[0]);
			} catch (NoSuchElementException nsex) {
				break;
			}

			if (parts.length == 0)
				continue;

			if ("Exit".equals(parts[0])) {
				System.out.println("Goodbye!");
				break;
			}
			
			if (parts.length == 2) {
				if ("Class".equals(parts[0])) {
					runClassCommand(parts[1]);
					ranCommand = true;
				} else if ("Set".equals(parts[0])) {
					runSetCommand(parts[1]);
					ranCommand = true;
				} else if ("Get".equals(parts[0])) {
					runGetCommand(parts[1]);
					ranCommand = true;
				} else if ("Index".equals(parts[0])) {
					runIndexCommand(parts[1]);
					ranCommand = true;
				}
			}
			
			if (!ranCommand)
				runMethodName(parts);
			
			printLastResult();
		} while (true);
	}
}
