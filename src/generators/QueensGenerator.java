import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public final class QueensGenerator {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java QueensGenerator <n>");
            return;
        }
        int n = Integer.parseInt(args[0]);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(n + "Queens.csp"))) {
            writer.write("//" + n + "-Queens.\n");
            writer.write("\n// Number of variables:\n" + n + "\n");
            writer.write("\n// Domains of the variables: 0.. (inclusive)\n");
            for (int i = 0; i < n; i++) {
                writer.write("0, " + (n - 1) + "\n");
            }
            writer.write("\n// constraints (vars indexed from 0, allowed tuples):\n");

            for (int row1 = 0; row1 < n - 1; row1++) {
                for (int row2 = row1 + 1; row2 < n; row2++) {
                    writer.write("c(" + row1 + ", " + row2 + ")\n");
                    for (int col1 = 0; col1 < n; col1++) {
                        for (int col2 = 0; col2 < n; col2++) {
                            if ((col1 != col2) && (Math.abs(col1 - col2) != (row2 - row1))) {
                                writer.write(col1 + ", " + col2 + "\n");
                            }
                        }
                    }
                    writer.write("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
