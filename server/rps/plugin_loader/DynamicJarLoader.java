package server.rps.plugin_loader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class DynamicJarLoader {

    public static List<Class<?>> load(String jarPath, String interfaceName) throws IOException, ClassNotFoundException {
        File jar = new File(jarPath);

        // Check if the JAR file exists
        if (!jar.exists()) {
            throw new IOException("JAR file not found at: " + jarPath);
        }

        List<Class<?>> implementingClasses = new ArrayList<>();
        try (JarFile jarFile = new JarFile(jar)) {
            URL[] urls = { new URL("jar:file:" + jar.getAbsolutePath() + "!/") };
            try (URLClassLoader classLoader = URLClassLoader.newInstance(urls)) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".class")) {
                        String className = entry.getName().replace("/", ".").replace(".class", "");
                        Class<?> cls = classLoader.loadClass(className);

                        if (implementsInterface(cls, interfaceName)) {
                            implementingClasses.add(cls);
                        }
                    }
                }
            }
        }
        return implementingClasses;
    }

    private static boolean implementsInterface(Class<?> cls, String interfaceName) {
        if (!cls.isInterface()) {
            Class<?>[] interfaces = cls.getInterfaces();
            for (Class<?> iface : interfaces) {
                if (iface.getName().equals(interfaceName)) {
                    return true;
                }
            }
        }
        return false;
    }
}