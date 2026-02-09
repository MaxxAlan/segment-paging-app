using System;
using System.Diagnostics;
using System.IO;

class Launcher {
    static void Main() {
        // Paths relative to the .exe
        string baseDir = AppDomain.CurrentDomain.BaseDirectory;
        string bundledJava = Path.Combine(baseDir, "runtime", "bin", "javaw.exe");
        string jarFile = Path.Combine(baseDir, "Simulator.jar");

        // Command to run
        ProcessStartInfo startInfo = new ProcessStartInfo();
        
        if (File.Exists(bundledJava)) {
            // Use bundled Java (Portable mode)
            startInfo.FileName = bundledJava;
        } else {
            // Fallback to system Java
            startInfo.FileName = "javaw.exe";
        }

        startInfo.Arguments = "-jar "" + jarFile + """;
        startInfo.UseShellExecute = false;
        startInfo.CreateNoWindow = true; // Hide console window

        try {
            Process.Start(startInfo);
        } catch (Exception) {
            // Simple error dialog logic could go here, but keeping it minimal/native
             // Attempt to show error via cmd if silent launch fails
             Process.Start("cmd", "/c echo Error: Could not launch Simulator.jar. Check if Java is installed or 'runtime' folder exists. & pause");
        }
    }
}
