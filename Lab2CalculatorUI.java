import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Lab2CalculatorUI extends JFrame {
    private JTextArea output;
    // Make the text area available to static helpers
    private static JTextArea OUT;

    // ----- Model -----
    static class Job {
        String name;
        long timeMs; // ms
        Job(String n, long t) { name = n; timeMs = t; }
    }

    // To dedupe assignment variants
    static Set<String> seen = new LinkedHashSet<>();

    public Lab2CalculatorUI() {
        setTitle("Lab2 Calculator");
        setSize(700, 520);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        output = new JTextArea();
        output.setEditable(false);
        output.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 13));
        add(new JScrollPane(output), BorderLayout.CENTER);
        OUT = output;

        JPanel buttons = new JPanel(new GridLayout(1, 6, 8, 8));
        for (String t : new String[]{"Task1","Task2","Task3","Task4","Task5","Clear"}) {
            JButton b = new JButton(t);
            b.addActionListener(e -> runTask(t));
            buttons.add(b);
        }
        add(buttons, BorderLayout.SOUTH);
    }

    private void runTask(String task) {
        try {
            switch (task) {
                case "Task1": {
                    seen.clear();
                    List<Job> jobs = new ArrayList<>();
                    jobs.add(new Job("P0", 10));
                    jobs.add(new Job("P1", 15));
                    jobs.add(new Job("P2", 30));
                    append("=== Task 1: 2 CPUs, 2 branches (sample P0..P2) ===\n");
                    generateAssignments(jobs, 2, 2);
                    break;
                }
                case "Task2": {
                    seen.clear();
                    String jobsStr = JOptionPane.showInputDialog(this,
                        "Enter jobs (name=time in ms, space separated)\nExample: p0=10 p1=15 p2=30");
                    if (jobsStr == null || jobsStr.trim().isEmpty()) break;

                    String cpuStr = JOptionPane.showInputDialog(this, "CPU count:");
                    String brStr  = JOptionPane.showInputDialog(this, "Branches per CPU:");
                    if (cpuStr == null || brStr == null) break;

                    int cpu = Integer.parseInt(cpuStr.trim());
                    int br  = Integer.parseInt(brStr.trim());

                    List<Job> jobs = new ArrayList<>();
                    for (String p : jobsStr.trim().split("\\s+")) {
                        String[] kv = p.split("=");
                        if (kv.length != 2) throw new IllegalArgumentException("Bad job spec: " + p);
                        jobs.add(new Job(kv[0], Long.parseLong(kv[1])));
                    }
                    append(String.format("=== Task 2: %d CPUs, %d branches each ===\n", cpu, br));
                    generateAssignments(jobs, cpu, br);
                    break;
                }
                case "Task3": {
                    // A,B,V,G
                    String clkStr = JOptionPane.showInputDialog(this, "Clock period (ns):");
                    if (clkStr == null) break;
                    double clkNs = Double.parseDouble(clkStr.trim());

                    String seqStr = JOptionPane.showInputDialog(this, "Instruction sequence (e.g. A B V G A ...):");
                    if (seqStr == null || seqStr.trim().isEmpty()) break;

                    Map<String,Integer> cpi = new LinkedHashMap<>();
                    cpi.put("A", askInt("CPI(A):"));
                    cpi.put("B", askInt("CPI(B):"));
                    cpi.put("V", askInt("CPI(V):"));
                    cpi.put("G", askInt("CPI(G):"));

                    List<String> seq = Arrays.asList(seqStr.trim().split("\\s+"));
                    long cycles = sumCycles(seq, cpi);
                    double ns = timeNsByCPISequence(seq, cpi, clkNs);
                    append("=== Task 3 ===\n");
                    append(String.format("Total cycles: %,d\n", cycles));
                    printTimeAll(ns);
                    break;
                }
                case "Task4": {
                    // A,B,V
                    String clkStr = JOptionPane.showInputDialog(this, "Clock period (ns):");
                    if (clkStr == null) break;
                    double clkNs = Double.parseDouble(clkStr.trim());

                    String seqStr = JOptionPane.showInputDialog(this, "Instruction sequence (e.g. A B A V ...):");
                    if (seqStr == null || seqStr.trim().isEmpty()) break;

                    Map<String,Integer> cpi = new LinkedHashMap<>();
                    cpi.put("A", askInt("CPI(A):"));
                    cpi.put("B", askInt("CPI(B):"));
                    cpi.put("V", askInt("CPI(V):"));

                    List<String> seq = Arrays.asList(seqStr.trim().split("\\s+"));
                    long cycles = sumCycles(seq, cpi);
                    double ns = timeNsByCPISequence(seq, cpi, clkNs);
                    append("=== Task 4 ===\n");
                    append(String.format("Total cycles: %,d\n", cycles));
                    printTimeAll(ns);
                    break;
                }
                case "Task5": {
                    String fStr = JOptionPane.showInputDialog(this, "Enter CPU frequency (GHz):");
                    if (fStr == null) break;
                    double freqGHz = Double.parseDouble(fStr.trim());
                    double T_ns = 1.0 / freqGHz;
                    double ms = T_ns / 1e6;
                    double ps = T_ns * 1000.0;
                    append("=== Task 5 ===\n");
                    append(String.format("Clock period: %.9f ms (%.6f ns, %.3f ps)\n\n", ms, T_ns, ps));
                    break;
                }
                case "Clear": {
                    output.setText("");
                    break;
                }
            }
        } catch (Exception ex) {
            append("Error: " + ex.getMessage() + "\n");
        }
    }

    // ----- Assignment generation (uses OUT to print) -----
    static void generateAssignments(List<Job> jobs, int cpuCount, int branchesPerCpu) {
        List<List<Job>> cpus = new ArrayList<>();
        for (int i = 0; i < cpuCount; i++) cpus.add(new ArrayList<>());
        backtrack(jobs, 0, cpus, cpuCount, branchesPerCpu);
    }

    static void backtrack(List<Job> jobs, int idx, List<List<Job>> cpus, int cpuCount, int branchesPerCpu) {
        if (idx == jobs.size()) {
            // unique key by sorted job names per cpu, then sorted across cpus
            List<String> parts = new ArrayList<>();
            for (List<Job> cpu : cpus) {
                List<String> names = new ArrayList<>();
                for (Job j : cpu) names.add(j.name);
                java.util.Collections.sort(names);
                parts.add(String.join("-", names));
            }
            java.util.Collections.sort(parts);
            String key = String.join("|", parts);

            if (!seen.contains(key)) {
                seen.add(key);

                long makespanMs = 0;
                for (List<Job> cpu : cpus) {
                    long sum = 0;
                    for (Job j : cpu) sum += j.timeMs;
                    makespanMs = Math.max(makespanMs, sum);
                }
                double ns = makespanMs * 1e6; // ms -> ns
                double ps = ns * 1000;        // ns -> ps

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < cpuCount; i++) {
                    sb.append("CPU").append(i + 1).append(": ");
                    for (Job j : cpus.get(i)) {
                        sb.append(j.name).append("(").append(j.timeMs).append("ms) ");
                    }
                    sb.append("\n");
                }
                sb.append(String.format("Makespan = %d ms (%.3f ns, %.3f ps)\n\n",
                        makespanMs, ns, ps));
                append(sb.toString());
            }
            return;
        }

        Job job = jobs.get(idx);
        for (int i = 0; i < cpuCount; i++) {
            if (cpus.get(i).size() < branchesPerCpu) {
                cpus.get(i).add(job);
                backtrack(jobs, idx + 1, cpus, cpuCount, branchesPerCpu);
                cpus.get(i).remove(cpus.get(i).size() - 1);
            }
        }
    }

    // ----- CPI helpers -----
    static long sumCycles(List<String> seq, Map<String,Integer> cpi) {
        long cycles = 0;
        for (String s : seq) {
            Integer v = cpi.get(s);
            if (v == null) throw new IllegalArgumentException("Unknown instruction: " + s);
            cycles += v;
        }
        return cycles;
    }

    static double timeNsByCPISequence(List<String> seq, Map<String,Integer> cpi, double clockNs) {
        return sumCycles(seq, cpi) * clockNs;
    }

    static void printTimeAll(double ns) {
        double ms = ns / 1e6;
        double ps = ns * 1000;
        append(String.format("Total time: %.6f ms (%.3f ns, %.3f ps)\n\n", ms, ns, ps));
    }

    static int askInt(String prompt) {
        String s = JOptionPane.showInputDialog(null, prompt);
        if (s == null) throw new RuntimeException("Canceled");
        return Integer.parseInt(s.trim());
    }

    // safe append to UI
    static void append(String s) {
        if (OUT != null) OUT.append(s);
        System.out.print(s); // also mirror to console (optional)
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Lab2CalculatorUI().setVisible(true));
    }
}
