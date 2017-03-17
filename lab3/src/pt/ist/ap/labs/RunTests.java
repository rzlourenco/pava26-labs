package pt.ist.ap.labs;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class RunTests {
    private static boolean invokeAndSwallow(Method m) {
        try {
            m.setAccessible(true);
            m.invoke(null);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        int passed = 0, failed = 0;

        List<Method> tests = new ArrayList<>();
        Map<String, Method> setups = new HashMap<>();

        getTestAndSetupMethodsForClassAndPutInto(args[0], tests, setups);

        for (Method test : tests) {
            Test annotation = test.getAnnotation(Test.class);
            boolean runAllSetups = annotation.value().length == 1 && "*".equals(annotation.value()[0]);
            boolean setupFailed = false;

            if (runAllSetups) {
                for (Method setup : setups.values()) {
                    String setupName = setup.getAnnotation(Setup.class).value();

                    if (!invokeAndSwallow(setup))
                        setupFailed = true;
                }
            } else {
                for (String setupName : annotation.value()) {
                    Method setup = setups.get(setupName);

                    if (setup == null || !invokeAndSwallow(setup))
                        setupFailed = true;
                }
            }

            if (!setupFailed && invokeAndSwallow(test)) {
                System.out.printf("Test %s OK!\n", test);
                passed++;
            } else {
                System.out.printf("Test %s failed\n", test);
                failed++;
            }
        }

        System.out.printf("Passed: %d, Failed: %d%n", passed, failed);
    }

    private static void getTestAndSetupMethodsForClassAndPutInto(String className, List<Method> tests, Map<String, Method> setups) throws ClassNotFoundException {
        List<Method> allMethods = new ArrayList<>();

        Class<?> theClass = Class.forName(className);
        while (theClass != null) {
            allMethods.addAll(Arrays.asList(theClass.getDeclaredMethods()));
            theClass = theClass.getSuperclass();
        }

        for (Method m : allMethods) {
            if (m.getParameterCount() != 0)
                continue;

            if (!m.getReturnType().equals(void.class))
                continue;

            if (!Modifier.isStatic(m.getModifiers()))
                continue;

            if (m.isAnnotationPresent(Test.class))
                tests.add(m);

            if (m.isAnnotationPresent(Setup.class)) {
                Setup annotation = m.getAnnotation(Setup.class);
                Method other;

                // Lazily throwing a RuntimeException
                if ((other = setups.put(annotation.value(), m)) != null)
                    throw new RuntimeException(
                            String.format("Setup method %s already exists: %s",
                                    annotation.value(),
                                    other.toString()));
            }
        }
    }
}
