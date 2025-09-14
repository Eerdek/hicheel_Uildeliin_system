import java.util.*;

public class MemoryCalculator {

    // -------- Text/graphics size (хуучин бодлого) --------
    static long textBytes(int rows, int cols, int colors) {
        long cells = (long) rows * cols;
        double colorBits = Math.log(colors) / Math.log(2);
        double bitsPerCell = 8.0 + colorBits;
        return Math.round(cells * bitsPerCell / 8.0);
    }

    static long graphicsBytes(int width, int height, int bitsPerPixel) {
        return (long) width * height * bitsPerPixel / 8L;
    }

    static void printSize(String label, long bytes) {
        double mb = bytes / (1024.0 * 1024.0);
        System.out.printf(Locale.US, "%s: %,d bytes (%.3f MB)%n", label, bytes, mb);
    }

    // -------- Q5/Q6: per-symbol timing --------
    static class Timings {
        // нэг тэмдэгтэд зарцуулах хугацаа
        double cacheNsPerChar; // жишээ: 2 ns
        double ramNsPerChar;   // жишээ: 10 ns
        // disk: 1024 тэмдэгт тутам 10 ms гэх мэт
        double diskMsPer1024Chars; // жишээ: 10 ms
    }

    static void printDeviceTimesPerSymbol(String label, long symbols, Timings t) {
        double tCache_ms = symbols * t.cacheNsPerChar * 1e-6;   // ns -> ms
        double tRam_ms   = symbols * t.ramNsPerChar   * 1e-6;   // ns -> ms
        double tDisk_ms  = (symbols / 1024.0) * t.diskMsPer1024Chars;

        System.out.printf(Locale.US, "%s (symbols = %,d)%n", label, symbols);
        System.out.printf(Locale.US, "  Cache: %.3f ms (%.3f ns/char)%n", tCache_ms, t.cacheNsPerChar);
        System.out.printf(Locale.US, "  RAM:   %.3f ms (%.3f ns/char)%n", tRam_ms, t.ramNsPerChar);
        System.out.printf(Locale.US, "  Disk:  %.3f ms (%.3f ms / 1024 chars)%n%n", tDisk_ms, t.diskMsPer1024Chars);
    }

    // хэрэглэгчээс “нийт тэмдэгт”-ийг авна (шууд эсвэл хуудас×мөр×тэмдэгт)
    static long askSymbols(Scanner sc) {
        System.out.print("Direct total symbols? (y/n): ");
        boolean direct = sc.nextLine().trim().equalsIgnoreCase("y");
        if (direct) {
            System.out.print("Total symbols: ");
            return Long.parseLong(sc.nextLine().trim());
        } else {
            System.out.print("Pages: ");
            long pages = Long.parseLong(sc.nextLine().trim());
            System.out.print("Lines per page: ");
            long lines = Long.parseLong(sc.nextLine().trim());
            System.out.print("Chars per line: ");
            long chars = Long.parseLong(sc.nextLine().trim());
            return pages * lines * chars;
        }
    }

    static Timings askTimingsOrDefault(Scanner sc) {
        System.out.print("Use default timings (cache=2ns, RAM=10ns, Disk=10ms/1024)? (y/n): ");
        boolean def = sc.nextLine().trim().equalsIgnoreCase("y");
        Timings t = new Timings();
        if (def) {
            t.cacheNsPerChar = 2.0;
            t.ramNsPerChar = 10.0;
            t.diskMsPer1024Chars = 10.0;
        } else {
            System.out.print("Cache time (ns per char): ");
            t.cacheNsPerChar = Double.parseDouble(sc.nextLine().trim());
            System.out.print("RAM time (ns per char): ");
            t.ramNsPerChar = Double.parseDouble(sc.nextLine().trim());
            System.out.print("Disk time (ms per 1024 chars): ");
            t.diskMsPer1024Chars = Double.parseDouble(sc.nextLine().trim());
        }
        return t;
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("==== Memory Calculator ====");
            System.out.println("1) Sizes (1–4) [fixed samples]");
            System.out.println("5) Q5: per-symbol timing (interactive)");
            System.out.println("6) Q6: per-symbol timing (interactive)");
            System.out.println("0) Exit");
            System.out.print("Choose: ");
            String ch = sc.nextLine().trim();
            if (ch.equals("0")) break;

            try {
                switch (ch) {
                    case "1": {
                        long q1 = textBytes(25, 80, 4);
                        long q2 = textBytes(40, 60, 16);
                        long q3 = graphicsBytes(800, 640, 4);
                        long q4 = graphicsBytes(600, 400, 8);

                        printSize("1) 80x25, 4 colors", q1);
                        printSize("2) 60x40, 16 colors", q2);
                        printSize("3) 800x640, 16 colors (4 bpp)", q3);
                        printSize("4) 600x400, 8-bit color (8 bpp)", q4);
                        System.out.println();
                        break;
                    }
                    case "5": {
                        System.out.println("Q5 — enter symbols or pages×lines×chars");
                        long symbols = askSymbols(sc);
                        Timings t = askTimingsOrDefault(sc);
                        printDeviceTimesPerSymbol("Q5 result", symbols, t);
                        break;
                    }
                    case "6": {
                        System.out.println("Q6 — enter symbols or pages×lines×chars");
                        long symbols = askSymbols(sc);
                        Timings t = askTimingsOrDefault(sc);
                        printDeviceTimesPerSymbol("Q6 result", symbols, t);
                        break;
                    }
                    default:
                        System.out.println("Invalid choice.\n");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage() + "\n");
            }
        }
    }
}
