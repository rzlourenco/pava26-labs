package pt.ist.ap.labs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Lab1 {
	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException {
		System.out.println("Please input a class name: ");
		String s = new BufferedReader(new InputStreamReader(System.in)).readLine();
		
		Class<?> sayer;
		try {
			sayer = Class.forName("pt.ist.ap.labs." + s);
		} catch (ClassNotFoundException e) {
			System.err.println("Class not found.");
			return;
		}
		
		if (Message.class.isAssignableFrom(sayer)) {		
			Message m = (Message)sayer.newInstance();
			m.say();
		} else {
			System.err.println("Not a Message.");
		}
	}
}
